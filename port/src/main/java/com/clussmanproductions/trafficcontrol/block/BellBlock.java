package com.clussmanproductions.trafficcontrol.block;

import com.clussmanproductions.trafficcontrol.ModBlockEntities;
import com.clussmanproductions.trafficcontrol.block.entity.BellBlockEntity;

import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.sound.SoundEvent;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

/**
 * A crossing bell. The bell rings (loops its sound) while the block receives
 * redstone power, replacing the original ImmersiveRailroading-driven trigger.
 */
public class BellBlock extends Block implements BlockEntityProvider {
	public static final DirectionProperty FACING = HorizontalFacingBlock.FACING;

	private static final VoxelShape SHAPE = createCuboidShape(6.0, 0.0, 6.0, 10.0, 16.0, 10.0);

	private final SoundEvent sound;

	public BellBlock(Settings settings, SoundEvent sound) {
		super(settings);
		this.sound = sound;
		setDefaultState(getDefaultState().with(FACING, Direction.NORTH));
	}

	public SoundEvent getSound() {
		return sound;
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
	public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		return SHAPE;
	}

	@Override
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
		return new BellBlockEntity(pos, state);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state,
			BlockEntityType<T> type) {
		if (!world.isClient || type != ModBlockEntities.BELL) {
			return null;
		}
		return (BlockEntityTicker<T>) (BlockEntityTicker<BellBlockEntity>) BellBlockEntity::clientTick;
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
		if (world.getBlockEntity(pos) instanceof BellBlockEntity be) {
			be.setRinging(world.isReceivingRedstonePower(pos));
		}
	}
}
