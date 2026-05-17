package com.clussmanproductions.trafficcontrol;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public final class ModItemGroups {
	private ModItemGroups() {}

	public static final Identifier GROUP_ID = new Identifier(TrafficControl.MOD_ID, "traffic_control");

	public static void register() {
		ItemGroup group = FabricItemGroup.builder()
			.displayName(Text.translatable("itemGroup.trafficcontrol"))
			.icon(() -> {
				var cone = ModBlocks.BY_NAME.get("cone");
				return cone != null ? new ItemStack(cone) : new ItemStack(Items.STONE);
			})
			.entries((displayContext, entries) -> {
				ModBlocks.BLOCKS.forEach(entries::add);
				ModItems.ITEMS.forEach(entries::add);
			})
			.build();
		Registry.register(Registries.ITEM_GROUP, GROUP_ID, group);
	}
}
