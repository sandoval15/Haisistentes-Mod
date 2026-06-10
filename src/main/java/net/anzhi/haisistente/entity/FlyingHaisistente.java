package net.anzhi.haisistente.entity;

import javax.annotation.Nullable;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.FollowOwnerGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomFlyingGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
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
 * Flying Haisistente base with two real movement modes:
 * - WALK (default): ground navigation, walks like any mob.
 * - FLY: air navigation with smooth takeoffs, used to catch up with the
 *   owner, cross obstacles, or wander to a tree perch.
 *
 * All flight mechanics (mode decision, approach speed, climb physics) live
 * here so every goal and future flying entity reuses the same behavior
 * through {@link #shouldFly}, {@link #flightSpeedFactor},
 * {@link #walkSpeedFactor} and {@link #setFlightMode}.
 */
public abstract class FlyingHaisistente extends HaisistenteAbstract implements FlyingAnimal {

	/** Walking distance band: beyond this it takes off even at a calm pace. */
	public static final double TAKE_OFF_DISTANCE = 14.0D;
	/** When the target moves fast (sprint/ride/glide), take off sooner. */
	public static final double SPRINT_TAKE_OFF_DISTANCE = 8.0D;
	/** Close enough to land and continue on foot. */
	public static final double LAND_DISTANCE = 6.0D;
	/** Height difference that walking cannot solve. */
	public static final double VERTICAL_GAP_FOR_FLIGHT = 4.0D;
	/** Ticks without progress on foot before resorting to flight. */
	public static final int WALK_STUCK_TICKS_FOR_FLIGHT = 40;

	/** Climb rate cap so takeoffs ramp up instead of launching. */
	private static final double MAX_CLIMB_SPEED = 0.3D;
	/** Upward nudge to hop low obstacles the flight path clips. */
	private static final double OBSTACLE_HOP_BOOST = 0.1D;
	/** Max approach-speed multipliers, scaled down with proximity. */
	private static final double MAX_FLIGHT_SPEED_FACTOR = 2.4D;
	private static final double MAX_WALK_SPEED_FACTOR = 1.25D;

	/**
	 * Beyond this distance, pathfinding is skipped and the mob steers its
	 * velocity straight at the target: node-based navigation caps effective
	 * speed far below what a real dive should feel like.
	 */
	public static final double DIRECT_PURSUIT_DISTANCE = 8.0D;
	/** Pursuit cruise speed in blocks/tick (~14 m/s, ~2.5x player sprint). */
	private static final double PURSUIT_SPEED = 0.7D;
	/** Velocity decay per tick during pursuit; with the matching acceleration
	 *  below it converges on PURSUIT_SPEED. */
	private static final double PURSUIT_DRAG = 0.9D;
	private static final double PURSUIT_ACCEL = PURSUIT_SPEED * (1.0D - PURSUIT_DRAG) / PURSUIT_DRAG;

	private GroundPathNavigation groundNav;
	private FlyingPathNavigation airNav;
	private boolean flightMode;

	public FlyingHaisistente(EntityType<? extends HaisistenteAbstract> type, Level world) {
		super(type, world);
		this.moveControl = new MoveControl(this);
		this.setMaxUpStep(1.0f);
	}

	@Override
	protected PathNavigation createNavigation(Level level) {
		this.groundNav = new GroundPathNavigation(this, level);
		return this.groundNav;
	}

	public boolean isFlightMode() {
		return this.flightMode;
	}

	public void setFlightMode(boolean fly) {
		if (this.flightMode == fly) {
			return;
		}
		this.flightMode = fly;
		this.navigation.stop();
		if (fly) {
			if (this.airNav == null) {
				this.airNav = new FlyingPathNavigation(this, level());
				this.airNav.setCanOpenDoors(false);
				this.airNav.setCanFloat(true);
				this.airNav.setCanPassDoors(true);
			}
			this.moveControl = new SmoothFlightMoveControl(this);
			this.navigation = this.airNav;
		} else {
			this.setNoGravity(false);
			this.moveControl = new MoveControl(this);
			this.navigation = this.groundNav;
		}
	}

	/**
	 * Central walk-vs-fly decision, with hysteresis so the mode does not
	 * flap: taking off and landing use different thresholds.
	 */
	public boolean shouldFly(LivingEntity target, double distance, boolean targetMovingFast, int stuckTicks) {
		boolean targetAirborne = !target.onGround() && !target.isInWater();
		boolean bigVerticalGap = Math.abs(target.getY() - this.getY()) > VERTICAL_GAP_FOR_FLIGHT;

		if (this.flightMode) {
			// Stay airborne while the situation that caused the takeoff lasts
			return targetAirborne || bigVerticalGap || targetMovingFast || distance > LAND_DISTANCE;
		}
		// On foot: only take off when walking genuinely cannot keep up
		return targetAirborne
				|| bigVerticalGap
				|| distance > TAKE_OFF_DISTANCE
				|| (targetMovingFast && distance > SPRINT_TAKE_OFF_DISTANCE)
				|| stuckTicks > WALK_STUCK_TICKS_FOR_FLIGHT;
	}

	/** Flight speed multiplier: calm nearby, fast when closing distance. */
	public double flightSpeedFactor(double distance) {
		return Mth.clamp(1.0D + (distance - LAND_DISTANCE) * 0.12D, 1.0D, MAX_FLIGHT_SPEED_FACTOR);
	}

	/**
	 * Direct flight: steers velocity straight at the target, ignoring path
	 * nodes. Aims slightly above it so terrain clips less; the collision hop
	 * in {@link SmoothFlightMoveControl} covers the rest.
	 */
	public void flyDirectlyTowards(LivingEntity target) {
		this.getNavigation().stop();
		Vec3 aim = target.position().add(0.0D, 1.5D, 0.0D);
		Vec3 dir = aim.subtract(this.position()).normalize();
		Vec3 velocity = this.getDeltaMovement().scale(PURSUIT_DRAG).add(dir.scale(PURSUIT_ACCEL));
		this.setDeltaMovement(velocity);

		float yaw = (float) (Mth.atan2(velocity.z, velocity.x) * (180F / (float) Math.PI)) - 90.0F;
		this.setYRot(yaw);
		this.yBodyRot = yaw;
	}

	/** Walk speed multiplier: light jog when the owner pulls ahead. */
	public double walkSpeedFactor(double distance) {
		return Mth.clamp(1.0D + (distance - 10.0D) * 0.05D, 1.0D, MAX_WALK_SPEED_FACTOR);
	}

	@Override
	protected void registerGoals() {
		super.registerGoals();
		this.goalSelector.getAvailableGoals().removeIf(
			g -> g.getGoal() instanceof FollowOwnerGoal
		);
		this.goalSelector.addGoal(6, new FlyingFollowOwnerGoal(this, 1.1D, (float) 10, (float) 2, true));
		this.goalSelector.addGoal(7, new FlyingHaisistenteWanderGoal(this, 0.6D));
	}

	@Override
	public boolean isFlying() {
		return !this.onGround();
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
		// Glide: slow descents so it lands gently instead of dropping
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

	/**
	 * Flight control with believable physics: hovers in place, ramps climbs
	 * instead of rocketing upward, and hops low obstacles on contact.
	 */
	static class SmoothFlightMoveControl extends FlyingMoveControl {
		SmoothFlightMoveControl(Mob mob) {
			super(mob, 10, true);
		}

		@Override
		public void tick() {
			super.tick();
			Vec3 delta = this.mob.getDeltaMovement();
			if (delta.y > MAX_CLIMB_SPEED) {
				this.mob.setDeltaMovement(delta.x, MAX_CLIMB_SPEED, delta.z);
			}
			if (this.mob.horizontalCollision && this.operation == Operation.MOVE_TO) {
				this.mob.setDeltaMovement(this.mob.getDeltaMovement().add(0.0D, OBSTACLE_HOP_BOOST, 0.0D));
			}
		}
	}

	static class FlyingHaisistenteWanderGoal extends WaterAvoidingRandomFlyingGoal {
		private final FlyingHaisistente haise;
		private int stuckTicks;

		public FlyingHaisistenteWanderGoal(FlyingHaisistente mob, double speed) {
			super(mob, speed);
			this.haise = mob;
		}

		@Override
		public void start() {
			stuckTicks = 0;
			this.haise.setFlightMode(true);
			super.start();
		}

		@Override
		public void stop() {
			super.stop();
			this.haise.setFlightMode(false);
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
