package com.clussmanproductions.trafficcontrol.client.render;

import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

/**
 * Draws axis-aligned boxes into a {@code VertexConsumer}, porting the original
 * mod's {@code RenderBoxHelper}. Coordinates are in model pixels (16 per
 * block). Each box face takes a UV rectangle (also in pixels). Faces are
 * emitted double-sided so they are visible regardless of the render layer's
 * culling.
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

	/**
	 * @param faces UV rectangles in collection order: south, up, north, down,
	 *              east, west.
	 */
	public static void box(MatrixStack matrices, VertexConsumer vc, int light, int overlay,
			double x, double y, double z, double w, double h, double d, float[]... faces) {
		double cx = x / 16, cy = y / 16, cz = z / 16;
		double cw = w / 16, ch = h / 16, cd = d / 16;
		double x1 = cx + cw, y1 = cy + ch, z1 = cz + cd;

		double[][] p = {
			{x1, cy, cz}, {cx, cy, cz}, {cx, y1, cz}, {x1, y1, cz},          // Front
			{x1, y1, cz}, {cx, y1, cz}, {cx, y1, z1}, {x1, y1, z1},          // Up
			{cx, cy, z1}, {x1, cy, z1}, {x1, y1, z1}, {cx, y1, z1},          // Back
			{x1, cy, z1}, {cx, cy, z1}, {cx, cy, cz}, {x1, cy, cz},          // Down
			{x1, cy, z1}, {x1, cy, cz}, {x1, y1, cz}, {x1, y1, z1},          // Right
			{cx, cy, cz}, {cx, cy, z1}, {cx, y1, z1}, {cx, y1, cz},          // Left
		};
		// Face -> index into the south/up/north/down/east/west array.
		int[] faceTex = {2, 1, 0, 3, 4, 5};

		Matrix4f pos = matrices.peek().getPositionMatrix();
		Matrix3f norm = matrices.peek().getNormalMatrix();

		for (int face = 0; face < 6; face++) {
			float[] t = faces[faceTex[face]];
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
