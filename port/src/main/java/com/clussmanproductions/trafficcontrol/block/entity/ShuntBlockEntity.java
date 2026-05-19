package com.clussmanproductions.trafficcontrol.block.entity;

import com.clussmanproductions.trafficcontrol.ModBlockEntities;
import com.clussmanproductions.trafficcontrol.TrafficControl;
import com.clussmanproductions.trafficcontrol.compat.MtrTrainDetector;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Block entity for a train shunt. Train detection runs client-side (MTR keeps
 * its live train list on the client only — see {@link MtrTrainDetector}); the
 * client notifies the server with a shunt-detection packet whenever the result
 * changes, and periodically re-sends a positive result so the server's
 * {@link #DETECTION_TIMEOUT} does not expire while a train sits on the shunt.
 * The actual packet send is deferred to {@link #PACKET_SENDER}, installed by
 * the client entrypoint, so this class stays free of client-only networking
 * API. The server-side {@code trainDetected} flag is read by linked relays to
 * power the crossing.
 */
public class ShuntBlockEntity extends BlockEntity {
	/** How far (in blocks) an MTR train is detected from the shunt centre. */
	public static final double DETECTION_RANGE = 16.0;
	/** Server clears a stale detection if no packet arrives within this many ticks. */
	private static final int DETECTION_TIMEOUT = 40;
	/** Client re-sends a standing positive result at least this often (ticks). */
	private static final int CLIENT_REFRESH_INTERVAL = 20;

	/** Sends a client-to-server shunt-detection packet; installed by the client entrypoint. */
	@FunctionalInterface
	public interface DetectionSender {
		void send(BlockPos pos, boolean detected);
	}

	/** No-op on the server; the client entrypoint installs the real sender. */
	public static DetectionSender PACKET_SENDER = (pos, detected) -> {};

	private boolean trainDetected;
	private int ticksSinceUpdate;

	// Client-only detection bookkeeping.
	private boolean lastSentDetected;
	private int ticksSinceSent;

	public ShuntBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.SHUNT, pos, state);
	}

	public boolean isTrainDetected() {
		return trainDetected;
	}

	/** Applies a detection result received from a client. */
	public void onDetectionPacket(boolean detected) {
		ticksSinceUpdate = 0;
		if (trainDetected != detected) {
			trainDetected = detected;
			markDirty();
		}
	}

	public static void serverTick(World world, BlockPos pos, BlockState state, ShuntBlockEntity be) {
		if (be.trainDetected && ++be.ticksSinceUpdate > DETECTION_TIMEOUT) {
			be.trainDetected = false;
			be.ticksSinceUpdate = 0;
			be.markDirty();
		}
	}

	public static void clientTick(World world, BlockPos pos, BlockState state, ShuntBlockEntity be) {
		if (!TrafficControl.MTR_LOADED) {
			return;
		}
		boolean detected = MtrTrainDetector.isTrainNear(pos, DETECTION_RANGE);
		be.ticksSinceSent++;
		// Send on every change, and refresh a standing positive result so the
		// server's detection timeout does not lapse while a train is parked.
		if (detected != be.lastSentDetected
				|| (detected && be.ticksSinceSent >= CLIENT_REFRESH_INTERVAL)) {
			be.lastSentDetected = detected;
			be.ticksSinceSent = 0;
			PACKET_SENDER.send(pos, detected);
		}
	}

	@Override
	protected void writeNbt(NbtCompound nbt) {
		super.writeNbt(nbt);
		nbt.putBoolean("trainDetected", trainDetected);
	}

	@Override
	public void readNbt(NbtCompound nbt) {
		super.readNbt(nbt);
		trainDetected = nbt.getBoolean("trainDetected");
	}
}
