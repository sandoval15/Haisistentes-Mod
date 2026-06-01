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

import net.minecraft.ChatFormatting;

public class HaisistenteAnother extends HaisistenteAbstract {

	public HaisistenteAnother(PlayMessages.SpawnEntity packet, Level world) {
		this(HaisistenteEntities.HAISISTENTE_ANOTHER.get(), world);
	}

	public HaisistenteAnother(EntityType<? extends HaisistenteAbstract> type, Level world) {
		super(type, world);
	}

	@Override
	public String getTexture() {
		return isAnother() ? "texture_another2" : "texture_another";
	}
	
	@Override
	public String getModel() {
		return isAnother() ? "geo/ropa_another2.geo.json" : "geo/ropa_another.geo.json";
	}
	
	@Override
	public String getGeoAnimation() {
		return isAnother() ? "animations/ropa_another2.animation.json" : "animations/ropa_another.animation.json";
	}

	public boolean isAnother() {
    	if (this.hasCustomName()) {
        	String s = ChatFormatting.stripFormatting(this.getCustomName().getString());
        	return "anotherguy".equalsIgnoreCase(s);
    	}
    	return false;
	}

	public static AttributeSupplier.Builder createAttributes() {
		AttributeSupplier.Builder builder = Mob.createMobAttributes();
		builder = builder.add(Attributes.MOVEMENT_SPEED, 0.3D);
		builder = builder.add(Attributes.MAX_HEALTH, 34.0D);
		builder = builder.add(Attributes.ARMOR, 0.0D);
		builder = builder.add(Attributes.ATTACK_DAMAGE, 4.5D);
		builder = builder.add(Attributes.FOLLOW_RANGE, 16.0D);
		return builder;
	}
	
	@Override
	public AgeableMob getBreedOffspring(ServerLevel serverWorld, AgeableMob ageable) {
		HaisistenteAnother retval = HaisistenteEntities.HAISISTENTE_ANOTHER.get().create(serverWorld);
		retval.finalizeSpawn(serverWorld, serverWorld.getCurrentDifficultyAt(retval.blockPosition()), MobSpawnType.BREEDING, null, null);
		return retval;
	}

	public static void init() {
		SpawnPlacements.register(HaisistenteEntities.HAISISTENTE_ANOTHER.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
				(entityType, world, reason, pos, random) -> (world.getDifficulty() != Difficulty.PEACEFUL && Mob.checkMobSpawnRules(entityType, world, reason, pos, random)));
	}
}
