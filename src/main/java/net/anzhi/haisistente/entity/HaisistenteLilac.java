package net.anzhi.haisistente.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.network.PlayMessages;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.Difficulty;
import net.minecraft.server.level.ServerLevel;
import net.anzhi.haisistente.init.HaisistenteEntities;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;

public class HaisistenteLilac extends FlyingHaisistente {
    
	public HaisistenteLilac(PlayMessages.SpawnEntity packet, Level world) {
		this(HaisistenteEntities.HAISISTENTE_LILAC.get(), world);
	}

	public HaisistenteLilac(EntityType<? extends HaisistenteAbstract> type, Level world) {
		super(type, world);
	}

	public boolean initFlying() {
		return false;
	}

	@Override
	public String getTexture() {
		return "lilac_texture";
	}
	
	@Override
	public String getModel() {
		return "geo/outfit_cat.geo.json";
	}
	
	@Override
	public String getGeoAnimation() {
		return "animations/outfit_cat.animation.json";
	}

	public static AttributeSupplier.Builder createAttributes() {
		AttributeSupplier.Builder builder = Mob.createMobAttributes();
		builder = builder.add(Attributes.MOVEMENT_SPEED, 0.3D);
		builder = builder.add(Attributes.MAX_HEALTH, 24.0D);
		builder = builder.add(Attributes.ARMOR, 0.0D);
		builder = builder.add(Attributes.ATTACK_DAMAGE, 5.5D);
		builder = builder.add(Attributes.FOLLOW_RANGE, 16.0D);
		builder = builder.add(Attributes.FLYING_SPEED,0.6);
		return builder;
	}

	@Override
	public InteractionResult customInteract(Player player, InteractionHand hand) {
		if (!level().isClientSide && player.isShiftKeyDown() && isOwnedBy(player)) {
		}
		return null;
	}
    
	@Override
	public AgeableMob getBreedOffspring(ServerLevel serverWorld, AgeableMob ageable) {
		HaisistenteLilac retval = HaisistenteEntities.HAISISTENTE_LILAC.get().create(serverWorld);
		retval.finalizeSpawn(serverWorld, serverWorld.getCurrentDifficultyAt(retval.blockPosition()), MobSpawnType.BREEDING, null, null);
		return retval;
	}

	public static void init() {
		SpawnPlacements.register(HaisistenteEntities.HAISISTENTE_LILAC.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
				(entityType, world, reason, pos, random) -> (world.getDifficulty() != Difficulty.PEACEFUL && Mob.checkMobSpawnRules(entityType, world, reason, pos, random)));
	}
}

