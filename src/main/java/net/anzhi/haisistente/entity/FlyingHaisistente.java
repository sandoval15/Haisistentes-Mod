package net.mcreator.haisistente.entity;

import javax.annotation.Nullable;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.animation.AnimationState;

import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.FollowOwnerGoal;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.FlyingAnimal;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.ai.navigation.PathNavigation;

import net.mcreator.haisistente.goal.FlyingFollowOwnerGoal;

import net.minecraft.core.Direction;

import net.minecraft.tags.BlockTags;

import net.minecraft.world.level.block.LeavesBlock;

import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomFlyingGoal;
import net.minecraft.world.entity.ai.util.LandRandomPos;


import net.minecraft.world.phys.HitResult;
import net.minecraft.world.level.ClipContext;
import net.minecraft.nbt.CompoundTag;

public abstract class FlyingHaisistente extends HaisistenteAbstract implements FlyingAnimal {
	protected static final EntityDataAccessor<Boolean> FLYING = SynchedEntityData.defineId(FlyingHaisistente.class, EntityDataSerializers.BOOLEAN);

	private FlyingFollowOwnerGoal flyowner;
	private FollowOwnerGoal walkowner;

	private boolean CanSwitchFly = false;
	
	public FlyingHaisistente(EntityType<? extends HaisistenteAbstract> type, Level world) {
		super(type, world);
		this.moveControl = new FlyingHaisistenteMoveControl(this);
		flyowner = new FlyingFollowOwnerGoal(this, 1.2D, (float) 10, (float) 2, true);
		walkowner = new FollowOwnerGoal(this, 1.2, (float) 10, (float) 2, false);
	}
	
	@Override
	protected void defineSynchedData() {
		super.defineSynchedData();
		this.entityData.define(FLYING, initFlying());
	}

	public boolean initFlying() {
		return true;
	}

    @Override
	protected void registerGoals() {
		super.registerGoals();
		this.goalSelector.getAvailableGoals().removeIf(
    		g -> g.getGoal() instanceof RandomStrollGoal
		);
		this.goalSelector.getAvailableGoals().removeIf(
    		g -> g.getGoal() instanceof FollowOwnerGoal
		);
		this.goalSelector.getAvailableGoals().removeIf(
    		g -> g.getGoal() instanceof HaisistenteAbstract.HaiseSleepOnOwnerGoal
		);
		this.goalSelector.addGoal(3, new HaisistenteAbstract.HaiseSleepOnOwnerGoal(this){
			private boolean lastFly;

			public void start() {
				lastFly = isFlying();
				setFlying(false);
				super.start();
			}

			public void stop() {
				setFlying(lastFly);
				super. stop();
			}
		});
		this.goalSelector.addGoal(7, new FlyingHaisistente.FlyingHaisistenteWanderGoal(this, 0.6D));
		this.goalSelector.addGoal(7, new RandomStrollGoal(this, 1){
			public boolean canUse() {
				return super.canUse() && !isFlying();
			}

			public boolean canContinueToUse() {
				return super.canContinueToUse() && !isFlying();
			}
		});
	}

   	public boolean isFlying() {
      	return this.entityData.get(FLYING);
   	}

   	public void setFlying(boolean value) {
   		if (this.entityData.get(FLYING) == value) return;
   		this.navigation.stop();
   		this.entityData.set(FLYING, value);
   		CanSwitchFly = true;
   	}

   	public void switchFly(boolean value) {
   		if (value) {
   			this.goalSelector.removeGoal(walkowner);
   			this.moveControl = new FlyingHaisistenteMoveControl(this);
   			this.navigation = (FlyingPathNavigation)createNavigation(level());
   			flyowner = new FlyingFollowOwnerGoal(this, 1.2, (float) 10, (float) 2, true);
   			this.goalSelector.addGoal(6, flyowner);
   		} else {
   			setNoGravity(false);
   			this.goalSelector.removeGoal(flyowner);
   			this.moveControl = new MoveControl(this);
   			this.navigation = new GroundPathNavigation(this, level());
   			walkowner = new FollowOwnerGoal(this, 1.2, (float) 10, (float) 2, false);
   			this.goalSelector.addGoal(6, walkowner);
   		}
   	}

   	public void whenTamed() {
   		CanSwitchFly = true;
   	}

	protected PathNavigation createNavigation(Level level) {
      	FlyingPathNavigation flyingpathnavigation = new FlyingPathNavigation(this, level);
     	flyingpathnavigation.setCanOpenDoors(false);
      	flyingpathnavigation.setCanFloat(true);
      	flyingpathnavigation.setCanPassDoors(true);
      	return flyingpathnavigation;
   	}

   	@Override
    public void tick() {
        super.tick();
    }

    protected void checkFallDamage(double p_29370_, boolean p_29371_, BlockState p_29372_, BlockPos p_29373_) {
   	}
   	
   	public boolean causeFallDamage(float distance, float damageMultiplier) {
        return false;
    }

    @Override
    public void aiStep() {
    	super.aiStep();
    	if (CanSwitchFly) {
    		CanSwitchFly = false;
    		switchFly(isFlying());
    	}
    	Vec3 vec3 = this.getDeltaMovement();
      	if (!this.onGround() && vec3.y < 0.0D && navigation.isDone()) {
      		if (isFlying() && !isOrderedToSit()) this.setDeltaMovement(vec3.multiply(1.0D, 0D, 1.0D));
      		else this.setDeltaMovement(vec3.multiply(1.0D, 0.7D, 1.0D));
      	}
    }

    @Override
   	public void addAdditionalSaveData(CompoundTag tag) {
      	super.addAdditionalSaveData(tag);
      	tag.putBoolean("fly", isFlying());
   }

    @Override
	public void readAdditionalSaveData(CompoundTag tag) {
      	super.readAdditionalSaveData(tag);
      	this.entityData.set(FLYING, tag.getBoolean("fly"));
      	switchFly(isFlying());
   	}
   
	@Override
    public PlayState MovementAnimationController(AnimationState event) {
    	 if (!this.onGround()) {
            return event.setAndContinue(RawAnimation.begin().thenLoop("idlefly"));
        } 
    	return super.MovementAnimationController(event);
    }

	static class FlyingHaisistenteMoveControl extends MoveControl {

    	private final Mob mob;
    	private final FlyingHaisistente haise;

    	public FlyingHaisistenteMoveControl(Mob mob) {
        	super(mob);
        	this.mob = mob;
        	haise = (FlyingHaisistente)mob;
    	}
    	
		@Override
		public void tick() {
    		if (this.operation != Operation.MOVE_TO) {
        		mob.setNoGravity(false);
		        return;
    		}
	
    		mob.setNoGravity(true);

    		double dx = wantedX - mob.getX();
    		double dy = wantedY - mob.getY();
    		double dz = wantedZ - mob.getZ();

    		Vec3 dir = new Vec3(dx, dy, dz);
    		double dist = dir.length();

    		if (dist < 0.25D) {
        		this.operation = Operation.WAIT;
        		mob.setDeltaMovement(mob.getDeltaMovement().scale(0.2));
        		mob.setNoGravity(false);
        		return;
    		}

		    Vec3 norm = dir.normalize();

    		norm = new Vec3(
        		norm.x,
        		Mth.clamp(norm.y, -0.3D, 0.3D),
        		norm.z
    		).normalize();

    		double speed = mob.getAttributeValue(Attributes.FLYING_SPEED);

    		double slowRadius = 1.2D;
    		double factor = dist < slowRadius
        		? Mth.clamp(dist / slowRadius, 0.05D, 1.0D)
        		: 1.0D;

    		mob.setDeltaMovement(mob.getDeltaMovement().scale(0.85D));

    		Vec3 push = norm.scale(speed * 0.15D * factor);
    		mob.setDeltaMovement(mob.getDeltaMovement().add(push));

    		if (mob.getNavigation().isInProgress()) {

        		Vec3 look = mob.getLookAngle().normalize();
        		Vec3 start = mob.position().add(0, mob.getBbHeight() * 0.3, 0);
        		Vec3 end = start.add(look.scale(0.6));

        		HitResult hit = mob.level().clip(new ClipContext(
            		start,
            		end,
            		ClipContext.Block.COLLIDER,
            		ClipContext.Fluid.NONE,
            		mob
        		));

        		boolean frontBlocked = hit.getType() == HitResult.Type.BLOCK;
        		boolean stuck = mob.horizontalCollision && mob.getDeltaMovement().horizontalDistanceSqr() < 0.005;

        		if (frontBlocked && stuck && !haise.isOrderedToSit()) {
            		mob.setDeltaMovement(
                		mob.getDeltaMovement().add(0, 0.08D, 0)
            		);
        		}
    		}

    		mob.setYRot(
        		rotlerp(
            		mob.getYRot(),
            		(float)(Mth.atan2(norm.z, norm.x) * (180F / Math.PI)) - 90F,
            		10F
        		)
    		);
    		mob.yBodyRot = mob.getYRot();
		}
	}

	static class FlyingHaisistenteWanderGoal extends WaterAvoidingRandomFlyingGoal {
		private int stuckTicks;
		private FlyingHaisistente haise;
		
    	public FlyingHaisistenteWanderGoal(PathfinderMob p_186224_, double p_186225_) {
         	super(p_186224_, p_186225_);
         	haise = (FlyingHaisistente)p_186224_;
   		}

   		@Override
   		public void start() {
   			stuckTicks = 0;
   			super.start();
   		}

   		public boolean canUse() {
   			return super.canUse() && haise.isFlying();
   		}

   		@Override
   		public boolean canContinueToUse() {
   			stuckTicks++;

			if (stuckTicks > 100) {
    			this.mob.getNavigation().stop();
			}
   			return super.canContinueToUse() && stuckTicks <= 100 && haise.isFlying(); 
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
         	BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();
         	BlockPos.MutableBlockPos blockpos$mutableblockpos1 = new BlockPos.MutableBlockPos();

         	for(BlockPos blockpos1 : BlockPos.betweenClosed(Mth.floor(this.mob.getX() - 3.0D), Mth.floor(this.mob.getY() - 6.0D), Mth.floor(this.mob.getZ() - 3.0D), Mth.floor(this.mob.getX() + 3.0D), Mth.floor(this.mob.getY() + 6.0D), Mth.floor(this.mob.getZ() + 3.0D))) {
            	if (!blockpos.equals(blockpos1)) {
               		BlockState blockstate = this.mob.level().getBlockState(blockpos$mutableblockpos1.setWithOffset(blockpos1, Direction.DOWN));
               		boolean flag = blockstate.getBlock() instanceof LeavesBlock || blockstate.is(BlockTags.LOGS);
               		if (flag && this.mob.level().isEmptyBlock(blockpos1) && this.mob.level().isEmptyBlock(blockpos$mutableblockpos.setWithOffset(blockpos1, Direction.UP))) {
                  		return Vec3.atBottomCenterOf(blockpos1);
               		}
            	}
         	}
         return null;
      	}
   	}
}
