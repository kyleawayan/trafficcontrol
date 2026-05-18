package com.clussmanproductions.trafficcontrol.screen;

import com.clussmanproductions.trafficcontrol.ModScreens;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.util.math.BlockPos;

/**
 * Slot-less screen handler backing the crossing gate config screen. It only
 * carries the gate's position and current settings to the client; the edited
 * values are sent back with a separate packet (see {@link ModScreens}).
 */
public class CrossingGateScreenHandler extends ScreenHandler {
	public final BlockPos pos;
	public final int gateLength;
	public final int closeDelayTicks;

	public CrossingGateScreenHandler(int syncId, PlayerInventory inventory, PacketByteBuf buf) {
		this(syncId, buf.readBlockPos(), buf.readVarInt(), buf.readVarInt());
	}

	public CrossingGateScreenHandler(int syncId, BlockPos pos, int gateLength, int closeDelayTicks) {
		super(ModScreens.CROSSING_GATE, syncId);
		this.pos = pos;
		this.gateLength = gateLength;
		this.closeDelayTicks = closeDelayTicks;
	}

	@Override
	public ItemStack quickMove(PlayerEntity player, int slot) {
		return ItemStack.EMPTY;
	}

	@Override
	public boolean canUse(PlayerEntity player) {
		return player.squaredDistanceTo(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) <= 64.0;
	}
}
