package com.ninni.teallib.common.animation;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.LivingEntity;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Holds multiple independent AnimationState channels for one entity.
 * <p>
 * Server-side:
 * - call playOnce(...)
 * - call playLoop(...)
 * - call stop(...)
 * - call setProgress(...) for normalized pose drives
 * <p>
 * Client-side:
 * - call readSync(...) whenever synced data changes
 * <p>
 * Rendering:
 * - use state(id) for vanilla AnimationState-based animations
 * - use progress(id) for normalized pose / lerp-style animations
 */
public final class EntityAnimationController {
    private static final String ROOT_KEY = "animations";
    private static final String ID_KEY = "id";
    private static final String STARTED_KEY = "started";
    private static final String LOOPING_KEY = "looping";
    private static final String START_GAME_TIME_KEY = "start_game_time";
    private static final String DURATION_TICKS_KEY = "duration_ticks";
    private static final String PROGRESS_KEY = "progress";

    private final LivingEntity entity;

    private final Map<ResourceLocation, AnimationState> states = new LinkedHashMap<>();
    private final Map<ResourceLocation, AnimationSnapshot> snapshots = new LinkedHashMap<>();

    private boolean dirty;

    public EntityAnimationController(LivingEntity entity) {
        this.entity = entity;
    }

    public AnimationState state(ResourceLocation id) {
        return states.computeIfAbsent(id, ignored -> new AnimationState());
    }

    public boolean isPlaying(ResourceLocation id) {
        AnimationSnapshot snapshot = snapshots.get(id);
        return snapshot != null && snapshot.started;
    }

    public float progress(ResourceLocation id) {
        return snapshots.getOrDefault(id, AnimationSnapshot.EMPTY).progress;
    }

    public void playOnce(ResourceLocation id, int durationTicks) {
        int clampedDuration = Math.max(1, durationTicks);
        long now = entity.level().getGameTime();

        snapshots.put(id, new AnimationSnapshot(true, false, now, clampedDuration, progress(id)));
        state(id).start(entity.tickCount);
        dirty = true;
    }

    public void playLoop(ResourceLocation id) {
        long now = entity.level().getGameTime();

        snapshots.put(id, new AnimationSnapshot(true, true, now, -1, progress(id)));
        state(id).startIfStopped(entity.tickCount);
        dirty = true;
    }

    public void stop(ResourceLocation id) {
        snapshots.remove(id);

        AnimationState state = states.get(id);
        if (state != null) {
            state.stop();
        }

        dirty = true;
    }

    /**
     * Set a normalized 0 to 1 progress value for an animation.
     */
    public void setProgress(ResourceLocation id, float value) {
        float clamped = Mth.clamp(value, 0.0F, 1.0F);

        AnimationSnapshot old = snapshots.getOrDefault(id, AnimationSnapshot.EMPTY);
        snapshots.put(id, old.withProgress(clamped));
        dirty = true;
    }

    public void tickClient() {
        expireFinished();
    }

    public void tickServer() {
        expireFinished();
    }

    public boolean consumeDirty() {
        boolean wasDirty = dirty;
        dirty = false;
        return wasDirty;
    }

    public CompoundTag writeSync() {
        CompoundTag root = new CompoundTag();
        ListTag list = new ListTag();

        for (Map.Entry<ResourceLocation, AnimationSnapshot> entry : snapshots.entrySet()) {
            ResourceLocation id = entry.getKey();
            AnimationSnapshot snapshot = entry.getValue();

            CompoundTag nbt = new CompoundTag();
            nbt.putString(ID_KEY, id.toString());
            nbt.putBoolean(STARTED_KEY, snapshot.started);
            nbt.putBoolean(LOOPING_KEY, snapshot.looping);
            nbt.putLong(START_GAME_TIME_KEY, snapshot.startGameTime);
            nbt.putInt(DURATION_TICKS_KEY, snapshot.durationTicks);
            nbt.putFloat(PROGRESS_KEY, snapshot.progress);
            list.add(nbt);
        }

        root.put(ROOT_KEY, list);
        return root;
    }

    public void readSync(CompoundTag root) {
        Set<ResourceLocation> seen = new HashSet<>();

        if (root.contains(ROOT_KEY, Tag.TAG_LIST)) {
            ListTag list = root.getList(ROOT_KEY, Tag.TAG_COMPOUND);

            for (Tag tag : list) {
                if (!(tag instanceof CompoundTag nbt)) continue;

                ResourceLocation id = ResourceLocation.tryParse(nbt.getString(ID_KEY));
                if (id == null) continue;

                boolean started = nbt.getBoolean(STARTED_KEY);
                boolean looping = nbt.getBoolean(LOOPING_KEY);
                long startGameTime = nbt.getLong(START_GAME_TIME_KEY);
                int durationTicks = nbt.getInt(DURATION_TICKS_KEY);
                float progress = Mth.clamp(nbt.getFloat(PROGRESS_KEY), 0.0F, 1.0F);

                seen.add(id);

                AnimationSnapshot snapshot = new AnimationSnapshot(started, looping, startGameTime, durationTicks, progress);
                snapshots.put(id, snapshot);

                if (started) {
                    startFromSnapshot(id, snapshot);
                } else {
                    AnimationState state = states.get(id);
                    if (state != null) state.stop();
                }
            }
        }

        Iterator<Map.Entry<ResourceLocation, AnimationSnapshot>> it = snapshots.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<ResourceLocation, AnimationSnapshot> entry = it.next();
            if (seen.contains(entry.getKey())) continue;

            AnimationState state = states.get(entry.getKey());
            if (state != null) state.stop();
            it.remove();
        }

        dirty = false;
    }

    private void startFromSnapshot(ResourceLocation id, AnimationSnapshot snapshot) {
        long now = entity.level().getGameTime();
        long elapsed = Math.max(0L, now - snapshot.startGameTime);
        AnimationState state = state(id);

        if (!snapshot.looping && snapshot.durationTicks > 0 && elapsed >= snapshot.durationTicks) {
            state.stop();
            return;
        }

        int entityStartTick = Math.max(0, entity.tickCount - (int) elapsed);
        state.start(entityStartTick);
    }

    private void expireFinished() {
        long now = entity.level().getGameTime();

        Iterator<Map.Entry<ResourceLocation, AnimationSnapshot>> it = snapshots.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<ResourceLocation, AnimationSnapshot> entry = it.next();
            AnimationSnapshot snapshot = entry.getValue();

            if (!snapshot.started) continue;

            if (!snapshot.looping && snapshot.durationTicks > 0 && now - snapshot.startGameTime >= snapshot.durationTicks) {
                AnimationState state = states.get(entry.getKey());
                if (state != null) state.stop();
                it.remove();
                dirty = true;
            }
        }
    }

    private record AnimationSnapshot(boolean started, boolean looping, long startGameTime, int durationTicks, float progress) {
        static final AnimationSnapshot EMPTY = new AnimationSnapshot(false, false, 0L, -1, 0.0F);

        AnimationSnapshot withProgress(float newProgress) {
            return new AnimationSnapshot(started, looping, startGameTime, durationTicks, newProgress);
        }
    }
}