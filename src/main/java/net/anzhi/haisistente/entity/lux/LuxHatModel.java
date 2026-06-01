package net.mcreator.haisistente.entity.lux;

import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;
import net.mcreator.haisistente.entity.HaisistenteAbstract;
import net.mcreator.haisistente.entity.HaisistenteLux;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;

public class LuxHatModel extends GeoModel<LuxHatAnimatable> {

    @Override
    public ResourceLocation getModelResource(LuxHatAnimatable animatable) {
        return new ResourceLocation("haisistente", "geo/gorro_favio.geo.json");
    }

	@Override
	public ResourceLocation getTextureResource(LuxHatAnimatable entity) {
		return new ResourceLocation("haisistente", "textures/entities/textura_favio.png");
	}

    @Override
    public ResourceLocation getAnimationResource(LuxHatAnimatable animatable) {
        return new ResourceLocation("haisistente", "animations/gorro_favio.animation.json");
    }
}

