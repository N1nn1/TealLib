package com.ninni.teallib.common.entity.animation;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.LivingEntity;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public final class EntityAnimationController {
    private static final String ROOT_KEY = "animations";
    private static final String ID_KEY = "id";
    private static final String STARTED_KEY = "started";
    private static final String LOOPING_KEY = "looping";
    private static final String START_GAME_TIME_KEY = "start_game_time";
    private static final String DURATION_TICKS_KEY = "duration_ticks";
    private static final String PROGRESS_KEY = "progress";
    private static final String LIFECYCLE_SERIAL_KEY = "lifecycle_serial";

    private final LivingEntity entity;

    private final Map<ResourceLocation, AnimationState> states = new LinkedHashMap<>();
    private final Map<ResourceLocation, AnimationSnapshot> snapshots = new LinkedHashMap<>();

    private final Map<ResourceLocation, Long> appliedLifecycleSerials = new HashMap<>();
    private final Map<ResourceLocation, Integer> clientStartTicks = new HashMap<>();

    private boolean dirty;

    public EntityAnimationController(LivingEntity entity) {
        this.entity = entity;
    }

    public AnimationState state(ResourceLocation id) {
        return states.computeIfAbsent(id, ignored -> new AnimationState());
    }

    public boolean isStopped(ResourceLocation id) {
        AnimationSnapshot snapshot = snapshots.get(id);
        return snapshot == null || !snapshot.started;
    }

    public boolean isPlaying(ResourceLocation id) {
        return !isStopped(id);
    }

    public float progress(ResourceLocation id) {
        return snapshots.getOrDefault(id, AnimationSnapshot.EMPTY).progress;
    }

    public void playOnce(ResourceLocation id, int durationTicks) {
        int clampedDuration = Math.max(1, durationTicks);
        long now = entity.level().getGameTime();

        AnimationSnapshot previous = snapshots.getOrDefault(id, AnimationSnapshot.EMPTY);
        long serial = previous.lifecycleSerial + 1L;

        snapshots.put(id, previous.withLifecycle(true, false, now, clampedDuration, serial));

        AnimationState state = state(id);
        state.stop();
        state.start(entity.tickCount);

        appliedLifecycleSerials.put(id, serial);
        if (entity.level().isClientSide) {
            clientStartTicks.put(id, entity.tickCount);
        }

        dirty = true;
    }

    public void playLoop(ResourceLocation id) {
        long now = entity.level().getGameTime();

        AnimationSnapshot previous = snapshots.getOrDefault(id, AnimationSnapshot.EMPTY);
        long serial = previous.lifecycleSerial + 1L;

        snapshots.put(id, previous.withLifecycle(true, true, now, -1, serial));

        AnimationState state = state(id);
        state.stop();
        state.start(entity.tickCount);

        appliedLifecycleSerials.put(id, serial);
        if (entity.level().isClientSide) {
            clientStartTicks.put(id, entity.tickCount);
        }

        dirty = true;
    }

    public void stop(ResourceLocation id) {
        AnimationSnapshot previous = snapshots.getOrDefault(id, AnimationSnapshot.EMPTY);
        long now = entity.level().getGameTime();
        long serial = previous.lifecycleSerial + 1L;

        snapshots.put(id, previous.withLifecycle(false, false, now, -1, serial));

        AnimationState state = states.get(id);
        if (state != null) {
            state.stop();
        }

        clientStartTicks.remove(id);
        appliedLifecycleSerials.put(id, serial);

        dirty = true;
    }

    public void setProgress(ResourceLocation id, float value) {
        float clamped = Mth.clamp(value, 0.0F, 1.0F);

        AnimationSnapshot previous = snapshots.getOrDefault(id, AnimationSnapshot.EMPTY);
        snapshots.put(id, previous.withProgress(clamped));

        dirty = true;
    }

    public void tickClient() {
        expireClientOneShots();
    }

    public void tickServer() {
        expireServerOneShots();
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
            nbt.putLong(LIFECYCLE_SERIAL_KEY, snapshot.lifecycleSerial);
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
                long serial = nbt.getLong(LIFECYCLE_SERIAL_KEY);

                seen.add(id);

                AnimationSnapshot incoming = new AnimationSnapshot(started, looping, startGameTime, durationTicks, progress, serial);
                snapshots.put(id, incoming);

                if (!started) {
                    AnimationState state = states.get(id);
                    if (state != null) state.stop();

                    clientStartTicks.remove(id);
                    appliedLifecycleSerials.put(id, serial);
                    continue;
                }

                Long applied = appliedLifecycleSerials.get(id);
                AnimationState state = state(id);

                boolean needsRestart = applied == null || applied.longValue() != serial || !state.isStarted();

                if (needsRestart) {
                    state.stop();
                    state.start(entity.tickCount);
                    clientStartTicks.put(id, entity.tickCount);
                    appliedLifecycleSerials.put(id, serial);
                }
            }
        }

        Iterator<Map.Entry<ResourceLocation, AnimationSnapshot>> it = snapshots.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<ResourceLocation, AnimationSnapshot> entry = it.next();
            if (seen.contains(entry.getKey())) continue;

            AnimationState state = states.get(entry.getKey());
            if (state != null) state.stop();

            clientStartTicks.remove(entry.getKey());
            appliedLifecycleSerials.remove(entry.getKey());
            it.remove();
        }

        dirty = false;
    }

    private void expireClientOneShots() {
        int nowTick = entity.tickCount;

        for (Map.Entry<ResourceLocation, AnimationSnapshot> entry : snapshots.entrySet()) {
            ResourceLocation id = entry.getKey();
            AnimationSnapshot snapshot = entry.getValue();

            if (!snapshot.started || snapshot.looping || snapshot.durationTicks <= 0) continue;

            Integer startedAt = clientStartTicks.get(id);
            if (startedAt == null) continue;

            if (nowTick - startedAt >= snapshot.durationTicks) {
                AnimationState state = states.get(id);
                if (state != null) state.stop();

                clientStartTicks.remove(id);
            }
        }
    }

    private void expireServerOneShots() {
        long now = entity.level().getGameTime();

        for (Map.Entry<ResourceLocation, AnimationSnapshot> entry : snapshots.entrySet()) {
            ResourceLocation id = entry.getKey();
            AnimationSnapshot snapshot = entry.getValue();

            if (!snapshot.started || snapshot.looping || snapshot.durationTicks <= 0) continue;

            if (now - snapshot.startGameTime >= snapshot.durationTicks) {
                AnimationState state = states.get(id);
                if (state != null) state.stop();

                snapshots.put(id, new AnimationSnapshot(false, false, snapshot.startGameTime, -1, snapshot.progress, snapshot.lifecycleSerial + 1L));
                clientStartTicks.remove(id);
                dirty = true;
            }
        }
    }

    public int getTick(ResourceLocation id) {
        return Math.toIntExact(snapshots.get(id).startGameTime - snapshots.get(id).durationTicks);
    }

    private record AnimationSnapshot(boolean started, boolean looping, long startGameTime, int durationTicks, float progress, long lifecycleSerial) {
        static final AnimationSnapshot EMPTY = new AnimationSnapshot(false, false, 0L, -1, 0.0F, 0L);

        AnimationSnapshot withProgress(float newProgress) {
            return new AnimationSnapshot(started, looping, startGameTime, durationTicks, newProgress, lifecycleSerial);
        }

        AnimationSnapshot withLifecycle(boolean newStarted, boolean newLooping, long newStartGameTime, int newDurationTicks, long newSerial) {
            return new AnimationSnapshot(newStarted, newLooping, newStartGameTime, newDurationTicks, progress, newSerial);
        }
    }
}