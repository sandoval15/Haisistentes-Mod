package net.anzhi.haisistente.init;

import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;

import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Entity;

import net.anzhi.haisistente.entity.Haisistente;
import net.anzhi.haisistente.entity.HaisistenteZombie;
import net.anzhi.haisistente.entity.HaisistenteLux;
import net.anzhi.haisistente.entity.HaisistenteFlou;
import net.anzhi.haisistente.entity.HaisistenteLilac;
import net.anzhi.haisistente.entity.HaisistenteAnother;
import net.anzhi.haisistente.entity.HaisistentePixel;
import net.anzhi.haisistente.entity.HaisistenteIsabella;
import net.anzhi.haisistente.entity.HaisistenteTate;
import net.anzhi.haisistente.entity.HaisistenteJenn;
import net.anzhi.haisistente.HaisistenteMod;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class HaisistenteEntities {
	public static final DeferredRegister<EntityType<?>> REGISTRY = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, HaisistenteMod.MODID);
	public static final RegistryObject<EntityType<Haisistente>> HAISISTENTE = register("haisistente",
			EntityType.Builder.<Haisistente>of(Haisistente::new, MobCategory.CREATURE).setShouldReceiveVelocityUpdates(true).setTrackingRange(64).setUpdateInterval(3).setCustomClientFactory(Haisistente::new)

					.sized(0.6f, 1.8f));
	public static final RegistryObject<EntityType<HaisistenteZombie>> HAISISTENTE_ZOMBIE = register("haisistente_zombie",
			EntityType.Builder.<HaisistenteZombie>of(HaisistenteZombie::new, MobCategory.MONSTER).setShouldReceiveVelocityUpdates(true).setTrackingRange(64).setUpdateInterval(3).setCustomClientFactory(HaisistenteZombie::new)

					.sized(0.6f, 1.8f));
	public static final RegistryObject<EntityType<HaisistenteLux>> HAISISTENTE_LUX = register("haisistente_lux",
			EntityType.Builder.<HaisistenteLux>of(HaisistenteLux::new, MobCategory.CREATURE).setShouldReceiveVelocityUpdates(true).setTrackingRange(64).setUpdateInterval(3).setCustomClientFactory(HaisistenteLux::new)

					.sized(0.6f, 1.8f));
	public static final RegistryObject<EntityType<HaisistenteFlou>> HAISISTENTE_FLOU = register("haisistente_flou",
			EntityType.Builder.<HaisistenteFlou>of(HaisistenteFlou::new, MobCategory.CREATURE).setShouldReceiveVelocityUpdates(true).setTrackingRange(64).setUpdateInterval(3).setCustomClientFactory(HaisistenteFlou::new)

					.sized(0.6f, 1.8f));
	public static final RegistryObject<EntityType<HaisistenteLilac>> HAISISTENTE_LILAC = register("haisistente_lilac",
			EntityType.Builder.<HaisistenteLilac>of(HaisistenteLilac::new, MobCategory.CREATURE).setShouldReceiveVelocityUpdates(true).setTrackingRange(64).setUpdateInterval(3).setCustomClientFactory(HaisistenteLilac::new)

					.sized(0.6f, 1.8f));
	public static final RegistryObject<EntityType<HaisistenteAnother>> HAISISTENTE_ANOTHER = register("haisistente_another",
			EntityType.Builder.<HaisistenteAnother>of(HaisistenteAnother::new, MobCategory.CREATURE).setShouldReceiveVelocityUpdates(true).setTrackingRange(64).setUpdateInterval(3).setCustomClientFactory(HaisistenteAnother::new)

					.sized(0.6f, 1.8f));
	public static final RegistryObject<EntityType<HaisistentePixel>> HAISISTENTE_PIXEL = register("haisistente_pixel",
			EntityType.Builder.<HaisistentePixel>of(HaisistentePixel::new, MobCategory.CREATURE).setShouldReceiveVelocityUpdates(true).setTrackingRange(64).setUpdateInterval(3).setCustomClientFactory(HaisistentePixel::new)

					.sized(0.6f, 1.8f));
	public static final RegistryObject<EntityType<HaisistenteIsabella>> HAISISTENTE_ISABELLA = register("haisistente_isabella",
			EntityType.Builder.<HaisistenteIsabella>of(HaisistenteIsabella::new, MobCategory.CREATURE).setShouldReceiveVelocityUpdates(true).setTrackingRange(64).setUpdateInterval(3).setCustomClientFactory(HaisistenteIsabella::new)

					.sized(0.6f, 1.8f));
	public static final RegistryObject<EntityType<HaisistenteTate>> HAISISTENTE_TATE = register("haisistente_tate",
			EntityType.Builder.<HaisistenteTate>of(HaisistenteTate::new, MobCategory.CREATURE).setShouldReceiveVelocityUpdates(true).setTrackingRange(64).setUpdateInterval(3).setCustomClientFactory(HaisistenteTate::new)

					.sized(0.6f, 1.8f));
	public static final RegistryObject<EntityType<HaisistenteJenn>> HAISISTENTE_JENN = register("haisistente_jenn",
			EntityType.Builder.<HaisistenteJenn>of(HaisistenteJenn::new, MobCategory.CREATURE).setShouldReceiveVelocityUpdates(true).setTrackingRange(64).setUpdateInterval(3).setCustomClientFactory(HaisistenteJenn::new)

					.sized(0.6f, 1.8f));

	private static <T extends Entity> RegistryObject<EntityType<T>> register(String registryname, EntityType.Builder<T> entityTypeBuilder) {
		return REGISTRY.register(registryname, () -> (EntityType<T>) entityTypeBuilder.build(registryname));
	}

	@SubscribeEvent
	public static void init(FMLCommonSetupEvent event) {
		event.enqueueWork(() -> {
			Haisistente.init();
			HaisistenteLux.init();
			HaisistenteFlou.init();
			HaisistenteLilac.init();
			HaisistenteAnother.init();
			HaisistentePixel.init();
			HaisistenteIsabella.init();
			HaisistenteTate.init();
			HaisistenteJenn.init();
			HaisistenteZombie.init();
		});
	}

	@SubscribeEvent
	public static void registerAttributes(EntityAttributeCreationEvent event) {
		event.put(HAISISTENTE.get(), Haisistente.createAttributes().build());
		event.put(HAISISTENTE_LUX.get(), HaisistenteLux.createAttributes().build());
		event.put(HAISISTENTE_FLOU.get(), HaisistenteFlou.createAttributes().build());
		event.put(HAISISTENTE_LILAC.get(), HaisistenteLilac.createAttributes().build());
		event.put(HAISISTENTE_ANOTHER.get(), HaisistenteAnother.createAttributes().build());
		event.put(HAISISTENTE_PIXEL.get(), HaisistentePixel.createAttributes().build());
		event.put(HAISISTENTE_ISABELLA.get(), HaisistenteIsabella.createAttributes().build());
		event.put(HAISISTENTE_TATE.get(), HaisistenteTate.createAttributes().build());
		event.put(HAISISTENTE_JENN.get(), HaisistenteJenn.createAttributes().build());
		event.put(HAISISTENTE_ZOMBIE.get(), HaisistenteZombie.createAttributes().build());
	}
}

