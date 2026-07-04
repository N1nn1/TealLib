package com.ninni.teallib.core.mixin.accessor;

import net.minecraft.client.model.HierarchicalModel;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(HierarchicalModel.class)
public interface HierarchicalModelAccessor {
    @Accessor
    static Vector3f getANIMATION_VECTOR_CACHE() {
        throw new UnsupportedOperationException();
    }
}
