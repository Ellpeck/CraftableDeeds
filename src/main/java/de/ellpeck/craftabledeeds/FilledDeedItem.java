package de.ellpeck.craftabledeeds;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResultType;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapData;

import javax.annotation.Nullable;

public class FilledDeedItem extends FilledMapItem {
    public FilledDeedItem() {
        super(new Properties().maxStackSize(1));
    }

    @Override
    public ActionResultType onItemUse(ItemUseContext context) {
        BlockState state = context.getWorld().getBlockState(context.getPos());
        if (state.getBlock() == Blocks.GRINDSTONE) {
            // delet the deed when using a grindstone
            DeedStorage.get(context.getWorld()).removeClaim(FilledMapItem.getMapId(context.getItem()));
            context.getPlayer().setHeldItem(context.getHand(), new ItemStack(CraftableDeeds.EMPTY_DEED.get()));
            return ActionResultType.SUCCESS;
        }
        return super.onItemUse(context);
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
