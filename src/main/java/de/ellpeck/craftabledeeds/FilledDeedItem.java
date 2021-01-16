package de.ellpeck.craftabledeeds;

import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;

public class FilledDeedItem extends Item {
    public FilledDeedItem() {
        super(new Properties().maxStackSize(1).group(ItemGroup.MISC));
    }
}
