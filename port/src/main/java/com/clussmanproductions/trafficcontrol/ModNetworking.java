package com.clussmanproductions.trafficcontrol;

import com.clussmanproductions.trafficcontrol.block.entity.ShuntBlockEntity;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.util.Identifier;

/**
 * Registers the mod's custom packets. The shunt-detection packet carries a
 * client-detected MTR train result back to the server (see
 * {@link ShuntBlockEntity} for why detection is client-side).
 */
public final class ModNetworking {
	private ModNetworking() {}

	/** Client to server: a shunt's MTR train detection result changed. */
	public static final Identifier SHUNT_DETECTION =
		new Identifier(TrafficControl.MOD_ID, "shunt_detection");

	public static void register() {
		ServerPlayNetworking.registerGlobalReceiver(SHUNT_DETECTION,
			(server, player, handler, buf, sender) -> {
				var pos = buf.readBlockPos();
				boolean detected = buf.readBoolean();
				server.execute(() -> {
					if (player.getWorld().getBlockEntity(pos) instanceof ShuntBlockEntity shunt) {
						shunt.onDetectionPacket(detected);
					}
				});
			});
	}
}
