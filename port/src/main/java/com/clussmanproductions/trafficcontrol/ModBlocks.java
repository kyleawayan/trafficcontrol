package com.clussmanproductions.trafficcontrol;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.clussmanproductions.trafficcontrol.block.BellBlock;
import com.clussmanproductions.trafficcontrol.block.CrossingGateBlock;
import com.clussmanproductions.trafficcontrol.block.CrossingLampsBlock;
import com.clussmanproductions.trafficcontrol.block.StreetLightBlock;
import com.clussmanproductions.trafficcontrol.block.TcHorizontalBlock;
import com.clussmanproductions.trafficcontrol.block.WigWagBlock;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.MapColor;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;

/**
 * Registers every Traffic Control block. For v1 each block is a plain static
 * {@link Block} with its converted model; rotation and animated behavior are
 * tracked as TODO(ka) in PORT_NOTES.md.
 */
public final class ModBlocks {
	private ModBlocks() {}

	/** All registered blocks, in registration order, for the creative tab. */
	public static final List<Block> BLOCKS = new ArrayList<>();
	/** Registry name -> block, for cross-referencing. */
	public static final Map<String, Block> BY_NAME = new LinkedHashMap<>();

	private static final String[] NAMES = {
		"crossing_gate_base", "stand", "crossing_gate_gate", "crossing_gate_lamps",
		"crossing_gate_pole", "crossing_gate_crossbuck", "safetran_type_3",
		"crossing_relay_se", "crossing_relay_sw", "crossing_relay_nw", "crossing_relay_ne",
		"crossing_relay_top_sw", "crossing_relay_top_se", "crossing_relay_top_nw",
		"crossing_relay_top_ne", "overhead_pole", "overhead", "overhead_lamps",
		"overhead_crossbuck", "safetran_mechanical", "sign", "cone", "channelizer",
		"drum", "street_light_single", "light_source", "street_light_double",
		"traffic_light", "traffic_light_control_box", "wig_wag", "vertical_wig_wag",
		"shunt_border", "shunt_island", "type_3_barrier", "type_3_barrier_right",
		"traffic_rail", "concrete_barrier", "horizontal_pole", "wch_bell",
		"wch_mechanical_bell", "traffic_sensor_left", "traffic_sensor_straight",
		"street_sign", "traffic_light_5", "traffic_light_5_upper",
		"traffic_light_doghouse", "traffic_light_1", "traffic_light_2",
		"traffic_light_4", "traffic_light_6", "pedestrian_button", "traffic_sensor_right",
	};

	public static void register() {
		for (String name : NAMES) {
			Block block = createBlock(name);
			Identifier id = new Identifier(TrafficControl.MOD_ID, name);
			Registry.register(Registries.BLOCK, id, block);
			Registry.register(Registries.ITEM, id, new BlockItem(block, new Item.Settings()));
			BLOCKS.add(block);
			BY_NAME.put(name, block);
		}
	}

	private static Block createBlock(String name) {
		switch (name) {
			case "street_light_single":
				return new StreetLightBlock(settingsFor(name), false);
			case "street_light_double":
				return new StreetLightBlock(settingsFor(name), true);
			case "crossing_gate_gate":
				return new CrossingGateBlock(settingsFor(name));
			case "crossing_gate_lamps":
				return new CrossingLampsBlock(settingsFor(name));
			case "wig_wag":
				return new WigWagBlock(settingsFor(name));
			case "wch_bell":
				return new BellBlock(settingsFor(name), ModSounds.WCH);
			case "wch_mechanical_bell":
				return new BellBlock(settingsFor(name), ModSounds.WCH_MECHANICAL_BELL);
			case "safetran_type_3":
				return new BellBlock(settingsFor(name), ModSounds.SAFETRAN_TYPE_3);
			case "safetran_mechanical":
				return new BellBlock(settingsFor(name), ModSounds.SAFETRAN_MECHANICAL);
			default:
				return new TcHorizontalBlock(settingsFor(name));
		}
	}

	private static AbstractBlock.Settings settingsFor(String name) {
		AbstractBlock.Settings settings = AbstractBlock.Settings.create()
			.mapColor(MapColor.GRAY)
			.strength(1.0F)
			.sounds(BlockSoundGroup.METAL)
			.nonOpaque();
		// light_source is the original mod's invisible illumination block.
		if (name.equals("light_source") || name.equals("traffic_light_5_upper")
				|| name.equals("street_light_single") || name.equals("street_light_double")) {
			settings.luminance(state -> 15);
		}
		// Crossing lamps glow only while lit (powered).
		if (name.equals("crossing_gate_lamps")) {
			settings.luminance(state -> state.get(CrossingLampsBlock.LIT) ? 15 : 0);
		}
		return settings;
	}
}
