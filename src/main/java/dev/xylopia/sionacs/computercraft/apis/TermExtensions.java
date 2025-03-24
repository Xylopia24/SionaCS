package dev.xylopia.sionacs.computercraft.apis;

import dan200.computercraft.core.terminal.Terminal;
import dev.xylopia.sionacs.SionaCore;
import dev.xylopia.sionacs.utils.SionaConfig;
import java.util.WeakHashMap;

/**
 * Extensions to the Terminal class for SionaCS mod.
 * Provides utilities for toggling CRT effects on a per-terminal basis.
 */
public class TermExtensions {
    // Using WeakHashMap to avoid memory leaks when terminals are destroyed
    private static final WeakHashMap<Terminal, Boolean> terminalCRTEnabled = new WeakHashMap<>();
    
    /**
     * Check if CRT effect is enabled for a terminal
     * @param terminal The terminal to check
     * @return true if CRT effect is enabled, false otherwise
     */
    public static boolean isCRTEnabled(Terminal terminal) {
        if (terminal == null) {
            SionaCore.LOGGER.warn("Attempted to check CRT status of null terminal");
            return false;
        }
        
        // Get status with proper default fallback
        Boolean status = terminalCRTEnabled.get(terminal);
        boolean result = status != null ? status : SionaConfig.getDefaultCRTEffect();
        
        // Debug log every time to trace this issue
        SionaCore.LOGGER.debug("CRT status for terminal {} is {}", 
            terminal.hashCode(), result);
        
        return result;
    }
    
    /**
     * Set CRT effect for a terminal
     * @param terminal The terminal to modify
     * @param enabled Whether CRT effect should be enabled
     */
    public static void setCRTEnabled(Terminal terminal, boolean enabled) {
        if (terminal == null) {
            SionaCore.LOGGER.warn("Attempted to set CRT status of null terminal");
            return;
        }
        
        // Get previous value for logging
        Boolean oldValue = terminalCRTEnabled.get(terminal);
        boolean oldStatus = oldValue != null ? oldValue : SionaConfig.getDefaultCRTEffect();
        
        // Only update if the status changed
        if (oldStatus != enabled) {
            SionaCore.LOGGER.info("Setting CRT status for terminal {} from {} to {}", 
                terminal.hashCode(), oldStatus, enabled);
            
            // Update the status in our map
            terminalCRTEnabled.put(terminal, enabled);
            
            // Ensure terminal is redrawn
            terminal.setChanged();
        } else {
            SionaCore.LOGGER.debug("CRT status unchanged for terminal {}: {}", 
                terminal.hashCode(), enabled);
        }
        
        // Verify the change was applied
        Boolean newValue = terminalCRTEnabled.get(terminal);
        SionaCore.LOGGER.debug("Terminal {} CRT status after update: {}", 
            terminal.hashCode(), newValue);
    }
    
    /**
     * Debug method to log all terminal CRT statuses
     */
    public static void logAllTerminalStatuses() {
        SionaCore.LOGGER.info("Current terminal CRT statuses:");
        terminalCRTEnabled.forEach((terminal, status) -> {
            SionaCore.LOGGER.info("Terminal {}: {}", terminal.hashCode(), status);
        });
        SionaCore.LOGGER.info("Total tracked terminals: {}", terminalCRTEnabled.size());
    }
}
