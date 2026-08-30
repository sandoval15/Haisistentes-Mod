package net.anzhi.haisistente.client.overlay;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.LivingEntity;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

/**
 * Geo equivalent of vanilla's "re-render the model with another texture" layers
 * (charged creeper aura, Supplementaries' slimed goo...): re-draws the already
 * posed {@link BakedGeoModel} once per active {@link EntityOverlayProvider},
 * so overlays line up exactly with the animated model.
 */
public class EntityOverlayGeoLayer<T extends LivingEntity & GeoAnimatable> extends GeoRenderLayer<T> {

    public EntityOverlayGeoLayer(GeoRenderer<T> renderer) {
        super(renderer);
    }

    @Override
    public void render(PoseStack poseStack, T entity, BakedGeoModel bakedModel, RenderType renderType,
            MultiBufferSource bufferSource, VertexConsumer vertexConsumer, float partialTick,
            int packedLight, int packedOverlay) {
        for (EntityOverlayProvider provider : EntityOverlayRegistry.providers()) {
            float alpha = provider.getAlpha(entity, partialTick);
            if (alpha <= 0f)
                continue;

            RenderType overlayType = provider.getRenderType(entity, partialTick);
            getRenderer().reRender(bakedModel, poseStack, bufferSource, entity, overlayType,
                    bufferSource.getBuffer(overlayType), partialTick, packedLight,
                    OverlayTexture.NO_OVERLAY, 1f, 1f, 1f, alpha);
        }
    }
}
