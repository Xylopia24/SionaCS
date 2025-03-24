package dev.xylopia.sionacs.core.registers;

import dev.xylopia.sionacs.SionaCore;
import dev.xylopia.sionacs.utils.Constants;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Central registry system for the SionaCS mod.
 * Handles registration of all game elements.
 */
public class SionaRegister {
    // DeferredRegisters for different types of game objects
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, Constants.MOD_ID);
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, Constants.MOD_ID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, Constants.MOD_ID);
    
    // Additional registries can be added here as needed
    
    /**
     * Initialize all registries and connect them to the mod event bus
     * @param eventBus The mod event bus to register with
     */
    public static void init(IEventBus eventBus) {
        SionaCore.LOGGER.info("Initializing SionaCS Registries");
        
        // Register all the DeferredRegisters to the event bus
        ITEMS.register(eventBus);
        BLOCKS.register(eventBus);
        BLOCK_ENTITIES.register(eventBus);
        
        // Initialize any sub-registries
        // ModItems.register();
        // ModBlocks.register();
        
        SionaCore.LOGGER.info("SionaCS Registry initialization complete");
    }
    
    /**
     * Creates a ResourceLocation with the mod ID
     * @param path Path for the resource
     * @return A ResourceLocation with the mod ID and provided path
     */
    @SuppressWarnings("removal")
    public static ResourceLocation resource(String path) {
        return new ResourceLocation(Constants.MOD_ID, path);
    }
    
    /**
     * Register an item with a custom Item.Properties
     * @param name Registry name for the item
     * @param itemSupplier Supplier function for the item
     * @return RegistryObject reference to the registered item
     */
    public static <T extends Item> RegistryObject<T> registerItem(String name, java.util.function.Supplier<T> itemSupplier) {
        return ITEMS.register(name, itemSupplier);
    }
    
    /**
     * Register a block with a custom Block.Properties
     * @param name Registry name for the block
     * @param blockSupplier Supplier function for the block
     * @return RegistryObject reference to the registered block
     */
    public static <T extends Block> RegistryObject<T> registerBlock(String name, java.util.function.Supplier<T> blockSupplier) {
        return BLOCKS.register(name, blockSupplier);
    }
    
    /**
     * Register a block with its corresponding item
     * @param name Registry name for the block and item
     * @param blockSupplier Supplier function for the block
     * @param itemProperties Properties for the automatically generated item
     * @return RegistryObject reference to the registered block
     */
    public static <T extends Block> RegistryObject<T> registerBlockWithItem(String name, java.util.function.Supplier<T> blockSupplier, Item.Properties itemProperties) {
        RegistryObject<T> block = registerBlock(name, blockSupplier);
        registerItem(name, () -> new net.minecraft.world.item.BlockItem(block.get(), itemProperties));
        return block;
    }
}
