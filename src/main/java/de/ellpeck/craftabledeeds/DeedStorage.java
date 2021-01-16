package de.ellpeck.craftabledeeds;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.FilledMapItem;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.MapData;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DeedStorage extends WorldSavedData {
    private static final String NAME = CraftableDeeds.ID + ":deed_storage";
    private static DeedStorage clientStorage;

    private final World world;
    private final Map<Integer, Claim> claims = new HashMap<>();

    public DeedStorage(World world) {
        super(NAME);
        this.world = world;
    }

    public void addClaim(int id, PlayerEntity owner) {
        this.claims.put(id, new Claim(this.world, id, owner.getUniqueID()));
        PacketHandler.sendDeedsToEveryone(owner.world);
        this.markDirty();
    }

    public Claim getClaim(double x, double z) {
        for (Claim claim : this.claims.values()) {
            if (claim.isOnMap(x, z))
                return claim;
        }
        return null;
    }

    public void update() {

    }

    @Override
    public void read(CompoundNBT nbt) {
        this.claims.clear();
        ListNBT claims = nbt.getList("claims", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < claims.size(); i++) {
            Claim claim = new Claim(this.world, claims.getCompound(i));
            this.claims.put(claim.mapId, claim);
        }
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        ListNBT claims = new ListNBT();
        for (Claim claim : this.claims.values())
            claims.add(claim.serializeNBT());
        compound.put("claims", claims);
        return compound;
    }

    public static DeedStorage get(World world) {
        if (world.isRemote) {
            if (clientStorage == null)
                clientStorage = new DeedStorage(world);
            return clientStorage;
        } else {
            return ((ServerWorld) world).getSavedData().getOrCreate(() -> new DeedStorage(world), NAME);
        }
    }

    public static class Claim implements INBTSerializable<CompoundNBT> {

        private final World world;
        public int mapId;
        public UUID owner;

        public Claim(World world, int mapId, UUID owner) {
            this.world = world;
            this.mapId = mapId;
            this.owner = owner;
        }

        public Claim(World world, CompoundNBT nbt) {
            this.world = world;
            this.deserializeNBT(nbt);
        }

        public MapData getData() {
            return this.world.getMapData(FilledMapItem.getMapName(this.mapId));
        }

        // MapData#updateDecorations
        public boolean isOnMap(double worldX, double worldZ) {
            MapData data = this.getData();
            int i = 1 << data.scale;
            // TODO the client doesn't know about xCenter and zCenter so we'll have to sync that ourselves
            float mapX = (float) (worldX - data.xCenter) / i;
            float mapZ = (float) (worldZ - data.zCenter) / i;
            return mapX >= -64 && mapZ >= -64 && mapX <= 64 && mapZ <= 64;
        }

        @Override
        public CompoundNBT serializeNBT() {
            CompoundNBT nbt = new CompoundNBT();
            nbt.putInt("id", this.mapId);
            nbt.putUniqueId("owner", this.owner);
            return nbt;
        }

        @Override
        public void deserializeNBT(CompoundNBT nbt) {
            this.mapId = nbt.getInt("id");
            this.owner = nbt.getUniqueId("owner");
        }

        @Override
        public String toString() {
            return "Claim{world=" + this.world + ", mapId=" + this.mapId + ", owner=" + this.owner + '}';
        }
    }
}
