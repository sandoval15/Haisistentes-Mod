package net.mcreator.haisistente.entity.layer;

import net.minecraft.world.item.ItemDisplayContext;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

import net.mcreator.haisistente.entity.HaisistenteAbstract;

import software.bernie.geckolib.renderer.layer.GeoRenderLayer;
import net.mcreator.haisistente.entity.flag.FrameFlag;


public class EatGeoRendererLayer extends GeoRenderLayer<HaisistenteAbstract> {

    public EatGeoRendererLayer(GeoRenderer<HaisistenteAbstract> renderer) {
        super(renderer);
    }

    public void render(
            PoseStack poseStack,
            HaisistenteAbstract entity,
            BakedGeoModel bakedModel,
            RenderType renderType,
            MultiBufferSource bufferSource,
            VertexConsumer vertexConsumer,
            float partialTicks,
            int packedLight,
            int packedOverlay
    ) {

        //ItemStack itemStack = new ItemStack(Blocks.BAMBOO);
        ItemStack itemStack = entity.getItemInHand(InteractionHand.MAIN_HAND);
        if (itemStack.isEmpty()) return;
        if (!entity.getFrameFlag(FrameFlag.FRAME_START_EAT)) return;

        GeoBone bone = bakedModel.getBone("obj").orElse(null);
        if (bone == null || !entity.isEating()) return;

        poseStack.pushPose();
        float Yaw = entity.yBodyRot * ((float)Math.PI / 180F);
        
        poseStack.mulPose(Axis.YP.rotation(-Yaw));
        poseStack.translate(
    		bone.getModelPosition().x()*0.08,
    		bone.getModelPosition().y()*0.06,
		    -bone.getModelPosition().z()*0.06
		);
        /*poseStack.translate(bone.getPosX()*0.16, bone.getPosY()*0.95, bone.getPosZ()*0.2);*/
		poseStack.mulPose(Axis.XP.rotation(-bone.getRotX()));
		poseStack.mulPose(Axis.YP.rotation(bone.getRotY()));
		poseStack.mulPose(Axis.ZP.rotation(bone.getRotZ()));

        poseStack.scale(0.8F, 0.8F, 0.8F);

        Minecraft.getInstance().getItemRenderer().renderStatic(
                itemStack,
                ItemDisplayContext.GROUND,
                packedLight,
                packedOverlay,
                poseStack,
                bufferSource,
                entity.level(),
                entity.getId()
        );

        poseStack.popPose();
    }
}
