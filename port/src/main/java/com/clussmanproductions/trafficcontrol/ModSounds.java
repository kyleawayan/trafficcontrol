package com.clussmanproductions.trafficcontrol;

import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

/** Registers the mod's sound events; entries are defined in {@code sounds.json}. */
public final class ModSounds {
	private ModSounds() {}

	public static SoundEvent GATE;
	public static SoundEvent WIG_WAG;
	public static SoundEvent WCH;
	public static SoundEvent WCH_MECHANICAL_BELL;
	public static SoundEvent SAFETRAN_TYPE_3;
	public static SoundEvent SAFETRAN_MECHANICAL;
	public static SoundEvent PED_BUTTON;
	public static SoundEvent SCREWDRIVER;

	public static void register() {
		GATE = reg("gate");
		WIG_WAG = reg("wigwag");
		WCH = reg("wch");
		WCH_MECHANICAL_BELL = reg("wch_mechanical_bell");
		SAFETRAN_TYPE_3 = reg("safetran_type_3");
		SAFETRAN_MECHANICAL = reg("safetran_mechanical");
		PED_BUTTON = reg("ped_button");
		SCREWDRIVER = reg("screwdriver");
	}

	private static SoundEvent reg(String name) {
		Identifier id = new Identifier(TrafficControl.MOD_ID, name);
		return Registry.register(Registries.SOUND_EVENT, id, SoundEvent.of(id));
	}
}
