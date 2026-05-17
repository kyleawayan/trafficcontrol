package com.clussmanproductions.trafficcontrol;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

/**
 * Registers the non-block items. For v1 every item is a plain {@link Item};
 * the original capability/automation behavior is tracked as TODO(ka).
 */
public final class ModItems {
	private ModItems() {}

	/** All registered items, in registration order, for the creative tab. */
	public static final List<Item> ITEMS = new ArrayList<>();

	private static final String[] NAMES = {
		"crossing_relay_box", "crossing_relay_tuner", "traffic_light_bulb",
		"traffic_light_frame", "traffic_light_1_frame", "traffic_light_2_frame",
		"traffic_light_4_frame", "traffic_light_5_frame", "traffic_light_6_frame",
		"traffic_light_doghouse_frame", "screwdriver",
	};

	public static void register() {
		for (String name : NAMES) {
			Item item = new Item(new Item.Settings());
			Registry.register(Registries.ITEM, new Identifier(TrafficControl.MOD_ID, name), item);
			ITEMS.add(item);
		}
	}
}
