package net.mcreator.haisistente.entity.lux;

import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animatable.instance.SingletonAnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;
import software.bernie.geckolib.util.RenderUtils;

import net.mcreator.haisistente.entity.HaisistenteLux;

public class LuxHatAnimatable implements GeoAnimatable {

	private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
            
    private HaisistenteLux lux;

    public LuxHatAnimatable(HaisistenteLux entity) {
        lux = entity;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(
            new AnimationController<>(
                this,
                "hat",
                5,
                this::predicate
            )
        );
    }

    private PlayState predicate(AnimationState<LuxHatAnimatable> state) {
        if (!lux.onGround())
            return state.setAndContinue(RawAnimation.begin().thenLoop("hat_fly"));

        if (lux.walkAnimation.isMoving())
            return state.setAndContinue(RawAnimation.begin().thenLoop("hat_move"));

        return state.setAndContinue(RawAnimation.begin().thenLoop("hat_move"));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    public long getInstanceId() {
        return 1;
    }

	@Override
	public double getTick(Object object) {
    	return RenderUtils.getCurrentTick();
	}
}
