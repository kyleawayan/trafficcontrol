package com.clussmanproductions.trafficcontrol.block;

import com.clussmanproductions.trafficcontrol.ModBlockEntities;
import com.clussmanproductions.trafficcontrol.block.entity.CrossingGateBlockEntity;
import com.clussmanproductions.trafficcontrol.screen.CrossingGateScreenHandler;

import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

/**
 * Crossing gate. The counterweight and gate arm are drawn by a
 * {@code BlockEntityRenderer}; the gate lowers while the block receives
 * redstone power and raises when power is removed.
 */
public class CrossingGateBlock extends Block implements BlockEntityProvider {
	public static final IntProperty ROTATION = IntProperty.of("rotation", 0, 15);

	private static final VoxelShape SHAPE = createCuboidShape(4.0, 0.0, 4.0, 12.0, 16.0, 12.0);

	public CrossingGateBlock(Settings settings) {
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
		return new CrossingGateBlockEntity(pos, state);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state,
			BlockEntityType<T> type) {
		if (!world.isClient || type != ModBlockEntities.CROSSING_GATE) {
			return null;
		}
		return (BlockEntityTicker<T>) (BlockEntityTicker<CrossingGateBlockEntity>) CrossingGateBlockEntity::clientTick;
	}

	@Override
	public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player,
			Hand hand, BlockHitResult hit) {
		// Right-clicking empty-handed opens the gate's config screen.
		if (!player.getStackInHand(hand).isEmpty()) {
			return ActionResult.PASS;
		}
		if (!world.isClient && world.getBlockEntity(pos) instanceof CrossingGateBlockEntity gate) {
			player.openHandledScreen(new ExtendedScreenHandlerFactory() {
				@Override
				public Text getDisplayName() {
					return Text.translatable("block.trafficcontrol.crossing_gate_gate");
				}

				@Override
				public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity p) {
					return new CrossingGateScreenHandler(syncId, pos,
						gate.getGateLength(), gate.getCloseDelayTicks());
				}

				@Override
				public void writeScreenOpeningData(ServerPlayerEntity p, PacketByteBuf buf) {
					buf.writeBlockPos(pos);
					buf.writeVarInt(gate.getGateLength());
					buf.writeVarInt(gate.getCloseDelayTicks());
				}
			});
		}
		return ActionResult.SUCCESS;
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
		if (world.getBlockEntity(pos) instanceof CrossingGateBlockEntity be) {
			be.setClosed(world.isReceivingRedstonePower(pos));
		}
	}
}
