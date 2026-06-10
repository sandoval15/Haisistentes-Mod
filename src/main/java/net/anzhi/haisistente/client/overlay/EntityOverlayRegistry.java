package net.anzhi.haisistente.client.overlay;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Client-side registry of {@link EntityOverlayProvider}s consumed by
 * {@link EntityOverlayGeoLayer}. Providers are normally registered once during
 * {@code FMLClientSetupEvent} (see {@code HaisistenteOverlays}).
 */
public final class EntityOverlayRegistry {
    private static final List<EntityOverlayProvider> PROVIDERS = new CopyOnWriteArrayList<>();

    private EntityOverlayRegistry() {
    }

    public static void register(EntityOverlayProvider provider) {
        PROVIDERS.add(provider);
    }

    public static List<EntityOverlayProvider> providers() {
        return PROVIDERS;
    }
}
