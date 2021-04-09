package de.ellpeck.craftabledeeds.blocks;

import de.ellpeck.craftabledeeds.CraftableDeeds;
import de.ellpeck.craftabledeeds.DeedStorage;
import de.ellpeck.craftabledeeds.items.FilledDeedItem;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.storage.MapData;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nullable;

// TODO figure out a way to send the map data to the client since it doesn't do it on its own yet
public class DeedPedestalTileEntity extends TileEntity {

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
}
