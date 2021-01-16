package de.ellpeck.craftabledeeds;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

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
        if (shouldCancelInteraction(event.getPlayer(), event.getPos()))
            event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        if (event.getEntity() instanceof PlayerEntity && shouldCancelInteraction((PlayerEntity) event.getEntity(), event.getPos()))
            event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onBlockInteract(PlayerInteractEvent.RightClickBlock event) {
        if (shouldCancelInteraction(event.getPlayer(), event.getPos()))
            event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onItemInteract(PlayerInteractEvent.RightClickItem event) {
        if (shouldCancelInteraction(event.getPlayer(), event.getPos()))
            event.setCanceled(true);
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
        // TODO item frame punching but not deleting
    }

    private static boolean shouldCancelInteraction(PlayerEntity player, BlockPos pos) {
        // y 15 and below should be ignored
        if (pos.getY() <= 15)
            return false;
        // opped players should be ignored
        if (player.hasPermissionLevel(2))
            return false;
        DeedStorage storage = DeedStorage.get(player.world);
        DeedStorage.Claim claim = storage.getClaim(pos.getX(), pos.getZ());
        System.out.println(claim + " " + (claim == null ? "none" : claim.owner) + " " + player.getUniqueID());
        return claim != null && !claim.owner.equals(player.getUniqueID());
    }
}
