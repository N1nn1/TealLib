package com.ninni.teallib.api.common.item.tooltip;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import com.ninni.teallib.api.common.data.variant.VariantManager;
import com.ninni.teallib.api.common.data.variant.VariantTarget;
import com.ninni.teallib.core.TealLib;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class TooltipUtils {
    public static final Style GRAY_ITALIC = Style.EMPTY.withItalic(true).withColor(ChatFormatting.GRAY);

    public static Component getEntityName(CompoundTag entityTag) {
        Optional<EntityType<?>> type = EntityType.byString(entityTag.getString("id"));
        return type.map(entityType -> getEntityName(entityTag, entityType)).orElse(Component.empty());
    }

    public static Component getEntityName(@NotNull Item.TooltipContext context, CompoundTag entityTag, EntityType<?> type) {
        if (entityTag.contains("CustomName", Tag.TAG_STRING)) {
            String s = entityTag.getString("CustomName");
            try {
                return Component.Serializer.fromJson(s, Objects.requireNonNull(context.registries()));
            } catch (Exception exception) {
                TealLib.LOGGER.warn("Failed to parse entity custom name {}", s, exception);
            }
        }
        return getEntityName(entityTag, type);
    }

    public static Component getEntityName(CompoundTag entityTag, EntityType<?> type) {
        MutableComponent entityName = Component.translatable(type.getDescriptionId());

        if ((entityTag.contains("Age", Tag.TAG_INT) && entityTag.getInt("Age") < 0) || (entityTag.contains("IsBaby") && entityTag.getBoolean("IsBaby"))) {
            String key = type.getDescriptionId() + ".baby";
            MutableComponent baby = Component.translatable(key);
            if (baby.getString().equals(key) || !TealLib.CLIENT_CONFIG.replaceBabyMobNames.get()) {
                baby = Component.translatable("tooltip.teallib.default_baby");
                return baby.append(entityName);
            } else {
                return baby;
            }
        } else {
            return entityName;
        }
    }

    public static void addJsonBlockEntityVariantTooltip(@NotNull ItemStack stack, @NotNull Item.TooltipContext context, @NotNull List<Component> list) {
        addJsonBlockEntityVariantTooltip(stack, context, list, GRAY_ITALIC);
    }

    public static void addJsonBlockEntityVariantTooltip(@NotNull ItemStack stack, @NotNull Item.TooltipContext context, @NotNull List<Component> list, Style style) {
        if (context.level() == null) return;
        if (stack.has(DataComponents.BLOCK_ENTITY_DATA)) {
            CompoundTag tag = stack.getOrDefault(DataComponents.BLOCK_ENTITY_DATA, CustomData.EMPTY).copyTag();
            addJsonBlockEntityVariantTooltip(context, list, style, tag);
        }
    }

    public static void addJsonBlockEntityVariantTooltip(Item.@NotNull TooltipContext context, @NotNull List<Component> list, CompoundTag tag) {
        addJsonBlockEntityVariantTooltip(context, list, GRAY_ITALIC, tag);
    }

    public static void addJsonBlockEntityVariantTooltip(Item.@NotNull TooltipContext context, @NotNull List<Component> list, Style style, CompoundTag tag) {
        if (tag.contains("Variant", Tag.TAG_STRING)) {
            ResourceLocation beId;

            if (tag.contains("VariantId")) beId = ResourceLocation.tryParse(tag.getString("VariantId"));
            else beId = ResourceLocation.tryParse(tag.getString("id"));
            if (beId != null) {
                if (VariantManager.getVariantCountFor(context.level().registryAccess(), VariantTarget.of(BuiltInRegistries.BLOCK_ENTITY_TYPE.get(beId))) > 1) {
                    ResourceLocation variant = ResourceLocation.tryParse(tag.getString("Variant"));
                    if (variant != null) {
                        list.add(Component.translatable("variant." + variant.getNamespace() + "." + beId.getPath() + "." + variant.getPath()).withStyle(style));
                    }
                }
            }
        }
    }


    public static void addFoodTooltip(@NotNull ItemStack stack, @NotNull Item.TooltipContext context, @NotNull List<Component> list) {
        //from farmer's delight
        FoodProperties foodStats = stack.getFoodProperties(null);
        if (foodStats != null) {
            List<FoodProperties.PossibleEffect> effectList = foodStats.effects();
            List<Pair<Holder<Attribute>, AttributeModifier>> attributeList = Lists.newArrayList();
            if (!effectList.isEmpty()) {
                for(FoodProperties.PossibleEffect possibleEffect : effectList) {
                    MobEffectInstance instance = possibleEffect.effect();
                    MutableComponent mutableComponent = Component.translatable(instance.getDescriptionId());
                    MobEffect effect = instance.getEffect().value();
                    effect.createModifiers(instance.getAmplifier(), (attributeHolder, attributeModifier) -> attributeList.add(new Pair(attributeHolder, attributeModifier)));
                    if (instance.getAmplifier() > 0) {
                        mutableComponent = Component.translatable("potion.withAmplifier", mutableComponent, Component.translatable("potion.potency." + instance.getAmplifier()));
                    }

                    if (instance.getDuration() > 20) {
                        mutableComponent = Component.translatable("potion.withDuration", mutableComponent, MobEffectUtil.formatDuration(instance, 1, context.tickRate()));
                    }

                    list.add(mutableComponent.withStyle(effect.getCategory().getTooltipFormatting()));
                }
            }

            if (!attributeList.isEmpty()) {
                list.add(CommonComponents.EMPTY);
                list.add(Component.translatable("potion.whenDrank").withStyle(ChatFormatting.DARK_PURPLE));

                for(Pair<Holder<Attribute>, AttributeModifier> pair : attributeList) {
                    AttributeModifier attributemodifier = pair.getSecond();
                    double amount = attributemodifier.amount();
                    double formattedAmount;
                    if (attributemodifier.operation() != AttributeModifier.Operation.ADD_MULTIPLIED_BASE && attributemodifier.operation() != AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL) {
                        formattedAmount = attributemodifier.amount();
                    } else formattedAmount = attributemodifier.amount() * (double)100.0F;

                    if (amount > (double)0.0F) {
                        list.add(Component.translatable("attribute.modifier.plus." + attributemodifier.operation().id(), ItemAttributeModifiers.ATTRIBUTE_MODIFIER_FORMAT.format(formattedAmount), Component.translatable(((Attribute)((Holder)pair.getFirst()).value()).getDescriptionId())).withStyle(ChatFormatting.BLUE));
                    } else if (amount < (double)0.0F) {
                        formattedAmount *= -1.0F;
                        list.add(Component.translatable("attribute.modifier.take." + attributemodifier.operation().id(), ItemAttributeModifiers.ATTRIBUTE_MODIFIER_FORMAT.format(formattedAmount), Component.translatable(((Attribute)((Holder)pair.getFirst()).value()).getDescriptionId())).withStyle(ChatFormatting.RED));
                    }
                }
            }
        }
        //
    }
}
