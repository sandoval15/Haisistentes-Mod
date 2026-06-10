package net.anzhi.haisistente.entity.lux;

import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;
import net.anzhi.haisistente.entity.HaisistenteAbstract;
import net.anzhi.haisistente.entity.HaisistenteLux;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;

public class LuxHatModel extends GeoModel<LuxHatAnimatable> {

    @Override
    public ResourceLocation getModelResource(LuxHatAnimatable animatable) {
        return new ResourceLocation("haisistente", "geo/favio_hat.geo.json");
    }

	@Override
	public ResourceLocation getTextureResource(LuxHatAnimatable entity) {
		return new ResourceLocation("haisistente", "textures/entities/favio_texture.png");
	}

    @Override
    public ResourceLocation getAnimationResource(LuxHatAnimatable animatable) {
        return new ResourceLocation("haisistente", "animations/favio_hat.animation.json");
    }
}

