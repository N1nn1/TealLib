package com.ninni.teallib.api.common.data.variantdata;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.ninni.teallib.core.registry.VariantDataTypes;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

/**
 * Permanently applies an Attribute Modifier to a Living Entity.
 *
 * @param chance the chance, from {@code 0.0} to {@code 1.0}, that this Variant Data is applied.
 * @param attribute the Attribute to modify.
 * @param modifier the permanent Attribute Modifier to add.
 */
public record AttributeData(float chance, Holder<Attribute> attribute, AttributeModifier modifier) implements VariantData {
    public static final MapCodec<AttributeData> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            Codec.FLOAT.optionalFieldOf("chance", 1f).forGetter(AttributeData::chance),
            Attribute.CODEC.fieldOf("attribute").forGetter(AttributeData::attribute),
            AttributeModifier.MAP_CODEC.fieldOf("modifier").forGetter(AttributeData::modifier)
    ).apply(inst, AttributeData::new));

    @Override
    public void applyEntity(Entity entity, ServerLevel level, RandomSource random) {
        if (entity instanceof LivingEntity living) {
            if (random.nextFloat() > chance) return;
            AttributeInstance instance = living.getAttribute(attribute);
            if (instance != null) {
                instance.addPermanentModifier(modifier);
            }
        }
    }

    @Override
    public MapCodec<? extends VariantData> type() {
        return VariantDataTypes.ATTRIBUTE.get();
    }
}