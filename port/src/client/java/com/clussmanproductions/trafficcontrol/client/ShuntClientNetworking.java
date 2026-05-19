package com.clussmanproductions.trafficcontrol.client;

import com.clussmanproductions.trafficcontrol.ModNetworking;
import com.clussmanproductions.trafficcontrol.block.entity.ShuntBlockEntity;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.PacketByteBuf;

/**
 * Installs the client-side shunt-detection packet sender. {@link ShuntBlockEntity}
 * lives in the main source set and cannot reference the client-only
 * {@link ClientPlayNetworking} API directly, so the actual send is supplied
 * here as a hook.
 */
public final class ShuntClientNetworking {
	private ShuntClientNetworking() {}

	public static void register() {
		ShuntBlockEntity.PACKET_SENDER = (pos, detected) -> {
			PacketByteBuf buf = PacketByteBufs.create();
			buf.writeBlockPos(pos);
			buf.writeBoolean(detected);
			ClientPlayNetworking.send(ModNetworking.SHUNT_DETECTION, buf);
		};
	}
}
