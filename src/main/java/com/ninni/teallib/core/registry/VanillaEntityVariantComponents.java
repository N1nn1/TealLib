package com.ninni.teallib.core.registry;

import com.ninni.teallib.api.common.data.variant.EntityVariantComponents;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.TropicalFish;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.ninni.teallib.api.common.data.variant.EntityVariantComponents.GRAY_ITALIC;

//Here's some small examples using vanilla things!

public class VanillaEntityVariantComponents {

    public static void register() {
        EntityVariantComponents.register(EntityType.FOX, 0, VanillaEntityVariantComponents::fox);
        EntityVariantComponents.register(EntityType.MOOSHROOM, 0, VanillaEntityVariantComponents::mooshroom);
        EntityVariantComponents.register(EntityType.RABBIT, 0, VanillaEntityVariantComponents::rabbit);
        EntityVariantComponents.register(EntityType.HORSE, 100, VanillaEntityVariantComponents::horse);
        EntityVariantComponents.register(EntityType.TROPICAL_FISH, 100, VanillaEntityVariantComponents::tropicalFish);
    }

    public static Optional<List<Component>> fox(EntityVariantComponents.Context context) {
        return EntityVariantComponents.valueToOneComponent(context.entityId(), EntityVariantComponents.firstString(context, "Type"));
    }

    public static Optional<List<Component>> mooshroom(EntityVariantComponents.Context context) {
        return EntityVariantComponents.valueToOneComponent(context.entityId(), EntityVariantComponents.firstString(context, "Type"));
    }

    public static Optional<List<Component>> rabbit(EntityVariantComponents.Context context) {
        return EntityVariantComponents.valueToOneComponent(context.entityId(), EntityVariantComponents.firstString(context, "RabbitType"));
    }

    private static Optional<List<Component>> horse(EntityVariantComponents.Context context) {
        if (!context.has("Variant", Tag.TAG_INT)) return Optional.empty();
        int packed = context.getInt("Variant");

        int colorId = packed & 0xFF;
        int markingsId = (packed >> 8) & 0xFF;

        String color = horseColorName(colorId);
        String markings = horseMarkingName(markingsId);

        if (color != null && markings != null) {
            List<Component> rows = new ArrayList<>();

            MutableComponent colorComponent = EntityVariantComponents.translated(context.entityId(), "color", ResourceLocation.withDefaultNamespace(color));
            MutableComponent markingsComponent = EntityVariantComponents.translated(context.entityId(), "markings", ResourceLocation.withDefaultNamespace(markings));

            if (markingsId != 0) rows.add(Component.translatable("variant.minecraft.horse.with_markings", colorComponent, markingsComponent).withStyle(GRAY_ITALIC));
            else rows.add(colorComponent);

            return Optional.of(rows);
        }

        return Optional.empty();
    }

    private static Optional<List<Component>> tropicalFish(EntityVariantComponents.Context context) {
        int intVariant;
        if (context.has("Variant", Tag.TAG_INT)) intVariant = context.getInt("Variant");
        else if (context.has("BucketVariantTag", Tag.TAG_INT)) intVariant = context.getInt("BucketVariantTag");
        else return Optional.empty();

        TropicalFish.Variant variant = new TropicalFish.Variant(intVariant);

        List<Component> rows = new ArrayList<>();

        String s = "color.minecraft." + variant.baseColor();
        String s1 = "color.minecraft." + variant.patternColor();
        int i = TropicalFish.COMMON_VARIANTS.indexOf(variant);
        if (i != -1) {
            rows.add(Component.translatable(TropicalFish.getPredefinedName(i)).withStyle(GRAY_ITALIC));
        } else {
            rows.add(variant.pattern().displayName().plainCopy().withStyle(GRAY_ITALIC));
            MutableComponent mutablecomponent = Component.translatable(s).setStyle(Style.EMPTY.withColor(variant.baseColor().getFireworkColor()));
            if (!s.equals(s1)) {
                mutablecomponent.append(", ").append(Component.translatable(s1).setStyle(Style.EMPTY.withColor(variant.patternColor().getFireworkColor())));
            }
            rows.add(mutablecomponent);
        }

        return Optional.of(rows);
    }


    private static String horseColorName(int id) {
        return switch (id) {
            case 0 -> "white";
            case 1 -> "creamy";
            case 2 -> "chestnut";
            case 3 -> "brown";
            case 4 -> "black";
            case 5 -> "gray";
            case 6 -> "dark_brown";
            default -> null;
        };
    }

    private static String horseMarkingName(int id) {
        return switch (id) {
            case 0 -> "none";
            case 1 -> "white";
            case 2 -> "white_field";
            case 3 -> "white_dots";
            case 4 -> "black_dots";
            default -> null;
        };
    }
}
