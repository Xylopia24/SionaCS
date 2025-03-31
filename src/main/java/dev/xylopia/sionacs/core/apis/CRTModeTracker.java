package dev.xylopia.sionacs.core.apis;

import dan200.computercraft.core.terminal.Terminal;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Tracks which terminals have CRT mode enabled
 * Uses WeakHashMap to allow terminals to be garbage collected when no longer referenced
 */
public class CRTModeTracker {
    
    // Using WeakHashMap so terminals can be garbage collected when no longer needed
    private static final Map<Terminal, Boolean> crtEnabledTerminals = 
            Collections.synchronizedMap(new WeakHashMap<>());
    
    /**
     * Check if CRT mode is enabled for a terminal
     * 
     * @param terminal The terminal to check
     * @return true if CRT mode is enabled, false otherwise
     */
    public static boolean isCRTModeEnabled(Terminal terminal) {
        if (terminal == null) return false;
        return crtEnabledTerminals.getOrDefault(terminal, false);
    }
    
    /**
     * Enable CRT mode for a terminal
     * 
     * @param terminal The terminal to enable CRT mode for
     */
    public static void enableCRTMode(Terminal terminal) {
        if (terminal != null) {
            crtEnabledTerminals.put(terminal, true);
        }
    }
    
    /**
     * Disable CRT mode for a terminal
     * 
     * @param terminal The terminal to disable CRT mode for
     */
    public static void disableCRTMode(Terminal terminal) {
        if (terminal != null) {
            crtEnabledTerminals.put(terminal, false);
        }
    }
    
    /**
     * Toggle CRT mode for a terminal
     * 
     * @param terminal The terminal to toggle CRT mode for
     * @return The new state (true = enabled, false = disabled)
     */
    public static boolean toggleCRTMode(Terminal terminal) {
        if (terminal == null) return false;
        
        boolean newState = !isCRTModeEnabled(terminal);
        crtEnabledTerminals.put(terminal, newState);
        return newState;
    }
    
    /**
     * Clear CRT mode data for a terminal when it's no longer needed
     * This is optional since we're using WeakHashMap, but can help with immediate cleanup
     * 
     * @param terminal The terminal to clear
     */
    public static void clearTerminal(Terminal terminal) {
        if (terminal != null) {
            crtEnabledTerminals.remove(terminal);
        }
    }
}
