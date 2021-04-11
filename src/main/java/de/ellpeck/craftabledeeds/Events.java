package de.ellpeck.craftabledeeds;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.item.HangingEntity;
import net.minecraft.entity.item.TNTEntity;
import net.minecraft.entity.monster.CreeperEntity;
import net.minecraft.entity.monster.EndermanEntity;
import net.minecraft.entity.monster.ZombieEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.WitherSkullEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityMobGriefingEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.ExplosionEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

@Mod.EventBusSubscriber
public final class Events {

    @SubscribeEvent
    public static void onPlayerTeleport(PlayerEvent.PlayerChangedDimensionEvent event) {
        PacketHandler.sendDeeds(event.getPlayer());
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        PacketHandler.sendDeeds(event.getPlayer());
    }

    @SubscribeEvent
    public static void onWorldTick(TickEvent.WorldTickEvent event) {
        if (event.phase == TickEvent.Phase.END)
            DeedStorage.get(event.world).update();
    }

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (shouldCancelInteraction(event.getPlayer(), event.getPos())) {
            if (isExempt(CraftableDeeds.breakableBlocks.get(), event.getState().getBlock()))
                return;
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        if (event.getEntity() instanceof PlayerEntity && shouldCancelInteraction(event.getEntity(), event.getPos()))
            event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onBlockInteract(PlayerInteractEvent.RightClickBlock event) {
        if (shouldCancelInteraction(event.getPlayer(), event.getPos())) {
            BlockState state = event.getWorld().getBlockState(event.getPos());
            // always allow interacting with the pedestal!
            if (state.getBlock() == CraftableDeeds.DEED_PEDESTAL_BLOCK.get())
                return;
            if (isExempt(CraftableDeeds.interactableBlocks.get(), state.getBlock()))
                return;

            if (!CraftableDeeds.allowOpeningBlocks.get())
                event.setUseBlock(Event.Result.DENY);
            event.setUseItem(Event.Result.DENY);
        }
    }

    @SubscribeEvent
    public static void onBlockClick(PlayerInteractEvent.LeftClickBlock event) {
        if (shouldCancelInteraction(event.getPlayer(), event.getPos()))
            event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (shouldCancelInteraction(event.getPlayer(), event.getPos()))
            event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onEntityAttack(AttackEntityEvent event) {
        Entity target = event.getTarget();
        if (target instanceof HangingEntity && shouldCancelInteraction(event.getPlayer(), target.getPosition()))
            event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onMobGriefing(EntityMobGriefingEvent event) {
        Entity entity = event.getEntity();
        // endermen picking stuff up and zombies breaking down doors should be disallowed
        if (entity instanceof EndermanEntity || entity instanceof ZombieEntity) {
            if (shouldCancelInteraction(entity, entity.getPosition()))
                event.setResult(Event.Result.DENY);
        }
    }

    @SubscribeEvent
    public static void doExplosion(ExplosionEvent.Start event) {
        Explosion explosion = event.getExplosion();
        Entity exploder = explosion.getExploder();
        if (exploder != null && shouldCancelInteraction(exploder, new BlockPos(explosion.getPosition()))) {
            if (exploder instanceof CreeperEntity && CraftableDeeds.allowCreeperExplosions.get())
                return;
            if (exploder instanceof TNTEntity && CraftableDeeds.allowTntExplosions.get())
                return;
            if ((exploder instanceof WitherEntity || exploder instanceof WitherSkullEntity) && CraftableDeeds.allowWitherExplosions.get())
                return;

            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onServerStarting(RegisterCommandsEvent event) {
        DeedCommand.register(event.getDispatcher());
    }

    private static boolean shouldCancelInteraction(Entity entity, BlockPos pos) {
        // opped players should be ignored
        if (entity.hasPermissionLevel(2))
            return false;
        DeedStorage storage = DeedStorage.get(entity.world);
        DeedStorage.Claim claim = storage.getClaim(pos.getX(), pos.getY(), pos.getZ());
        return claim != null && claim.isActive() && !claim.owner.equals(entity.getUniqueID()) && !claim.friends.contains(entity.getUniqueID());
    }

    private static boolean isExempt(List<? extends String> config, Block block) {
        return isExempt(config, block.getRegistryName().toString());
    }

    private static boolean isExempt(List<? extends String> config, String search) {
        return config.stream().anyMatch(search::matches);
    }
}
