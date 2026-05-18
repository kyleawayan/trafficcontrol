package com.clussmanproductions.trafficcontrol.block;

import com.clussmanproductions.trafficcontrol.block.entity.CrossingLampsBlockEntity;

import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

/**
 * A pair of crossing lamps. The flashing bulbs are drawn by a
 * {@code BlockEntityRenderer}; the lamps flash while the block receives
 * redstone power, or while a paired relay drives them. {@code LIT} mirrors that
 * powered state so the block can emit light and the renderer can flash.
 */
public class CrossingLampsBlock extends Block implements BlockEntityProvider {
	public static final IntProperty ROTATION = IntProperty.of("rotation", 0, 15);
	public static final BooleanProperty LIT = BooleanProperty.of("lit");

	private static final VoxelShape SHAPE = createCuboidShape(6.0, 0.0, 6.0, 10.0, 16.0, 10.0);

	/** Overhead lamps hang their bulbs differently from gate-mounted lamps. */
	private final boolean overhead;

	public CrossingLampsBlock(Settings settings, boolean overhead) {
		super(settings);
		this.overhead = overhead;
		setDefaultState(getDefaultState().with(ROTATION, 0).with(LIT, false));
	}

	public boolean isOverhead() {
		return overhead;
	}

	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		builder.add(ROTATION, LIT);
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
		return new CrossingLampsBlockEntity(pos, state);
	}

	@Override
	public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
		updatePower(world, pos, state);
	}

	@Override
	public void neighborUpdate(BlockState state, World world, BlockPos pos, Block sourceBlock,
			BlockPos sourcePos, boolean notify) {
		updatePower(world, pos, state);
	}

	private static void updatePower(World world, BlockPos pos, BlockState state) {
		if (world.isClient) {
			return;
		}
		setLit(world, pos, world.isReceivingRedstonePower(pos));
	}

	/** Sets the lit state; called by redstone updates and by a driving relay. */
	public static void setLit(World world, BlockPos pos, boolean lit) {
		BlockState state = world.getBlockState(pos);
		if (state.getBlock() instanceof CrossingLampsBlock && state.get(LIT) != lit) {
			// NOTIFY_LISTENERS only: syncs the change to clients and updates
			// light without poking neighbours. Notifying neighbours would make
			// adjacent crossing components re-check their own redstone and
			// switch off, fighting a relay that is driving the whole crossing.
			world.setBlockState(pos, state.with(LIT, lit), Block.NOTIFY_LISTENERS);
		}
	}
}
