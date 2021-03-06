package de.ellpeck.craftabledeeds;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapData;

public class EmptyDeedItem extends Item {
    public EmptyDeedItem() {
        super(new Properties().maxStackSize(1).group(ItemGroup.MISC));
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand handIn) {
        ItemStack held = playerIn.getHeldItem(handIn);

        // if there is already a claim here, don't let us overwrite it
        DeedStorage.Claim existing = DeedStorage.get(worldIn).getClaim(playerIn.getPosX(), 64, playerIn.getPosZ());
        if (existing != null)
            return ActionResult.resultFail(held);

        if (!playerIn.abilities.isCreativeMode)
            held.shrink(1);

        ItemStack filled = new ItemStack(CraftableDeeds.FILLED_DEED.get());
        createMapData(filled, playerIn, MathHelper.floor(playerIn.getPosX()), MathHelper.floor(playerIn.getPosZ()), (byte) 0, true, false);

        playerIn.addStat(Stats.ITEM_USED.get(this));
        playerIn.playSound(SoundEvents.UI_CARTOGRAPHY_TABLE_TAKE_RESULT, 1, 1);
        playerIn.playSound(SoundEvents.ENTITY_PLAYER_LEVELUP, 1, 1);

        if (held.isEmpty()) {
            return ActionResult.func_233538_a_(filled, worldIn.isRemote());
        } else {
            if (!playerIn.inventory.addItemStackToInventory(filled.copy()))
                playerIn.dropItem(filled, false);
            return ActionResult.func_233538_a_(held, worldIn.isRemote());
        }
    }

    public static MapData createMapData(ItemStack stack, PlayerEntity player, int x, int z, int scale, boolean trackingPosition, boolean unlimitedTracking) {
        int id = player.world.getNextMapId();
        MapData ret = new MapData(FilledMapItem.getMapName(id));
        ret.initData(x, z, scale, trackingPosition, unlimitedTracking, player.world.getDimensionKey());
        player.world.registerMapData(ret);
        if (!player.world.isRemote)
            DeedStorage.get(player.world).addClaim(id, player);
        stack.getOrCreateTag().putInt("map", id);
        return ret;
    }
}
