package de.ellpeck.craftabledeeds;

import net.minecraft.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

@Mod(CraftableDeeds.ID)
public class CraftableDeeds {

    public static final String ID = "craftabledeeds";

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, ID);
    public static final RegistryObject<Item> EMPTY_DEED = ITEMS.register("empty_deed", EmptyDeedItem::new);
    public static final RegistryObject<Item> FILLED_DEED = ITEMS.register("filled_deed", FilledDeedItem::new);

    public CraftableDeeds() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        ITEMS.register(bus);
    }
}
