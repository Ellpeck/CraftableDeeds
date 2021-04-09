package de.ellpeck.craftabledeeds.blocks;

import de.ellpeck.craftabledeeds.CraftableDeeds;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ContainerBlock;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nullable;

public class DeedPedestalBlock extends ContainerBlock {

    private static final VoxelShape SHAPE = makeCuboidShape(0, 0, 0, 16, 12, 16);

    public DeedPedestalBlock() {
        super(Properties.from(Blocks.STONE_BRICKS));
    }

    @Override
    public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
        TileEntity tile = worldIn.getTileEntity(pos);
        if (!(tile instanceof DeedPedestalTileEntity))
            return ActionResultType.FAIL;
        ItemStackHandler items = ((DeedPedestalTileEntity) tile).items;
        ItemStack pedestal = items.getStackInSlot(0);
        ItemStack hand = player.getHeldItem(handIn);
        if (pedestal.isEmpty()) {
            if (hand.getItem() == CraftableDeeds.FILLED_DEED.get()) {
                items.setStackInSlot(0, hand);
                player.setHeldItem(handIn, ItemStack.EMPTY);
                return ActionResultType.SUCCESS;
            }
        } else {
            if (!player.addItemStackToInventory(pedestal))
                worldIn.addEntity(new ItemEntity(worldIn, pos.getX() + 0.5F, pos.getY() + 1, pos.getZ() + 0.5F, pedestal));
            pedestal.setCount(0);
            return ActionResultType.SUCCESS;
        }
        return ActionResultType.FAIL;
    }

    @Override
    public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            TileEntity tile = worldIn.getTileEntity(pos);
            if (tile instanceof DeedPedestalTileEntity) {
                IItemHandler handler = ((DeedPedestalTileEntity) tile).items;
                for (int i = 0; i < handler.getSlots(); i++) {
                    ItemStack stack = handler.getStackInSlot(i);
                    if (!stack.isEmpty())
                        worldIn.addEntity(new ItemEntity(worldIn, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, stack));
                }
            }
        }
        super.onReplaced(state, worldIn, pos, newState, isMoving);
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(IBlockReader worldIn) {
        return new DeedPedestalTileEntity();
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        return SHAPE;
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }
}
