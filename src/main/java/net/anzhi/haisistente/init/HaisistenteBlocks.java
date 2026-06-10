package net.anzhi.haisistente.init;

import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.DeferredRegister;

import net.minecraft.world.level.block.Block;

import net.anzhi.haisistente.HaisistenteMod;
import net.anzhi.haisistente.block.PlushHaiseBlock;

public class HaisistenteBlocks {
	public static final DeferredRegister<Block> REGISTRY = DeferredRegister.create(ForgeRegistries.BLOCKS, HaisistenteMod.MODID);
	public static final RegistryObject<Block> PLUSH_HAISE = REGISTRY.register("peluchehaise", () -> new PlushHaiseBlock());
}
