package com.clussmanproductions.trafficcontrol.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.Direction;

/**
 * A plain decorative block that can be placed in any of the four horizontal
 * directions, oriented from the player's facing on placement.
 */
public class TcHorizontalBlock extends Block {
	public static final DirectionProperty FACING = Properties.HORIZONTAL_FACING;

	public TcHorizontalBlock(Settings settings) {
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
}
