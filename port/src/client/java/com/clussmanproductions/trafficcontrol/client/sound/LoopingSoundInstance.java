package com.clussmanproductions.trafficcontrol.client.sound;

import java.util.function.BooleanSupplier;

import net.minecraft.client.sound.MovingSoundInstance;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;

/**
 * A looping sound fixed at a block position. It stops when {@code shouldContinue}
 * returns false — used for crossing bells and the crossing gate motor.
 */
public class LoopingSoundInstance extends MovingSoundInstance {
	private final BooleanSupplier shouldContinue;

	public LoopingSoundInstance(SoundEvent sound, BlockPos pos, float volume, BooleanSupplier shouldContinue) {
		super(sound, SoundCategory.BLOCKS, Random.create());
		this.shouldContinue = shouldContinue;
		this.x = pos.getX() + 0.5;
		this.y = pos.getY() + 0.5;
		this.z = pos.getZ() + 0.5;
		this.volume = volume;
		this.pitch = 1.0F;
		this.repeat = true;
		this.repeatDelay = 0;
	}

	@Override
	public void tick() {
		if (!shouldContinue.getAsBoolean()) {
			setDone();
		}
	}
}
