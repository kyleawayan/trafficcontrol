package com.clussmanproductions.trafficcontrol.block;

import com.clussmanproductions.trafficcontrol.block.entity.StreetLightBlockEntity;

import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;

/**
 * Street light (single- or double-armed). The visible geometry is several
 * blocks tall and is drawn by a {@code BlockEntityRenderer}; the block itself
 * is invisible and only occupies its 1x1 post column.
 */
public class StreetLightBlock extends Block implements BlockEntityProvider {
	/** 16-way placement rotation, matching the original mod's custom angle. */
	public static final IntProperty ROTATION = IntProperty.of("rotation", 0, 15);

	private static final VoxelShape POST_SHAPE =
		createCuboidShape(6.0, 0.0, 6.0, 10.0, 16.0, 10.0);

	private final boolean doubleSided;

	public StreetLightBlock(Settings settings, boolean doubleSided) {
		super(settings);
		this.doubleSided = doubleSided;
		setDefaultState(getDefaultState().with(ROTATION, 0));
	}

	public boolean isDoubleSided() {
		return doubleSided;
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
	public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos,
			net.minecraft.block.ShapeContext context) {
		return POST_SHAPE;
	}

	@Override
	public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos,
			net.minecraft.block.ShapeContext context) {
		return POST_SHAPE;
	}

	@Override
	public BlockRenderType getRenderType(BlockState state) {
		// Drawn entirely by the BlockEntityRenderer.
		return BlockRenderType.INVISIBLE;
	}

	@Override
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
		return new StreetLightBlockEntity(pos, state);
	}
}
