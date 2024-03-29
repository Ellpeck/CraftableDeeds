package de.ellpeck.craftabledeeds;

import de.ellpeck.craftabledeeds.blocks.DeedPedestalBlock;
import de.ellpeck.craftabledeeds.blocks.DeedPedestalRenderer;
import de.ellpeck.craftabledeeds.blocks.DeedPedestalTileEntity;
import de.ellpeck.craftabledeeds.items.EmptyDeedItem;
import de.ellpeck.craftabledeeds.items.FilledDeedItem;
import de.ellpeck.craftabledeeds.ui.DeedPedestalContainer;
import de.ellpeck.craftabledeeds.ui.DeedPedestalScreen;
import net.minecraft.block.Block;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig.Type;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Mod(CraftableDeeds.ID)
public class CraftableDeeds {

    public static final String ID = "craftabledeeds";

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, ID);
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, ID);
    public static final DeferredRegister<TileEntityType<?>> TILES = DeferredRegister.create(ForgeRegistries.TILE_ENTITIES, ID);
    public static final DeferredRegister<ContainerType<?>> CONTAINERS = DeferredRegister.create(ForgeRegistries.CONTAINERS, ID);

    public static final RegistryObject<Item> EMPTY_DEED = ITEMS.register("empty_deed", EmptyDeedItem::new);
    public static final RegistryObject<Item> FILLED_DEED = ITEMS.register("filled_deed", FilledDeedItem::new);

    public static final RegistryObject<Block> DEED_PEDESTAL_BLOCK = BLOCKS.register("deed_pedestal", DeedPedestalBlock::new);
    public static final RegistryObject<Item> DEED_PEDESTAL_ITEM = ITEMS.register("deed_pedestal", () -> new BlockItem(DEED_PEDESTAL_BLOCK.get(), new Item.Properties().group(ItemGroup.DECORATIONS).isImmuneToFire()));
    public static final RegistryObject<TileEntityType<DeedPedestalTileEntity>> DEED_PEDESTAL_TILE = TILES.register("deed_pedestal", () -> TileEntityType.Builder.create(DeedPedestalTileEntity::new, DEED_PEDESTAL_BLOCK.get()).build(null));
    public static final RegistryObject<ContainerType<DeedPedestalContainer>> DEED_PEDESTAL_CONTAINER = CONTAINERS.register("deed_pedestal", () -> IForgeContainerType.create((id, inv, data) -> new DeedPedestalContainer(id, inv.player, data.readBlockPos())));

    public static ForgeConfigSpec.ConfigValue<Boolean> requirePedestals;
    public static ForgeConfigSpec.ConfigValue<Boolean> allowOpeningBlocks;
    public static ForgeConfigSpec.ConfigValue<List<? extends String>> allowedDimensions;
    public static ForgeConfigSpec.ConfigValue<Integer> claimCooldown;
    public static ForgeConfigSpec.ConfigValue<List<? extends String>> breakableBlocks;
    public static ForgeConfigSpec.ConfigValue<List<? extends String>> interactableBlocks;
    public static ForgeConfigSpec.ConfigValue<Boolean> allowTntExplosions;
    public static ForgeConfigSpec.ConfigValue<Boolean> allowCreeperExplosions;
    public static ForgeConfigSpec.ConfigValue<Boolean> allowWitherExplosions;
    public static ForgeConfigSpec.ConfigValue<Integer> deedBypassPermissionLevel;
    public static ForgeConfigSpec.ConfigValue<List<? extends String>> additionalLoyalMobs;

    public CraftableDeeds() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        bus.addListener(PacketHandler::init);
        bus.addListener(Client::init);
        ITEMS.register(bus);
        BLOCKS.register(bus);
        TILES.register(bus);
        CONTAINERS.register(bus);

        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        requirePedestals = builder.comment("Whether the deed needs to be in a deed pedestal inside the claimed area for the claim to be valid").define("requirePedestals", true);
        allowOpeningBlocks = builder.comment("Whether opening blocks (like furnaces and chests) is allowed inside other players' claims").define("allowOpeningBlocks", false);
        allowedDimensions = builder.comment("The dimension ids of dimensions that using claims is allowed in. To allow all dimensions, add an entry \"*\"").defineList("allowedDimensions", Arrays.asList("minecraft:overworld", "minecraft:the_nether", "minecraft:the_end"), o -> true);
        claimCooldown = builder.comment("The amount of hours that have to pass before a destroyed claim's area can be claimed by anyone but the previous owner again").define("claimCooldown", 12);
        breakableBlocks = builder.comment("The list of blocks that can be broken in an area even if it is claimed, supports regex").defineList("breakableBlocks", Collections.emptyList(), o -> true);
        interactableBlocks = builder.comment("The list of blocks that can be interacted with in an area even if it is claimed, supports regex").defineList("interactableBlocks", Arrays.asList("minecraft:lever", ".*_door", ".*_fence_gate", ".*_button"), o -> true);
        deedBypassPermissionLevel = builder.comment("The permission level required to bypass deed restrictions").define("deedBypassPermissionLevel", 2);
        additionalLoyalMobs = builder.comment("The registry names of additional mobs that should be considered loyal and attack players in a claim. Note that, if a mob is tameable, it only attacks if tamed").defineList("additionalLoyalMobs", Collections.emptyList(), o -> true);
        builder.push("explosions");
        allowTntExplosions = builder.comment("Whether TNT explosions are allowed in claimed areas").define("tnt", true);
        allowCreeperExplosions = builder.comment("Whether creeper explosions are allowed in claimed areas").define("creepers", false);
        allowWitherExplosions = builder.comment("Whether explosions caused by withers are allowed in claimed areas").define("withers", true);
        builder.pop();
        ModLoadingContext.get().registerConfig(Type.COMMON, builder.build());
    }

    private static class Client {

        private static void init(FMLClientSetupEvent event) {
            RenderTypeLookup.setRenderLayer(DEED_PEDESTAL_BLOCK.get(), RenderType.getCutout());
            ClientRegistry.bindTileEntityRenderer(DEED_PEDESTAL_TILE.get(), DeedPedestalRenderer::new);
            ScreenManager.registerFactory(DEED_PEDESTAL_CONTAINER.get(), DeedPedestalScreen::new);
        }
    }
}
