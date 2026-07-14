package com.ninni.teallib.api.client.renderer.item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.ninni.teallib.api.common.entity.catchable.Catchable;
import com.ninni.teallib.api.common.entity.catchable.CustomInventoryRendering;
import com.ninni.teallib.api.common.entity.variant.JsonVariantHolder;
import com.ninni.teallib.api.common.item.tooltip.CapturedMobsTooltipData;
import com.ninni.teallib.core.mixin.accessor.EntityAccessor;
import com.ninni.teallib.core.mixin.accessor.TropicalFishAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.animal.Bucketable;
import net.minecraft.world.entity.animal.axolotl.Axolotl;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

/**
 * Client-side tooltip renderer for displaying captured mobs inside an item tooltip.
 * <p>
 * This renderer visualizes entities stored in a {@link net.minecraft.nbt.ListTag}
 * by reconstructing them from their saved NBT data and rendering them as 3D previews
 * inside a grid layout.
 * <p>
 * It supports multiple entity types, custom name rendering, variant-aware data,
 * bucket-style entities, and optional custom inventory rendering hooks via
 * {@link com.ninni.teallib.api.common.entity.catchable.CustomInventoryRendering}.
 */
@OnlyIn(Dist.CLIENT)
public class CapturedMobsTooltipRenderer implements ClientTooltipComponent {
    private final ListTag capturedMobs;

    public CapturedMobsTooltipRenderer(CapturedMobsTooltipData data) {
        this.capturedMobs = data.capturedMobs();
    }

    @Override
    public int getWidth(@NotNull Font font) {
        return (4 * 16) + 16;
    }

    @Override
    public int getHeight() {
        int total = 0;
        for (int h : computeRowHeights(Minecraft.getInstance().font)) total += h;
        return total;
    }

    @Override
    public void renderImage(@NotNull Font font, int x, int y, @NotNull GuiGraphics graphics) {
        Minecraft mc = Minecraft.getInstance();
        int cellSize = 16;
        int maxPerRow = 4;

        for (int i = 0; i < capturedMobs.size(); i++) {
            CompoundTag tag = capturedMobs.getCompound(i);
            EntityType<?> type = EntityType.byString(tag.getString("id")).orElse(null);
            if (type == null || mc.level == null) continue;

            Entity entity = type.create(mc.level);
            if (!(entity instanceof LivingEntity living)) continue;

            CompoundTag fishTag = tag.copy();
            fishTag.remove("id");
            fishTag.remove("CustomName");
            EntityType.updateCustomEntityTag(mc.level, null, entity, CustomData.of(fishTag));
            if (tag.contains("CustomName")) entity.setCustomName(Component.literal(tag.getString("CustomName")));

            if (entity instanceof Bucketable bucketable) {
                bucketable.loadFromBucketTag(fishTag);
                if (entity instanceof TropicalFishAccessor tf) {
                    if (fishTag.contains("BucketVariantTag")) tf.callSetPackedVariant(fishTag.getInt("BucketVariantTag"));
                    else tf.callSetPackedVariant(65536);
                }
            }
            if (entity instanceof Catchable catchable && entity instanceof Mob mob) catchable.loadDataFromTag(mob, fishTag);
            else if (entity instanceof JsonVariantHolder holder && !fishTag.contains("Variant")) holder.setVariant(holder.getDefaultVariant());
            if (entity instanceof EntityAccessor accessor) accessor.setTouchingWater(true);
            entity.setYHeadRot(0);

            float scaleMultiplier = 1;

            if (entity instanceof CustomInventoryRendering pose) {
                pose.setRenderedInTooltip(true);
                if (living instanceof AgeableMob ageableMob) {
                    if (pose.babyByDefault()) ageableMob.setBaby(true);
                    if (ageableMob.isBaby() && pose.scaleBaby()) scaleMultiplier = 0.5F;
                }
                pose.setCustomData(entity, fishTag);
            } else {
                if (living instanceof AgeableMob ageableMob && ageableMob.isBaby()) scaleMultiplier = 0.5F;
            }
            if (living instanceof Axolotl) scaleMultiplier *= 1.5F;

            //Scaling
            float maxSize = Math.max(entity.getDimensions(entity.getPose()).width(), entity.getDimensions(entity.getPose()).height());
            float scale = (cellSize * 0.8f) / maxSize;
            scale = Math.min(scale, 32.0F) * scaleMultiplier;

            //Positioning
            int col = i % maxPerRow;
            int row = i / maxPerRow;
            int[] rowHeights = computeRowHeights(font);
            int yOffset = 8;
            for (int r = 0; r < row; r++) yOffset += rowHeights[r];
            float renderX = x + col * cellSize + (cellSize / 2f) + 8;
            float renderY = y + yOffset + (rowHeights[row] / 2f);

            //Bobbing
            float time = (mc.level.getGameTime() + mc.getTimer().getGameTimeDeltaPartialTick(false)) / 20.0f;
            float bob = (float) Math.sin((time + i * 0.4f) * Math.PI * 0.5f) * 0.05f;
            if (entity instanceof CustomInventoryRendering pose && !pose.animateBob()) bob = 0;

            //Render
            PoseStack stack = graphics.pose();
            stack.pushPose();
            stack.translate(renderX, renderY + bob * scale, 50.0f + (i * 2));
            stack.scale(scale, -scale, scale);
            stack.mulPose(Axis.YP.rotationDegrees(45));
            stack.mulPose(Axis.XP.rotationDegrees((i % 4 - 1.5f) * 5f));
            if (living instanceof Axolotl) stack.mulPose(Axis.XP.rotationDegrees(25));

            int light = LightTexture.pack(15, 15);
            if (entity instanceof CustomInventoryRendering pose) light = LightTexture.pack(pose.getInventoryBlockLight(), pose.getInventorySkyLight());

            Minecraft.getInstance().getEntityRenderDispatcher().setRenderShadow(false);

            Minecraft.getInstance().getEntityRenderDispatcher().render(
                    living,
                    0.0, 0.0, 0.0,
                    0.0f,
                    1.0f,
                    stack,
                    graphics.bufferSource(),
                    light
            );
            Minecraft.getInstance().getEntityRenderDispatcher().setRenderShadow(true);

            if (living.hasCustomName()) {
                String name = living.getName().getString();
                int rawWidth = font.width(name);
                float maxWidthInPixels = cellSize * 0.8f;
                float nameScale = Math.min(maxWidthInPixels / rawWidth, 0.03f);
                float nameY = (living.getDimensions(living.getPose()).height() * 2);
                if (living instanceof AgeableMob ageableMob && ageableMob.isBaby()) nameY *= 2F;
                nameY += 0.15F;

                //Reset rotation so the name stays upright
                stack.mulPose(Axis.YN.rotationDegrees(45));
                stack.mulPose(Axis.XN.rotationDegrees((i % 4 - 1.5f) * 5f));

                stack.pushPose();
                stack.translate(0, nameY, 1);
                stack.scale(-nameScale, -nameScale, nameScale);

                //Background Box
                int textWidth = font.width(name);
                int padding = 2;
                int x1 = -textWidth / 2 - padding;
                int y1 = -1;
                int x2 = textWidth / 2 + padding;
                int y2 = font.lineHeight + 1;

                graphics.fill(x1, y1, x2, y2, 0xAA000000);
                stack.translate(0, nameY, 1);

                graphics.drawString(font, name, -textWidth / 2, 0, 0xFFFFFF, false);
                stack.popPose();
            }


            stack.popPose();
        }
    }

    @SuppressWarnings("unused")
    private int[] computeRowHeights(Font font) {
        int maxPerRow = 4;
        int rows = (capturedMobs.size() + 7) / maxPerRow;
        int[] rowHeights = new int[rows];

        for (int row = 0; row < rows; row++) {
            boolean hasName = false;
            for (int col = 0; col < maxPerRow; col++) {
                int index = row * maxPerRow + col;
                if (index >= capturedMobs.size()) break;

                CompoundTag tag = capturedMobs.getCompound(index);
                if (tag.contains("CustomName")) {
                    hasName = true;
                    break;
                }
            }
            rowHeights[row] = hasName ? 32 : 16;
        }

        return rowHeights;
    }

}
