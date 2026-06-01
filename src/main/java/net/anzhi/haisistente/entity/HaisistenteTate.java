package net.mcreator.haisistente.entity;

import net.minecraftforge.network.PlayMessages;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.Difficulty;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;

import net.mcreator.haisistente.init.HaisistenteEntities;

public class HaisistenteTate extends HaisistenteAbstract {

	public HaisistenteTate(PlayMessages.SpawnEntity packet, Level world) {
		this(HaisistenteEntities.HAISISTENTE_TATE.get(), world);
	}

	public HaisistenteTate(EntityType<? extends HaisistenteAbstract> type, Level world) {
		super(type, world);
	}

	@Override
	public String getTexture() {
		return "texture_tate_new";
	}
	
	@Override
	public String getModel() {
		return "geo/ropa_sapo2.geo.json";
	}
	
	@Override
	public String getGeoAnimation() {
		return "animations/ropa_sapo2.animation.json";
	}
	
	@Override
	public AgeableMob getBreedOffspring(ServerLevel serverWorld, AgeableMob ageable) {
		HaisistenteTate retval = HaisistenteEntities.HAISISTENTE_TATE.get().create(serverWorld);
		retval.finalizeSpawn(serverWorld, serverWorld.getCurrentDifficultyAt(retval.blockPosition()), MobSpawnType.BREEDING, null, null);
		return retval;
	}

	public static AttributeSupplier.Builder createAttributes() {
		AttributeSupplier.Builder builder = Mob.createMobAttributes();
		builder = builder.add(Attributes.MOVEMENT_SPEED, 0.3D);
		builder = builder.add(Attributes.MAX_HEALTH, 28.0D);
		builder = builder.add(Attributes.ARMOR, 0.0D);
		builder = builder.add(Attributes.ATTACK_DAMAGE, 3.0D);
		builder = builder.add(Attributes.FOLLOW_RANGE, 16.0D);
		return builder;
	}

	public static void init() {
		SpawnPlacements.register(HaisistenteEntities.HAISISTENTE_TATE.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
				(entityType, world, reason, pos, random) -> (world.getDifficulty() != Difficulty.PEACEFUL && Mob.checkMobSpawnRules(entityType, world, reason, pos, random)));
	}
}
