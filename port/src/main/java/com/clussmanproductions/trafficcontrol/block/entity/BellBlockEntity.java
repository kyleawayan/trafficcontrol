package com.clussmanproductions.trafficcontrol.block.entity;

import java.util.function.Consumer;

import com.clussmanproductions.trafficcontrol.ModBlockEntities;
import com.clussmanproductions.trafficcontrol.block.BellBlock;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * State for a crossing bell. {@code ringing} is set from redstone on the server
 * and synced. The looping ring sound is managed client-side by
 * {@link #SOUND_HOOK}, which the client entrypoint installs.
 */
public class BellBlockEntity extends BlockEntity {
	/** Installed by the client entrypoint to start/stop the looping ring. */
	public static Consumer<BellBlockEntity> SOUND_HOOK = be -> {};

	private boolean ringing;
	/** Client-only handle to the active looping sound instance. */
	public Object clientSound;

	public BellBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.BELL, pos, state);
	}

	public boolean isRinging() {
		return ringing;
	}

	public SoundEvent getSound() {
		return getCachedState().getBlock() instanceof BellBlock bell ? bell.getSound() : null;
	}

	public void setRinging(boolean ringing) {
		if (ringing == this.ringing) {
			return;
		}
		this.ringing = ringing;
		markDirty();
		if (world != null && !world.isClient) {
			world.updateListeners(pos, getCachedState(), getCachedState(), Block.NOTIFY_LISTENERS);
		}
	}

	public static void clientTick(World world, BlockPos pos, BlockState state, BellBlockEntity be) {
		SOUND_HOOK.accept(be);
	}

	@Override
	protected void writeNbt(NbtCompound nbt) {
		super.writeNbt(nbt);
		nbt.putBoolean("ringing", ringing);
	}

	@Override
	public void readNbt(NbtCompound nbt) {
		super.readNbt(nbt);
		ringing = nbt.getBoolean("ringing");
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
