package de.ellpeck.craftabledeeds;

import net.minecraftforge.event.entity.player.PlayerEvent;
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
}
