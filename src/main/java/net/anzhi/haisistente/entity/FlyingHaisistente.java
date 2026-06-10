package net.anzhi.haisistente.entity;

import javax.annotation.Nullable;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.FollowOwnerGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomFlyingGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.entity.animal.FlyingAnimal;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.animation.AnimationState;

import net.anzhi.haisistente.goal.FlyingFollowOwnerGoal;

/**
 * Parrot-style flying base: walks on the ground like any mob and only flies
 * when a path requires it (following its owner, wandering to a tree, fleeing).
 * When idle in the air, gravity brings it down gently and it lands.
 */
public abstract class FlyingHaisistente extends HaisistenteAbstract implements FlyingAnimal {

	public FlyingHaisistente(EntityType<? extends HaisistenteAbstract> type, Level world) {
		super(type, world);
		this.moveControl = new FlyingMoveControl(this, 10, false);
	}

	@Override
	protected void registerGoals() {
		super.registerGoals();
		this.goalSelector.getAvailableGoals().removeIf(
			g -> g.getGoal() instanceof FollowOwnerGoal
		);
		this.goalSelector.addGoal(6, new FlyingFollowOwnerGoal(this, 1.2D, (float) 10, (float) 2, true));
		this.goalSelector.addGoal(7, new FlyingHaisistenteWanderGoal(this, 0.6D));
	}

	@Override
	public boolean isFlying() {
		return !this.onGround();
	}

	@Override
	protected PathNavigation createNavigation(Level level) {
		FlyingPathNavigation flyingpathnavigation = new FlyingPathNavigation(this, level);
		flyingpathnavigation.setCanOpenDoors(false);
		flyingpathnavigation.setCanFloat(true);
		flyingpathnavigation.setCanPassDoors(true);
		return flyingpathnavigation;
	}

	@Override
	protected void checkFallDamage(double y, boolean onGround, BlockState state, BlockPos pos) {
	}

	public boolean causeFallDamage(float distance, float damageMultiplier) {
		return false;
	}

	@Override
	public void aiStep() {
		super.aiStep();
		// Parrot-style glide: slow descents so it lands gently instead of dropping
		Vec3 delta = this.getDeltaMovement();
		if (!this.onGround() && delta.y < 0.0D) {
			this.setDeltaMovement(delta.multiply(1.0D, 0.6D, 1.0D));
		}
	}

	@Override
	public PlayState handleMovementAnimation(AnimationState event) {
		if (!this.onGround() && !this.isInWaterOrBubble()) {
			return event.setAndContinue(RawAnimation.begin().thenLoop("idlefly"));
		}
		return super.handleMovementAnimation(event);
	}

	static class FlyingHaisistenteWanderGoal extends WaterAvoidingRandomFlyingGoal {
		private int stuckTicks;

		public FlyingHaisistenteWanderGoal(PathfinderMob mob, double speed) {
			super(mob, speed);
		}

		@Override
		public void start() {
			stuckTicks = 0;
			super.start();
		}

		@Override
		public boolean canContinueToUse() {
			stuckTicks++;
			if (stuckTicks > 100) {
				this.mob.getNavigation().stop();
			}
			return super.canContinueToUse() && stuckTicks <= 100;
		}

		@Nullable
		protected Vec3 getPosition() {
			Vec3 vec3 = null;
			if (this.mob.isInWater()) {
				vec3 = LandRandomPos.getPos(this.mob, 15, 15);
			}

			if (this.mob.getRandom().nextFloat() >= this.probability) {
				vec3 = this.getTreePos();
			}

			return vec3 == null ? super.getPosition() : vec3;
		}

		@Nullable
		private Vec3 getTreePos() {
			BlockPos blockpos = this.mob.blockPosition();
			BlockPos.MutableBlockPos above = new BlockPos.MutableBlockPos();
			BlockPos.MutableBlockPos below = new BlockPos.MutableBlockPos();

			for (BlockPos candidate : BlockPos.betweenClosed(Mth.floor(this.mob.getX() - 3.0D), Mth.floor(this.mob.getY() - 6.0D), Mth.floor(this.mob.getZ() - 3.0D), Mth.floor(this.mob.getX() + 3.0D), Mth.floor(this.mob.getY() + 6.0D), Mth.floor(this.mob.getZ() + 3.0D))) {
				if (!blockpos.equals(candidate)) {
					BlockState blockstate = this.mob.level().getBlockState(below.setWithOffset(candidate, Direction.DOWN));
					boolean isTree = blockstate.getBlock() instanceof LeavesBlock || blockstate.is(BlockTags.LOGS);
					if (isTree && this.mob.level().isEmptyBlock(candidate) && this.mob.level().isEmptyBlock(above.setWithOffset(candidate, Direction.UP))) {
						return Vec3.atBottomCenterOf(candidate);
					}
				}
			}
			return null;
		}
	}
}
