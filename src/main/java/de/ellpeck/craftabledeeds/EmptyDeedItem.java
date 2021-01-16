package de.ellpeck.craftabledeeds;

import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;

public class EmptyDeedItem extends Item {
    public EmptyDeedItem() {
        super(new Properties().maxStackSize(1).group(ItemGroup.MISC));
    }
}
