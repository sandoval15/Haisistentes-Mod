package net.anzhi.haisistente.entity;

import net.anzhi.haisistente.entity.flag.FrameFlag;
import net.anzhi.haisistente.entity.flag.States;
import software.bernie.geckolib.util.GeckoLibUtil;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animatable.GeoEntity;
import net.minecraft.core.BlockPos;
import javax.annotation.Nullable;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;

import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.network.NetworkHooks;

import net.minecraft.world.level.Level;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.entity.player.Player;
import java.util.UUID;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.FollowOwnerGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.JukeboxBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.EnumSet;

import net.anzhi.haisistente.init.HaisistenteItems;
import net.anzhi.haisistente.HaisistenteMod;

import net.minecraft.world.item.Items;

import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.phys.AABB;

import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.SpawnGroupData;

import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.effect.MobEffectInstance;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.EnumMap;

import net.anzhi.haisistente.entity.flag.FrameFlagPacket;

public abstract class HaisistenteAbstract extends TamableAnimal implements GeoEntity {
	private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
	private boolean swinging;
	private long lastSwing;
	public AnimationState<HaisistenteAbstract> animationState = null;
	private static final EntityDataAccessor<Boolean> SIT = SynchedEntityData.defineId(HaisistenteAbstract.class, EntityDataSerializers.BOOLEAN);

	public record StateValues(EntityDataAccessor<Boolean> accessor, String animation) {}
	private static final Map<States, StateValues> STATES = new EnumMap<>(States.class);
	private static final EntityDataAccessor<Boolean> EAT = SynchedEntityData.defineId(HaisistenteAbstract.class, EntityDataSerializers.BOOLEAN);
	private static final EntityDataAccessor<Boolean> DANCE = SynchedEntityData.defineId(HaisistenteAbstract.class, EntityDataSerializers.BOOLEAN);
	private static final EntityDataAccessor<Boolean> SLEEP = SynchedEntityData.defineId(HaisistenteAbstract.class, EntityDataSerializers.BOOLEAN);

	static {
    	STATES.put(States.EAT, new StateValues(EAT,"eat"));
    	STATES.put(States.DANCE, new StateValues(DANCE,"dance"));
    	STATES.put(States.SLEEP, new StateValues(SLEEP,"sleep"));
	}
	
	private static final Map<FrameFlag, EntityDataAccessor<Boolean>> FRAME_FLAGS = new EnumMap<>(FrameFlag.class);
	private static final EntityDataAccessor<Boolean> FRAME_HIT = SynchedEntityData.defineId(HaisistenteAbstract.class, EntityDataSerializers.BOOLEAN);
	private static final EntityDataAccessor<Boolean> FRAME_START_IDLE = SynchedEntityData.defineId(HaisistenteAbstract.class, EntityDataSerializers.BOOLEAN);
	private static final EntityDataAccessor<Boolean> FRAME_START_EAT = SynchedEntityData.defineId(HaisistenteAbstract.class, EntityDataSerializers.BOOLEAN);
	private static final EntityDataAccessor<Boolean> FRAME_END_EAT = SynchedEntityData.defineId(HaisistenteAbstract.class, EntityDataSerializers.BOOLEAN);
	
	static {
    	FRAME_FLAGS.put(FrameFlag.FRAME_HIT, FRAME_HIT);
    	FRAME_FLAGS.put(FrameFlag.FRAME_START_IDLE, FRAME_START_IDLE);
    	FRAME_FLAGS.put(FrameFlag.FRAME_START_EAT, FRAME_START_EAT);
    	FRAME_FLAGS.put(FrameFlag.FRAME_END_EAT, FRAME_END_EAT);
	}
	
	public int danceType = 1;
	public final int maxDances = 6;
	public UUID tamer = null;
	
	@Nullable
   	public JukeboxBlockEntity jukebox;

	public HaisistenteAbstract(EntityType<? extends HaisistenteAbstract> type, Level world) {
		super(type, world);
		xpReward = 0;
		setNoAi(false);
		setMaxUpStep(0.6f);
	}

	@Override
	protected void defineSynchedData() {
		super.defineSynchedData();
		
		this.entityData.define(SIT, false);
		
		//STATES
		for (StateValues acc : STATES.values()) {
        	this.entityData.define(acc.accessor(), false);
    	}
		
		//FRAMES_FLAGS
		for (EntityDataAccessor<Boolean> acc : FRAME_FLAGS.values()) {
        	this.entityData.define(acc, false);
    	}
	}

	public String getTexture() {
		return "";
	}

	public String getModel() {
		return "";
	}

	public String getGeoAnimation() {
		return "";
	}

	public List<String> getAnimationsWithHeadRotation() {
		ArrayList<String> list = new ArrayList<>(List.of("eat", "swinmove", "backflip", "sleep"));
		return list;
	}

	public List<Item> getEdibleItems() { 
		ArrayList<Item> list = new ArrayList<>(List.of(HaisistenteItems.GOLDEN_BAMBOO.get(),Blocks.BAMBOO.asItem()));
		return list;
	}

	public void setEating() {
    	changeState(States.EAT);
	}

	public void setDancing() {
    	changeState(States.DANCE);
	}

	public void setSleeping() {
    	changeState(States.SLEEP);
	}

	public boolean isEating() {
    	return getState(States.EAT);
	}

	public boolean isDancing() {
    	return getState(States.DANCE);
	}

	public boolean isSleepingOnOwner() {
    	return getState(States.SLEEP);
	}

	public boolean isSitting() {
		return this.entityData.get(SIT);
	}
	
	public void setState(States state, boolean value) {
    	this.entityData.set(STATES.get(state).accessor(), value);
	}

	public void changeState(States state) {
		clearState();
		setState(state, true);
	}

	public boolean getState(States state) {
    	return this.entityData.get(STATES.get(state).accessor());
	}

	public States getCurrentState() {
		for (States state : STATES.keySet()) {
        	if (getState(state)) return state;
    	}
    	return States.NONE;
	}

	public String getAnimation(States state) {
		if (state == States.NONE) return "none";
		return STATES.get(state).animation();
	}

	public boolean clearState(){
		if (getCurrentState() != States.NONE) {
			setState(getCurrentState(), false);
			return true;
		}
		return false;
	}

	public void setFrameFlag(FrameFlag flag, boolean value) {
    	this.entityData.set(FRAME_FLAGS.get(flag), value);
	}

	public boolean getFrameFlag(FrameFlag flag) {
    	return this.entityData.get(FRAME_FLAGS.get(flag));
	}

	public boolean consumeFrameFlag(FrameFlag flag) {
		boolean val = getFrameFlag(flag);
		if (val) {
			setFrameFlag(flag, false);
		}
		return val;
	}
	
	@Override
	public void setOrderedToSit(boolean value) {
      super.setOrderedToSit(value);
      this.entityData.set(SIT, value);
   }

	@Override
	public Packet<ClientGamePacketListener> getAddEntityPacket() {
		return NetworkHooks.getEntitySpawningPacket(this);
	}

	@Override
	protected void registerGoals() {
		this.goalSelector.addGoal(1, new FloatGoal(this));
		this.goalSelector.addGoal(1, new PanicGoal(this, 1.2) {
			@Override
			public boolean canUse() {
				return super.canUse() && !isTame();
			}
		});
		this.goalSelector.addGoal(1, new HaisistenteAbstract.HaiseEatGoal(this));
		this.goalSelector.addGoal(2, new HaisistenteAbstract.HaisistenteSitWhenOrderedToGoal(this));
		this.goalSelector.addGoal(3, new HaisistenteAbstract.HaiseSleepOnOwnerGoal(this));
		this.goalSelector.addGoal(4, new HaisistenteAbstract.HaiseDanceGoal(this));
		this.goalSelector.addGoal(5, new MeleeAttackGoal(this, 1.2, false) {
			@Override
			public void stop() {
				super.stop();
				setFrameFlag(FrameFlag.FRAME_HIT, false);
			}
	
			@Override
    		protected void checkAndPerformAttack(LivingEntity target, double distanceSq) {
    			double d0 = this.getAttackReachSqr(target);
      			if (distanceSq <= d0 && getTicksUntilNextAttack() <= 0) {
      				this.mob.swing(InteractionHand.MAIN_HAND);
      			}
      			if (consumeFrameFlag(FrameFlag.FRAME_HIT)){
					this.resetAttackCooldown();
					if (distanceSq <= d0) this.mob.doHurtTarget(target);
				}
   			}

			@Override
			protected double getAttackReachSqr(LivingEntity entity) {
				double reach = (this.mob.getBbWidth() * 2.0F) + getReach();
				return reach * reach + entity.getBbWidth();
			}
		});
		this.goalSelector.addGoal(6, new FollowOwnerGoal(this, 1.2, (float) 10, (float) 2, false));
		this.goalSelector.addGoal(7, new RandomStrollGoal(this, 1));
		this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0F));
		this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
		this.targetSelector.addGoal(1, new OwnerHurtByTargetGoal(this) {
			@Override
			public boolean canUse() {
				boolean cantarget = true;
				Entity entity = HaisistenteAbstract.this;
				
				if (HaisistenteAbstract.this.isTame() && getOwner() != null){
					if (getOwner().getLastHurtByMob() instanceof TamableAnimal ta){
						cantarget = !ta.isOwnedBy(getOwner());
					}
				}
				return super.canUse() && !isEating() && cantarget;
			}
		});
		this.targetSelector.addGoal(2, new OwnerHurtTargetGoal(this) {
			@Override
			public boolean canUse() {
				boolean cantarget = true;
				Entity entity = HaisistenteAbstract.this;
				if (HaisistenteAbstract.this.isTame() && getOwner() != null){
					if (getOwner().getLastHurtMob() instanceof TamableAnimal ta){
						cantarget = !ta.isOwnedBy(getOwner());
					}
				}
				return super.canUse() && !isEating() && cantarget;
			}
		});
	}

	@Override
	public boolean isBaby() {
    	return false;
	}

	@Override
	public void setBaby(boolean baby) {}
	
	@Override
	public int getAge() {
    	return 0;
	}

	@Override
	public void setAge(int age) {}

	public boolean isShaking() {
		return false;
	}

	public double getReach() {
		return 0.6D;
	}

	public double getSqrReach() {
		return getReach() * getReach();
	}

	@Override
	public MobType getMobType() {
		return MobType.UNDEFINED;
	}

	@Override
	public SoundEvent getHurtSound(DamageSource ds) {
		return ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("entity.generic.hurt"));
	}

	@Override
	public SoundEvent getDeathSound() {
		return ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("entity.generic.death"));
	}

	@Override
	public boolean hurt(DamageSource source, float amount) {
		return super.hurt(source, amount);
	}

	@Override
	public void die(DamageSource source) {
		super.die(source);
	}

	public InteractionResult customInteract(Player player, InteractionHand hand) {
		return null;
	}

	@Override
	public InteractionResult mobInteract(Player player, InteractionHand hand) {
		ItemStack itemstack = player.getItemInHand(hand);
		InteractionResult retval = InteractionResult.sidedSuccess(this.level().isClientSide());
		Item item = itemstack.getItem();
		if (this.level().isClientSide()) {
			retval = (this.isTame() && this.isOwnedBy(player) || this.isFood(itemstack)) ? InteractionResult.CONSUME : InteractionResult.PASS;
		} else {
			InteractionResult custom = customInteract(player, hand);
			if (custom != null) return custom;
			if (isFood(player.getMainHandItem())) {
				if (canEat()) {
					giveFood(player);
					this.setPersistenceRequired();
        			return InteractionResult.SUCCESS;
				}
				else {
					return InteractionResult.PASS;
				}
			}
			if (canSit()){
		    	InteractionResult interactionresult = super.mobInteract(player, hand);
            	if ((!interactionresult.consumesAction() || this.isBaby()) && this.isOwnedBy(player)) {
               		this.setOrderedToSit(!this.isOrderedToSit());
               		this.jumping = false;
              	 	this.navigation.stop();
               		this.setTarget((LivingEntity)null);
               		return InteractionResult.SUCCESS;
            	} else {
               		return interactionresult;
		    	}
			} else {
				retval = super.mobInteract(player, hand);
				if (retval == InteractionResult.SUCCESS || retval == InteractionResult.CONSUME)
					this.setPersistenceRequired();
			}
		}
		return retval;
	}

	public boolean canEat() {
		return !isEating() && !isOrderedToSit();
	}

	public boolean canSit() {
		return !isEating() && this.isTame();
	}
	
	private void giveFood(Player player){
		ItemStack foodStack = player.getMainHandItem().copy();
		foodStack.setCount(1);
		this.setItemInHand(InteractionHand.MAIN_HAND, foodStack);
		player.getInventory().setChanged();
		player.getMainHandItem().shrink(1);
		setEating();
        this.jumping = false;
        this.navigation.stop();
        this.setTarget((LivingEntity)null);
        tamer = player.getUUID();
	}

	public void spawnEatingParticles(){
		ItemStack itemStack = this.getItemInHand(InteractionHand.MAIN_HAND);
		if (itemStack.isEmpty()) return;
		if (!getFrameFlag(FrameFlag.FRAME_START_EAT)) return;
    	if (this.level() instanceof ServerLevel server) {
        	for (int i = 0; i < 3; i++) {
				double ox = (server.random.nextDouble() - 0.5D) * 0.1D;
				double oy = server.random.nextDouble() * 0.1D + 0.05D;
				double oz = (server.random.nextDouble() - 0.5D) * 0.1D;
				float Yaw = this.yBodyRot * ((float)Math.PI / 180F);
            	
            	server.sendParticles(
                	new ItemParticleOption(ParticleTypes.ITEM, itemStack),
                	this.getX() - Math.sin(Yaw)*0.4,
                	this.getY() + this.getBbHeight() * 0.55,
                	this.getZ() + Math.cos(Yaw)*0.4,
                	1,
                	ox, oy, oz,
                	0.05
            	);
	        }
    	}
	}

	public void tryTame() {
		if (!this.isTame()){
         	if (((this.random.nextInt(3) == 0 && this.getMainHandItem().getItem() == Blocks.BAMBOO.asItem()) || this.getMainHandItem().getItem() == HaisistenteItems.GOLDEN_BAMBOO.get()) 
         	&& !net.minecraftforge.event.ForgeEventFactory.onAnimalTame(this, level().getPlayerByUUID(this.tamer))) {
            	this.tame(level().getPlayerByUUID(this.tamer));
            	this.navigation.stop();
            	this.setTarget((LivingEntity)null);
            	this.setOrderedToSit(false);
            	this.whenTamed();
            	this.level().broadcastEntityEvent(this, (byte)7);
         	} else if (this.getMainHandItem().getItem() == Blocks.BAMBOO.asItem()){
            	this.level().broadcastEntityEvent(this, (byte)6);
         	}
		}
	}

	public void whenTamed(){
		
	}

	public void whenFeeding(){
		
	}
	
	@Override
	public void awardKillScore(Entity entity, int score, DamageSource damageSource) {
		super.awardKillScore(entity, score, damageSource);
	}

	@Override
	public void baseTick() {
		super.baseTick();
		this.refreshDimensions();
	}

	public EntityDimensions getCustomDimensions(Pose pose) {
		if (isSitting()){
			return EntityDimensions.fixed(0.65F, 1.1F);
		} else if (isSleepingOnOwner()){
			return EntityDimensions.fixed(0.8F, 0.6F);
		} else if (isInWaterOrBubble()){
			return EntityDimensions.fixed(0.8F, 0.6F);
		}

		return null;
	}
	
	@Override
	public EntityDimensions getDimensions(Pose p_33597_) {
		EntityDimensions dimension = getCustomDimensions(p_33597_);
		if (dimension != null) return dimension;
		return EntityDimensions.fixed(0.65F, 1.3F);
	}

	@Override
	public boolean isFood(ItemStack stack) {
		return getEdibleItems().contains(stack.getItem());
	}

	@Override
	public void aiStep() {
		super.aiStep();
		this.updateSwingTime();
		if (jukebox != null)
		{
			if (!jukebox.isRecordPlaying() || getTarget()!=null || isOrderedToSit() || !isDancing()){
				jukebox = null;
			}
		}
	}

	public void addAdditionalSaveData(CompoundTag tag) {
		super.addAdditionalSaveData(tag);
	}

	@Override
	public void readAdditionalSaveData(CompoundTag tag) {
      	super.readAdditionalSaveData(tag);
      	setOrderedToSit(this.isOrderedToSit());
   	}

	@Override
	public SpawnGroupData finalizeSpawn(ServerLevelAccessor level,DifficultyInstance difficulty,MobSpawnType reason,@Nullable SpawnGroupData data,@Nullable CompoundTag tag) {
    	SpawnGroupData d = super.finalizeSpawn(level, difficulty, reason, data, tag);
    	return d;
	}

	public PlayState handleMovementAnimation(AnimationState event) {
		if (this.isSitting()){
			return event.setAndContinue(RawAnimation.begin().thenLoop("sitidle"));
		}
		if (this.isInWaterOrBubble()) {
			return event.setAndContinue(RawAnimation.begin().thenLoop("swinmove"));
		}
		if ((event.isMoving() || !(event.getLimbSwingAmount() > -0.15F && event.getLimbSwingAmount() < 0.15F))) {
			return event.setAndContinue(RawAnimation.begin().thenLoop("move"));
		}
		return event.setAndContinue(RawAnimation.begin().thenLoop("idle"));
	}

	private PlayState movementPredicate(AnimationState event) {
		if (this.getCurrentState() == States.NONE) {
			return handleMovementAnimation(event);
		}
		event.getController().forceAnimationReset();
		return PlayState.STOP;
	}

	private PlayState attackingPredicate(AnimationState event) {
		double d1 = this.getX() - this.xOld;
		double d0 = this.getZ() - this.zOld;
		float velocity = (float) Math.sqrt(d1 * d1 + d0 * d0);
		if (getAttackAnim(event.getPartialTick()) > 0f && !this.swinging) {
			this.swinging = true;
			this.lastSwing = level().getGameTime();
		}
		if (this.swinging && this.lastSwing + 7L <= level().getGameTime()) {
			this.swinging = false;
		}
		if (this.swinging && event.getController().getAnimationState() == AnimationController.State.STOPPED) {
			event.getController().forceAnimationReset();
			return event.setAndContinue(RawAnimation.begin().thenPlay("punch"));
		}
		return PlayState.CONTINUE;
	}

	String prevAnim = "none";

	public PlayState handleStateAnimation(AnimationState event,States state, String animation) {
		if (event.getController().getAnimationState() == AnimationController.State.STOPPED || !prevAnim.equals(animation)) {
        	event.getController().forceAnimationReset();
        	if (state == States.DANCE) {
        		event.getController().setAnimation(RawAnimation.begin().thenPlay(animation+danceType));
        	} else {
        		event.getController().setAnimation(RawAnimation.begin().thenPlay(animation));
        	}
        	prevAnim = animation;
    	}
    	return PlayState.CONTINUE;
	}
	
	private PlayState procedurePredicate(AnimationState event) {
    	States state = this.getCurrentState();
    	String animation = getAnimation(state);
		if (state != States.NONE) {
			return handleStateAnimation(event,state,animation);
		}
		prevAnim = "none";
        return PlayState.STOP;
	}

	@Override
	protected void tickDeath() {
		++this.deathTime;
		clearState();
		if (this.deathTime == 20) {
			this.level().broadcastEntityEvent(this, (byte)60);
			this.remove(Entity.RemovalReason.KILLED);
			this.dropExperience();
		}
	}

	public void onMovementKeyframe(AnimatableManager.ControllerRegistrar data) {
	}

	public void onAttackKeyframe(AnimatableManager.ControllerRegistrar data) {	
	}

	public void onCustomKeyframe(AnimatableManager.ControllerRegistrar data) {	
	}

	@Override
	public void registerControllers(AnimatableManager.ControllerRegistrar data) {
		data.add(new AnimationController<>(this, "movement", 4, this::movementPredicate).setCustomInstructionKeyframeHandler(event -> {
    		onMovementKeyframe(data);
		}));
		data.add(new AnimationController<>(this, "attacking", 4, this::attackingPredicate).setCustomInstructionKeyframeHandler(event -> {
    		String instruction = event.getKeyframeData().getInstructions();

    		instruction = instruction.replace(";", "").trim();

    		if ("hit".equals(instruction)) {
        		HaisistenteAbstract e = (HaisistenteAbstract) event.getAnimatable();
        		if (level().isClientSide()) {
           			HaisistenteMod.PACKET_HANDLER.sendToServer(
                		new FrameFlagPacket(this.getId(), FrameFlag.FRAME_HIT)
            		);
       	 		}
    		}
    		onAttackKeyframe(data);
		}));
		data.add(new AnimationController<>(this, "procedure", 4, this::procedurePredicate).setCustomInstructionKeyframeHandler(event -> {
    		String instruction = event.getKeyframeData().getInstructions();

    		instruction = instruction.replace(";", "").trim();

    	    if ("start_eat".equals(instruction)) {
        		HaisistenteAbstract e = (HaisistenteAbstract) event.getAnimatable();
        		if (level().isClientSide()) {
           			HaisistenteMod.PACKET_HANDLER.sendToServer(
                		new FrameFlagPacket(this.getId(), FrameFlag.FRAME_START_EAT)
            		);
       	 		}
    		}
    	    if ("end_eat".equals(instruction)) {
        		HaisistenteAbstract e = (HaisistenteAbstract) event.getAnimatable();
        		if (level().isClientSide()) {
           			HaisistenteMod.PACKET_HANDLER.sendToServer(
                		new FrameFlagPacket(this.getId(), FrameFlag.FRAME_END_EAT)
            		);
       	 		}
    		}
    		onCustomKeyframe(data);
		}));
	}

	@Override
	public AnimatableInstanceCache getAnimatableInstanceCache() {
		return this.cache;
	}

	static public class HaisistenteSitWhenOrderedToGoal extends Goal {
   		private final TamableAnimal mob;

   		public HaisistenteSitWhenOrderedToGoal(TamableAnimal p_25898_) {
      		this.mob = p_25898_;
      		this.setFlags(EnumSet.of(Goal.Flag.JUMP, Goal.Flag.MOVE));
   		}

   		public boolean canContinueToUse() {
      		return this.mob.isOrderedToSit();
   		}

   		public boolean canUse() {
      		if (!this.mob.isTame()) {
         		return false;
      		} else if (this.mob.isInWaterOrBubble()) {
         		return false;
      		} else {
         		LivingEntity livingentity = this.mob.getOwner();
         		if (livingentity == null) {
            		return true;
         		} else {
            		return this.mob.distanceToSqr(livingentity) < 144.0D && livingentity.getLastHurtByMob() != null ? false : this.mob.isOrderedToSit();
         		}
      		}
   		}

   		public void start() {
      		this.mob.getNavigation().stop();
      		this.mob.setInSittingPose(true);
   		}

   		public void stop() {
      		this.mob.setInSittingPose(false);
   		}
	}

	static class HaiseEatGoal extends Goal{
		private final HaisistenteAbstract haise;
		protected int eatTick = 0;
		
		public HaiseEatGoal(HaisistenteAbstract ta){
			this.setFlags(EnumSet.of(Goal.Flag.JUMP, Goal.Flag.MOVE, Goal.Flag.LOOK));
			haise = ta;
		}

	   	public boolean canContinueToUse() {
      		return this.haise.isEating() && eatTick <= 86;
   		}
	
   		public boolean canUse() {
            return this.haise.isEating();
   		}
   		
   	   	public boolean requiresUpdateEveryTick() {
      		return true;
   		}

   		public void tick() {
   			this.haise.spawnEatingParticles();
   			eatTick++;
   			if (this.haise.consumeFrameFlag(FrameFlag.FRAME_END_EAT)){
   				this.haise.setState(States.EAT, false);
   			}
   		}

   		public void start() {
      		this.haise.getNavigation().stop();
   		}

   		public void stop() {
   			this.haise.tryTame();
   			this.haise.whenFeeding();
			if (this.haise.isTame()) {
				if (this.haise.getMainHandItem().getItem() == HaisistenteItems.GOLDEN_BAMBOO.get()){
					this.haise.heal(6);
				}
				else {
					this.haise.heal(1);
				}
			}

			if (this.haise.getMainHandItem().isEdible()) {
    			this.haise.getMainHandItem().finishUsingItem(this.haise.level(), this.haise);
			}
			
			if (this.haise.getMainHandItem().is(Items.POTION)) {
                  List<MobEffectInstance> list = PotionUtils.getMobEffects(this.haise.getMainHandItem());
            	if (list != null) {
                	for(MobEffectInstance mobeffectinstance : list) {
                    this.haise.addEffect(new MobEffectInstance(mobeffectinstance));
              		}
         		}
        	}

			this.haise.getMainHandItem().shrink(1);
   			this.haise.setFrameFlag(FrameFlag.FRAME_START_EAT, false);
      		eatTick = 0;
   		}
	}

	static class HaiseDanceGoal extends Goal{
		private final HaisistenteAbstract haise;
		
		public HaiseDanceGoal(HaisistenteAbstract ta){
			this.setFlags(EnumSet.of(Goal.Flag.JUMP, Goal.Flag.MOVE));
			haise = ta;
		}

	   	public boolean canContinueToUse() {
      		return this.haise.isDancing() && this.haise.jukebox != null;
   		}
	
   		public boolean canUse() {
            return this.haise.isDancing();
   		}

   		public void start() {
      		this.haise.getNavigation().stop();
   		}

   		public void stop(){
   			this.haise.setState(States.DANCE, false);
   			if (this.haise.jukebox != null) {
   				this.haise.jukebox = null;
   			}
   		}
	}

   static class HaiseSleepOnOwnerGoal extends Goal {
      private final HaisistenteAbstract haise;
      @Nullable
      private Player ownerPlayer;
      @Nullable
      private BlockPos goalPos;
      private int onBedTicks;

      public HaiseSleepOnOwnerGoal(HaisistenteAbstract haise) {
         this.haise = haise;
         this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
      }

      public boolean canUse() {
         if (!this.haise.isTame() || this.haise.getCurrentState() != States.NONE) {
            return false;
         } else if (this.haise.isOrderedToSit() || this.haise.isEating() || this.haise.isDancing()) {
            return false;
         } else {
            LivingEntity livingentity = this.haise.getOwner();
            if (livingentity instanceof Player) {
               this.ownerPlayer = (Player)livingentity;
               if (!livingentity.isSleeping()) {
                  return false;
               }

               if (this.haise.distanceToSqr(this.ownerPlayer) > 100.0D) {
                  return false;
               }

               BlockPos blockpos = this.ownerPlayer.blockPosition();
               BlockState blockstate = this.haise.level().getBlockState(blockpos);
               if (blockstate.is(BlockTags.BEDS)) {
                  this.goalPos = blockstate.getOptionalValue(BedBlock.FACING).map((p_28209_) -> {
                     return blockpos.relative(p_28209_.getOpposite());
                  }).orElseGet(() -> {
                     return new BlockPos(blockpos);
                  });
                  return !this.spaceIsOccupied();
               }
            }

            return false;
         }
      }

      private boolean spaceIsOccupied() {
         for(HaisistenteAbstract haise : this.haise.level().getEntitiesOfClass(HaisistenteAbstract.class, (new AABB(this.goalPos)).inflate(2.0D))) {
            if (haise != this.haise && (haise.isSleepingOnOwner())) {
               return true;
            }
         }
         return false;
      }

      public boolean canContinueToUse() {
         return this.haise.isTame() && !this.haise.isOrderedToSit() && this.ownerPlayer != null && this.ownerPlayer.isSleeping() && this.goalPos != null && !this.spaceIsOccupied();
      }

      public void start() {
         if (this.goalPos != null) {
            this.haise.getNavigation().moveTo((double)this.goalPos.getX(), (double)this.goalPos.getY(), (double)this.goalPos.getZ(), (double)1.1F);
         }

      }

      public void stop() {
         float f = this.haise.level().getTimeOfDay(1.0F);
         this.onBedTicks = 0;
         this.haise.setState(States.SLEEP, false);
         this.haise.getNavigation().stop();
      }

      public void tick() {
         if (this.ownerPlayer != null && this.goalPos != null) {
            this.haise.getNavigation().moveTo((double)this.goalPos.getX(), (double)this.goalPos.getY(), (double)this.goalPos.getZ(), (double)1.1F);
            if (this.haise.distanceToSqr(this.ownerPlayer) < 2.5D) {
               ++this.onBedTicks;
               this.haise.getLookControl().setLookAt(this.ownerPlayer.getX(), this.ownerPlayer.getEyeY(), this.ownerPlayer.getZ());
               this.haise.setSleeping();
            }
         }
      }
   }	
}
