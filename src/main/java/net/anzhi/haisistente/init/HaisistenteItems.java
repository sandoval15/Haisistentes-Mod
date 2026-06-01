package net.mcreator.haisistente.init;

import net.mcreator.haisistente.HaisistenteMod;
import net.mcreator.haisistente.init.HaisistenteEntities;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import net.minecraft.world.item.Item;

import net.mcreator.haisistente.item.BambooDorado;

public class HaisistenteItems {
	public static final DeferredRegister<Item> REGISTRY = DeferredRegister.create(ForgeRegistries.ITEMS, HaisistenteMod.MODID);
	public static final RegistryObject<Item> HAISISTENTE_SPAWN_EGG = REGISTRY.register("haisistente_spawn_egg", () -> new ForgeSpawnEggItem(HaisistenteEntities.HAISISTENTE, -1, -1, new Item.Properties()){
			@Override
			public Component getName(ItemStack stack) {
    			return Component.literal("Haisistente Spawn Egg");
			}
		});
	public static final RegistryObject<Item> HAISISTENTE_ZOMBIE_SPAWN_EGG = REGISTRY.register("haisistente_zombie_spawn_egg", () -> new ForgeSpawnEggItem(HaisistenteEntities.HAISISTENTE_ZOMBIE, -1, -1, new Item.Properties()){
			@Override
			public Component getName(ItemStack stack) {
    			return Component.literal("Haisistente Zombie Spawn Egg");
			}
		});
	public static final RegistryObject<Item> HAISISTENTE_LUX_SPAWN_EGG = REGISTRY.register("haisistente_lux_spawn_egg", () -> new ForgeSpawnEggItem(HaisistenteEntities.HAISISTENTE_LUX, -1, -1, new Item.Properties()){
			@Override
			public Component getName(ItemStack stack) {
    			return Component.literal("Haisistente Lux Spawn Egg");
			}
		});
	public static final RegistryObject<Item> HAISISTENTE_FLOU_SPAWN_EGG = REGISTRY.register("haisistente_flou_spawn_egg", () -> new ForgeSpawnEggItem(HaisistenteEntities.HAISISTENTE_FLOU, -1, -1, new Item.Properties()){
			@Override
			public Component getName(ItemStack stack) {
    			return Component.literal("Haisistente Flou Spawn Egg");
			}
		});
	public static final RegistryObject<Item> HAISISTENTE_LILAC_SPAWN_EGG = REGISTRY.register("haisistente_lilac_spawn_egg", () -> new ForgeSpawnEggItem(HaisistenteEntities.HAISISTENTE_LILAC, -1, -1, new Item.Properties()){
			@Override
			public Component getName(ItemStack stack) {
    			return Component.literal("Haisistente Lilac Spawn Egg");
			}
		});
	public static final RegistryObject<Item> HAISISTENTE_ANOTHER_SPAWN_EGG = REGISTRY.register("haisistente_another_spawn_egg", () -> new ForgeSpawnEggItem(HaisistenteEntities.HAISISTENTE_ANOTHER, -1, -1, new Item.Properties()){
			@Override
			public Component getName(ItemStack stack) {
    			return Component.literal("Haisistente Another Spawn Egg");
			}
		});
	public static final RegistryObject<Item> HAISISTENTE_PIXEL_SPAWN_EGG = REGISTRY.register("haisistente_pixel_spawn_egg", () -> new ForgeSpawnEggItem(HaisistenteEntities.HAISISTENTE_PIXEL, -1, -1, new Item.Properties()){
			@Override
			public Component getName(ItemStack stack) {
    			return Component.literal("Haisistente Pixel Spawn Egg");
			}
		});
	public static final RegistryObject<Item> HAISISTENTE_ISABELLA_SPAWN_EGG = REGISTRY.register("haisistente_isabella_spawn_egg", () -> new ForgeSpawnEggItem(HaisistenteEntities.HAISISTENTE_ISABELLA, -1, -1, new Item.Properties()){
			@Override
			public Component getName(ItemStack stack) {
    			return Component.literal("Haisistente Isabella Spawn Egg");
			}
		});
	public static final RegistryObject<Item> HAISISTENTE_TATE_SPAWN_EGG = REGISTRY.register("haisistente_tate_spawn_egg", () -> new ForgeSpawnEggItem(HaisistenteEntities.HAISISTENTE_TATE, -1, -1, new Item.Properties()){
			@Override
			public Component getName(ItemStack stack) {
    			return Component.literal("Haisistente Tate Spawn Egg");
			}
		});
	public static final RegistryObject<Item> HAISISTENTE_JENN_SPAWN_EGG = REGISTRY.register("haisistente_jenn_spawn_egg", () -> new ForgeSpawnEggItem(HaisistenteEntities.HAISISTENTE_JENN, -1, -1, new Item.Properties()){
			@Override
			public Component getName(ItemStack stack) {
    			return Component.literal("Haisistente Jenn Spawn Egg");
			}
		});
	public static final RegistryObject<Item> BAMBOO_DORADO = REGISTRY.register("golden_bamboo", () -> new BambooDorado());
}

