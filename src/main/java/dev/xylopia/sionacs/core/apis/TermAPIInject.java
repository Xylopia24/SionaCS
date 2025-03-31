package dev.xylopia.sionacs.core.apis;

import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.core.apis.TermAPI;
import dan200.computercraft.core.terminal.Terminal;
import dev.xylopia.sionacs.SionaCore;

import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

/**
 * Injects additional terminal methods into ComputerCraft's term API
 * Specifically adds support for CRT mode rendering
 */
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class TermAPIInject {

    /**
     * Register our API extensions during mod initialization
     */
    @SubscribeEvent
    public static void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            SionaCore.LOGGER.info("Registering Terminal API extensions");
            // We'll use a mixin-based approach instead of trying to register a custom API directly
        });
    }

    /**
     * Extension to the standard Term API that adds CRT mode capabilities
     * This class will be used as a reference for the mixin injection
     */
    public static class CRTTermMethods {
        /**
         * Gets whether CRT mode is enabled for this terminal
         * 
         * @param terminal The terminal to check
         * @return true if CRT mode is enabled, false otherwise
         */
        public static boolean getCRT(Terminal terminal) {
            return CRTModeTracker.isCRTModeEnabled(terminal);
        }
        
        /**
         * Sets CRT mode for a terminal to a specific state
         * 
         * @param terminal The terminal to modify
         * @param enabled Whether to enable or disable CRT mode
         * @return The new CRT mode state
         */
        public static boolean setCRT(Terminal terminal, boolean enabled) {
            if (enabled) {
                CRTModeTracker.enableCRTMode(terminal);
            } else {
                CRTModeTracker.disableCRTMode(terminal);
            }
            return enabled;
        }
        
        /**
         * Toggles CRT mode for a terminal
         * 
         * @param terminal The terminal to toggle CRT mode for
         * @return The new CRT mode state
         */
        public static boolean toggleCRT(Terminal terminal) {
            return CRTModeTracker.toggleCRTMode(terminal);
        }
    }
}
