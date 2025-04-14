package com.github.standobyte.jojo.init;

import java.util.function.Supplier;

import com.github.standobyte.jojo.core.JojoMod;

import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModSoundEvents {
	public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(Registries.SOUND_EVENT, JojoMod.MOD_ID);
	
	
	public static final Supplier<SoundEvent> STAND_SUMMON = SOUNDS.register("stand_summon", SoundEvent::createVariableRangeEvent);
	public static final Supplier<SoundEvent> STAND_UNSUMMON = SOUNDS.register("stand_unsummon", SoundEvent::createVariableRangeEvent);
	public static final Supplier<SoundEvent> STAND_DAMAGE_BLOCK = SOUNDS.register("stand_damage_block", SoundEvent::createVariableRangeEvent);
	public static final Supplier<SoundEvent> STAND_PUNCH_LIGHT = SOUNDS.register("stand_punch_light", SoundEvent::createVariableRangeEvent);
	public static final Supplier<SoundEvent> STAND_PUNCH_BARRAGE = SOUNDS.register("stand_punch_barrage", SoundEvent::createVariableRangeEvent);
	public static final Supplier<SoundEvent> STAND_PUNCH_HEAVY = SOUNDS.register("stand_punch_heavy", SoundEvent::createVariableRangeEvent);
	public static final Supplier<SoundEvent> STAND_CRASH = SOUNDS.register("stand_crash", SoundEvent::createVariableRangeEvent);
	public static final Supplier<SoundEvent> STAND_PARRY = SOUNDS.register("stand_parry", SoundEvent::createVariableRangeEvent);
	public static final Supplier<SoundEvent> STAND_LEAP = SOUNDS.register("stand_leap", SoundEvent::createVariableRangeEvent);
	public static final Supplier<SoundEvent> STAND_PUNCH_SWING = SOUNDS.register("stand_punch_swing", SoundEvent::createVariableRangeEvent);
	public static final Supplier<SoundEvent> STAND_PUNCH_HEAVY_SWING = SOUNDS.register("stand_punch_heavy_swing", SoundEvent::createVariableRangeEvent);
	public static final Supplier<SoundEvent> STAND_PUNCH_BARRAGE_SWING = SOUNDS.register("stand_punch_barrage_swing", SoundEvent::createVariableRangeEvent);
	public static final Supplier<SoundEvent> STAND_PUNCH_CRY = SOUNDS.register("stand_punch_cry", SoundEvent::createVariableRangeEvent);
	public static final Supplier<SoundEvent> STAND_PUNCH_HEAVY_CRY = SOUNDS.register("stand_punch_heavy_cry", SoundEvent::createVariableRangeEvent);
	public static final Supplier<SoundEvent> STAND_BARRAGE_CRY = SOUNDS.register("stand_barrage_cry", SoundEvent::createVariableRangeEvent);
	public static final Supplier<SoundEvent> STAR_PLATINUM_STAR_FINGER = SOUNDS.register("star_platinum_star_finger", SoundEvent::createVariableRangeEvent);
	public static final Supplier<SoundEvent> STAR_PLATINUM_ZOOM = SOUNDS.register("star_platinum_zoom", SoundEvent::createVariableRangeEvent);
	public static final Supplier<SoundEvent> STAR_PLATINUM_ZOOM_CLICK = SOUNDS.register("star_platinum_zoom_click", SoundEvent::createVariableRangeEvent);
	public static final Supplier<SoundEvent> STAR_PLATINUM_INHALE = SOUNDS.register("star_platinum_inhale", SoundEvent::createVariableRangeEvent);
	public static final Supplier<SoundEvent> TIME_STOP = SOUNDS.register("time_stop", SoundEvent::createVariableRangeEvent);
	public static final Supplier<SoundEvent> TIME_RESUME = SOUNDS.register("time_resume", SoundEvent::createVariableRangeEvent);
	public static final Supplier<SoundEvent> TIME_STOP_BLINK = SOUNDS.register("time_stop_blink", SoundEvent::createVariableRangeEvent);
	
}
