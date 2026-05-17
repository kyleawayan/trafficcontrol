package com.clussmanproductions.trafficcontrol.block;

import com.clussmanproductions.trafficcontrol.ModBlockEntities;
import com.clussmanproductions.trafficcontrol.block.entity.WigWagBlockEntity;

import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

/**
 * Lower-quadrant wig wag. The swinging banner is drawn by a
 * {@code BlockEntityRenderer}; the banner swings while the block receives
 * redstone power.
 */
public class WigWagBlock extends Block implements BlockEntityProvider {
	public static final IntProperty ROTATION = IntProperty.of("rotation", 0, 15);

	private static final VoxelShape SHAPE = createCuboidShape(5.0, 0.0, 5.0, 11.0, 16.0, 11.0);

	public WigWagBlock(Settings settings) {
		super(settings);
		setDefaultState(getDefaultState().with(ROTATION, 0));
	}

	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		builder.add(ROTATION);
	}

	@Override
	public BlockState getPlacementState(ItemPlacementContext ctx) {
		float yaw = ctx.getPlayerYaw();
		int rotation = MathHelper.floor((double) ((yaw + 180.0F) * 16.0F / 360.0F) + 0.5D) & 15;
		return getDefaultState().with(ROTATION, rotation);
	}

	@Override
	public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		return SHAPE;
	}

	@Override
	public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		return SHAPE;
	}

	@Override
	public BlockRenderType getRenderType(BlockState state) {
		return BlockRenderType.INVISIBLE;
	}

	@Override
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
		return new WigWagBlockEntity(pos, state);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state,
			BlockEntityType<T> type) {
		if (!world.isClient || type != ModBlockEntities.WIG_WAG) {
			return null;
		}
		return (BlockEntityTicker<T>) (BlockEntityTicker<WigWagBlockEntity>) WigWagBlockEntity::clientTick;
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
		if (world.getBlockEntity(pos) instanceof WigWagBlockEntity be) {
			be.setActive(world.isReceivingRedstonePower(pos));
		}
	}
}
