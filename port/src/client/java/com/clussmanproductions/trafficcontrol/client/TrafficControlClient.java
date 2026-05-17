package com.clussmanproductions.trafficcontrol.client;

import net.fabricmc.api.ClientModInitializer;

public class TrafficControlClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ModRenderers.register();
	}
}
