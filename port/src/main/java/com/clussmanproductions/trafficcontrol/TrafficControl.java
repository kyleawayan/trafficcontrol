package com.clussmanproductions.trafficcontrol;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TrafficControl implements ModInitializer {
	public static final String MOD_ID = "trafficcontrol";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		ModSounds.register();
		ModBlocks.register();
		ModItems.register();
		ModBlockEntities.register();
		ModScreens.register();
		ModItemGroups.register();
		LOGGER.info("Traffic Control initialized");
	}
}
