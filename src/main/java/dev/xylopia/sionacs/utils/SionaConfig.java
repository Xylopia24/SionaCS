package dev.xylopia.sionacs.utils;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.apache.commons.lang3.tuple.Pair;

import dev.xylopia.sionacs.SionaCore;

/**
 * Configuration management for SionaCS mod.
 * Handles client, common, and server configurations.
 */
public class SionaConfig {
    // Client Configuration
    public static class Client {
        // Default CRT effect setting for new terminals
        public final ForgeConfigSpec.BooleanValue defaultCRTEffect;
        
        // Default CRT intensity (could be used for varying effects)
        public final ForgeConfigSpec.DoubleValue crtIntensity;

        Client(ForgeConfigSpec.Builder builder) {
            builder.comment("Client-side settings for SionaCS")
                   .push("general");
            
            defaultCRTEffect = builder
                    .comment("Default CRT effect for all terminals")
                    .define("defaultCRTEffect", false);
            
            crtIntensity = builder
                    .comment("Intensity of the CRT effect (0.0 to 1.0)")
                    .defineInRange("crtIntensity", 1.0, 0.0, 1.0);
            
            builder.pop();
        }
    }
    
    // Common Configuration (synced between server and client)
    public static class Common {
        // Add common configuration settings here
        
        Common(ForgeConfigSpec.Builder builder) {
            builder.comment("Common settings for SionaCS")
                   .push("general");
            
            // Add common settings here
            
            builder.pop();
        }
    }
    
    // Server Configuration
    public static class Server {
        // Add server-specific configuration settings here
        
        Server(ForgeConfigSpec.Builder builder) {
            builder.comment("Server settings for SionaCS")
                   .push("general");
            
            // Add server settings here
            
            builder.pop();
        }
    }
    
    // Public instances of the configurations
    public static final Client CLIENT;
    public static final ForgeConfigSpec CLIENT_SPEC;
    
    public static final Common COMMON;
    public static final ForgeConfigSpec COMMON_SPEC;
    
    public static final Server SERVER;
    public static final ForgeConfigSpec SERVER_SPEC;
    
    // Initialize configuration specs
    static {
        final Pair<Client, ForgeConfigSpec> clientSpecPair = new ForgeConfigSpec.Builder().configure(Client::new);
        CLIENT = clientSpecPair.getLeft();
        CLIENT_SPEC = clientSpecPair.getRight();
        
        final Pair<Common, ForgeConfigSpec> commonSpecPair = new ForgeConfigSpec.Builder().configure(Common::new);
        COMMON = commonSpecPair.getLeft();
        COMMON_SPEC = commonSpecPair.getRight();
        
        final Pair<Server, ForgeConfigSpec> serverSpecPair = new ForgeConfigSpec.Builder().configure(Server::new);
        SERVER = serverSpecPair.getLeft();
        SERVER_SPEC = serverSpecPair.getRight();
    }
    
    // Register all configurations
    public static void register(final ModLoadingContext context) {
        SionaCore.LOGGER.info("Registering SionaCS configurations");
        
        context.registerConfig(ModConfig.Type.CLIENT, CLIENT_SPEC);
        context.registerConfig(ModConfig.Type.COMMON, COMMON_SPEC);
        context.registerConfig(ModConfig.Type.SERVER, SERVER_SPEC);
    }
    
    // List of handlers to call when configs are loaded/reloaded
    private static final List<Consumer<ModConfigEvent>> CALLBACKS = new ArrayList<>();
    
    // Register a callback for when configs are loaded/reloaded
    public static void addLoadCallback(Consumer<ModConfigEvent> callback) {
        CALLBACKS.add(callback);
    }
    
    // Called when configs are loaded/reloaded
    public static void onLoad(final ModConfigEvent event) {
        SionaCore.LOGGER.info("Loaded SionaCS configuration file {}", event.getConfig().getFileName());
        
        // Call all registered callbacks
        for (Consumer<ModConfigEvent> callback : CALLBACKS) {
            callback.accept(event);
        }
    }
    
    // Get settings from the configuration
    public static boolean getDefaultCRTEffect() {
        return CLIENT.defaultCRTEffect.get();
    }
    
    public static double getCRTIntensity() {
        return CLIENT.crtIntensity.get();
    }
}
