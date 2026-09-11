package com.ninni.teallib.api.client.model;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;
import net.neoforged.neoforge.client.model.pipeline.VertexConsumerWrapper;
import org.jetbrains.annotations.NotNull;

public class AnimatedTextureVertexConsumer extends VertexConsumerWrapper {
    private final int frame;
    private final int frameCount;
    private final float alpha;

    public AnimatedTextureVertexConsumer(VertexConsumer parent, int frame, int frameCount) {
        this(parent, frame, frameCount, 1.0F);
    }

    public AnimatedTextureVertexConsumer(VertexConsumer parent, int frame, int frameCount, float alpha) {
        super(parent);
        this.frame = frame;
        this.frameCount = frameCount;
        this.alpha = alpha;
    }

    @Override
    public @NotNull VertexConsumer setUv(float u, float v) {
        float frameSize = 1.0F / frameCount;
        float animatedV = v * frameSize + frame * frameSize;
        return super.setUv(u, animatedV);
    }

    @Override
    public @NotNull VertexConsumer setColor(int red, int green, int blue, int alpha) {
        return super.setColor(red, green, blue, Mth.clamp((int) (alpha * this.alpha), 0, 255));
    }

    public static VertexConsumer getConsumer(VertexConsumer parent, float ageInTicks, float speed, int frameCount) {
        int frame = (int) (ageInTicks * speed) % frameCount;
        return new AnimatedTextureVertexConsumer(parent, frame, frameCount);
    }

    public static InterpolatedConsumers getInterpolatedConsumers(MultiBufferSource buffer, RenderType renderType, float ageInTicks, float speed, int frameCount) {
        float animationTime = ageInTicks * speed;

        int frame = Mth.floor(animationTime) % frameCount;
        int nextFrame = (frame + 1) % frameCount;

        float interpolation = Mth.frac(animationTime);
        VertexConsumer current = new AnimatedTextureVertexConsumer(buffer.getBuffer(renderType), frame, frameCount, 1.0F - interpolation);
        VertexConsumer next = new AnimatedTextureVertexConsumer(buffer.getBuffer(renderType), nextFrame, frameCount, interpolation);

        return new InterpolatedConsumers(current, next);
    }

    public record InterpolatedConsumers(VertexConsumer current, VertexConsumer next) { }
}