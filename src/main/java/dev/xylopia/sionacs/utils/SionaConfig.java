package dev.xylopia.sionacs.utils;

// Forge imports
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

// Siona imports
import org.apache.commons.lang3.tuple.Pair;

/**
 * Configuration handler for Siona: Chronosphere.
 * This class manages mod settings through Forge's config system.
 */
@Mod.EventBusSubscriber(modid = Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class SionaConfig {
    
    // Common Configuration
    public static class Common {
        // TARDIM Peripheral Settings
        public final ForgeConfigSpec.BooleanValue enableTardimPeripheral;
        public final ForgeConfigSpec.IntValue tardimPeripheralRange;
        public final ForgeConfigSpec.BooleanValue tardimPeripheralRequiresFuel;
        
        // Integration Settings
        public final ForgeConfigSpec.BooleanValue enableComputerCraftIntegration;
        
        public Common(ForgeConfigSpec.Builder builder) {
            builder.comment("Siona: Chronosphere - Common Configuration")
                   .push("common");
            
            // TARDIM Peripheral Settings
            builder.comment("TARDIM Peripheral Settings")
                   .push("tardim_peripheral");
            
            enableTardimPeripheral = builder
                    .comment("Enable or disable the TARDIM peripheral functionality")
                    .define("enableTardimPeripheral", true);
            
            tardimPeripheralRange = builder
                    .comment("Maximum range for TARDIM peripheral operations (in blocks)")
                    .defineInRange("tardimPeripheralRange", 16, 1, 64);
            
            tardimPeripheralRequiresFuel = builder
                    .comment("Whether the TARDIM peripheral requires TARDIM fuel for operation")
                    .define("tardimPeripheralRequiresFuel", true);
            
            builder.pop();
            
            // Integration Settings
            builder.comment("Integration Settings")
                   .push("integration");
            
            enableComputerCraftIntegration = builder
                    .comment("Enable or disable the ComputerCraft integration")
                    .define("enableComputerCraftIntegration", true);
            
            builder.pop();
            
            builder.pop();
        }
    }
    
    // Client Configuration
    public static class Client {
        // Visual Settings
        public final ForgeConfigSpec.BooleanValue showDetailedTooltips;
        public final ForgeConfigSpec.BooleanValue enableFancyRendering;
        
        // Sound Settings
        public final ForgeConfigSpec.BooleanValue enableCustomSounds;
        public final ForgeConfigSpec.DoubleValue soundVolume;
        
        public Client(ForgeConfigSpec.Builder builder) {
            builder.comment("Siona: Chronosphere - Client Configuration")
                   .push("client");
            
            // Visual Settings
            builder.comment("Visual Settings")
                   .push("visuals");
            
            showDetailedTooltips = builder
                    .comment("Show detailed tooltips with extra information")
                    .define("showDetailedTooltips", true);
            
            enableFancyRendering = builder
                    .comment("Enable special visual effects and animations")
                    .define("enableFancyRendering", true);
            
            builder.pop();
            
            // Sound Settings
            builder.comment("Sound Settings")
                   .push("sound");
            
            enableCustomSounds = builder
                    .comment("Enable custom sound effects")
                    .define("enableCustomSounds", true);
            
            soundVolume = builder
                    .comment("Volume multiplier for mod sounds (0.0-1.0)")
                    .defineInRange("soundVolume", 0.8, 0.0, 1.0);
            
            builder.pop();
            
            builder.pop();
        }
    }
    
    // Create the configuration specifications
    public static final ForgeConfigSpec COMMON_SPEC;
    public static final Common COMMON;
    
    public static final ForgeConfigSpec CLIENT_SPEC;
    public static final Client CLIENT;
    
    static {
        // Build Common Config
        final Pair<Common, ForgeConfigSpec> commonSpecPair = new ForgeConfigSpec.Builder().configure(Common::new);
        COMMON = commonSpecPair.getLeft();
        COMMON_SPEC = commonSpecPair.getRight();
        
        // Build Client Config
        final Pair<Client, ForgeConfigSpec> clientSpecPair = new ForgeConfigSpec.Builder().configure(Client::new);
        CLIENT = clientSpecPair.getLeft();
        CLIENT_SPEC = clientSpecPair.getRight();
    }
    
    /**
     * Handle the config loading event
     */
    @SubscribeEvent
    public static void onLoad(final ModConfigEvent.Loading event) {
        Constants.LOGGER.debug("Loaded {} config file {}", Constants.MOD_ID, event.getConfig().getFileName());
    }
    
    /**
     * Handle the config reloading event
     */
    @SubscribeEvent
    public static void onReload(final ModConfigEvent.Reloading event) {
        Constants.LOGGER.debug("Reloaded {} config file {}", Constants.MOD_ID, event.getConfig().getFileName());
    }
}
