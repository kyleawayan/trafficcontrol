package com.clussmanproductions.trafficcontrol.client;

import com.clussmanproductions.trafficcontrol.ModBlockEntities;
import com.clussmanproductions.trafficcontrol.ModBlocks;
import com.clussmanproductions.trafficcontrol.client.render.StreetLightBlockEntityRenderer;

import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.minecraft.block.Block;
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
	}
}
