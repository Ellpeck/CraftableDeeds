package de.ellpeck.craftabledeeds;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.network.simple.SimpleChannel;

import java.util.function.Supplier;

public final class PacketHandler {

    private static SimpleChannel network;

    public static void init(FMLCommonSetupEvent event) {
        String version = "1";
        network = NetworkRegistry.newSimpleChannel(new ResourceLocation(CraftableDeeds.ID, "network"), () -> version, version::equals, version::equals);
        network.registerMessage(0, PacketDeeds.class, PacketDeeds::toBytes, PacketDeeds::fromBytes, PacketDeeds::onMessage);
    }

    public static void sendDeeds(PlayerEntity player) {
        PacketDeeds packet = new PacketDeeds(DeedStorage.get(player.world).write(new CompoundNBT()));
        network.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) player), packet);
    }

    public static void sendDeedsToEveryone(World world) {
        PacketDeeds packet = new PacketDeeds(DeedStorage.get(world).write(new CompoundNBT()));
        network.send(PacketDistributor.DIMENSION.with(world::getDimensionKey), packet);
    }

    private static class PacketDeeds {

        private final CompoundNBT data;

        public PacketDeeds(CompoundNBT data) {
            this.data = data;
        }

        public static PacketDeeds fromBytes(PacketBuffer buf) {
            return new PacketDeeds(buf.readCompoundTag());
        }

        public static void toBytes(PacketDeeds packet, PacketBuffer buf) {
            buf.writeCompoundTag(packet.data);
        }

        // lambda causes classloading issues on a server here
        @SuppressWarnings("Convert2Lambda")
        public static void onMessage(PacketDeeds message, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(new Runnable() {
                @Override
                public void run() {
                    Minecraft mc = Minecraft.getInstance();
                    if (mc.world != null)
                        DeedStorage.get(mc.world).read(message.data);
                }
            });
            ctx.get().setPacketHandled(true);
        }
    }
}