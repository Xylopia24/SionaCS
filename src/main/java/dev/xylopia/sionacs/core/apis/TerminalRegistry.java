package dev.xylopia.sionacs.core.apis;

import dan200.computercraft.core.terminal.Terminal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry to track terminals by ID for the custom CRT API
 */
public class TerminalRegistry {
    // Use a ConcurrentHashMap for thread safety
    private static final Map<Integer, Terminal> terminals = new ConcurrentHashMap<>();
    
    /**
     * Register a terminal with a unique ID
     * 
     * @param id The ID to register the terminal with
     * @param terminal The terminal to register
     */
    public static void registerTerminal(int id, Terminal terminal) {
        if (terminal != null) {
            terminals.put(id, terminal);
        }
    }
    
    /**
     * Remove a terminal from the registry
     * 
     * @param id The ID of the terminal to remove
     */
    public static void unregisterTerminal(int id) {
        terminals.remove(id);
    }
    
    /**
     * Get a terminal by its ID
     * 
     * @param id The ID of the terminal to get
     * @return The terminal, or null if not found
     */
    public static Terminal getTerminal(int id) {
        return terminals.get(id);
    }
}
