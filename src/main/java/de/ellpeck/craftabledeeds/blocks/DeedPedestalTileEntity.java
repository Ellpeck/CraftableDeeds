package de.ellpeck.craftabledeeds.blocks;

import de.ellpeck.craftabledeeds.CraftableDeeds;
import de.ellpeck.craftabledeeds.DeedStorage;
import de.ellpeck.craftabledeeds.items.FilledDeedItem;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.server.ServerChunkProvider;
import net.minecraft.world.storage.MapData;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nullable;

public class DeedPedestalTileEntity extends TileEntity implements ITickableTileEntity {

    public final ItemStackHandler items = new ItemStackHandler(1);

    public DeedPedestalTileEntity() {
        super(CraftableDeeds.DEED_PEDESTAL_TILE.get());
    }

    public MapData getMapData() {
        ItemStack stack = this.items.getStackInSlot(0);
        if (stack.getItem() == CraftableDeeds.FILLED_DEED.get())
            return FilledDeedItem.getData(stack, this.world);
        return null;
    }

    @Override
    public void tick() {
        // send map data every 10 ticks similarly to how TrackedEntity does for item frames
        if (!this.world.isRemote && this.world.getGameTime() % 10 == 0) {
            MapData data = this.getMapData();
            if (data != null) {
                ItemStack stack = this.items.getStackInSlot(0);
                ((ServerChunkProvider) this.world.getChunkProvider()).chunkManager
                        .getTrackingPlayers(new ChunkPos(this.pos), false)
                        .forEach(p -> {
                            IPacket<?> ipacket = data.getMapInfo(p).getPacket(stack);
                            if (ipacket != null)
                                p.connection.sendPacket(ipacket);
                        });
            }
        }
    }

    @Override
    public void validate() {
        super.validate();
        DeedStorage.get(this.world).pedestals.put(this.pos, this);
    }

    @Override
    public void remove() {
        super.remove();
        DeedStorage.get(this.world).pedestals.remove(this.pos);
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        compound.put("items", this.items.serializeNBT());
        return super.write(compound);
    }

    @Override
    public void read(BlockState state, CompoundNBT nbt) {
        this.items.deserializeNBT(nbt.getCompound("items"));
        super.read(state, nbt);
    }

    @Override
    public CompoundNBT getUpdateTag() {
        return this.write(new CompoundNBT());
    }

    @Override
    public void handleUpdateTag(BlockState state, CompoundNBT tag) {
        this.read(state, tag);
    }

    @Nullable
    @Override
    public SUpdateTileEntityPacket getUpdatePacket() {
        return new SUpdateTileEntityPacket(this.pos, 0, this.getUpdateTag());
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
        this.handleUpdateTag(this.getBlockState(), pkt.getNbtCompound());
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public AxisAlignedBB getRenderBoundingBox() {
        return new AxisAlignedBB(this.pos, this.pos.add(1, 2, 1));
    }
}
