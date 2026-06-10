package net.anzhi.haisistente;

import net.anzhi.haisistente.entity.flag.FrameFlagPacket;
import net.anzhi.haisistente.init.*;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.fml.util.thread.SidedThreadGroups;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.common.MinecraftForge;

import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.FriendlyByteBuf;

import net.anzhi.haisistente.entity.HaisistenteAbstract;
import net.anzhi.haisistente.entity.flag.FrameFlag;

import java.util.function.Supplier;
import java.util.function.Function;
import java.util.function.BiConsumer;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.List;
import java.util.Collection;
import java.util.ArrayList;
import java.util.AbstractMap;

@Mod("haisistente")
public class HaisistenteMod {
	public static final Logger LOGGER = LogManager.getLogger(HaisistenteMod.class);
	public static final String MODID = "haisistente";

	public HaisistenteMod() {
		MinecraftForge.EVENT_BUS.register(this);
		IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
		HaisistenteCreativeTab.TABS.register(bus);
		HaisistenteEntities.REGISTRY.register(bus);
		HaisistenteBlocks.REGISTRY.register(bus);
		HaisistenteItems.REGISTRY.register(bus);
		HaisistenteMenus.REGISTRY.register(bus);
		HaisistenteMod.addNetworkMessage(FrameFlagPacket.class,
				// ENCODE
				(packet, buffer) -> {
					buffer.writeInt(packet.entityId());
					buffer.writeEnum(packet.flag());
				},
				// DECODE
				buffer -> new FrameFlagPacket(buffer.readInt(), buffer.readEnum(FrameFlag.class)),
				// HANDLE (SERVER)
				(packet, contextSupplier) -> {
					NetworkEvent.Context context = contextSupplier.get();
					context.enqueueWork(() -> {
						ServerPlayer sender = context.getSender();
						if (sender == null)
							return;
						Entity entity = sender.level().getEntity(packet.entityId());
						if (entity instanceof HaisistenteAbstract haisen) {
							haisen.setFrameFlag(packet.flag(), true);
						}
					});
					context.setPacketHandled(true);
				});
	}

	private static final String PROTOCOL_VERSION = "1";
	public static final SimpleChannel PACKET_HANDLER = NetworkRegistry.newSimpleChannel(new ResourceLocation(MODID, MODID), () -> PROTOCOL_VERSION, PROTOCOL_VERSION::equals, PROTOCOL_VERSION::equals);
	private static int messageID = 0;

	public static <T> void addNetworkMessage(Class<T> messageType, BiConsumer<T, FriendlyByteBuf> encoder, Function<FriendlyByteBuf, T> decoder, BiConsumer<T, Supplier<NetworkEvent.Context>> messageConsumer) {
		PACKET_HANDLER.registerMessage(messageID, messageType, encoder, decoder, messageConsumer);
		messageID++;
	}

	private static final Collection<AbstractMap.SimpleEntry<Runnable, Integer>> workQueue = new ConcurrentLinkedQueue<>();

	public static void queueServerWork(int tick, Runnable action) {
		if (Thread.currentThread().getThreadGroup() == SidedThreadGroups.SERVER)
			workQueue.add(new AbstractMap.SimpleEntry<>(action, tick));
	}

	@SubscribeEvent
	public void tick(TickEvent.ServerTickEvent event) {
		if (event.phase == TickEvent.Phase.END) {
			List<AbstractMap.SimpleEntry<Runnable, Integer>> actions = new ArrayList<>();
			workQueue.forEach(work -> {
				work.setValue(work.getValue() - 1);
				if (work.getValue() == 0)
					actions.add(work);
			});
			actions.forEach(e -> e.getKey().run());
			workQueue.removeAll(actions);
		}
	}
}
