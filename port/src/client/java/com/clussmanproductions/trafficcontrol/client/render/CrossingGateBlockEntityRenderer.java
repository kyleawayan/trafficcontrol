package com.clussmanproductions.trafficcontrol.client.render;

import com.clussmanproductions.trafficcontrol.block.CrossingGateBlock;
import com.clussmanproductions.trafficcontrol.block.entity.CrossingGateBlockEntity;

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
 * Draws a crossing gate: the static housing (its block model) plus the
 * counterweight assembly and gate arm, ported box-for-box from the original
 * TESR. The arm and counterweight rotate with the animated gate angle.
 */
public class CrossingGateBlockEntityRenderer implements BlockEntityRenderer<CrossingGateBlockEntity> {
	private static final RenderLayer METAL = RenderLayer.getEntityCutout(
		new Identifier("trafficcontrol", "textures/block/generic.png"));
	private static final RenderLayer GATE = RenderLayer.getEntityCutout(
		new Identifier("trafficcontrol", "textures/block/gate.png"));

	private static final float[][] CROSS = {
		TcBoxRenderer.uv(0, 0, 1, 1), TcBoxRenderer.uv(0, 0, 8, 1),
		TcBoxRenderer.uv(0, 0, 1, 1), TcBoxRenderer.uv(0, 0, 8, 1),
		TcBoxRenderer.uv(0, 0, 8, 1), TcBoxRenderer.uv(0, 0, 8, 1),
	};
	private static final float[][] ARM_SUPPORT = {
		TcBoxRenderer.uv(3, 4, 8, 5), TcBoxRenderer.uv(7, 4, 8, 5),
		TcBoxRenderer.uv(3, 3, 8, 4), TcBoxRenderer.uv(4, 4, 5, 5),
		TcBoxRenderer.uv(4, 4, 5, 9), TcBoxRenderer.uv(4, 4, 5, 9),
	};
	private static final float[][] CONNECTOR = {
		TcBoxRenderer.uv(5, 9, 12, 12), TcBoxRenderer.uv(1, 7, 8, 8),
		TcBoxRenderer.uv(6, 10, 13, 13), TcBoxRenderer.uv(2, 7, 9, 8),
		TcBoxRenderer.uv(5, 6, 6, 9), TcBoxRenderer.uv(2, 3, 3, 6),
	};
	private static final float[][] GATE_FACES = {
		TcBoxRenderer.uv(0, 0, 16, 0.7F), TcBoxRenderer.uv(0, 2, 16, 2.7F),
		TcBoxRenderer.uv(0, 0, 15, 0.7F), TcBoxRenderer.uv(0, 1, 16, 1.7F),
		TcBoxRenderer.uv(0, 2, 3, 4), TcBoxRenderer.uv(0, 2, 3, 4),
	};

	public CrossingGateBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {
	}

	@Override
	public void render(CrossingGateBlockEntity be, float tickDelta, MatrixStack matrices,
			VertexConsumerProvider vc, int light, int overlay) {
		BlockState state = be.getCachedState();
		if (!(state.getBlock() instanceof CrossingGateBlock)) {
			return;
		}
		int rotation = state.get(CrossingGateBlock.ROTATION);

		matrices.push();
		matrices.translate(0.5, 0.5, 0.5);
		matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-rotation * 22.5F));

		// Static housing model.
		matrices.push();
		matrices.translate(-0.5, -0.5, -0.5);
		BlockRenderManager brm = MinecraftClient.getInstance().getBlockRenderManager();
		brm.getModelRenderer().render(matrices.peek(),
			vc.getBuffer(RenderLayer.getCutout()), state, brm.getModel(state),
			1.0F, 1.0F, 1.0F, light, overlay);
		matrices.pop();

		// Counterweight assembly and gate arm, rotated by the animated angle.
		matrices.translate(3.0 / 16.0, 2.0 / 16.0, 0.0);
		matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(be.getGateAngle()));

		// Rotator cross.
		TcBoxRenderer.box(matrices, vc, METAL, light, overlay, -7.5, -9.5, 4, 1, 2, -8, CROSS);
		// Rotator arm supports.
		TcBoxRenderer.box(matrices, vc, METAL, light, overlay, -6.5, -9.5, 4, 7, 2, -1, ARM_SUPPORT);
		TcBoxRenderer.box(matrices, vc, METAL, light, overlay, -6.5, -9.5, -3, 7, 2, -1, ARM_SUPPORT);
		// Rotator connectors.
		TcBoxRenderer.box(matrices, vc, METAL, light, overlay, -2.5, -7.5, 4, 3, 8.5, -1, CONNECTOR);
		TcBoxRenderer.box(matrices, vc, METAL, light, overlay, -2.5, -7.5, -3, 3, 8.5, -1, CONNECTOR);
		// Weight connectors.
		TcBoxRenderer.box(matrices, vc, METAL, light, overlay, 0.5, -2, 4, 3, 3, -1, CONNECTOR);
		TcBoxRenderer.box(matrices, vc, METAL, light, overlay, 0.5, -2, -3, 3, 3, -1, CONNECTOR);
		// Counterweights.
		TcBoxRenderer.box(matrices, vc, METAL, light, overlay, 3.5, -3.5, 4, 10, 6, -1, CONNECTOR);
		TcBoxRenderer.box(matrices, vc, METAL, light, overlay, 3.5, -3.5, -3, 10, 6, -1, CONNECTOR);

		// Gate arm. Its length is configurable through the gate's screen.
		float gateLength = be.getGateLength();
		TcBoxRenderer.box(matrices, vc, GATE, light, overlay,
			-(gateLength * 16) - 13, -9.5, 0.5, (gateLength * 16) + 5.5, 2, -1, GATE_FACES);

		matrices.pop();
	}
}
