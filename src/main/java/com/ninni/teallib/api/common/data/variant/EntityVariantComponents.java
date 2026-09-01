package com.ninni.teallib.api.common.data.variant;

import com.ninni.teallib.api.common.data.variant.util.VariantAttachments;
import com.ninni.teallib.core.TealLib;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Extensible entity-variant tooltip system.
 * <p>
 * Providers basically try to understand how a given entity handles
 * its variant information, then return rows of components to use in tooltips.
 */
public final class EntityVariantComponents {
    private static final Map<ResourceLocation, List<Registration>> PROVIDERS = new LinkedHashMap<>();
    private static final List<Registration> GLOBAL_PROVIDERS = new ArrayList<>();
    public static final Style GRAY_ITALIC = Style.EMPTY.withColor(ChatFormatting.GRAY).withItalic(true);


    @FunctionalInterface
    public interface Provider {
        @NotNull Optional<List<Component>> getComponents(Context context);
    }
    
    /**
     * Information available to a provider.
     */
    public record Context(@NotNull ResourceLocation entityId, @NotNull CompoundTag tag, @NotNull Item.TooltipContext tooltipContext) {
        public boolean has(String key) {
            return tag.contains(key);
        }
        public boolean has(String key, int expectedTagType) {
            return tag.contains(key, expectedTagType);
        }
        public Tag get(String key) {
            return tag.get(key);
        }
        public String getString(String key) {
            return tag.getString(key);
        }
        public int getInt(String key) {
            return tag.getInt(key);
        }
    }

    private record Registration(int priority, Provider provider) { }


    /**
     * Register a provider which tries to handle an entity.
     */
    public static void register(ResourceLocation entityId, int priority, Provider provider) {
        PROVIDERS.computeIfAbsent(entityId, ignored -> new ArrayList<>()).add(new Registration(priority, provider));
        sort(PROVIDERS.get(entityId));
    }
    public static void register(ResourceLocation entityId, Provider provider) {
        register(entityId, 0, provider);
    }
    public static void register(EntityType<?> entityType, int priority, Provider provider) {
        ResourceLocation id = BuiltInRegistries.ENTITY_TYPE.getKey(entityType);
        register(id, priority, provider);
    }
    public static void register(EntityType<?> entityType, Provider provider) {
        register(entityType, 0, provider);
    }

    /**
     * Register a provider which tries to handle every entity.
     */
    public static void registerGlobal(int priority, Provider provider) {
        GLOBAL_PROVIDERS.add(new Registration(priority, provider));
        sort(GLOBAL_PROVIDERS);
    }

    public static void registerGlobal(Provider provider) {
        registerGlobal(0, provider);
    }


    public static Optional<List<Component>> getComponents(CompoundTag tag, Item.TooltipContext tooltipContext, ResourceLocation entityId) {
        Context context = new Context(entityId, tag, tooltipContext);
        List<Registration> exact = PROVIDERS.get(entityId);

        if (exact != null) {
            Optional<List<Component>> result = tryProviders(exact, context);
            if (result.isPresent()) return result;
        }

        Optional<List<Component>> global = tryProviders(GLOBAL_PROVIDERS, context);
        if (global.isPresent()) return global;

        return genericFallback(context);
    }

    public static void addToTooltip(List<Component> tooltip, CompoundTag tag, Item.TooltipContext tooltipContext, EntityType<?> type) {
        addToTooltip(tooltip, tag, tooltipContext, BuiltInRegistries.ENTITY_TYPE.getKey(type));
    }

    public static void addToTooltip(List<Component> tooltip, CompoundTag tag, Item.TooltipContext tooltipContext, ResourceLocation entityId) {
        Optional<List<Component>> rows = getComponents(tag, tooltipContext, entityId);
        if (rows.isEmpty() || rows.get().isEmpty()) return;
        tooltip.addAll(rows.get());
    }

    private static Optional<List<Component>> tryProviders(List<Registration> registrations, Context context) {
        for (Registration registration : registrations) {
            try {
                Optional<List<Component>> result = registration.provider().getComponents(context);
                if (result.isPresent()) return result;
            } catch (Throwable throwable) {
                TealLib.LOGGER.error("Entity variant tooltip provider failed for {}", context.entityId(), throwable);
            }
        }

        return Optional.empty();
    }

    private static void sort(List<Registration> registrations) {
        registrations.sort(Comparator.comparingInt(Registration::priority).reversed());
    }

    /**
     * Creates a translation key for a generic variant.
     * <p>
     * Example: variant.minecraft.fox.minecraft.snow
     */
    public static String key(ResourceLocation entityId, ResourceLocation value) {
        return "variant." + entityId.getNamespace() + "." + entityId.getPath() + "." + value.getNamespace() + "." + value.getPath();
    }

    /**
     * Creates a translation key for a variant, with an additional category.
     * <p>
     * Example: variant.minecraft.horse.markings.minecraft.white
     */
    public static String key(ResourceLocation entityId, String category, ResourceLocation value) {
        return "variant." + entityId.getNamespace() + "." + entityId.getPath() + "." + category + "." + value.getNamespace() + "." + value.getPath();
    }

    public static MutableComponent translated(ResourceLocation entityId, ResourceLocation value) {
        return Component.translatable(key(entityId, value)).withStyle(GRAY_ITALIC);
    }

    public static MutableComponent translated(ResourceLocation entityId, ResourceLocation value, Style style) {
        return translated(entityId, value).withStyle(style);
    }

    public static MutableComponent translated(ResourceLocation entityId, ResourceLocation value, ChatFormatting... formats) {
        return translated(entityId, value).withStyle(formats);
    }

    public static MutableComponent translated(ResourceLocation entityId, String category, ResourceLocation value) {
        return Component.translatable(key(entityId, category, value)).withStyle(Style.EMPTY.withColor(ChatFormatting.GRAY).withItalic(true));
    }

    public static MutableComponent translated(ResourceLocation entityId, String category, ResourceLocation value, Style style) {
        return translated(entityId, category, value).withStyle(style);
    }

    public static MutableComponent translated(ResourceLocation entityId, String category, ResourceLocation value, ChatFormatting... formats) {
        return translated(entityId, category, value).withStyle(formats);
    }

    public static MutableComponent translated(ResourceLocation entityId, String value) {
        return translated(entityId, ResourceLocation.fromNamespaceAndPath(entityId.getNamespace(), value));
    }

    public static MutableComponent translated(ResourceLocation entityId, String value, Style style) {
        return translated(entityId, value).withStyle(style);
    }

    public static MutableComponent translated(ResourceLocation entityId, String value, ChatFormatting... formats) {
        return translated(entityId, value).withStyle(formats);
    }

    public static Optional<List<Component>> genericFallback(EntityVariantComponents.Context context) {
        String value = firstString(context, "Variant", "variant", "Type", "type", "RabbitType");

        Level level = context.tooltipContext.level();
        if (level != null) {
            EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.get(context.entityId);
            Entity entity = type.create(level);

            //TODO
            if (VariantAttachments.has(entity)) {
                if (VariantManager.getVariantCountFor(level.registryAccess(), VariantTarget.of(type)) == 1) {
                    return Optional.empty();
                }
            }
        }

        return valueToOneComponent(context.entityId(), value);
    }

    public static Optional<List<Component>> valueToOneComponent(ResourceLocation entityId, String rawValue) {
        if (rawValue == null || rawValue.isBlank()) return Optional.empty();

        String value = cleanVariant(rawValue);
        if (value.isBlank()) return Optional.empty();

        ResourceLocation parsed = ResourceLocation.tryParse(value);

        if (parsed != null) return Optional.of(List.of(EntityVariantComponents.translated(entityId, parsed)));

        return Optional.of(List.of(EntityVariantComponents.translated(entityId, value)));
    }

    public static String firstString(EntityVariantComponents.Context context, String... keys) {
        for (String key : keys) {
            if (!context.has(key)) continue;

            Tag tag = context.get(key);
            if (tag == null) continue;


            if (tag.getId() == Tag.TAG_STRING) {
                String value = context.getString(key);
                if (!value.isBlank()) return value;
                continue;
            }

            if (isNumeric(tag.getId())) return tag.getAsString();
        }

        return null;
    }

    public static boolean isNumeric(int type) {
        return type == Tag.TAG_BYTE
                || type == Tag.TAG_SHORT
                || type == Tag.TAG_INT
                || type == Tag.TAG_LONG
                || type == Tag.TAG_FLOAT
                || type == Tag.TAG_DOUBLE;
    }

    public static String cleanVariant(String value) {
        return value.replace("\"", "").trim();
    }
}