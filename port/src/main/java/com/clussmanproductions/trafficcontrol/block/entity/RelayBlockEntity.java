package com.clussmanproductions.trafficcontrol.block.entity;

import java.util.ArrayList;
import java.util.List;

import com.clussmanproductions.trafficcontrol.ModBlockEntities;
import com.clussmanproductions.trafficcontrol.block.BellBlock;
import com.clussmanproductions.trafficcontrol.block.CrossingGateBlock;
import com.clussmanproductions.trafficcontrol.block.CrossingLampsBlock;
import com.clussmanproductions.trafficcontrol.block.ShuntBlock;
import com.clussmanproductions.trafficcontrol.block.WigWagBlock;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Stores the components linked to a crossing relay and re-asserts their state.
 * The relay is powered when it receives redstone power <em>or</em> when any
 * linked train shunt detects an MTR train; while powered, a one-second
 * heartbeat keeps every linked gate closed, bell ringing, lamp flashing and
 * wig wag swinging; when power drops they are all reset. Links are added and
 * removed with the tuner.
 *
 * <p>Gates, lamps, bells and wig wags are driven <em>outputs</em>; shunts are
 * trigger <em>inputs</em> — they are polled, never driven.
 */
public class RelayBlockEntity extends BlockEntity {
	private static final int HEARTBEAT_TICKS = 20;

	private boolean powered;
	private boolean redstonePowered;
	private int heartbeat;
	private final List<BlockPos> gates = new ArrayList<>();
	private final List<BlockPos> lamps = new ArrayList<>();
	private final List<BlockPos> bells = new ArrayList<>();
	private final List<BlockPos> wigWags = new ArrayList<>();
	private final List<BlockPos> shunts = new ArrayList<>();

	public RelayBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.RELAY, pos, state);
	}

	public boolean isPowered() {
		return powered;
	}

	/** Sets whether the relay block is currently receiving redstone power. */
	public void setRedstonePowered(boolean redstonePowered) {
		if (redstonePowered == this.redstonePowered) {
			return;
		}
		this.redstonePowered = redstonePowered;
		markDirty();
		recomputePower();
	}

	/**
	 * Recomputes the relay's effective power from redstone and linked shunts,
	 * driving the linked components when it changes.
	 */
	private void recomputePower() {
		boolean effective = redstonePowered || anyShuntTriggered();
		if (effective == powered) {
			return;
		}
		powered = effective;
		markDirty();
		driveAll();
	}

	/** True if any linked shunt currently detects a train; prunes dead links. */
	private boolean anyShuntTriggered() {
		if (world == null) {
			return false;
		}
		boolean triggered = false;
		shunts.removeIf(p -> !(world.getBlockState(p).getBlock() instanceof ShuntBlock));
		for (BlockPos p : shunts) {
			if (world.getBlockEntity(p) instanceof ShuntBlockEntity shunt && shunt.isTrainDetected()) {
				triggered = true;
			}
		}
		return triggered;
	}

	public static void serverTick(World world, BlockPos pos, BlockState state, RelayBlockEntity be) {
		// Poll linked shunts every tick so the crossing reacts promptly.
		be.recomputePower();
		if (++be.heartbeat < HEARTBEAT_TICKS) {
			return;
		}
		be.heartbeat = 0;
		be.driveAll();
	}

	/**
	 * Links or unlinks the component at {@code target}. Returns a human-readable
	 * result for the tuner to relay, or {@code null} if the target is not a
	 * linkable crossing component.
	 */
	public String toggleLink(BlockPos target) {
		if (world == null) {
			return null;
		}
		Block block = world.getBlockState(target).getBlock();
		List<BlockPos> list;
		String label;
		if (block instanceof CrossingGateBlock) {
			list = gates;
			label = "crossing gate";
		} else if (block instanceof CrossingLampsBlock) {
			list = lamps;
			label = "crossing lamps";
		} else if (block instanceof BellBlock) {
			list = bells;
			label = "bell";
		} else if (block instanceof WigWagBlock) {
			list = wigWags;
			label = "wig wag";
		} else if (block instanceof ShuntBlock) {
			list = shunts;
			label = "train shunt";
		} else {
			return null;
		}

		boolean linked;
		if (list.remove(target)) {
			linked = false;
		} else {
			list.add(target.toImmutable());
			linked = true;
		}
		markDirty();
		driveAll();
		recomputePower();
		return (linked ? "Linked " : "Unlinked ") + label
			+ (linked ? " to" : " from") + " relay";
	}

	private void driveAll() {
		if (world == null || world.isClient) {
			return;
		}
		gates.removeIf(p -> {
			if (world.getBlockEntity(p) instanceof CrossingGateBlockEntity gate) {
				gate.setClosed(powered);
				return false;
			}
			return !(world.getBlockState(p).getBlock() instanceof CrossingGateBlock);
		});
		lamps.removeIf(p -> {
			if (world.getBlockState(p).getBlock() instanceof CrossingLampsBlock) {
				CrossingLampsBlock.setLit(world, p, powered);
				return false;
			}
			return true;
		});
		bells.removeIf(p -> {
			if (world.getBlockEntity(p) instanceof BellBlockEntity bell) {
				bell.setRinging(powered);
				return false;
			}
			return !(world.getBlockState(p).getBlock() instanceof BellBlock);
		});
		wigWags.removeIf(p -> {
			if (world.getBlockEntity(p) instanceof WigWagBlockEntity wigWag) {
				wigWag.setActive(powered);
				return false;
			}
			return !(world.getBlockState(p).getBlock() instanceof WigWagBlock);
		});
	}

	private static int[] toArray(List<BlockPos> list) {
		int[] data = new int[list.size() * 3];
		for (int i = 0; i < list.size(); i++) {
			BlockPos p = list.get(i);
			data[i * 3] = p.getX();
			data[i * 3 + 1] = p.getY();
			data[i * 3 + 2] = p.getZ();
		}
		return data;
	}

	private static void fromArray(int[] data, List<BlockPos> list) {
		list.clear();
		for (int i = 0; i + 2 < data.length; i += 3) {
			list.add(new BlockPos(data[i], data[i + 1], data[i + 2]));
		}
	}

	@Override
	protected void writeNbt(NbtCompound nbt) {
		super.writeNbt(nbt);
		nbt.putBoolean("powered", powered);
		nbt.putBoolean("redstonePowered", redstonePowered);
		nbt.putIntArray("gates", toArray(gates));
		nbt.putIntArray("lamps", toArray(lamps));
		nbt.putIntArray("bells", toArray(bells));
		nbt.putIntArray("wigwags", toArray(wigWags));
		nbt.putIntArray("shunts", toArray(shunts));
	}

	@Override
	public void readNbt(NbtCompound nbt) {
		super.readNbt(nbt);
		powered = nbt.getBoolean("powered");
		redstonePowered = nbt.getBoolean("redstonePowered");
		fromArray(nbt.getIntArray("gates"), gates);
		fromArray(nbt.getIntArray("lamps"), lamps);
		fromArray(nbt.getIntArray("bells"), bells);
		fromArray(nbt.getIntArray("wigwags"), wigWags);
		fromArray(nbt.getIntArray("shunts"), shunts);
	}
}
