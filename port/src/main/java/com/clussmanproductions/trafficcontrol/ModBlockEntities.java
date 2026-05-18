package com.clussmanproductions.trafficcontrol;

import com.clussmanproductions.trafficcontrol.block.entity.BellBlockEntity;
import com.clussmanproductions.trafficcontrol.block.entity.CrossingGateBlockEntity;
import com.clussmanproductions.trafficcontrol.block.entity.CrossingLampsBlockEntity;
import com.clussmanproductions.trafficcontrol.block.entity.StreetLightBlockEntity;
import com.clussmanproductions.trafficcontrol.block.entity.WigWagBlockEntity;

import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public final class ModBlockEntities {
	private ModBlockEntities() {}

	public static BlockEntityType<StreetLightBlockEntity> STREET_LIGHT;
	public static BlockEntityType<WigWagBlockEntity> WIG_WAG;
	public static BlockEntityType<CrossingGateBlockEntity> CROSSING_GATE;
	public static BlockEntityType<CrossingLampsBlockEntity> LAMPS;
	public static BlockEntityType<BellBlockEntity> BELL;

	public static void register() {
		STREET_LIGHT = register("street_light",
			BlockEntityType.Builder.<StreetLightBlockEntity>create(
				StreetLightBlockEntity::new,
				ModBlocks.BY_NAME.get("street_light_single"),
				ModBlocks.BY_NAME.get("street_light_double")));

		WIG_WAG = register("wig_wag",
			BlockEntityType.Builder.<WigWagBlockEntity>create(
				WigWagBlockEntity::new,
				ModBlocks.BY_NAME.get("wig_wag")));

		CROSSING_GATE = register("crossing_gate",
			BlockEntityType.Builder.<CrossingGateBlockEntity>create(
				CrossingGateBlockEntity::new,
				ModBlocks.BY_NAME.get("crossing_gate_gate")));

		LAMPS = register("lamps",
			BlockEntityType.Builder.<CrossingLampsBlockEntity>create(
				CrossingLampsBlockEntity::new,
				ModBlocks.BY_NAME.get("crossing_gate_lamps")));

		BELL = register("bell",
			BlockEntityType.Builder.<BellBlockEntity>create(
				BellBlockEntity::new,
				ModBlocks.BY_NAME.get("wch_bell"),
				ModBlocks.BY_NAME.get("wch_mechanical_bell"),
				ModBlocks.BY_NAME.get("safetran_type_3"),
				ModBlocks.BY_NAME.get("safetran_mechanical")));
	}

	private static <T extends net.minecraft.block.entity.BlockEntity> BlockEntityType<T> register(
			String name, BlockEntityType.Builder<T> builder) {
		return Registry.register(Registries.BLOCK_ENTITY_TYPE,
			new Identifier(TrafficControl.MOD_ID, name), builder.build(null));
	}
}
