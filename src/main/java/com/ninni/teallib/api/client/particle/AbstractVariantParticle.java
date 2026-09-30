package com.ninni.teallib.api.client.particle;

import com.ninni.teallib.api.common.data.variant.VariantDefinition;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.TextureSheetParticle;

public abstract class AbstractVariantParticle extends TextureSheetParticle {
    private final VariantDefinition variant;

    public AbstractVariantParticle(ClientLevel level, VariantDefinition variant, double x, double y, double z) {
        super(level, x, y, z);
        this.variant = variant;
    }
    public AbstractVariantParticle(ClientLevel level, VariantDefinition variant, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
        super(level, x, y, z, xSpeed, ySpeed, zSpeed);
        this.variant = variant;
    }

    public VariantDefinition getVariant() {
        return variant;
    }
}
