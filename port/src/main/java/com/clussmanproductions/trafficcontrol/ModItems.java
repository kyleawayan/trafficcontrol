package com.clussmanproductions.trafficcontrol;

import java.util.ArrayList;
import java.util.List;

import com.clussmanproductions.trafficcontrol.item.TunerItem;

import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

/**
 * Registers the non-block items. Most are plain {@link Item}s; the relay box is
 * the {@link BlockItem} that places the relay block, and the tuner links
 * crossing components to a relay.
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
			Item item = createItem(name);
			Registry.register(Registries.ITEM, new Identifier(TrafficControl.MOD_ID, name), item);
			ITEMS.add(item);
		}
	}

	private static Item createItem(String name) {
		switch (name) {
			case "crossing_relay_box":
				return new BlockItem(ModBlocks.BY_NAME.get("crossing_relay_se"), new Item.Settings());
			case "crossing_relay_tuner":
				return new TunerItem(new Item.Settings().maxCount(1));
			default:
				return new Item(new Item.Settings());
		}
	}
}
