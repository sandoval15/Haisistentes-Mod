package net.mcreator.haisistente.procedures;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.JukeboxBlockEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.tags.ItemTags;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.AABB;

import net.mcreator.haisistente.entity.HaisistenteAbstract;
import net.mcreator.haisistente.entity.flag.States;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

@Mod.EventBusSubscriber
public class ClickTocaDiscosProcedure {

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        execute(event, event.getLevel(),
                event.getPos().getX(),
                event.getPos().getY(),
                event.getPos().getZ(),
                event.getEntity());
    }

    private static void execute(@Nullable Event event, LevelAccessor world,
                                double x, double y, double z, Entity entity) {

        if (entity == null || world == null) return;

        if (entity.level().isClientSide()) return;

        if (!(entity instanceof LivingEntity living)) return;

        ItemStack held = living.getMainHandItem();
        if (!held.is(ItemTags.create(new ResourceLocation("minecraft:music_discs"))))
            return;

        BlockPos pos = BlockPos.containing(x, y, z);

        if (!(world.getBlockEntity(pos) instanceof JukeboxBlockEntity jukebox))
            return;

        Vec3 center = Vec3.atCenterOf(pos);

        List<HaisistenteAbstract> haisistentes = world.getEntitiesOfClass(
                HaisistenteAbstract.class,
                new AABB(center, center).inflate(15),
                hs -> hs.jukebox == null
                        && hs.getCurrentState() == States.NONE
                        && !hs.isSitting()
        );

        for (HaisistenteAbstract hs : haisistentes) {
        	Random random = new Random();
            hs.jukebox = jukebox;
            hs.typedance = random.nextInt(hs.maxdances) + 1;
            hs.setDancing();
        }
    }
}
