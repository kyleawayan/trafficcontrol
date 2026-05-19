package com.clussmanproductions.trafficcontrol.block;

import com.clussmanproductions.trafficcontrol.ModBlockEntities;
import com.clussmanproductions.trafficcontrol.block.entity.RelayBlockEntity;

import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

/**
 * The crossing relay. While the relay receives redstone power it drives every
 * component linked to it with the tuner — closing gates, ringing bells,
 * flashing lamps and swinging wig wags — replacing the original mod's
 * ImmersiveRailroading train detection. Linking is single-block; the original
 * eight-block multiblock relay is collapsed onto this one block.
 */
public class RelayBlock extends Block implements BlockEntityProvider {
	public static final DirectionProperty FACING = HorizontalFacingBlock.FACING;

	public RelayBlock(Settings settings) {
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
		return new RelayBlockEntity(pos, state);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state,
			BlockEntityType<T> type) {
		if (world.isClient || type != ModBlockEntities.RELAY) {
			return null;
		}
		return (BlockEntityTicker<T>) (BlockEntityTicker<RelayBlockEntity>) RelayBlockEntity::serverTick;
	}

	@Override
	public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
		updatePower(world, pos);
	}

	@Override
	public void neighborUpdate(BlockState state, World world, BlockPos pos, Block sourceBlock,
			BlockPos sourcePos, boolean notify) {
		updatePower(world, pos);
	}

	private static void updatePower(World world, BlockPos pos) {
		if (world.isClient) {
			return;
		}
		if (world.getBlockEntity(pos) instanceof RelayBlockEntity be) {
			be.setRedstonePowered(world.isReceivingRedstonePower(pos));
		}
	}

	@Override
	public boolean hasComparatorOutput(BlockState state) {
		return true;
	}

	@Override
	public int getComparatorOutput(BlockState state, World world, BlockPos pos) {
		return world.getBlockEntity(pos) instanceof RelayBlockEntity be && be.isPowered() ? 15 : 0;
	}
}
