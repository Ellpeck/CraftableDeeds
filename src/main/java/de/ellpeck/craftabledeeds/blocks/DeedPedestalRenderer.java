package de.ellpeck.craftabledeeds.blocks;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.world.storage.MapData;

public class DeedPedestalRenderer extends TileEntityRenderer<DeedPedestalTileEntity> {

    public DeedPedestalRenderer(TileEntityRendererDispatcher rendererDispatcherIn) {
        super(rendererDispatcherIn);
    }

    @Override
    public void render(DeedPedestalTileEntity tile, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn) {
        MapData mapdata = tile.getMapData();
        if (mapdata != null) {
            PlayerEntity player = Minecraft.getInstance().player;
            double d0 = player.getPosX() - (tile.getPos().getX() + 0.5);
            double d1 = player.getPosZ() - (tile.getPos().getZ() + 0.5);
            matrixStackIn.translate(0.5F, 1.5F, 0.5F);
            matrixStackIn.translate(0, Math.sin((tile.getWorld().getGameTime() + partialTicks) / 15) * 0.05F, 0);
            matrixStackIn.rotate(Vector3f.ZP.rotationDegrees(180.0F));
            matrixStackIn.rotate(Vector3f.YP.rotation((float) (MathHelper.atan2(d1, d0) + Math.PI / 2)));
            matrixStackIn.scale(0.0078125F, 0.0078125F, 0.0078125F);
            matrixStackIn.translate(-64.0D, -64.0D, 0.0D);
            Minecraft.getInstance().gameRenderer.getMapItemRenderer().renderMap(matrixStackIn, bufferIn, mapdata, true, combinedLightIn);
        }
    }
}
