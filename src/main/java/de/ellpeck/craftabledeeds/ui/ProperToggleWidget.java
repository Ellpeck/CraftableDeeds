package de.ellpeck.craftabledeeds.ui;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.ToggleWidget;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.client.gui.GuiUtils;

import java.util.Collections;
import java.util.function.Consumer;

// the default toggle widget doesn't actually get toggled on click because why would it
// the recipe book just iterates all of its toggle widgets on click instead... why
public class ProperToggleWidget extends ToggleWidget {

    private final Consumer<Boolean> onToggled;

    public ProperToggleWidget(int xIn, int yIn, int widthIn, int heightIn, int xTexStartIn, int yTexStartIn, int xDiffTexIn, int yDiffTexIn, ResourceLocation resourceLocationIn, ITextComponent tooltip, boolean triggered, Consumer<Boolean> onToggled) {
        super(xIn, yIn, widthIn, heightIn, triggered);
        this.initTextureValues(xTexStartIn, yTexStartIn, xDiffTexIn, yDiffTexIn, resourceLocationIn);
        this.onToggled = onToggled;
        this.setMessage(tooltip);
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        this.stateTriggered = !this.stateTriggered;
        this.onToggled.accept(this.stateTriggered);
    }

    @Override
    public void renderToolTip(MatrixStack matrixStack, int mouseX, int mouseY) {
        GuiUtils.drawHoveringText(matrixStack, Collections.singletonList(this.getMessage()), mouseX, mouseY, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, Minecraft.getInstance().fontRenderer);
    }
}
