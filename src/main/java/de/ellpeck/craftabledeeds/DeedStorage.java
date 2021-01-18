package de.ellpeck.craftabledeeds;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemFrameEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.math.AxisAlignedBB;
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
        PacketHandler.sendDeedsToEveryone(this.world);
        this.markDirty();
    }

    public void removeClaim(int id) {
        if (this.claims.remove(id) != null) {
            PacketHandler.sendDeedsToEveryone(this.world);
            this.markDirty();
        }
    }

    public Claim getClaim(double x, double y, double z) {
        for (Claim claim : this.claims.values()) {
            if (claim.getArea().contains(x, y, z))
                return claim;
        }
        return null;
    }

    public void update() {
        if (this.world.isRemote || this.world.getGameTime() % 100 != 0)
            return;
        // update the frame status of claims
        for (Claim claim : this.claims.values()) {
            if (claim.itemFrame >= 0) {
                // check if the existing frame still contains our deed and skip if it does
                Entity existing = this.world.getEntityByID(claim.itemFrame);
                if (existing instanceof ItemFrameEntity) {
                    ItemStack stack = ((ItemFrameEntity) existing).getDisplayedItem();
                    if (stack.getItem() == CraftableDeeds.FILLED_DEED.get() && FilledMapItem.getMapId(stack) == claim.mapId) {
                        continue;
                    }
                }
                claim.itemFrame = -1;
                PacketHandler.sendDeedsToEveryone(this.world);
            }

            // if not, check if there is any frame
            for (ItemFrameEntity frame : this.world.getEntitiesWithinAABB(ItemFrameEntity.class, claim.getArea())) {
                ItemStack stack = frame.getDisplayedItem();
                if (stack.getItem() == CraftableDeeds.FILLED_DEED.get() && FilledMapItem.getMapId(stack) == claim.mapId) {
                    claim.itemFrame = frame.getEntityId();
                    PacketHandler.sendDeedsToEveryone(this.world);
                    break;
                }
            }
        }
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
            if (clientStorage == null || clientStorage.world != world)
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
        public int xCenter;
        public int zCenter;
        public int scale;
        public int itemFrame = -1;

        public Claim(World world, int mapId, UUID owner) {
            MapData data = world.getMapData(FilledMapItem.getMapName(mapId));
            this.world = world;
            this.mapId = mapId;
            this.owner = owner;
            this.xCenter = data.xCenter;
            this.zCenter = data.zCenter;
            this.scale = data.scale;
        }

        public Claim(World world, CompoundNBT nbt) {
            this.world = world;
            this.deserializeNBT(nbt);
        }

        public AxisAlignedBB getArea() {
            int i = 1 << this.scale;
            return new AxisAlignedBB(
                    // start at y 15
                    this.xCenter - 64 * i, 15, this.zCenter - 64 * i,
                    this.xCenter + 64 * i, this.world.getHeight(), this.zCenter + 64 * i);
        }

        @Override
        public CompoundNBT serializeNBT() {
            CompoundNBT nbt = new CompoundNBT();
            nbt.putInt("id", this.mapId);
            nbt.putUniqueId("owner", this.owner);
            nbt.putInt("xCenter", this.xCenter);
            nbt.putInt("zCenter", this.zCenter);
            nbt.putInt("scale", this.scale);
            nbt.putInt("frame", this.itemFrame);
            return nbt;
        }

        @Override
        public void deserializeNBT(CompoundNBT nbt) {
            this.mapId = nbt.getInt("id");
            this.owner = nbt.getUniqueId("owner");
            this.xCenter = nbt.getInt("xCenter");
            this.zCenter = nbt.getInt("zCenter");
            this.scale = nbt.getInt("scale");
            this.itemFrame = nbt.getInt("frame");
        }

        @Override
        public String toString() {
            return "Claim{" + "world=" + this.world + ", mapId=" + this.mapId + ", owner=" + this.owner + ", xCenter=" + this.xCenter + ", zCenter=" + this.zCenter + ", scale=" + this.scale + '}';
        }
    }
}
