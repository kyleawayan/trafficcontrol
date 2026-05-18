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
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

/**
 * State for a crossing gate. {@code closed} is set from redstone (or a relay)
 * on the server and synced; the gate arm angle is animated client-side from
 * -60 (raised) to 0 (lowered), after a configurable delay when closing. The
 * arm length and close delay are adjustable through the gate's config screen.
 */
public class CrossingGateBlockEntity extends BlockEntity {
	private static final float RAISED = -60.0F;
	private static final float LOWERED = 0.0F;

	public static final int DEFAULT_GATE_LENGTH = 4;
	public static final int DEFAULT_CLOSE_DELAY = 15;
	public static final int MIN_GATE_LENGTH = 1;
	public static final int MAX_GATE_LENGTH = 16;
	public static final int MIN_CLOSE_DELAY = 0;
	public static final int MAX_CLOSE_DELAY = 200;

	/** Installed by the client entrypoint to start/stop the motor sound. */
	public static java.util.function.Consumer<CrossingGateBlockEntity> SOUND_HOOK = be -> {};

	private boolean closed;
	private float gateAngle = RAISED;
	private int closeDelay;
	private int gateLength = DEFAULT_GATE_LENGTH;
	private int closeDelayTicks = DEFAULT_CLOSE_DELAY;
	/** Client-only handle to the active looping motor sound. */
	public Object clientSound;

	public CrossingGateBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.CROSSING_GATE, pos, state);
	}

	public float getGateAngle() {
		return gateAngle;
	}

	public int getGateLength() {
		return gateLength;
	}

	public int getCloseDelayTicks() {
		return closeDelayTicks;
	}

	/** True while the gate arm is actively raising or lowering. */
	public boolean isMoving() {
		if (closed) {
			return closeDelay >= closeDelayTicks && gateAngle < LOWERED;
		}
		return gateAngle > RAISED;
	}

	public void setClosed(boolean closed) {
		if (closed == this.closed) {
			return;
		}
		this.closed = closed;
		markDirty();
		sync();
	}

	/** Applies config from the gate's screen and re-syncs to clients. */
	public void applyConfig(int gateLength, int closeDelayTicks) {
		this.gateLength = MathHelper.clamp(gateLength, MIN_GATE_LENGTH, MAX_GATE_LENGTH);
		this.closeDelayTicks = MathHelper.clamp(closeDelayTicks, MIN_CLOSE_DELAY, MAX_CLOSE_DELAY);
		markDirty();
		sync();
	}

	private void sync() {
		if (world != null && !world.isClient) {
			world.updateListeners(pos, getCachedState(), getCachedState(), Block.NOTIFY_LISTENERS);
		}
	}

	public static void clientTick(World world, BlockPos pos, BlockState state, CrossingGateBlockEntity be) {
		if (be.closed) {
			if (be.closeDelay < be.closeDelayTicks) {
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
		nbt.putInt("gateLength", gateLength);
		nbt.putInt("closeDelayTicks", closeDelayTicks);
	}

	@Override
	public void readNbt(NbtCompound nbt) {
		super.readNbt(nbt);
		closed = nbt.getBoolean("closed");
		if (nbt.contains("gateLength")) {
			gateLength = MathHelper.clamp(nbt.getInt("gateLength"), MIN_GATE_LENGTH, MAX_GATE_LENGTH);
		}
		if (nbt.contains("closeDelayTicks")) {
			closeDelayTicks = MathHelper.clamp(nbt.getInt("closeDelayTicks"), MIN_CLOSE_DELAY, MAX_CLOSE_DELAY);
		}
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
