package com.clussmanproductions.trafficcontrol.client;

import com.clussmanproductions.trafficcontrol.ModBlockEntities;
import com.clussmanproductions.trafficcontrol.ModBlocks;
import com.clussmanproductions.trafficcontrol.ModScreens;
import com.clussmanproductions.trafficcontrol.client.render.CrossingGateBlockEntityRenderer;
import com.clussmanproductions.trafficcontrol.client.render.CrossingLampsBlockEntityRenderer;
import com.clussmanproductions.trafficcontrol.client.render.StreetLightBlockEntityRenderer;
import com.clussmanproductions.trafficcontrol.client.render.WigWagBlockEntityRenderer;
import com.clussmanproductions.trafficcontrol.client.screen.CrossingGateScreen;
import com.clussmanproductions.trafficcontrol.client.sound.TcClientSounds;

import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.minecraft.block.Block;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;

public final class ModRenderers {
	private ModRenderers() {}

	public static void register() {
		// Traffic Control models rely on transparent texture regions; render
		// them on the cutout layer so those regions are not drawn opaque.
		for (Block block : ModBlocks.BLOCKS) {
			BlockRenderLayerMap.INSTANCE.putBlock(block, RenderLayer.getCutout());
		}

		BlockEntityRendererFactories.register(
			ModBlockEntities.STREET_LIGHT, StreetLightBlockEntityRenderer::new);
		BlockEntityRendererFactories.register(
			ModBlockEntities.WIG_WAG, WigWagBlockEntityRenderer::new);
		BlockEntityRendererFactories.register(
			ModBlockEntities.CROSSING_GATE, CrossingGateBlockEntityRenderer::new);
		BlockEntityRendererFactories.register(
			ModBlockEntities.LAMPS, CrossingLampsBlockEntityRenderer::new);

		HandledScreens.register(ModScreens.CROSSING_GATE, CrossingGateScreen::new);

		TcClientSounds.register();
	}
}
