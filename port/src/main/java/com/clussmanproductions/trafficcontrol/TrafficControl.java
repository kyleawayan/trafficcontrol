package com.clussmanproductions.trafficcontrol;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TrafficControl implements ModInitializer {
	public static final String MOD_ID = "trafficcontrol";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	/**
	 * Whether Minecraft Transit Railway is installed. MTR is a soft dependency:
	 * shunt train detection is only attempted when this is true, which keeps the
	 * JVM from ever linking MTR classes when MTR is absent.
	 */
	public static final boolean MTR_LOADED = FabricLoader.getInstance().isModLoaded("mtr");

	@Override
	public void onInitialize() {
		ModSounds.register();
		ModBlocks.register();
		ModItems.register();
		ModBlockEntities.register();
		ModScreens.register();
		ModNetworking.register();
		ModItemGroups.register();
		LOGGER.info("Traffic Control initialized" + (MTR_LOADED ? " (MTR integration active)" : ""));
	}
}
