package net.mcreator.haisistente.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.*;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.network.PlayMessages;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.Level;
import net.minecraft.world.Difficulty;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;

import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.effect.MobEffects;

import net.mcreator.haisistente.init.HaisistenteEntities;

public class HaisistenteZombie extends HaisistenteAbstract {
	private static final EntityDataAccessor<Boolean> CONVERTING = SynchedEntityData.defineId(HaisistenteAbstract.class, EntityDataSerializers.BOOLEAN);
	private int ConversionTime;

	public HaisistenteZombie(PlayMessages.SpawnEntity packet, Level world) {
		this(HaisistenteEntities.HAISISTENTE_ZOMBIE.get(), world);
	}

	public HaisistenteZombie(EntityType<? extends HaisistenteAbstract> type, Level world) {
		super(type, world);
	}

	@Override
	protected void defineSynchedData() {
		super.defineSynchedData();
		this.entityData.define(CONVERTING, false);
	}

	@Override
	public String getTexture() {
		return "haisezombie_texture";
	}
	
	@Override
	public String getModel() {
		return "geo/ropa_zombie.geo.json";
	}
	
	@Override
	public String getGeoAnimation() {
		return "animations/ropa_zombie.animation.json";
	}

	@Override
	public boolean canEat() {
		return super.canEat() && hasEffect(MobEffects.WEAKNESS) && !isConverting();
	}

	public boolean isConverting() {
		return (Boolean)this.getEntityData().get(CONVERTING);
	}
	
	@Override
	protected void registerGoals() {
		super.registerGoals();
		this.goalSelector.getAvailableGoals().removeIf(
    		g -> g.getGoal() instanceof PanicGoal
		);
		this.goalSelector.getAvailableGoals().removeIf(
    		g -> g.getGoal() instanceof HaisistenteAbstract.HaiseSleepOnOwnerGoal
		);
		this.goalSelector.getAvailableGoals().removeIf(
    		g -> g.getGoal() instanceof HaisistenteAbstract.HaiseDanceGoal
		);
		this.targetSelector.addGoal(1, (new HurtByTargetGoal(this)).setAlertOthers(HaisistenteZombie.class));
		this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
		this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, HaisistenteAbstract.class, true, target -> !(target instanceof HaisistenteZombie)));
	}
	
	@Override
	public AgeableMob getBreedOffspring(ServerLevel serverWorld, AgeableMob ageable) {
		HaisistenteZombie retval = HaisistenteEntities.HAISISTENTE_ZOMBIE.get().create(serverWorld);
		retval.finalizeSpawn(serverWorld, serverWorld.getCurrentDifficultyAt(retval.blockPosition()), MobSpawnType.BREEDING, null, null);
		return retval;
	}

	@Override
	public MobType getMobType() {
		return MobType.UNDEAD;
	}

	protected boolean isSunSensitive() {
      return true;
   	}

	public boolean removeWhenFarAway(double p_34414_) {
		return !this.isConverting();
	}

	public boolean isShaking() {
		return isConverting();
	}

	@Override
	public void tryTame() {
   	}

	@Override
	public void whenFeeding() {
		this.startConverting(this.random.nextInt(2401) + 3600);
	}

   	public void aiStep() {
    	if (this.isAlive()) {
      		boolean flag = this.isSunSensitive() && this.isSunBurnTick();
            if (flag) this.setSecondsOnFire(8);
         }

      	super.aiStep();
   }

	public void tick() {
		if (!this.level().isClientSide && this.isAlive() && this.isConverting()) {
			this.ConversionTime -= 1;
			if (this.ConversionTime <= 0 && ForgeEventFactory.canLivingConvert(this, HaisistenteEntities.HAISISTENTE.get(), (timer) -> {
				this.ConversionTime = timer;
			})) {
				this.finishConversion((ServerLevel)this.level());
			}
		}

		super.tick();
	}

   @Override
   public void addAdditionalSaveData(CompoundTag tag) {
		super.addAdditionalSaveData(tag);
		tag.putInt("conversiontime", this.isConverting() ? this.ConversionTime : -1);
	}

	@Override
	public void readAdditionalSaveData(CompoundTag tag) {
		super.readAdditionalSaveData(tag);
		if (tag.contains("conversiontime", 99) && tag.getInt("conversiontime") > -1) {
			this.startConverting(tag.getInt("conversiontime"));
		}
	}

	private void startConverting(int time) {
		this.ConversionTime = time;
		this.getEntityData().set(CONVERTING, true);
		this.removeEffect(MobEffects.WEAKNESS);
		this.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, time, Math.min(this.level().getDifficulty().getId() - 1, 0)));
		this.level().broadcastEntityEvent(this, (byte)16);
	}

	public void handleEntityEvent(byte i) {
		if (i == 16) {
			if (!this.isSilent()) {
				this.level().playLocalSound(this.getX(), this.getEyeY(), this.getZ(), SoundEvents.ZOMBIE_VILLAGER_CURE, this.getSoundSource(), 1.0F + this.random.nextFloat(), this.random.nextFloat() * 0.7F + 0.3F, false);
			}
		} else {
			super.handleEntityEvent(i);
		}

	}

	private void finishConversion(ServerLevel level) {
		Haisistente haisistente = (Haisistente)this.convertTo(HaisistenteEntities.HAISISTENTE.get(), false);

		haisistente.finalizeSpawn(level, level.getCurrentDifficultyAt(haisistente.blockPosition()), MobSpawnType.CONVERSION, (SpawnGroupData)null, (CompoundTag)null);

		haisistente.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 200, 0));
		if (!this.isSilent()) {
			level.levelEvent((Player)null, 1027, this.blockPosition(), 0);
		}

		ForgeEventFactory.onLivingConvert(this, haisistente);
	}

	public static AttributeSupplier.Builder createAttributes() {
		AttributeSupplier.Builder builder = Mob.createMobAttributes();
		builder = builder.add(Attributes.MOVEMENT_SPEED, 0.3D);
		builder = builder.add(Attributes.MAX_HEALTH, 15.0D);
		builder = builder.add(Attributes.ARMOR, 0.0D);
		builder = builder.add(Attributes.ATTACK_DAMAGE, 3.0D);
		builder = builder.add(Attributes.FOLLOW_RANGE, 35.0D);
		return builder;
	}

	public static void init() {
		SpawnPlacements.register(HaisistenteEntities.HAISISTENTE_ZOMBIE.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
				(entityType, world, reason, pos, random) -> (world.getDifficulty() != Difficulty.PEACEFUL && Mob.checkMobSpawnRules(entityType, world, reason, pos, random)));
	}
}

