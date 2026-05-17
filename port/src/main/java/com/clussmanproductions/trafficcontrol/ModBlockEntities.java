package com.clussmanproductions.trafficcontrol;

import com.clussmanproductions.trafficcontrol.block.entity.StreetLightBlockEntity;

import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public final class ModBlockEntities {
	private ModBlockEntities() {}

	public static BlockEntityType<StreetLightBlockEntity> STREET_LIGHT;

	public static void register() {
		STREET_LIGHT = Registry.register(Registries.BLOCK_ENTITY_TYPE,
			new Identifier(TrafficControl.MOD_ID, "street_light"),
			BlockEntityType.Builder.<StreetLightBlockEntity>create(
				StreetLightBlockEntity::new,
				ModBlocks.BY_NAME.get("street_light_single"),
				ModBlocks.BY_NAME.get("street_light_double")
			).build(null));
	}
}
