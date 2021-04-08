package de.ellpeck.craftabledeeds;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapData;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

public class FilledDeedItem extends FilledMapItem {

    public FilledDeedItem() {
        super(new Properties().maxStackSize(1));
    }

    @Override
    public boolean hasEffect(ItemStack stack) {
        return true;
    }

    @Override
    public ActionResultType onItemUse(ItemUseContext context) {
        BlockState state = context.getWorld().getBlockState(context.getPos());
        // delet the deed when using a grindstone
        if (state.getBlock() == Blocks.GRINDSTONE) {
            if (!context.getWorld().isRemote) {
                DeedStorage storage = DeedStorage.get(context.getWorld());
                storage.removeClaim(FilledMapItem.getMapId(context.getItem()));
                context.getPlayer().setHeldItem(context.getHand(), new ItemStack(CraftableDeeds.EMPTY_DEED.get()));
                storage.markDirtyAndSend();
            }
            return ActionResultType.SUCCESS;
        }
        return super.onItemUse(context);
    }

    @Override
    public ActionResultType itemInteractionForEntity(ItemStack stack, PlayerEntity playerIn, LivingEntity target, Hand hand) {
        if (target instanceof PlayerEntity) {
            DeedStorage storage = DeedStorage.get(playerIn.world);
            DeedStorage.Claim claim = storage.getClaim(getMapId(stack));
            if (claim == null)
                return ActionResultType.FAIL;
            if (!playerIn.world.isRemote) {
                if (claim.friends.contains(target.getUniqueID())) {
                    claim.friends.remove(target.getUniqueID());
                    playerIn.sendStatusMessage(new TranslationTextComponent("info." + CraftableDeeds.ID + ".removed_friend", target.getDisplayName()), true);
                } else {
                    claim.friends.add(target.getUniqueID());
                    playerIn.sendStatusMessage(new TranslationTextComponent("info." + CraftableDeeds.ID + ".added_friend", target.getDisplayName()), true);
                }
                storage.markDirtyAndSend();
            }
            return ActionResultType.SUCCESS;
        }
        return ActionResultType.PASS;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        DeedStorage.Claim claim = DeedStorage.get(worldIn).getClaim(getMapId(stack));
        if (claim == null)
            return;
        PlayerEntity owner = worldIn.getPlayerByUuid(claim.owner);
        tooltip.add(new TranslationTextComponent("info." + CraftableDeeds.ID + ".owner", owner == null ? claim.owner : owner.getDisplayName()));
        if (!claim.friends.isEmpty()) {
            tooltip.add(new TranslationTextComponent("info." + CraftableDeeds.ID + ".friends"));
            for (UUID id : claim.friends) {
                PlayerEntity friend = worldIn.getPlayerByUuid(id);
                tooltip.add(friend == null ? new StringTextComponent(id.toString()) : friend.getDisplayName());
            }
        }
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
