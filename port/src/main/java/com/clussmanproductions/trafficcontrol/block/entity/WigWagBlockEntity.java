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
 * State for a lower-quadrant wig wag. {@code active} is set from redstone on
 * the server and synced; the swing angle is animated client-side.
 */
public class WigWagBlockEntity extends BlockEntity {
	private boolean active;
	private int swingAngle;
	private int swingDirection = 1;

	public WigWagBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.WIG_WAG, pos, state);
	}

	public boolean isActive() {
		return active;
	}

	public int getSwingAngle() {
		return swingAngle;
	}

	public void setActive(boolean active) {
		if (active == this.active) {
			return;
		}
		this.active = active;
		markDirty();
		if (world != null && !world.isClient) {
			world.updateListeners(pos, getCachedState(), getCachedState(), Block.NOTIFY_LISTENERS);
		}
	}

	public static void clientTick(World world, BlockPos pos, BlockState state, WigWagBlockEntity be) {
		if (!be.active && be.swingAngle != 0) {
			if (be.swingAngle < 0) {
				be.swingDirection = 1;
			} else {
				be.swingDirection = -1;
			}
		}
		if (!be.active && be.swingAngle == 0) {
			return;
		}
		if (be.active) {
			if (be.swingAngle > 30) {
				be.swingDirection = -1;
			} else if (be.swingAngle < -30) {
				be.swingDirection = 1;
			}
		}
		be.swingAngle += be.swingDirection * 4;
	}

	@Override
	protected void writeNbt(NbtCompound nbt) {
		super.writeNbt(nbt);
		nbt.putBoolean("active", active);
	}

	@Override
	public void readNbt(NbtCompound nbt) {
		super.readNbt(nbt);
		active = nbt.getBoolean("active");
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
