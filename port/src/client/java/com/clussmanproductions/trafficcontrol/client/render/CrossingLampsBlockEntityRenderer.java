package com.clussmanproductions.trafficcontrol.client.render;

import com.clussmanproductions.trafficcontrol.block.CrossingLampsBlock;
import com.clussmanproductions.trafficcontrol.block.entity.CrossingLampsBlockEntity;

import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;

/**
 * Draws a pair of crossing lamps: the static housing (its block model) plus the
 * four flashing bulb panels overlaid on the housing's lamp recesses. While lit,
 * the left and right bulb pairs alternate every flash cycle; the phase is taken
 * from world time so every crossing lamp in the world flashes in unison.
 */
public class CrossingLampsBlockEntityRenderer implements BlockEntityRenderer<CrossingLampsBlockEntity> {
	private static final RenderLayer LAMP_ON = RenderLayer.getEntityCutout(
		new Identifier("trafficcontrol", "textures/block/red.png"));
	private static final RenderLayer LAMP_OFF = RenderLayer.getEntityCutout(
		new Identifier("trafficcontrol", "textures/block/lamp_off.png"));

	/** Ticks per flash phase; matches the original mod's 20-tick cadence. */
	private static final int FLASH_TICKS = 20;

	private static final float[][] BULB = {
		TcBoxRenderer.uv(0, 0, 16, 16), TcBoxRenderer.uv(0, 0, 16, 16),
		TcBoxRenderer.uv(0, 0, 16, 16), TcBoxRenderer.uv(0, 0, 16, 16),
		TcBoxRenderer.uv(0, 0, 16, 16), TcBoxRenderer.uv(0, 0, 16, 16),
	};

	public CrossingLampsBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {
	}

	@Override
	public void render(CrossingLampsBlockEntity be, float tickDelta, MatrixStack matrices,
			VertexConsumerProvider vc, int light, int overlay) {
		BlockState state = be.getCachedState();
		if (!(state.getBlock() instanceof CrossingLampsBlock)) {
			return;
		}
		int rotation = state.get(CrossingLampsBlock.ROTATION);
		boolean lit = state.get(CrossingLampsBlock.LIT);

		matrices.push();
		matrices.translate(0.5, 0.5, 0.5);
		// The lamp model faces +Z; the original renderer adds 180 so the bulbs
		// point back toward the placing player.
		matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-rotation * 22.5F + 180.0F));
		matrices.translate(-0.5, -0.5, -0.5);

		// Static housing model.
		BlockRenderManager brm = MinecraftClient.getInstance().getBlockRenderManager();
		brm.getModelRenderer().render(matrices.peek(),
			vc.getBuffer(RenderLayer.getCutout()), state, brm.getModel(state),
			1.0F, 1.0F, 1.0F, light, overlay);

		if (((CrossingLampsBlock) state.getBlock()).isOverhead()) {
			// Overhead lamps: one lens per side, steady red while lit.
			drawBulb(matrices, vc, lit, light, overlay, 9.9, 6.0, 12.0);
			drawBulb(matrices, vc, lit, light, overlay, 9.9, 6.0, 4.7);
		} else {
			// Gate-mounted lamps: left and right pairs alternate.
			boolean leftLit = false;
			boolean rightLit = false;
			if (lit && be.getWorld() != null) {
				boolean phase = (be.getWorld().getTime() / FLASH_TICKS) % 2 == 0;
				leftLit = phase;
				rightLit = !phase;
			}
			drawBulb(matrices, vc, leftLit, light, overlay, 0.9, 10.0, 16.0);
			drawBulb(matrices, vc, leftLit, light, overlay, 0.9, 10.0, -2.3);
			drawBulb(matrices, vc, rightLit, light, overlay, 9.9, 10.0, 16.0);
			drawBulb(matrices, vc, rightLit, light, overlay, 9.9, 10.0, -2.3);
		}

		matrices.pop();
	}

	private static void drawBulb(MatrixStack matrices, VertexConsumerProvider vc, boolean on,
			int light, int overlay, double x, double y, double z) {
		RenderLayer layer = on ? LAMP_ON : LAMP_OFF;
		int bulbLight = on ? 0xF000F0 : light;
		TcBoxRenderer.box(matrices, vc, layer, bulbLight, overlay, x, y, z, 5.2, 5.0, 1.3, BULB);
	}
}
