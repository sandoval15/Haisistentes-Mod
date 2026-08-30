package net.anzhi.haisistente.client.overlay;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.LivingEntity;

/**
 * A full-model overlay effect (slime goo, frost, energy swirl...) that should be
 * drawn on top of a GeckoLib-rendered entity.
 *
 * <p>Other mods apply these effects through vanilla {@code RenderLayer}s attached to
 * {@code LivingEntityRenderer}, which GeckoLib renderers never receive. Implementations
 * of this interface replicate one such effect inside the GeckoLib render pipeline;
 * register them through {@link EntityOverlayRegistry}.</p>
 */
public interface EntityOverlayProvider {

    /**
     * Overlay opacity for this frame, in {@code [0, 1]}.
     * Return {@code 0} or less to skip rendering entirely.
     */
    float getAlpha(LivingEntity entity, float partialTick);

    /**
     * Render type the overlay pass is buffered with. Only called when
     * {@link #getAlpha} returned a positive value.
     */
    RenderType getRenderType(LivingEntity entity, float partialTick);
}
