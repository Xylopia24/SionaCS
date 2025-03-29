package dev.xylopia.sionacs.core.registers;

import dev.xylopia.sionacs.integration.computercraft.ComputerCraftRegistration;
import dev.xylopia.sionacs.integration.tardim.TardimPeripheralRegistry;
import dev.xylopia.sionacs.utils.Constants;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

/**
 * Central class for registering all mod content
 */
@SuppressWarnings("unused")
public class SionaRegister {
    // Create DeferredRegisters for blocks and items
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, Constants.MOD_ID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, Constants.MOD_ID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = 
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, Constants.MOD_ID);
    
    /**
     * Register all mod content with the game
     */
    @SuppressWarnings("removal")
    public static void register() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        
        // Register our main deferred registers
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        BLOCK_ENTITIES.register(modEventBus);
        
        // Register content from other modules
        TardimPeripheralRegistry.register(modEventBus);
        ComputerCraftRegistration.register(modEventBus);
    }
    
    /**
     * Helper method to register a block with an item form
     * @param name Registry name for the block
     * @param blockSupplier Supplier for the block instance
     * @return Registry object for the registered block
     */
    public static <T extends Block> RegistryObject<T> registerBlock(String name, Supplier<T> blockSupplier) {
        RegistryObject<T> block = BLOCKS.register(name, blockSupplier);
        registerBlockItem(name, block);
        return block;
    }
    
    /**
     * Helper method to register a block item for a block
     * @param name Registry name for the item
     * @param block Block to create an item for
     * @return Registry object for the registered item
     */
    private static <T extends Block> RegistryObject<Item> registerBlockItem(String name, RegistryObject<T> block) {
        return ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }
}
