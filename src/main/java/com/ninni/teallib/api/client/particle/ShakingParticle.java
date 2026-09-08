package com.ninni.teallib.api.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

public class ShakingParticle extends TextureSheetParticle {
    private final SpriteSet sprites;

    public ShakingParticle(ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, SpriteSet sprites) {
        super(level, x, y, z, xSpeed, ySpeed, zSpeed);
        this.sprites = sprites;
        this.lifetime = 10 + random.nextInt(10);
        this.setSpriteFromAge(sprites);
        this.quadSize = 0f;
    }

    @Override
    public @NotNull ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_LIT;
    }

    @Override
    public void tick() {
        super.tick();
        this.setSpriteFromAge(sprites);
        this.quadSize = Mth.lerp(0.4F, quadSize, 0.15F);

        xd = random.nextGaussian() * 0.04;
        yd = random.nextGaussian() * 0.04;
        zd = random.nextGaussian() * 0.04;
    }

    @Override
    protected int getLightColor(float partialTick) {
        return Math.min(super.getLightColor(partialTick) + 80, 240);
    }

    public static class Factory implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Factory(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(@NotNull SimpleParticleType type, @NotNull ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            return new ShakingParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, this.sprites);
        }
    }
}
