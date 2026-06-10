package net.anzhi.haisistente.entity;

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

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.Direction;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.network.NetworkHooks;

import net.anzhi.haisistente.init.HaisistenteEntities;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;

import net.anzhi.haisistente.entity.lux.LuxMenu;
import net.anzhi.haisistente.entity.lux.LuxHatAnimatable;

public class HaisistenteLux extends FlyingHaisistente {
	
	private final ItemStackHandler inventory = new ItemStackHandler(18);
    private final LazyOptional<IItemHandler> inventoryCap =
            LazyOptional.of(() -> inventory);
            
    public final LuxHatAnimatable hatAnim = new LuxHatAnimatable(this);
    
	public HaisistenteLux(PlayMessages.SpawnEntity packet, Level world) {
		this(HaisistenteEntities.HAISISTENTE_LUX.get(), world);
	}

	public HaisistenteLux(EntityType<? extends HaisistenteAbstract> type, Level world) {
		super(type, world);
	}

	@Override
	public String getTexture() {
		return "favio_texture";
	}
	
	@Override
	public String getModel() {
		return "geo/outfit_favio.geo.json";
	}
	
	@Override
	public String getGeoAnimation() {
		return "animations/outfit_favio.animation.json";
	}

    public static AttributeSupplier.Builder createAttributes() {
		AttributeSupplier.Builder builder = Mob.createMobAttributes();
		builder = builder.add(Attributes.MOVEMENT_SPEED, 0.3D);
		builder = builder.add(Attributes.MAX_HEALTH, 20.0D);
		builder = builder.add(Attributes.ARMOR, 0.0D);
		builder = builder.add(Attributes.ATTACK_DAMAGE, 1.5D);
		builder = builder.add(Attributes.FOLLOW_RANGE, 16.0D);
		builder = builder.add(Attributes.FOLLOW_RANGE, 16);
		builder = builder.add(Attributes.FLYING_SPEED,0.6);
		return builder;
	}

	public void openInventoryGui(Player player) {
    	if (!(player instanceof ServerPlayer serverPlayer)) return;

    	this.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(handler -> {
        	NetworkHooks.openScreen(
            	serverPlayer,
            	new MenuProvider() {

                	@Override
                	public Component getDisplayName() {
                    	return Component.translatable("container.haisistente.lux_menu");
                	}

                	@Override
                	public AbstractContainerMenu createMenu(int id, Inventory inv, Player p) {
                    	return new LuxMenu(id, inv, handler);
                	}
            	}
        	);
    	});
	}

	@Override
	public InteractionResult customInteract(Player player, InteractionHand hand) {
		if (!level().isClientSide && player.isShiftKeyDown() && isOwnedBy(player)) {
			openInventoryGui(player);
			return InteractionResult.SUCCESS;
		}
		return null;
	}

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            return inventoryCap.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        inventoryCap.invalidate();
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.put("Inventory", inventory.serializeNBT());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        inventory.deserializeNBT(tag.getCompound("Inventory"));
    }
    
	@Override
	public AgeableMob getBreedOffspring(ServerLevel serverWorld, AgeableMob ageable) {
		HaisistenteLux retval = HaisistenteEntities.HAISISTENTE_LUX.get().create(serverWorld);
		retval.finalizeSpawn(serverWorld, serverWorld.getCurrentDifficultyAt(retval.blockPosition()), MobSpawnType.BREEDING, null, null);
		return retval;
	}

	public static void init() {
		SpawnPlacements.register(HaisistenteEntities.HAISISTENTE_LUX.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
				(entityType, world, reason, pos, random) -> (world.getDifficulty() != Difficulty.PEACEFUL && Mob.checkMobSpawnRules(entityType, world, reason, pos, random)));
	}
}
