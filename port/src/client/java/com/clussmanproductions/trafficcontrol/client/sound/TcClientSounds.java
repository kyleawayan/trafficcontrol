package com.clussmanproductions.trafficcontrol.client.sound;

import com.clussmanproductions.trafficcontrol.ModSounds;
import com.clussmanproductions.trafficcontrol.block.entity.BellBlockEntity;
import com.clussmanproductions.trafficcontrol.block.entity.CrossingGateBlockEntity;

import net.minecraft.client.MinecraftClient;
import net.minecraft.sound.SoundEvent;

/**
 * Installs the client-side sound hooks for the bell and crossing gate block
 * entities, which start a looping sound when active and let it stop itself
 * when the block goes idle.
 */
public final class TcClientSounds {
	private TcClientSounds() {}

	public static void register() {
		BellBlockEntity.SOUND_HOOK = TcClientSounds::tickBell;
		CrossingGateBlockEntity.SOUND_HOOK = TcClientSounds::tickGate;
	}

	private static void tickBell(BellBlockEntity be) {
		LoopingSoundInstance current = (LoopingSoundInstance) be.clientSound;
		if (be.isRinging() && (current == null || current.isDone())) {
			SoundEvent sound = be.getSound();
			if (sound == null) {
				return;
			}
			LoopingSoundInstance instance = new LoopingSoundInstance(sound, be.getPos(), 4.0F,
				() -> !be.isRemoved() && be.isRinging());
			MinecraftClient.getInstance().getSoundManager().play(instance);
			be.clientSound = instance;
		}
	}

	private static void tickGate(CrossingGateBlockEntity be) {
		LoopingSoundInstance current = (LoopingSoundInstance) be.clientSound;
		if (be.isMoving() && (current == null || current.isDone())) {
			LoopingSoundInstance instance = new LoopingSoundInstance(ModSounds.GATE, be.getPos(), 0.3F,
				() -> !be.isRemoved() && be.isMoving());
			MinecraftClient.getInstance().getSoundManager().play(instance);
			be.clientSound = instance;
		}
	}
}
