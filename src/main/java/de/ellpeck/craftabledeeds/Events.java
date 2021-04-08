package de.ellpeck.craftabledeeds;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.HangingEntity;
import net.minecraft.entity.item.ItemFrameEntity;
import net.minecraft.entity.monster.CreeperEntity;
import net.minecraft.entity.monster.EndermanEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityMobGriefingEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;

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
        if (event.getEntity() instanceof PlayerEntity && shouldCancelInteraction(event.getEntity(), event.getPos()))
            event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onBlockInteract(PlayerInteractEvent.RightClickBlock event) {
        if (shouldCancelInteraction(event.getPlayer(), event.getPos())) {
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
        if (target instanceof HangingEntity && shouldCancelInteraction(event.getPlayer(), target.getPosition())) {
            // allow punching items out of item frames!
            if (target instanceof ItemFrameEntity && !((ItemFrameEntity) target).getDisplayedItem().isEmpty())
                return;
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onMobGriefing(EntityMobGriefingEvent event) {
        // creepers shouldn't explode
        Entity entity = event.getEntity();
        if ((entity instanceof CreeperEntity || entity instanceof EndermanEntity) && shouldCancelInteraction(entity, entity.getPosition()))
            event.setResult(Event.Result.DENY);
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
        return claim != null && (!CraftableDeeds.requireItemFrames.get() || claim.itemFrame >= 0) && !claim.owner.equals(entity.getUniqueID()) && !claim.friends.contains(entity.getUniqueID());
    }
}
