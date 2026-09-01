package com.ninni.teallib.core.registry;

import com.ninni.teallib.api.common.data.variant.VariantTextureSlot;
import com.ninni.teallib.api.common.data.variant.util.VariantTextureRegistry;
import com.ninni.teallib.core.TealLib;
import net.minecraft.client.renderer.entity.layers.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Crackiness;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Bee;
import net.minecraft.world.entity.animal.Fox;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.animal.Panda;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.entity.monster.Strider;
import net.minecraft.world.entity.monster.Vex;

public final class VanillaVariantTextureSlots {

    public static final VariantTextureSlot ENDERMAN_EYES = VariantTextureRegistry.registerLayer(
            ResourceLocation.fromNamespaceAndPath(TealLib.MODID, "eyes"),
            EntityType.ENDERMAN,
            EyesLayer.class
    );
    public static final VariantTextureSlot CREEPER_CHARGED = VariantTextureRegistry.registerLayer(
            ResourceLocation.fromNamespaceAndPath(TealLib.MODID, "charged"),
            EntityType.CREEPER,
            CreeperPowerLayer.class
    );
    public static final VariantTextureSlot BREEZE_EYES = VariantTextureRegistry.registerLayer(
            ResourceLocation.fromNamespaceAndPath(TealLib.MODID, "eyes"),
            EntityType.BREEZE,
            BreezeEyesLayer.class
    );
    public static final VariantTextureSlot BREEZE_WIND = VariantTextureRegistry.registerLayer(
            ResourceLocation.fromNamespaceAndPath(TealLib.MODID, "wind"),
            EntityType.BREEZE,
            BreezeWindLayer.class
    );
    public static final VariantTextureSlot DROWNED_OUTER = VariantTextureRegistry.registerLayer(
            ResourceLocation.fromNamespaceAndPath(TealLib.MODID, "outer"),
            EntityType.DROWNED,
            DrownedOuterLayer.class
    );
    public static final VariantTextureSlot IRON_GOLEM_CRACKINESS_LOW = VariantTextureRegistry.registerLayer(
            ResourceLocation.fromNamespaceAndPath(TealLib.MODID, "crackiness_low"),
            EntityType.IRON_GOLEM,
            IronGolemCrackinessLayer.class,
            entity -> entity instanceof IronGolem golem && golem.getCrackiness() == Crackiness.Level.LOW
    );
    public static final VariantTextureSlot IRON_GOLEM_CRACKINESS_MEDIUM = VariantTextureRegistry.registerLayer(
            ResourceLocation.fromNamespaceAndPath(TealLib.MODID, "crackiness_medium"),
            EntityType.IRON_GOLEM,
            IronGolemCrackinessLayer.class,
            entity -> entity instanceof IronGolem golem && golem.getCrackiness() == Crackiness.Level.MEDIUM
    );
    public static final VariantTextureSlot IRON_GOLEM_CRACKINESS_HIGH = VariantTextureRegistry.registerLayer(
            ResourceLocation.fromNamespaceAndPath(TealLib.MODID, "crackiness_high"),
            EntityType.IRON_GOLEM,
            IronGolemCrackinessLayer.class,
            entity -> entity instanceof IronGolem golem && golem.getCrackiness() == Crackiness.Level.HIGH
    );
    public static final VariantTextureSlot PHANTOM_EYES = VariantTextureRegistry.registerLayer(
            ResourceLocation.fromNamespaceAndPath(TealLib.MODID, "eyes"),
            EntityType.PHANTOM,
            EyesLayer.class
    );
    public static final VariantTextureSlot PIG_SADDLE = VariantTextureRegistry.registerLayer(
            ResourceLocation.fromNamespaceAndPath(TealLib.MODID, "saddle"),
            EntityType.PIG,
            SaddleLayer.class
    );
    public static final VariantTextureSlot STRIDER_SADDLE = VariantTextureRegistry.registerLayer(
            ResourceLocation.fromNamespaceAndPath(TealLib.MODID, "saddle"),
            EntityType.STRIDER,
            SaddleLayer.class
    );
    public static final VariantTextureSlot SHEEP_FUR = VariantTextureRegistry.registerLayer(
            ResourceLocation.fromNamespaceAndPath(TealLib.MODID, "fur"),
            EntityType.SHEEP,
            SheepFurLayer.class
    );
    public static final VariantTextureSlot BOGGED_CLOTHING = VariantTextureRegistry.registerLayer(
            ResourceLocation.fromNamespaceAndPath(TealLib.MODID, "clothing"),
            EntityType.BOGGED,
            SkeletonClothingLayer.class
    );
    public static final VariantTextureSlot STRAY_CLOTHING = VariantTextureRegistry.registerLayer(
            ResourceLocation.fromNamespaceAndPath(TealLib.MODID, "clothing"),
            EntityType.STRAY,
            SkeletonClothingLayer.class
    );
    public static final VariantTextureSlot SPIDER_EYES = VariantTextureRegistry.registerLayer(
            ResourceLocation.fromNamespaceAndPath(TealLib.MODID, "eyes"),
            EntityType.SPIDER,
            EyesLayer.class
    );
    public static final VariantTextureSlot CAVE_SPIDER_EYES = VariantTextureRegistry.registerLayer(
            ResourceLocation.fromNamespaceAndPath(TealLib.MODID, "eyes"),
            EntityType.CAVE_SPIDER,
            EyesLayer.class
    );
    public static final VariantTextureSlot WITHER_ARMOR = VariantTextureRegistry.registerLayer(
            ResourceLocation.fromNamespaceAndPath(TealLib.MODID, "armor"),
            EntityType.WITHER,
            WitherArmorLayer.class
    );
    public static final VariantTextureSlot GHAST_SHOOTING = VariantTextureRegistry.registerBase(
            ResourceLocation.fromNamespaceAndPath(TealLib.MODID, "shooting"),
            EntityType.GHAST,
            entity -> entity instanceof Ghast ghast && ghast.isCharging()
    );
    public static final VariantTextureSlot BEE_ANGRY = VariantTextureRegistry.registerBase(
            ResourceLocation.fromNamespaceAndPath(TealLib.MODID, "angry"),
            EntityType.BEE,
            entity -> entity instanceof Bee bee && bee.isAngry()
    );
    public static final VariantTextureSlot BEE_ANGRY_NECTAR = VariantTextureRegistry.registerBase(
            ResourceLocation.fromNamespaceAndPath(TealLib.MODID, "angry_nectar"),
            EntityType.BEE,
            entity -> entity instanceof Bee bee && bee.isAngry() && bee.hasNectar()
    );
    public static final VariantTextureSlot BEE_NECTAR = VariantTextureRegistry.registerBase(
            ResourceLocation.fromNamespaceAndPath(TealLib.MODID, "nectar"),
            EntityType.BEE,
            entity -> entity instanceof Bee bee && bee.hasNectar()
    );
    public static final VariantTextureSlot FOX_SLEEP = VariantTextureRegistry.registerBase(
            ResourceLocation.fromNamespaceAndPath(TealLib.MODID, "sleep"),
            EntityType.FOX,
            entity -> entity instanceof Fox fox && fox.isSleeping()
    );
    public static final VariantTextureSlot PANDA_BROWN = VariantTextureRegistry.registerBase(
            ResourceLocation.fromNamespaceAndPath(TealLib.MODID, "brown"),
            EntityType.PANDA,
            entity -> entity instanceof Panda panda && panda.getVariant() == Panda.Gene.BROWN
    );
    public static final VariantTextureSlot PANDA_LAZY = VariantTextureRegistry.registerBase(
            ResourceLocation.fromNamespaceAndPath(TealLib.MODID, "lazy"),
            EntityType.PANDA,
            entity -> entity instanceof Panda panda && panda.getVariant() == Panda.Gene.LAZY
    );
    public static final VariantTextureSlot PANDA_WORRIED = VariantTextureRegistry.registerBase(
            ResourceLocation.fromNamespaceAndPath(TealLib.MODID, "worried"),
            EntityType.PANDA,
            entity -> entity instanceof Panda panda && panda.getVariant() == Panda.Gene.WORRIED
    );
    public static final VariantTextureSlot PANDA_PLAYFUL = VariantTextureRegistry.registerBase(
            ResourceLocation.fromNamespaceAndPath(TealLib.MODID, "playful"),
            EntityType.PANDA,
            entity -> entity instanceof Panda panda && panda.getVariant() == Panda.Gene.PLAYFUL
    );
    public static final VariantTextureSlot PANDA_WEAK = VariantTextureRegistry.registerBase(
            ResourceLocation.fromNamespaceAndPath(TealLib.MODID, "weak"),
            EntityType.PANDA,
            entity -> entity instanceof Panda panda && panda.getVariant() == Panda.Gene.WEAK
    );
    public static final VariantTextureSlot PANDA_AGGRESSIVE = VariantTextureRegistry.registerBase(
            ResourceLocation.fromNamespaceAndPath(TealLib.MODID, "aggressive"),
            EntityType.PANDA,
            entity -> entity instanceof Panda panda && panda.getVariant() == Panda.Gene.AGGRESSIVE
    );
    public static final VariantTextureSlot STRIDER_COLD = VariantTextureRegistry.registerBase(
            ResourceLocation.fromNamespaceAndPath(TealLib.MODID, "cold"),
            EntityType.STRIDER,
            entity -> entity instanceof Strider strider && strider.isSuffocating()
    );
    public static final VariantTextureSlot VEX_CHARGING = VariantTextureRegistry.registerBase(
            ResourceLocation.fromNamespaceAndPath(TealLib.MODID, "charging"),
            EntityType.VEX,
            entity -> entity instanceof Vex vex && vex.isCharging()
    );

    public static void init() {
    }

}
