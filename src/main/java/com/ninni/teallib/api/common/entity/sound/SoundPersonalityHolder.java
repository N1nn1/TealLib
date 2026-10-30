package com.ninni.teallib.api.common.entity.sound;

import net.minecraft.sounds.SoundEvent;
import org.jetbrains.annotations.Nullable;

public interface SoundPersonalityHolder {
    SoundPersonality getDefaultPersonality();
    SoundPersonality getPersonality();
    void setPersonality(SoundPersonality personality);

    default SoundEvent getSoundOrElse(String name, SoundEvent event) {
        SoundEvent soundEvent = getSound(name);
        return soundEvent != null ? soundEvent : event;
    }
    @Nullable
    default SoundEvent getSound(String name) {
        return getPersonality().getSoundEvent(name);
    }
}
