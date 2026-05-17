package com.clussmanproductions.trafficcontrol.block.entity;

import com.clussmanproductions.trafficcontrol.ModBlockEntities;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * State for a crossing gate. {@code closed} is set from redstone on the server
 * and synced; the gate arm angle is animated client-side from -60 (raised) to
 * 0 (lowered), after a short delay when closing.
 */
public class CrossingGateBlockEntity extends BlockEntity {
	private static final float RAISED = -60.0F;
	private static final float LOWERED = 0.0F;
	private static final int CLOSE_DELAY_TICKS = 80;

	/** Installed by the client entrypoint to start/stop the motor sound. */
	public static java.util.function.Consumer<CrossingGateBlockEntity> SOUND_HOOK = be -> {};

	private boolean closed;
	private float gateAngle = RAISED;
	private int closeDelay;
	/** Client-only handle to the active looping motor sound. */
	public Object clientSound;

	public CrossingGateBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.CROSSING_GATE, pos, state);
	}

	public float getGateAngle() {
		return gateAngle;
	}

	/** True while the gate arm is actively raising or lowering. */
	public boolean isMoving() {
		if (closed) {
			return closeDelay >= CLOSE_DELAY_TICKS && gateAngle < LOWERED;
		}
		return gateAngle > RAISED;
	}

	public void setClosed(boolean closed) {
		if (closed == this.closed) {
			return;
		}
		this.closed = closed;
		markDirty();
		if (world != null && !world.isClient) {
			world.updateListeners(pos, getCachedState(), getCachedState(), Block.NOTIFY_LISTENERS);
		}
	}

	public static void clientTick(World world, BlockPos pos, BlockState state, CrossingGateBlockEntity be) {
		if (be.closed) {
			if (be.closeDelay < CLOSE_DELAY_TICKS) {
				be.closeDelay++;
				return;
			}
			if (be.gateAngle < LOWERED) {
				be.gateAngle = Math.min(LOWERED, be.gateAngle + 0.5F);
			}
		} else {
			be.closeDelay = 0;
			if (be.gateAngle > RAISED) {
				be.gateAngle = Math.max(RAISED, be.gateAngle - 0.5F);
			}
		}
		SOUND_HOOK.accept(be);
	}

	@Override
	protected void writeNbt(NbtCompound nbt) {
		super.writeNbt(nbt);
		nbt.putBoolean("closed", closed);
	}

	@Override
	public void readNbt(NbtCompound nbt) {
		super.readNbt(nbt);
		closed = nbt.getBoolean("closed");
	}

	@Override
	public NbtCompound toInitialChunkDataNbt() {
		return createNbt();
	}

	@Override
	public Packet<ClientPlayPacketListener> toUpdatePacket() {
		return BlockEntityUpdateS2CPacket.create(this);
	}
}
