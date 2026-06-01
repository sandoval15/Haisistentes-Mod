package net.mcreator.haisistente.init;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import net.mcreator.haisistente.HaisistenteMod;

public class HaisistenteCreativeTab {

    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, HaisistenteMod.MODID);

    public static final RegistryObject<CreativeModeTab> HAISISTENTE_TAB =
            TABS.register("haisistente", () ->
                    CreativeModeTab.builder()
                            .title(Component.translatable("Haisistente"))
                            .icon(() -> new ItemStack(HaisistenteItems.BAMBOO_DORADO.get()))
                            .displayItems((params, output) -> {

                                // 👉 AQUÍ SÍ PUEDES ESCRIBIR CÓDIGO LIBRE
                                output.accept(HaisistenteItems.BAMBOO_DORADO.get());
                                output.accept(HaisistenteItems.HAISISTENTE_SPAWN_EGG.get());
                                output.accept(HaisistenteItems.HAISISTENTE_ZOMBIE_SPAWN_EGG.get());
								output.accept(HaisistenteItems.HAISISTENTE_LUX_SPAWN_EGG.get());
								output.accept(HaisistenteItems.HAISISTENTE_FLOU_SPAWN_EGG.get());
								output.accept(HaisistenteItems.HAISISTENTE_LILAC_SPAWN_EGG.get());
								output.accept(HaisistenteItems.HAISISTENTE_ANOTHER_SPAWN_EGG.get());
								output.accept(HaisistenteItems.HAISISTENTE_PIXEL_SPAWN_EGG.get());
								output.accept(HaisistenteItems.HAISISTENTE_ISABELLA_SPAWN_EGG.get());
								output.accept(HaisistenteItems.HAISISTENTE_TATE_SPAWN_EGG.get());
								output.accept(HaisistenteItems.HAISISTENTE_JENN_SPAWN_EGG.get());
                            })
                            .build()
            );
}

