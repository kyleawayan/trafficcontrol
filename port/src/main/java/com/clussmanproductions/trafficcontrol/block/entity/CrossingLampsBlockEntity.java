package com.clussmanproductions.trafficcontrol.block.entity;

import com.clussmanproductions.trafficcontrol.ModBlockEntities;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;

/**
 * Marker block entity for a pair of crossing lamps. Carries no persistent
 * state: whether the lamps are lit lives in the {@code LIT} block state
 * property, and the flash phase is derived from world time by the renderer.
 * It exists only so a {@code BlockEntityRenderer} can be attached.
 */
public class CrossingLampsBlockEntity extends BlockEntity {
	public CrossingLampsBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.LAMPS, pos, state);
	}
}
