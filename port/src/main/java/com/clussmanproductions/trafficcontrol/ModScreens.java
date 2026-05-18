package com.clussmanproductions.trafficcontrol;

import com.clussmanproductions.trafficcontrol.block.entity.CrossingGateBlockEntity;
import com.clussmanproductions.trafficcontrol.screen.CrossingGateScreenHandler;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;

/**
 * Registers the crossing gate config screen handler and the packet that
 * carries the edited settings from the client back to the server.
 */
public final class ModScreens {
	private ModScreens() {}

	/** Client to server: apply edited crossing gate settings. */
	public static final Identifier CROSSING_GATE_CONFIG =
		new Identifier(TrafficControl.MOD_ID, "crossing_gate_config");

	public static ScreenHandlerType<CrossingGateScreenHandler> CROSSING_GATE;

	public static void register() {
		CROSSING_GATE = Registry.register(Registries.SCREEN_HANDLER,
			new Identifier(TrafficControl.MOD_ID, "crossing_gate"),
			new ExtendedScreenHandlerType<>(CrossingGateScreenHandler::new));

		ServerPlayNetworking.registerGlobalReceiver(CROSSING_GATE_CONFIG,
			(server, player, handler, buf, sender) -> {
				var pos = buf.readBlockPos();
				int gateLength = buf.readVarInt();
				int closeDelayTicks = buf.readVarInt();
				server.execute(() -> {
					if (player.getWorld().getBlockEntity(pos) instanceof CrossingGateBlockEntity gate
							&& player.squaredDistanceTo(pos.getX() + 0.5, pos.getY() + 0.5,
								pos.getZ() + 0.5) <= 64.0) {
						gate.applyConfig(gateLength, closeDelayTicks);
					}
				});
			});
	}
}
