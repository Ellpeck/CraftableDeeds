package de.ellpeck.craftabledeeds;

import de.ellpeck.craftabledeeds.items.EmptyDeedItem;
import de.ellpeck.craftabledeeds.items.FilledDeedItem;
import net.minecraft.item.Item;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig.Type;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Mod(CraftableDeeds.ID)
public class CraftableDeeds {

    public static final String ID = "craftabledeeds";

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, ID);
    public static final RegistryObject<Item> EMPTY_DEED = ITEMS.register("empty_deed", EmptyDeedItem::new);
    public static final RegistryObject<Item> FILLED_DEED = ITEMS.register("filled_deed", FilledDeedItem::new);

    public static ForgeConfigSpec.ConfigValue<Boolean> requireItemFrames;
    public static ForgeConfigSpec.ConfigValue<Boolean> allowOpeningBlocks;
    public static ForgeConfigSpec.ConfigValue<List<? extends String>> allowedDimensions;

    public CraftableDeeds() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        bus.addListener(this::init);
        ITEMS.register(bus);

        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        requireItemFrames = builder.comment("Whether the deed needs to be in an item frame inside the claimed area for the claim to be valid").define("requireItemFrames", true);
        allowOpeningBlocks = builder.comment("Whether opening blocks (like furnaces and chests) is allowed inside other players' claims").define("allowOpeningBlocks", false);
        allowedDimensions = builder.comment("The dimension ids of dimensions that using claims is allowed in").defineList("allowedDimensions", Arrays.asList("minecraft:overworld", "minecraft:the_nether", "minecraft:the_end"), o -> true);
        ModLoadingContext.get().registerConfig(Type.COMMON, builder.build());
    }

    private void init(FMLCommonSetupEvent event) {
        PacketHandler.init();
    }
}
