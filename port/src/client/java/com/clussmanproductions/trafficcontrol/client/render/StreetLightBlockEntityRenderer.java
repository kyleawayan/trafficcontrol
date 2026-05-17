package com.clussmanproductions.trafficcontrol.client.render;

import com.clussmanproductions.trafficcontrol.block.StreetLightBlock;
import com.clussmanproductions.trafficcontrol.block.entity.StreetLightBlockEntity;

import net.minecraft.block.BlockState;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;

/**
 * Draws single and double street lights — a multi-block-tall post, lamp arm(s)
 * and lamp(s) — ported box-for-box from the original mod's TESR.
 */
public class StreetLightBlockEntityRenderer implements BlockEntityRenderer<StreetLightBlockEntity> {
	private static final RenderLayer METAL = RenderLayer.getEntityCutout(
		new Identifier("trafficcontrol", "textures/block/generic.png"));
	private static final RenderLayer YELLOW = RenderLayer.getEntityCutout(
		new Identifier("trafficcontrol", "textures/block/yellow.png"));
	private static final int LAMP_LIGHT = 0xF000F0;

	private static final float[][] POST_THICK = {
		TcBoxRenderer.uv(0, 0, 4, 16), TcBoxRenderer.uv(0, 0, 4, 4),
		TcBoxRenderer.uv(0, 0, 4, 16), TcBoxRenderer.uv(0, 0, 4, 4),
		TcBoxRenderer.uv(0, 0, 4, 16), TcBoxRenderer.uv(0, 0, 4, 16),
	};
	private static final float[][] POST_THIN = {
		TcBoxRenderer.uv(0, 0, 2, 16), TcBoxRenderer.uv(0, 0, 2, 2),
		TcBoxRenderer.uv(0, 0, 2, 16), TcBoxRenderer.uv(0, 0, 2, 2),
		TcBoxRenderer.uv(0, 0, 2, 16), TcBoxRenderer.uv(0, 0, 2, 16),
	};
	private static final float[][] ARM = {
		TcBoxRenderer.uv(0, 0, 2, 2), TcBoxRenderer.uv(0, 0, 16, 2),
		TcBoxRenderer.uv(0, 0, 2, 2), TcBoxRenderer.uv(0, 0, 16, 2),
		TcBoxRenderer.uv(0, 0, 16, 2), TcBoxRenderer.uv(0, 0, 16, 2),
	};
	private static final float[][] LAMP = {
		TcBoxRenderer.uv(0, 0, 2, 1), TcBoxRenderer.uv(0, 0, 2, 13),
		TcBoxRenderer.uv(0, 0, 2, 1), TcBoxRenderer.uv(0, 0, 2, 13),
		TcBoxRenderer.uv(0, 0, 2, 13), TcBoxRenderer.uv(0, 0, 2, 13),
	};

	public StreetLightBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {
	}

	@Override
	public void render(StreetLightBlockEntity be, float tickDelta, MatrixStack matrices,
			VertexConsumerProvider vc, int light, int overlay) {
		BlockState state = be.getCachedState();
		if (!(state.getBlock() instanceof StreetLightBlock block)) {
			return;
		}
		boolean dbl = block.isDoubleSided();
		int rotation = state.get(StreetLightBlock.ROTATION);

		matrices.push();
		matrices.translate(0.5, 0.5, 0.5);
		matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(rotation * -22.5F));
		matrices.translate(-0.5, -0.5, -0.5);

		// Post.
		TcBoxRenderer.boxFixed(matrices, vc, METAL, light, overlay, 6, 0, 6, 4, 16, 4, POST_THICK);
		TcBoxRenderer.boxFixed(matrices, vc, METAL, light, overlay, 6, 16, 6, 4, 16, 4, POST_THICK);
		TcBoxRenderer.boxFixed(matrices, vc, METAL, light, overlay, 7, 32, 7, 2, 16, 2, POST_THIN);
		TcBoxRenderer.boxFixed(matrices, vc, METAL, light, overlay, 7, 48, 7, 2, 16, 2, POST_THIN);

		// Arm + lamp (+Z side).
		armAndLamp(matrices, vc, light, overlay, 23.2, 25.2, 38.2, 26.2);
		if (dbl) {
			// Arm + lamp (-Z side).
			armAndLamp(matrices, vc, light, overlay, -23.2, -23.2, -10.2, -22.2);
		}

		// Angled support strut(s).
		matrices.translate(0.4375, 3.75, 0.5625);
		matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-20));
		TcBoxRenderer.boxFixed(matrices, vc, METAL, light, overlay, 0, 0, 0, 2, 2, 16, ARM);
		if (dbl) {
			matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(20));
			matrices.translate(0, 0.34375, -1.0625);
			matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(20));
			TcBoxRenderer.boxFixed(matrices, vc, METAL, light, overlay, 0, 0, 0, 2, 2, 16, ARM);
		}

		matrices.pop();
	}

	private static void armAndLamp(MatrixStack matrices, VertexConsumerProvider vc,
			int light, int overlay, double armZ, double railZ, double endCapZ, double lampZ) {
		TcBoxRenderer.boxFixed(matrices, vc, METAL, light, overlay, 7, 65.35, armZ, 2, 2, 16, ARM);
		TcBoxRenderer.boxFixed(matrices, vc, METAL, light, overlay, 5, 64.35, railZ, 1, 1, 14, ARM);
		TcBoxRenderer.boxFixed(matrices, vc, METAL, light, overlay, 10, 64.35, railZ, 1, 1, 14, ARM);
		TcBoxRenderer.boxFixed(matrices, vc, METAL, light, overlay, 6, 64.35, railZ, 4, 1, 1, ARM);
		TcBoxRenderer.boxFixed(matrices, vc, METAL, light, overlay, 6, 64.35, endCapZ, 4, 1, 1, ARM);
		TcBoxRenderer.boxFixed(matrices, vc, METAL, light, overlay, 6, 65.34, railZ, 4, 0, 14, ARM);
		TcBoxRenderer.boxFixed(matrices, vc, YELLOW, LAMP_LIGHT, overlay, 7, 64.83, lampZ, 2, 0.5, 12, LAMP);
	}
}
