package com.clussmanproductions.trafficcontrol.client.render;

import com.clussmanproductions.trafficcontrol.block.WigWagBlock;
import com.clussmanproductions.trafficcontrol.block.entity.WigWagBlockEntity;

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
 * Draws a lower-quadrant wig wag: the static bracket (its block model) plus the
 * swinging suspended pole, banner backing and lamp, ported box-for-box from the
 * original TESR.
 */
public class WigWagBlockEntityRenderer implements BlockEntityRenderer<WigWagBlockEntity> {
	private static final RenderLayer METAL = RenderLayer.getEntityCutout(
		new Identifier("trafficcontrol", "textures/block/generic.png"));
	private static final RenderLayer BANNER = RenderLayer.getEntityCutout(
		new Identifier("trafficcontrol", "textures/block/wigwag.png"));
	private static final RenderLayer LAMP_ON = RenderLayer.getEntityCutout(
		new Identifier("trafficcontrol", "textures/block/red.png"));
	private static final RenderLayer LAMP_OFF = RenderLayer.getEntityCutout(
		new Identifier("trafficcontrol", "textures/block/lamp_off.png"));

	private static final float[][] POLE = {
		TcBoxRenderer.uv(0, 0, 1, 6), TcBoxRenderer.uv(0, 0, 1, 1),
		TcBoxRenderer.uv(0, 0, 1, 6), TcBoxRenderer.uv(0, 0, 1, 1),
		TcBoxRenderer.uv(0, 0, 1, 6), TcBoxRenderer.uv(0, 0, 1, 6),
	};
	private static final float[][] BACKING = {
		TcBoxRenderer.uv(0, 0, 16, 16), TcBoxRenderer.uv(0, 0, 0, 0),
		TcBoxRenderer.uv(0, 0, 16, 16), TcBoxRenderer.uv(0, 0, 0, 0),
		TcBoxRenderer.uv(0, 0, 0, 16), TcBoxRenderer.uv(0, 0, 0, 0),
	};
	private static final float[][] LAMP = {
		TcBoxRenderer.uv(0, 0, 6, 6), TcBoxRenderer.uv(0, 0, 6, 1),
		TcBoxRenderer.uv(0, 0, 6, 6), TcBoxRenderer.uv(0, 0, 6, 1),
		TcBoxRenderer.uv(0, 0, 1, 6), TcBoxRenderer.uv(0, 0, 1, 6),
	};

	public WigWagBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {
	}

	@Override
	public void render(WigWagBlockEntity be, float tickDelta, MatrixStack matrices,
			VertexConsumerProvider vc, int light, int overlay) {
		BlockState state = be.getCachedState();
		if (!(state.getBlock() instanceof WigWagBlock)) {
			return;
		}
		int rotation = state.get(WigWagBlock.ROTATION);

		matrices.push();
		matrices.translate(0.5, 0.0, 0.5);
		matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(rotation * -22.5F));
		matrices.translate(-0.5, 0.0, -0.5);

		BlockRenderManager brm = MinecraftClient.getInstance().getBlockRenderManager();
		brm.getModelRenderer().render(matrices.peek(),
			vc.getBuffer(RenderLayer.getCutout()), state, brm.getModel(state),
			1.0F, 1.0F, 1.0F, light, overlay);

		// Swinging assembly.
		matrices.translate(-3.5 / 16.0, 16.5 / 16.0, 7.5 / 16.0);
		matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(be.getSwingAngle()));
		matrices.translate(3.5 / 16.0, -16.5 / 16.0, -7.5 / 16.0);

		RenderLayer lampLayer = be.isActive() ? LAMP_ON : LAMP_OFF;
		int lampLight = be.isActive() ? 0xF000F0 : light;

		matrices.translate(-3.5 / 16.0, 10.5 / 16.0, 7.5 / 16.0);
		TcBoxRenderer.box(matrices, vc, METAL, light, overlay, 0, 0, 1, 1, 6.5, -1, POLE);

		matrices.translate(-2.5 / 16.0, -6.0 / 16.0, 0.0);
		TcBoxRenderer.box(matrices, vc, BANNER, light, overlay, 0, 0, 1, 6, 6, -1, BACKING);

		matrices.translate(1.7 / 16.0, 1.7 / 16.0, 0.7 / 16.0);
		TcBoxRenderer.box(matrices, vc, lampLayer, lampLight, overlay, 0, 0, 1, 2.5, 2.5, -2.5, LAMP);

		matrices.pop();
	}
}
