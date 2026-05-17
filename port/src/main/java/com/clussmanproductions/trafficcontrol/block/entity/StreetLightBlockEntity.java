package com.clussmanproductions.trafficcontrol.block.entity;

import com.clussmanproductions.trafficcontrol.ModBlockEntities;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;

/**
 * Marker block entity for single and double street lights. It carries no data;
 * it exists so a {@code BlockEntityRenderer} can draw the multi-block-tall
 * model, which cannot be expressed as a static block model.
 */
public class StreetLightBlockEntity extends BlockEntity {
	public StreetLightBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.STREET_LIGHT, pos, state);
	}
}
