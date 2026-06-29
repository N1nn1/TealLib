package com.ninni.teallib.compat.fieldguide;

import com.evandev.fieldguide.variant.FieldGuideVariantManager;
import com.ninni.teallib.common.entity.variant.JsonVariantHolder;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class FieldGuidePlugin {

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static void register() {
        JsonVariantProvider provider = new JsonVariantProvider();

        //TODO idk if this works
        for (Field field : EntityType.class.getDeclaredFields()) {
            Class<?> entityClass = getEntityClass(field);
            if (entityClass != null && Mob.class.isAssignableFrom(entityClass) && JsonVariantHolder.class.isAssignableFrom(entityClass)) {
                FieldGuideVariantManager.registerProvider((Class) entityClass, provider);
            }
        }
    }

    @Nullable
    private static Class<?> getEntityClass(Field field) {
        if (field.getType() != DeferredHolder.class) return null;
        if (!(field.getGenericType() instanceof ParameterizedType holderType)) return null;

        Type[] holderArgs = holderType.getActualTypeArguments();
        if (holderArgs.length < 2 || !(holderArgs[1] instanceof ParameterizedType entityType)) return null;

        Type[] entityArgs = entityType.getActualTypeArguments();
        if (entityArgs.length < 1 || !(entityArgs[0] instanceof Class<?> entityClass)) return null;

        return entityClass;
    }
}
