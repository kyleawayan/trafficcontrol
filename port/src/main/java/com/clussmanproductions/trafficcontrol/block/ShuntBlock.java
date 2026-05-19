package com.clussmanproductions.trafficcontrol.block;

import com.clussmanproductions.trafficcontrol.ModBlockEntities;
import com.clussmanproductions.trafficcontrol.block.entity.ShuntBlockEntity;

import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

/**
 * A train shunt ({@code shunt_border} / {@code shunt_island}). When Minecraft
 * Transit Railway is installed, a shunt detects MTR trains within range and
 * powers any relay it is linked to with the tuner, which then drives the
 * crossing. Without MTR the shunt is an inert decorative block. Both shunt
 * variants behave identically — they are proximity sensors.
 */
public class ShuntBlock extends Block implements BlockEntityProvider {
	public static final DirectionProperty FACING = Properties.HORIZONTAL_FACING;

	public ShuntBlock(Settings settings) {
		super(settings);
		setDefaultState(getDefaultState().with(FACING, Direction.NORTH));
	}

	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		builder.add(FACING);
	}

	@Override
	public BlockState getPlacementState(ItemPlacementContext ctx) {
		return getDefaultState().with(FACING, ctx.getHorizontalPlayerFacing().getOpposite());
	}

	@Override
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
		return new ShuntBlockEntity(pos, state);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state,
			BlockEntityType<T> type) {
		if (type != ModBlockEntities.SHUNT) {
			return null;
		}
		if (world.isClient) {
			return (BlockEntityTicker<T>) (BlockEntityTicker<ShuntBlockEntity>) ShuntBlockEntity::clientTick;
		}
		return (BlockEntityTicker<T>) (BlockEntityTicker<ShuntBlockEntity>) ShuntBlockEntity::serverTick;
	}
}
