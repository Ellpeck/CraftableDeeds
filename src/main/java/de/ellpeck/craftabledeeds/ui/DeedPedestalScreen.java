package de.ellpeck.craftabledeeds.ui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import de.ellpeck.craftabledeeds.CraftableDeeds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.button.AbstractButton;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.Locale;
import java.util.function.Consumer;

public class DeedPedestalScreen extends ContainerScreen<DeedPedestalContainer> {

    private static final ResourceLocation TEXTURE = new ResourceLocation(CraftableDeeds.ID, "textures/ui/deed_pedestal.png");
    private Tab currentTab;

    public DeedPedestalScreen(DeedPedestalContainer screenContainer, PlayerInventory inv, ITextComponent titleIn) {
        super(screenContainer, inv, titleIn);
        this.xSize = 248;
        this.ySize = 166;
    }

    @Override
    protected void init() {
        super.init();
        this.setTab(Tab.PLAYERS);
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(matrixStack);
        super.render(matrixStack, mouseX, mouseY, partialTicks);
        this.renderHoveredTooltip(matrixStack, mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(MatrixStack matrixStack, float partialTicks, int x, int y) {
        this.minecraft.textureManager.bindTexture(TEXTURE);
        this.blit(matrixStack, this.guiLeft, this.guiTop, 0, 0, this.xSize, this.ySize);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(MatrixStack matrixStack, int x, int y) {
        this.font.func_243248_b(matrixStack, this.title, this.titleX, this.titleY, 4210752);
    }

    private void setTab(Tab newTab) {
        this.buttons.clear();
        this.children.clear();

        // add tabs
        int yOffset = 16;
        int uOffset = 0;
        for (Tab tab : Tab.values()) {
            this.addButton(new TabWidget(this.guiLeft - 28, this.guiTop + yOffset, 32, 28, 1 + uOffset, 167, tab));
            yOffset += 29;
            uOffset += 33;
        }

        // open tab
        this.currentTab = newTab;
        newTab.init.accept(this);
    }

    private enum Tab {
        PLAYERS(s -> {
        }),
        FACTIONS(s -> {
        }),
        BLOCKS(s -> {
        });

        public Consumer<DeedPedestalScreen> init;

        Tab(Consumer<DeedPedestalScreen> init) {
            this.init = init;
        }
    }

    private class TabWidget extends AbstractButton {

        private final Tab tab;
        private final int u;
        private final int v;

        public TabWidget(int x, int y, int width, int height, int u, int v, Tab tab) {
            super(x, y, width, height, new TranslationTextComponent("tab." + CraftableDeeds.ID + "." + tab.name().toLowerCase(Locale.ROOT)));
            this.u = u;
            this.v = v;
            this.tab = tab;
        }

        @Override
        public void onPress() {
            DeedPedestalScreen.this.setTab(this.tab);
        }

        @Override
        public void renderButton(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
            Minecraft.getInstance().getTextureManager().bindTexture(TEXTURE);
            int v = this.v;
            if (this.isHovered() || DeedPedestalScreen.this.currentTab == this.tab)
                v += this.height;

            RenderSystem.enableDepthTest();
            blit(matrixStack, this.x, this.y, (float) this.u, (float) v, this.width, this.height, 256, 256);
            if (this.isHovered()) {
                this.renderToolTip(matrixStack, mouseX, mouseY);
            }

        }
    }
}
