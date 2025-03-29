package dev.xylopia.sionacs;

// Forge imports
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

// SionaCS imports
import dev.xylopia.sionacs.core.registers.SionaRegister;
import dev.xylopia.sionacs.utils.Constants;
import dev.xylopia.sionacs.utils.SionaConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main mod class for Siona: Chronosphere.
 * This class serves as the entry point for the mod and handles initialization.
 */
@Mod(Constants.MOD_ID)
public class SionaCore {
    public static final Logger LOGGER = LoggerFactory.getLogger(Constants.MOD_NAME);

    /**
     * Constructs the mod and sets up event handlers and registries.
     */
    @SuppressWarnings("removal")
    public SionaCore() {
        LOGGER.info("Initializing {}", Constants.MOD_NAME);
        
        // Get event buses
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        
        // Register config
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, SionaConfig.COMMON_SPEC);
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, SionaConfig.CLIENT_SPEC);
        
        // Register event handlers
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::clientSetup);
        
        // Register all content
        SionaRegister.register();
        
        // Register ourselves for server and other game events
        MinecraftForge.EVENT_BUS.register(this);
        
        LOGGER.info("{} initialization complete", Constants.MOD_NAME);
    }

    /**
     * Setup common mod components that are shared between client and server.
     * 
     * @param event The common setup event
     */
    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("Starting common setup for {}", Constants.MOD_NAME);
        
        event.enqueueWork(() -> {
            // Common setup operations that need to be thread-safe
        });
        
        LOGGER.info("Common setup complete for {}", Constants.MOD_NAME);
    }
    
    /**
     * Setup client-specific mod components.
     * 
     * @param event The client setup event
     */
    private void clientSetup(final FMLClientSetupEvent event) {
        LOGGER.info("Starting client setup for {}", Constants.MOD_NAME);
        
        event.enqueueWork(() -> {
            // Client setup operations that need to be thread-safe
        });
        
        LOGGER.info("Client setup complete for {}", Constants.MOD_NAME);
    }
}
