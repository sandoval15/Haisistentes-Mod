package net.anzhi.haisistente.client.renderer;

import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.cache.object.BakedGeoModel;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.MultiBufferSource;

import net.anzhi.haisistente.entity.model.HaisistenteModel;
import net.anzhi.haisistente.entity.HaisistenteAbstract;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.anzhi.haisistente.client.overlay.EntityOverlayGeoLayer;
import net.anzhi.haisistente.entity.layer.EatGeoRendererLayer;
import net.anzhi.haisistente.init.LayerFactory;

import java.util.List;

public class HaisistenteRenderer extends GeoEntityRenderer<HaisistenteAbstract> {
    public HaisistenteRenderer(EntityRendererProvider.Context renderManager, List<LayerFactory<HaisistenteAbstract>> factories) {
        super(renderManager, new HaisistenteModel());
        this.addRenderLayer(new EatGeoRendererLayer(this));
        if (factories != null) {
            for (LayerFactory<HaisistenteAbstract> f : factories) {
                this.addRenderLayer(f.create(this));
            }
        }
        // Last so overlays draw on top of the outfit/item layers (see docs/entity-overlays.md)
        this.addRenderLayer(new EntityOverlayGeoLayer<>(this));
        this.shadowRadius = 0.5f;
    }

    @Override
    public RenderType getRenderType(HaisistenteAbstract animatable, ResourceLocation texture, MultiBufferSource bufferSource, float partialTick) {
        return RenderType.entityTranslucent(getTextureLocation(animatable));
    }

    @Override
    public void preRender(PoseStack poseStack, HaisistenteAbstract entity, BakedGeoModel model, MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, float red, float green,
            float blue, float alpha) {
        float scale = 1f;
        this.scaleHeight = scale;
        this.scaleWidth = scale;
        super.preRender(poseStack, entity, model, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, red, green, blue, alpha);
    }

    public boolean isShaking(HaisistenteAbstract animatable) {
        return super.isShaking(animatable) || animatable.isShaking();
    }
}
