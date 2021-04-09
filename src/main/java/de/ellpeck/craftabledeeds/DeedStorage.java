package de.ellpeck.craftabledeeds;

import de.ellpeck.craftabledeeds.blocks.DeedPedestalTileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.LongArrayNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.MapData;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.*;

public class DeedStorage extends WorldSavedData {

    private static final String NAME = CraftableDeeds.ID + ":deed_storage";
    private static DeedStorage clientStorage;

    public Map<BlockPos, DeedPedestalTileEntity> pedestals = new HashMap<>();
    private final World world;
    private final Map<Integer, Claim> claims = new HashMap<>();

    public DeedStorage(World world) {
        super(NAME);
        this.world = world;
    }

    public void addClaim(int id, PlayerEntity owner) {
        this.claims.put(id, new Claim(this.world, id, owner.getUniqueID()));
        this.markDirtyAndSend();
    }

    public void removeClaim(int id) {
        if (this.claims.remove(id) != null)
            this.markDirtyAndSend();
    }

    public Claim getClaim(double x, double y, double z) {
        for (Claim claim : this.claims.values()) {
            if (claim.getArea().contains(x, y, z))
                return claim;
        }
        return null;
    }

    public Claim getClaim(int id) {
        return this.claims.get(id);
    }

    public void update() {
        if (this.world.isRemote || this.world.getGameTime() % 40 != 0)
            return;
        // update the pedestal status of claims
        for (Claim claim : this.claims.values()) {
            if (claim.pedestal != null) {
                // check if the existing pedestal still contains our deed and skip if it does
                DeedPedestalTileEntity existing = this.pedestals.get(claim.pedestal);
                if (existing != null) {
                    ItemStack stack = existing.items.getStackInSlot(0);
                    if (stack.getItem() == CraftableDeeds.FILLED_DEED.get() && FilledMapItem.getMapId(stack) == claim.mapId)
                        continue;
                }
                claim.pedestal = null;
                this.markDirtyAndSend();
            }

            // if it doesn't still contain our deed, check if there is any new pedestal
            AxisAlignedBB area = claim.getArea();
            for (DeedPedestalTileEntity tile : this.pedestals.values()) {
                BlockPos pos = tile.getPos();
                if (area.contains(pos.getX(), pos.getY(), pos.getZ())) {
                    ItemStack stack = tile.items.getStackInSlot(0);
                    if (stack.getItem() == CraftableDeeds.FILLED_DEED.get() && FilledMapItem.getMapId(stack) == claim.mapId) {
                        claim.pedestal = pos;
                        this.markDirtyAndSend();
                        break;
                    }
                }
            }
        }
    }

    public void markDirtyAndSend() {
        PacketHandler.sendDeedsToEveryone(this.world);
        this.markDirty();
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
        public final List<UUID> friends = new ArrayList<>();
        public int mapId;
        public UUID owner;
        public int xCenter;
        public int zCenter;
        public int scale;
        public BlockPos pedestal;

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

        public Object getOwnerName() {
            PlayerEntity owner = this.world.getPlayerByUuid(this.owner);
            return owner != null ? owner.getDisplayName() : this.owner;
        }

        @Override
        public CompoundNBT serializeNBT() {
            CompoundNBT nbt = new CompoundNBT();
            nbt.putInt("id", this.mapId);
            nbt.putUniqueId("owner", this.owner);
            nbt.putInt("xCenter", this.xCenter);
            nbt.putInt("zCenter", this.zCenter);
            nbt.putInt("scale", this.scale);
            if (this.pedestal != null)
                nbt.putLong("pedestal", this.pedestal.toLong());
            ListNBT friends = new ListNBT();
            for (UUID friend : this.friends)
                friends.add(new LongArrayNBT(new long[]{friend.getMostSignificantBits(), friend.getLeastSignificantBits()}));
            nbt.put("friends", friends);
            return nbt;
        }

        @Override
        public void deserializeNBT(CompoundNBT nbt) {
            this.mapId = nbt.getInt("id");
            this.owner = nbt.getUniqueId("owner");
            this.xCenter = nbt.getInt("xCenter");
            this.zCenter = nbt.getInt("zCenter");
            this.scale = nbt.getInt("scale");
            this.pedestal = nbt.contains("pedestal") ? BlockPos.fromLong(nbt.getLong("pedestal")) : null;
            this.friends.clear();
            ListNBT friends = nbt.getList("friends", Constants.NBT.TAG_LONG_ARRAY);
            for (INBT val : friends) {
                long[] friend = ((LongArrayNBT) val).getAsLongArray();
                this.friends.add(new UUID(friend[0], friend[1]));
            }
        }

        @Override
        public String toString() {
            return "Claim{" +
                    "world=" + this.world +
                    ", friends=" + this.friends +
                    ", mapId=" + this.mapId +
                    ", owner=" + this.owner +
                    ", xCenter=" + this.xCenter +
                    ", zCenter=" + this.zCenter +
                    ", scale=" + this.scale +
                    ", pedestal=" + this.pedestal +
                    '}';
        }
    }
}
