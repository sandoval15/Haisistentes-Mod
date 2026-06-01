package net.mcreator.haisistente.init;

import net.mcreator.haisistente.HaisistenteMod;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import net.mcreator.haisistente.client.renderer.HaisistenteRenderer;

import net.mcreator.haisistente.entity.lux.LuxHatLayer;

import java.util.List;

@Mod.EventBusSubscriber(
        modid = HaisistenteMod.MODID,
        bus = Mod.EventBusSubscriber.Bus.MOD,
        value = Dist.CLIENT
)
public class HaisistenteRenderers {
    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(HaisistenteEntities.HAISISTENTE.get(),
                ctx -> new HaisistenteRenderer(ctx, List.of()));
        event.registerEntityRenderer(HaisistenteEntities.HAISISTENTE_ZOMBIE.get(),
                ctx -> new HaisistenteRenderer(ctx, List.of()));
        event.registerEntityRenderer(HaisistenteEntities.HAISISTENTE_LUX.get(),
                ctx -> new HaisistenteRenderer(ctx, List.of(LuxHatLayer::new)));
        event.registerEntityRenderer(HaisistenteEntities.HAISISTENTE_FLOU.get(),
                ctx -> new HaisistenteRenderer(ctx, List.of()));
        event.registerEntityRenderer(HaisistenteEntities.HAISISTENTE_LILAC.get(),
                ctx -> new HaisistenteRenderer(ctx, List.of()));
        event.registerEntityRenderer(HaisistenteEntities.HAISISTENTE_ANOTHER.get(),
                ctx -> new HaisistenteRenderer(ctx, List.of()));
        event.registerEntityRenderer(HaisistenteEntities.HAISISTENTE_PIXEL.get(),
                ctx -> new HaisistenteRenderer(ctx, List.of()));
        event.registerEntityRenderer(HaisistenteEntities.HAISISTENTE_ISABELLA.get(),
                ctx -> new HaisistenteRenderer(ctx, List.of()));
        event.registerEntityRenderer(HaisistenteEntities.HAISISTENTE_TATE.get(),
                ctx -> new HaisistenteRenderer(ctx, List.of()));
        event.registerEntityRenderer(HaisistenteEntities.HAISISTENTE_JENN.get(),
                ctx -> new HaisistenteRenderer(ctx, List.of()));
    }
}
