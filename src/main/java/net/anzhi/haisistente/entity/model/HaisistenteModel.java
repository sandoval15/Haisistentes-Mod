package net.mcreator.haisistente.entity.model;

import software.bernie.geckolib.model.data.EntityModelData;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animatable.model.CoreGeoBone;
import software.bernie.geckolib.constant.DataTickets;

import net.minecraft.util.Mth;
import net.minecraft.resources.ResourceLocation;

import net.mcreator.haisistente.entity.HaisistenteAbstract;
import com.ibm.icu.util.DangiCalendar;
import com.ibm.icu.util.DangiCalendar;
import com.ibm.icu.util.DangiCalendar;
import com.ibm.icu.util.DangiCalendar;
import com.ibm.icu.util.DangiCalendar;

public class HaisistenteModel extends GeoModel<HaisistenteAbstract> {
	@Override
	public ResourceLocation getAnimationResource(HaisistenteAbstract entity) {
		return new ResourceLocation("haisistente",entity.getGeoAnimation());
	}

	@Override
	public ResourceLocation getModelResource(HaisistenteAbstract entity) {
		return new ResourceLocation("haisistente",entity.getModel());
	}

	@Override
	public ResourceLocation getTextureResource(HaisistenteAbstract entity) {
		return new ResourceLocation("haisistente", "textures/entities/" + entity.getTexture() + ".png");
	}

	@Override
	public void setCustomAnimations(HaisistenteAbstract animatable, long instanceId, AnimationState animationState) {
		CoreGeoBone head = getAnimationProcessor().getBone("head");
		animatable.animationState  = animationState;
		boolean dance = true;
		boolean canrothead = true;
		for (int i = 1; i <= animatable.maxdances; i++){
			dance = animationState.isCurrentAnimationStage("dance"+i);
			if (dance)
				break;
		}

		for (String s : animatable.AnimsWithHeadAnimation()) {
    		canrothead = !animationState.isCurrentAnimationStage(s);
    		if (!canrothead)
				break; 
		}
		
		if (head != null && canrothead && !(dance)) {
			EntityModelData entityData = (EntityModelData) animationState.getData(DataTickets.ENTITY_MODEL_DATA);
			head.setRotX(entityData.headPitch() * Mth.DEG_TO_RAD);
			head.setRotY(entityData.netHeadYaw() * Mth.DEG_TO_RAD);
		}
	}
}
