package net.anzhi.haisistente.goal;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import net.minecraft.world.phys.Vec3;

import net.anzhi.haisistente.entity.FlyingHaisistente;

import java.util.EnumSet;

/**
 * Owner-follow for flying Haisistentes with two movement modes:
 * - The owner walks and the pet is reasonably close -> it WALKS behind them.
 * - The owner sprints, glides, rides, or the pet falls behind or the terrain
 *   blocks the way -> it FLIES, fast, to catch up; then lands and walks again.
 * Teleporting only happens as a last resort when the owner is so far away
 * the pet would otherwise be lost.
 */
public class FlyingFollowOwnerGoal extends Goal {
	private static final double TELEPORT_DISTANCE_SQR = 48.0D * 48.0D;
	private static final double TAKE_OFF_DISTANCE = 14.0D;
	private static final double LAND_DISTANCE = 6.0D;
	private static final double VERTICAL_GAP_FOR_FLIGHT = 4.0D;
	private static final int PATH_RECALC_TICKS = 10;
	private static final int WALK_STUCK_TICKS_FOR_FLIGHT = 40;
	private static final int FLY_STUCK_TICKS_FOR_ASSIST = 20;

	private final FlyingHaisistente mob;
	private final LevelReader world;
	private final double baseSpeed;
	private final float startDist;
	private final float stopDist;
	private final boolean teleportToLeaves;
	private LivingEntity owner;
	private int recalcCooldown;
	private int stuckTicks;
	private Vec3 lastPos = Vec3.ZERO;
	private float oldWaterCost;

	public FlyingFollowOwnerGoal(FlyingHaisistente mob, double speed, float startDist, float stopDist, boolean teleportToLeaves) {
		this.mob = mob;
		this.world = mob.level();
		this.baseSpeed = speed;
		this.startDist = startDist;
		this.stopDist = stopDist;
		this.teleportToLeaves = teleportToLeaves;
		this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
	}

	@Override
	public boolean canUse() {
		LivingEntity livingentity = this.mob.getOwner();
		if (livingentity == null || livingentity.isSpectator()) {
			return false;
		} else if (this.mob.isOrderedToSit()) {
			return false;
		} else if (this.mob.distanceToSqr(livingentity) < (double) (this.startDist * this.startDist) || isInCombat()) {
			return false;
		} else {
			this.owner = livingentity;
			return true;
		}
	}

	@Override
	public boolean canContinueToUse() {
		if (this.mob.isOrderedToSit() || isInCombat()) {
			return false;
		}
		return this.mob.distanceToSqr(this.owner) > (double) (this.stopDist * this.stopDist);
	}

	private boolean isInCombat() {
		Entity ownerEntity = this.mob.getOwner();
		if (ownerEntity != null) {
			return this.mob.distanceTo(ownerEntity) < 30 && this.mob.getTarget() != null && this.mob.getTarget().isAlive();
		}
		return false;
	}

	@Override
	public boolean requiresUpdateEveryTick() {
		return true;
	}

	@Override
	public void start() {
		this.recalcCooldown = 0;
		this.stuckTicks = 0;
		this.lastPos = this.mob.position();
		this.oldWaterCost = this.mob.getPathfindingMalus(BlockPathTypes.WATER);
		this.mob.setPathfindingMalus(BlockPathTypes.WATER, 0.0F);
	}

	@Override
	public void stop() {
		LivingEntity lastOwner = this.owner;
		this.owner = null;
		this.mob.getNavigation().stop();
		this.mob.setPathfindingMalus(BlockPathTypes.WATER, this.oldWaterCost);
		// Land only if the owner is grounded; if they are hovering mid-air,
		// stay airborne next to them instead of dropping out of the sky
		if (lastOwner == null || lastOwner.onGround()) {
			this.mob.setFlightMode(false);
		}
	}

	@Override
	public void tick() {
		this.mob.getLookControl().setLookAt(this.owner, 10.0F, (float) this.mob.getMaxHeadXRot());
		if (this.mob.isLeashed() || this.mob.isPassenger()) {
			return;
		}

		double dist = this.mob.distanceTo(this.owner);
		trackStuckness();

		boolean ownerIsFast = this.owner.isSprinting() || this.owner.isFallFlying() || this.owner.getVehicle() != null || !this.owner.onGround();
		boolean bigVerticalGap = Math.abs(this.owner.getY() - this.mob.getY()) > VERTICAL_GAP_FOR_FLIGHT;

		boolean fly;
		if (this.mob.isFlightMode()) {
			// Stay airborne until the owner slows down and we are close again
			fly = ownerIsFast || dist > LAND_DISTANCE || bigVerticalGap;
		} else {
			fly = ownerIsFast || dist > TAKE_OFF_DISTANCE || bigVerticalGap || this.stuckTicks > WALK_STUCK_TICKS_FOR_FLIGHT;
		}
		this.mob.setFlightMode(fly);

		if (--this.recalcCooldown <= 0) {
			this.recalcCooldown = PATH_RECALC_TICKS;
			if (dist * dist >= TELEPORT_DISTANCE_SQR) {
				tryToTeleportNearEntity();
				return;
			}
			double speed;
			if (fly) {
				// Fast flight; even faster when left well behind
				speed = dist > 10.0D ? this.baseSpeed * 2.2D : this.baseSpeed * 1.6D;
			} else {
				speed = dist > 10.0D ? this.baseSpeed * 1.2D : this.baseSpeed;
			}
			this.mob.getNavigation().moveTo(this.owner, speed);
		}

		// Flight assist: airborne but pathfinding can't make progress
		// (dense leaves, odd geometry) -> steer straight at the owner.
		if (fly && this.stuckTicks > FLY_STUCK_TICKS_FOR_ASSIST) {
			Vec3 target = this.owner.position().add(0.0D, 1.0D, 0.0D);
			Vec3 dir = target.subtract(this.mob.position()).normalize();
			Vec3 assist = this.mob.getDeltaMovement().scale(0.7D).add(dir.scale(0.15D)).add(0.0D, 0.06D, 0.0D);
			this.mob.setDeltaMovement(assist);
		}
	}

	private void trackStuckness() {
		if (this.mob.position().distanceToSqr(this.lastPos) < 0.01D) {
			this.stuckTicks++;
		} else {
			this.stuckTicks = 0;
		}
		this.lastPos = this.mob.position();
	}

	private void tryToTeleportNearEntity() {
		BlockPos blockpos = this.owner.blockPosition();
		for (int i = 0; i < 10; ++i) {
			int x = this.getRandomNumber(-3, 3);
			int y = this.getRandomNumber(-1, 1);
			int z = this.getRandomNumber(-3, 3);
			if (this.tryToTeleportToLocation(blockpos.getX() + x, blockpos.getY() + y, blockpos.getZ() + z)) {
				return;
			}
		}
	}

	private boolean tryToTeleportToLocation(int x, int y, int z) {
		if (Math.abs((double) x - this.owner.getX()) < 2.0D && Math.abs((double) z - this.owner.getZ()) < 2.0D) {
			return false;
		} else if (!this.isTeleportFriendlyBlock(new BlockPos(x, y, z))) {
			return false;
		} else {
			this.mob.moveTo((double) x + 0.5D, (double) y, (double) z + 0.5D, this.mob.getYRot(), this.mob.getXRot());
			this.mob.getNavigation().stop();
			return true;
		}
	}

	private boolean isTeleportFriendlyBlock(BlockPos pos) {
		if (this.world.getBlockState(pos).isAir()) {
			BlockPos offset = pos.subtract(this.mob.blockPosition());
			return this.world.noCollision(this.mob, this.mob.getBoundingBox().move(offset));
		}
		BlockPathTypes pathType = WalkNodeEvaluator.getBlockPathTypeStatic(this.world, pos.mutable());
		if (pathType != BlockPathTypes.WALKABLE) {
			return false;
		}
		BlockState below = this.world.getBlockState(pos.below());
		if (!this.teleportToLeaves && below.getBlock() instanceof LeavesBlock) {
			return false;
		}
		BlockPos offset = pos.subtract(this.mob.blockPosition());
		return this.world.noCollision(this.mob, this.mob.getBoundingBox().move(offset));
	}

	private int getRandomNumber(int min, int max) {
		return this.mob.getRandom().nextInt(max - min + 1) + min;
	}
}
