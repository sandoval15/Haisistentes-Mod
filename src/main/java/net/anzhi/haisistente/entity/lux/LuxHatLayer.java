package net.mcreator.haisistente.entity.lux;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.model.HumanoidModel;

import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.util.RenderUtils;

import org.joml.Matrix4f;
import com.mojang.math.Axis;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.AnimatableManager;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import software.bernie.geckolib.model.GeoModel;

import net.mcreator.haisistente.entity.HaisistenteLux;
import net.mcreator.haisistente.entity.HaisistenteAbstract;

public class LuxHatLayer extends GeoRenderLayer<HaisistenteAbstract> {

    private final LuxHatRenderer hatRenderer = new LuxHatRenderer();

    public LuxHatLayer(GeoEntityRenderer<HaisistenteAbstract> renderer) {
        super(renderer);
    }
    
	public void renderForBone(PoseStack poseStack, HaisistenteAbstract animatable, GeoBone bone, RenderType renderType, 
                          	MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick, 
                          	int packedLight, int packedOverlay) {

    	if (bone.getName().equals("head")) { 
        	poseStack.pushPose();
        	poseStack.translate(
            	0.0,
            	0.0,
          		-0.0625
        	);
        	if (animatable instanceof HaisistenteLux lux){
        		RenderType type = RenderType.entityCutout(hatRenderer.getTextureLocation(lux.hatAnim));
				VertexConsumer vertexConsumer = bufferSource.getBuffer(type);
				GeoModel model = hatRenderer.getGeoModel();

				hatRenderer.actuallyRender(
    				poseStack,
    				lux.hatAnim,
    				model.getBakedModel(model.getModelResource(hatRenderer.getAnimatable())),
    				type,
    				bufferSource,
    				vertexConsumer,
    				false,
    				partialTick,
    				packedLight,
    				packedOverlay,
    				1.0f, 1.0f, 1.0f, 1.0f
				);
        	}
        	poseStack.popPose();
    	}
	}
}
