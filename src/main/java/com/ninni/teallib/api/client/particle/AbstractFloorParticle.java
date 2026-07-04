package com.ninni.teallib.api.client.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * A horizontal particle, can be made to rotate
 */
public abstract class AbstractFloorParticle extends TextureSheetParticle {
    protected AbstractFloorParticle(ClientLevel level, double x, double y, double z) {
        super(level, x, y, z);
    }
    @Override
    public @NotNull AABB getRenderBoundingBox(float partialTicks) {
        return AABB.INFINITE;
    }

    @SuppressWarnings("SameParameterValue")
    protected void renderRotatedParticle(VertexConsumer vertexConsumer, Camera camera, float delta, boolean bl, float rotatingAmount) {
        Vec3 vec3 = camera.getPosition();
        float f = (float)(Mth.lerp(delta, this.xo, this.x) - vec3.x());
        float f1 = (float)(Mth.lerp(delta, this.yo, this.y) - vec3.y());
        float f2 = (float)(Mth.lerp(delta, this.zo, this.z) - vec3.z());
        Vector3f vector = (new Vector3f(0.5F, 0.5F, 0.5F)).normalize();
        Quaternionf quaternionf1 = (new Quaternionf()).setAngleAxis(0.0F, vector.x(), vector.y(), vector.z());
        quaternionf1.rotationX(Mth.PI / 2);
        Vector3f[] avector3f = new Vector3f[]{new Vector3f(-1.0F, -1.0F, 0.0F), new Vector3f(-1.0F, 1.0F, 0.0F), new Vector3f(1.0F, 1.0F, 0.0F), new Vector3f(1.0F, -1.0F, 0.0F)};
        float f3 = this.getQuadSize(delta);

        for(int i = 0; i < 4; ++i) {
            Vector3f vector3f = avector3f[i];
            vector3f.rotate(quaternionf1);
            if (bl) vector3f.rotate(new Quaternionf().rotationX(Mth.PI));
            if (rotatingAmount > 0) vector3f.rotate(new Quaternionf().rotationY(this.age * rotatingAmount));
            vector3f.mul(f3);
            vector3f.add(f, f1, f2);
        }

        int j = this.getLightColor(delta);
        this.makeCornerVertex(vertexConsumer, avector3f[0], this.getU1(), this.getV1(), j);
        this.makeCornerVertex(vertexConsumer, avector3f[1], this.getU1(), this.getV0(), j);
        this.makeCornerVertex(vertexConsumer, avector3f[2], this.getU0(), this.getV0(), j);
        this.makeCornerVertex(vertexConsumer, avector3f[3], this.getU0(), this.getV1(), j);
    }

    void makeCornerVertex(VertexConsumer vertexConsumer, Vector3f pos, float u, float v, int light) {
        vertexConsumer.addVertex(pos.x(), pos.y(), pos.z()).setUv(u, v).setColor(this.rCol, this.gCol, this.bCol, this.alpha).setLight(light);
    }
}
