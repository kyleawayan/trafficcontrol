package com.example.docs.datagen;

import java.util.concurrent.CompletableFuture;

import net.minecraft.core.HolderLookup;
import net.minecraft.tags.FluidTags;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;

import com.example.docs.fluid.ModFluidTags;
import com.example.docs.fluid.ModFluids;

public class ExampleModFluidTagProvider extends FabricTagProvider.FluidTagProvider {
	public ExampleModFluidTagProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
		super(output, registriesFuture);
	}

	@Override
	protected void addTags(HolderLookup.Provider wrapperLookup) {
		valueLookupBuilder(ModFluidTags.ACID).add(ModFluids.ACID_STILL, ModFluids.ACID_FLOWING);
		valueLookupBuilder(FluidTags.WATER).addTag(ModFluidTags.ACID);
	}
}
