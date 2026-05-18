package com.clussmanproductions.trafficcontrol.item;

import com.clussmanproductions.trafficcontrol.block.entity.RelayBlockEntity;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Links crossing components to a relay. Right-click a relay to select it, then
 * right-click a crossing gate, lamps, bell or wig wag to link or unlink it.
 * Right-clicking the selected relay again clears the selection.
 */
public class TunerItem extends Item {
	private static final String SELECTED_RELAY = "relay";

	public TunerItem(Settings settings) {
		super(settings);
	}

	@Override
	public ActionResult useOnBlock(ItemUsageContext ctx) {
		World world = ctx.getWorld();
		PlayerEntity player = ctx.getPlayer();
		if (world.isClient || player == null) {
			return ActionResult.SUCCESS;
		}

		BlockPos pos = ctx.getBlockPos();
		ItemStack stack = ctx.getStack();
		NbtCompound nbt = stack.getOrCreateNbt();

		if (world.getBlockEntity(pos) instanceof RelayBlockEntity) {
			if (nbt.contains(SELECTED_RELAY) && readPos(nbt).equals(pos)) {
				nbt.remove(SELECTED_RELAY);
				message(player, "Cleared relay selection");
			} else {
				nbt.putIntArray(SELECTED_RELAY, new int[] {pos.getX(), pos.getY(), pos.getZ()});
				message(player, "Selected relay at " + pos.getX() + ", " + pos.getY()
					+ ", " + pos.getZ() + " - right-click components to link them");
			}
			return ActionResult.SUCCESS;
		}

		if (!nbt.contains(SELECTED_RELAY)) {
			message(player, "Select a relay first by right-clicking it");
			return ActionResult.SUCCESS;
		}

		BlockPos relayPos = readPos(nbt);
		if (world.getBlockEntity(relayPos) instanceof RelayBlockEntity relay) {
			String result = relay.toggleLink(pos);
			message(player, result != null ? result : "That block cannot be linked to a relay");
		} else {
			nbt.remove(SELECTED_RELAY);
			message(player, "The selected relay no longer exists - selection cleared");
		}
		return ActionResult.SUCCESS;
	}

	private static BlockPos readPos(NbtCompound nbt) {
		int[] p = nbt.getIntArray(SELECTED_RELAY);
		return p.length == 3 ? new BlockPos(p[0], p[1], p[2]) : BlockPos.ORIGIN;
	}

	private static void message(PlayerEntity player, String text) {
		player.sendMessage(Text.literal(text), false);
	}
}
