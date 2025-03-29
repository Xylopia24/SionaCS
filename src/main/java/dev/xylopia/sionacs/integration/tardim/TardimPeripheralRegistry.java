package dev.xylopia.sionacs.integration.tardim;

import dev.xylopia.sionacs.integration.tardim.peripheral.TardimPeripheralBlock;
import dev.xylopia.sionacs.integration.tardim.peripheral.TardimPeripheralBlockEntity;
import dev.xylopia.sionacs.utils.Constants;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.BlockEntityType.Builder;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Registry for TARDIM peripheral blocks and block entities
 */
public class TardimPeripheralRegistry {
    private static final Logger LOGGER = LoggerFactory.getLogger(Constants.MOD_ID + ":TardimRegistry");
    
    // Create DeferredRegisters for TARDIM peripheral components
    private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, Constants.MOD_ID);
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, Constants.MOD_ID);
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = 
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, Constants.MOD_ID);

    // Register the TARDIM peripheral block
    public static final RegistryObject<Block> TARDIM_PERIPHERAL_BLOCK = BLOCKS.register("tardim_peripheral", 
            TardimPeripheralBlock::new);
    
    // Register the block item for the TARDIM peripheral
    public static final RegistryObject<Item> TARDIM_PERIPHERAL_ITEM = ITEMS.register("tardim_peripheral", 
            () -> new BlockItem(TARDIM_PERIPHERAL_BLOCK.get(), new Item.Properties()));
    
    // Register the TARDIM peripheral block entity
    @SuppressWarnings("null")
public static final RegistryObject<BlockEntityType<TardimPeripheralBlockEntity>> TARDIM_PERIPHERAL_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("tardim_peripheral", 
                    () -> Builder.of(
                            TardimPeripheralBlockEntity::new, 
                            TARDIM_PERIPHERAL_BLOCK.get()
                    ).build(null));

    /**
     * Register all TARDIM peripheral components with the event bus
     */
    public static void register(IEventBus eventBus) {
        LOGGER.info("Registering TARDIM peripherals");
        BLOCKS.register(eventBus);
        ITEMS.register(eventBus);
        BLOCK_ENTITIES.register(eventBus);
    }
}
