package com.clussmanproductions.trafficcontrol.client.render;

import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

/**
 * Draws axis-aligned boxes into a {@code VertexConsumer}, porting the original
 * mod's box helpers. Coordinates and UV rectangles are in model pixels (16 per
 * block). Faces are emitted double-sided so they are visible regardless of the
 * render layer's culling.
 *
 * <p>The original used two vertex orderings: {@code box} matches
 * {@code getVertexPoints} (wig wags, crossing gates) and {@code boxFixed}
 * matches {@code getFixedVertexPoints} (street lights).
 */
public final class TcBoxRenderer {
	private TcBoxRenderer() {}

	/** UV rectangle in pixels: {u0, v0, u1, v1}. */
	public static float[] uv(float u0, float v0, float u1, float v1) {
		return new float[] {u0 / 16f, v0 / 16f, u1 / 16f, v1 / 16f};
	}

	private static final float[][] NORMALS = {
		{0, 0, -1}, {0, 1, 0}, {0, 0, 1}, {0, -1, 0}, {1, 0, 0}, {-1, 0, 0},
	};
	// Face -> index into the south/up/north/down/east/west UV array.
	private static final int[] FACE_TEX = {2, 1, 0, 3, 4, 5};

	public static void box(MatrixStack matrices, VertexConsumer vc, int light, int overlay,
			double x, double y, double z, double w, double h, double d, float[]... faces) {
		draw(matrices, vc, light, overlay, x, y, z, w, h, d, false, faces);
	}

	public static void boxFixed(MatrixStack matrices, VertexConsumer vc, int light, int overlay,
			double x, double y, double z, double w, double h, double d, float[]... faces) {
		draw(matrices, vc, light, overlay, x, y, z, w, h, d, true, faces);
	}

	private static void draw(MatrixStack matrices, VertexConsumer vc, int light, int overlay,
			double x, double y, double z, double w, double h, double d, boolean fixed, float[][] faces) {
		double x0 = x / 16, y0 = y / 16, z0 = z / 16;
		double x1 = x0 + w / 16, y1 = y0 + h / 16, z1 = z0 + d / 16;

		double[][] p = fixed ? new double[][] {
			{x1, y0, z0}, {x0, y0, z0}, {x0, y1, z0}, {x1, y1, z0},          // Front
			{x1, y1, z0}, {x0, y1, z0}, {x0, y1, z1}, {x1, y1, z1},          // Up
			{x0, y0, z1}, {x1, y0, z1}, {x1, y1, z1}, {x0, y1, z1},          // Back
			{x1, y0, z1}, {x0, y0, z1}, {x0, y0, z0}, {x1, y0, z0},          // Down
			{x1, y0, z1}, {x1, y0, z0}, {x1, y1, z0}, {x1, y1, z1},          // Right
			{x0, y0, z0}, {x0, y0, z1}, {x0, y1, z1}, {x0, y1, z0},          // Left
		} : new double[][] {
			{x1, y0, z0}, {x1, y1, z0}, {x0, y1, z0}, {x0, y0, z0},          // Front
			{x1, y1, z0}, {x1, y1, z1}, {x0, y1, z1}, {x0, y1, z0},          // Up
			{x0, y0, z1}, {x0, y1, z1}, {x1, y1, z1}, {x1, y0, z1},          // Back
			{x1, y0, z1}, {x1, y0, z0}, {x0, y0, z0}, {x0, y0, z1},          // Down
			{x1, y0, z1}, {x1, y1, z1}, {x1, y1, z0}, {x1, y0, z0},          // Right
			{x0, y0, z0}, {x0, y1, z0}, {x0, y1, z1}, {x0, y0, z1},          // Left
		};

		Matrix4f pos = matrices.peek().getPositionMatrix();
		Matrix3f norm = matrices.peek().getNormalMatrix();

		for (int face = 0; face < 6; face++) {
			float[] t = faces[FACE_TEX[face]];
			float[] n = NORMALS[face];
			int base = face * 4;
			float[][] uvs = {
				{t[2], t[3]}, {t[2], t[1]}, {t[0], t[1]}, {t[0], t[3]},
			};
			emitQuad(vc, pos, norm, p, base, uvs, light, overlay, n, false);
			emitQuad(vc, pos, norm, p, base, uvs, light, overlay, n, true);
		}
	}

	private static void emitQuad(VertexConsumer vc, Matrix4f pos, Matrix3f norm, double[][] p,
			int base, float[][] uvs, int light, int overlay, float[] n, boolean reverse) {
		float nx = reverse ? -n[0] : n[0];
		float ny = reverse ? -n[1] : n[1];
		float nz = reverse ? -n[2] : n[2];
		for (int i = 0; i < 4; i++) {
			int v = reverse ? 3 - i : i;
			double[] vp = p[base + v];
			vc.vertex(pos, (float) vp[0], (float) vp[1], (float) vp[2])
				.color(255, 255, 255, 255)
				.texture(uvs[v][0], uvs[v][1])
				.overlay(overlay)
				.light(light)
				.normal(norm, nx, ny, nz)
				.next();
		}
	}
}
