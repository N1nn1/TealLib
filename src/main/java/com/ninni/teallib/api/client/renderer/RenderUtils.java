package com.ninni.teallib.api.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.joml.Vector3f;

import java.util.List;

/**
 * A collection of rendering utilities for working with baked models and model layers.
 * <p>
 * These methods simplify rendering arbitrary baked models from a
 * {@link net.minecraft.resources.ResourceLocation ResourceLocation},
 * as well as baking {@link net.minecraft.client.model.geom.ModelPart ModelParts}
 * from a {@link net.minecraft.client.model.geom.ModelLayerLocation ModelLayerLocation}.
 * <p>
 * This is particularly useful when rendering custom blocks, entities or
 * special effects without having to repeatedly resolve baked models yourself.
 */
public class RenderUtils {

    /**
     * Renders a baked model with full white color ({@code 1,1,1,1}).
     *
     * @param loc The model Resource Location.
     * @param poseStack The current Pose Stack.
     * @param consumer The Vertex Consumer used for rendering.
     * @param packedLight The packed light value.
     * @param overlayCoord The packed overlay coordinates.
     */
    public static void renderModel(ResourceLocation loc, PoseStack poseStack, VertexConsumer consumer, int packedLight, int overlayCoord) {
        Vector3f colors = new Vector3f(1,1,1);
        renderModel(loc, poseStack, consumer, packedLight, overlayCoord, colors.x, colors.y, colors.z,1);
    }

    /**
     * Renders a baked model with the specified color tint.
     *
     * @param r The red color component.
     * @param g The green color component.
     * @param b The blue color component.
     * @param a The alpha component.
     */
    public static void renderModel(ResourceLocation loc, PoseStack poseStack, VertexConsumer consumer, int packedLight, int overlayCoord, float r, float g, float b, float a) {
        ItemRenderer renderer = Minecraft.getInstance().getItemRenderer();
        ModelManager manager = Minecraft.getInstance().getModelManager();
        for (BakedModel pass : manager.getModel(ModelResourceLocation.standalone(loc)).getRenderPasses(ItemStack.EMPTY, true)) {
            consumer.setColor(r, g, b, a);
            renderer.renderModelLists(pass, ItemStack.EMPTY, packedLight, overlayCoord, poseStack, consumer);
            consumer.setColor(1, 1, 1, 1);
        }
    }

    /**
     * Bakes and returns the {@link ModelPart Model Part} associated with the given
     * {@link ModelLayerLocation Model Layer Location}.
     *
     * @param location The Model Layer Location to bake.
     * @return the baked Model Part.
     */
    public static ModelPart bakeLayer(ModelLayerLocation location) {
        return Minecraft.getInstance().getEntityModels().bakeLayer(location);
    }

    /**
     * A cached baked model that has already resolved its render passes.
     * <p>
     * Resolving baked models can be relatively expensive, so if the same model
     * is rendered multiple times it is recommended to resolve it once and reuse
     * the resulting {@link ResolvedModel}.
     */
    public static final class ResolvedModel {
        private final List<BakedModel> passes;
        private ResolvedModel(List<BakedModel> passes) { this.passes = passes; }

        /**
         * Resolves all render passes for the specified baked model.
         *
         * @param loc The model Resource Location.
         * @return a cached Resolved Model that can be rendered repeatedly.
         */
        public static ResolvedModel resolve(ResourceLocation loc) {
            ModelManager manager = Minecraft.getInstance().getModelManager();
            return new ResolvedModel(manager.getModel(ModelResourceLocation.standalone(loc)).getRenderPasses(ItemStack.EMPTY, true));
        }

        /**
         * Renders all cached render passes using the supplied color.
         *
         * @param poseStack The current Pose Stack.
         * @param consumer The Vertex Consumer used for rendering.
         * @param packedLight The packed light value.
         * @param overlayCoord The packed overlay coordinates.
         * @param r The red color component.
         * @param g The green color component.
         * @param b The blue color component.
         * @param a The alpha component.
         */
        public void emit(PoseStack poseStack, VertexConsumer consumer, int packedLight, int overlayCoord, float r, float g, float b, float a) {
            ItemRenderer renderer = Minecraft.getInstance().getItemRenderer();

            for (BakedModel pass : passes) {
                consumer.setColor(r, g, b, a);
                renderer.renderModelLists(pass, ItemStack.EMPTY, packedLight, overlayCoord, poseStack, consumer);
                consumer.setColor(1, 1, 1, 1);
            }
        }
    }
}
