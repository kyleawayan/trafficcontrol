package com.clussmanproductions.trafficcontrol.compat;

import net.minecraft.util.math.BlockPos;

import org.mtr.core.tool.Vector;
import org.mtr.mod.client.MinecraftClientData;
import org.mtr.mod.data.VehicleExtension;

/**
 * The single class that touches the Minecraft Transit Railway API. It is only
 * ever called from {@code ShuntBlockEntity.clientTick} behind a
 * {@code TrafficControl.MTR_LOADED} guard, so when MTR is absent the JVM never
 * loads this class and never links MTR's classes.
 *
 * <p>Detection is necessarily client-side: MTR keeps its live train list
 * ({@link MinecraftClientData#vehicles}) on the client only, with no public
 * server-side query. This mirrors how MTR's own redstone train sensors work.
 *
 * <p>The MTR method signatures here are compile-checked against the MTR jar but
 * cannot be runtime-verified in a headless environment — the range and the
 * per-train geometry below are intentionally kept simple and easy to tweak.
 */
public final class MtrTrainDetector {
	private MtrTrainDetector() {}

	/**
	 * Returns true if any MTR train (any car of any vehicle) is within
	 * {@code range} blocks of the centre of {@code pos}.
	 */
	public static boolean isTrainNear(BlockPos pos, double range) {
		Vector centre = new Vector(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
		for (VehicleExtension vehicle : MinecraftClientData.getInstance().vehicles) {
			if (isVehicleNear(vehicle, centre, range)) {
				return true;
			}
		}
		return false;
	}

	private static boolean isVehicleNear(VehicleExtension vehicle, Vector centre, double range) {
		boolean checkedAnyCar = false;
		// getVehicleCarsAndPositions(): a list of cars, each paired with a list
		// of (front, back) bogie position pairs. Test every bogie position.
		for (var car : vehicle.getVehicleCarsAndPositions()) {
			for (var bogiePair : car.right()) {
				checkedAnyCar = true;
				if (bogiePair.left().distanceTo(centre) <= range
						|| bogiePair.right().distanceTo(centre) <= range) {
					return true;
				}
			}
		}
		// Fall back to the head position if no car geometry was available.
		return !checkedAnyCar && vehicle.getHeadPosition().distanceTo(centre) <= range;
	}
}
