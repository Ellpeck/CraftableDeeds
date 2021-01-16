package de.ellpeck.craftabledeeds;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapData;

import javax.annotation.Nullable;

public class FilledDeedItem extends FilledMapItem {
    public FilledDeedItem() {
        super(new Properties().maxStackSize(1));
    }

    @Override
    public void onCreated(ItemStack stack, World worldIn, PlayerEntity playerIn) {
        // no-op since super does some nbt stuff we don't need
    }

    @Nullable
    @Override
    protected MapData getCustomMapData(ItemStack stack, World worldIn) {
        return getData(stack, worldIn);
    }
}
