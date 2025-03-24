package dev.xylopia.sionacs;

// Siona Imports
import dev.xylopia.sionacs.utils.Constants;
import dev.xylopia.sionacs.utils.SionaConfig;
import dev.xylopia.sionacs.core.registers.SionaRegister;

// Forge Imports
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(Constants.MOD_ID)
public class SionaCore {
    // Logger for mod-related messages
    public static final Logger LOGGER = LogManager.getLogger(Constants.MOD_ID);
    
    // Instance of the mod (useful for some operations)
    public static SionaCore instance;
    
    /**
     * Main constructor for the mod
     * Sets up event listeners and initializes components
     */
    @SuppressWarnings("removal")
    public SionaCore() {
        instance = this;
        LOGGER.info("Initializing SionaCS - Computer Systems Extension");
        
        // Get the mod event bus
        final IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        
        // Register configuration
        SionaConfig.register(ModLoadingContext.get());
        modEventBus.addListener(SionaConfig::onLoad);
        
        // Register lifecycle events
        modEventBus.addListener(this::setup);
        modEventBus.addListener(this::clientSetup);
        
        // Initialize registry system
        SionaRegister.init(modEventBus);
        
        // Register ourselves for server and other game events
        MinecraftForge.EVENT_BUS.register(this);
        
        LOGGER.info("SionaCS initialization complete");
    }
    
    /**
     * Common setup method - runs during mod initialization
     * @param event The FMLCommonSetupEvent
     */
    private void setup(final FMLCommonSetupEvent event) {
        LOGGER.info("SionaCS common setup");
        
        // Other common setup code...
    }
    
    /**
     * Client-side setup method - runs during mod initialization on client only
     * @param event The FMLClientSetupEvent
     */
    private void clientSetup(final FMLClientSetupEvent event) {
        LOGGER.info("SionaCS client setup");
        // Client-specific setup code goes here
    }
}
