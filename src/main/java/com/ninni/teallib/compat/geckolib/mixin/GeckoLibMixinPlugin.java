package com.ninni.teallib.compat.geckolib.mixin;

import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

/** Keeps the bridge inert when GeckoLib is absent, since it is a compileOnly dependency. */
public final class GeckoLibMixinPlugin implements IMixinConfigPlugin {
    private static final String GECKOLIB_RENDERER = "software.bernie.geckolib.renderer.GeoRenderer";

    private boolean present;

    @Override
    public void onLoad(String mixinPackage) {
        boolean found;
        try {
            Class.forName(GECKOLIB_RENDERER, false, GeckoLibMixinPlugin.class.getClassLoader());
            found = true;
        } catch (Throwable throwable) {
            found = false;
        }
        this.present = found;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        return this.present;
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }
}
