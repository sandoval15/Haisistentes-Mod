package net.anzhi.haisistente.init;

import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.common.extensions.IForgeMenuType;

import net.minecraft.world.inventory.MenuType;

import net.anzhi.haisistente.entity.lux.LuxMenu;
import net.anzhi.haisistente.HaisistenteMod;


public class HaisistenteMenus {
	public static final DeferredRegister<MenuType<?>> REGISTRY = DeferredRegister.create(ForgeRegistries.MENU_TYPES, HaisistenteMod.MODID);
	public static final RegistryObject<MenuType<LuxMenu>> LUX_INVENTORY = REGISTRY.register("lux_inventory", () -> IForgeMenuType.create(LuxMenu::new));
}

