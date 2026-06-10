package net.anzhi.haisistente.entity;

import javax.annotation.Nullable;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
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
	/** In combat, take off sooner when ground pathing stalls. */
	public static final int COMBAT_STUCK_TICKS_FOR_FLIGHT = 20;

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
	/** Velocity decay per tick during pursuit; together with the
	 *  per-tick acceleration it converges on the cruise speed. */
	private static final double PURSUIT_DRAG = 0.9D;
	/** In combat, dive directly at the target while outside melee reach. */
	private static final double COMBAT_PURSUIT_MIN_DISTANCE = 2.5D;

	private GroundPathNavigation groundNav;
	private FlyingPathNavigation airNav;
	private boolean flightMode;
	private boolean combatFlight;
	private int combatStuckTicks;
	private Vec3 lastCombatPos = Vec3.ZERO;

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
	 * Direct flight: steers velocity straight at the aim point, ignoring path
	 * nodes. Cruise speed scales down with proximity so it converges on the
	 * target instead of overshooting and orbiting it. The collision hop in
	 * {@link SmoothFlightMoveControl} covers terrain clips.
	 *
	 * @param aimHeight offset above the target's feet: high for travel so
	 *                  terrain clips less, low for closing into melee reach
	 */
	public void flyDirectlyTowards(LivingEntity target, double aimHeight) {
		if (!this.getNavigation().isDone()) {
			this.getNavigation().stop();
		}
		Vec3 toAim = target.position().add(0.0D, aimHeight, 0.0D).subtract(this.position());
		double distance = toAim.length();
		if (distance < 0.05D) {
			return;
		}
		double cruise = Math.min(PURSUIT_SPEED, distance * 0.25D);
		double accel = cruise * (1.0D - PURSUIT_DRAG) / PURSUIT_DRAG;
		Vec3 velocity = this.getDeltaMovement().scale(PURSUIT_DRAG).add(toAim.normalize().scale(accel));
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
		updateCombatFlight();
		// Glide on idle descents only, so landings are gentle. Never while
		// navigating or fighting: dives must stay fast.
		Vec3 delta = this.getDeltaMovement();
		if (!this.onGround() && delta.y < 0.0D && this.getTarget() == null && this.getNavigation().isDone()) {
			this.setDeltaMovement(delta.multiply(1.0D, 0.6D, 1.0D));
		}
	}

	/**
	 * Combat goals move the mob but never pick a movement mode, so a ground
	 * path to an unreachable target leaves it hopping at an edge. While a
	 * target exists, decide walk-vs-fly here; land again once combat ends.
	 * Stuck detection is horizontal-only: jumping in place must still count
	 * as no progress.
	 */
	private void updateCombatFlight() {
		if (this.level().isClientSide()) {
			return;
		}
		LivingEntity target = this.getTarget();
		if (target == null || !target.isAlive()) {
			if (this.combatFlight) {
				this.combatFlight = false;
				this.setFlightMode(false);
			}
			this.combatStuckTicks = 0;
			return;
		}

		double dx = this.getX() - this.lastCombatPos.x;
		double dz = this.getZ() - this.lastCombatPos.z;
		this.combatStuckTicks = (dx * dx + dz * dz < 0.01D) ? this.combatStuckTicks + 1 : 0;
		this.lastCombatPos = this.position();

		double distance = this.distanceTo(target);
		boolean fly = this.shouldFly(target, distance, false, this.combatStuckTicks)
				|| this.combatStuckTicks > COMBAT_STUCK_TICKS_FOR_FLIGHT;
		if (fly != this.isFlightMode()) {
			this.setFlightMode(fly);
			this.combatFlight = fly;
		}

		// Airborne chase: dive straight at the target (path nodes orbit
		// around small fast flyers like bats); melee lands on contact
		if (fly && distance > COMBAT_PURSUIT_MIN_DISTANCE) {
			this.flyDirectlyTowards(target, 0.25D);
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
	 * Flight control with believable physics: hovers in place, hops low
	 * obstacles on contact, and always caps the climb rate LAST so no
	 * combination of pushes can stack into a runaway ascent.
	 */
	static class SmoothFlightMoveControl extends FlyingMoveControl {
		SmoothFlightMoveControl(Mob mob) {
			super(mob, 10, true);
		}

		@Override
		public void tick() {
			super.tick();
			if (this.mob.horizontalCollision) {
				this.mob.setDeltaMovement(this.mob.getDeltaMovement().add(0.0D, OBSTACLE_HOP_BOOST, 0.0D));
			}
			Vec3 delta = this.mob.getDeltaMovement();
			if (delta.y > MAX_CLIMB_SPEED) {
				this.mob.setDeltaMovement(delta.x, MAX_CLIMB_SPEED, delta.z);
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
