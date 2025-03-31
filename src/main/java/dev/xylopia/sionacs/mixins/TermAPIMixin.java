package dev.xylopia.sionacs.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.core.apis.TermAPI;
import dan200.computercraft.core.terminal.Terminal;
import dev.xylopia.sionacs.core.apis.CRTModeTracker;

/**
 * Mixin to add CRT mode functionality to ComputerCraft's TermAPI
 * This adds new methods rather than injecting into existing ones
 */
@Mixin(value = TermAPI.class, remap = false)
public abstract class TermAPIMixin {
    
    /**
     * Shadow method to get access to the terminal instance
     */
    @Shadow(remap = false)
    public abstract Terminal getTerminal() throws LuaException;
    
    /**
     * Gets whether CRT mode is enabled for the current terminal
     * 
     * @return true if CRT mode is enabled, false otherwise
     * @throws LuaException If the terminal is unavailable
     */
    @LuaFunction
    public final boolean getCRT() throws LuaException {
        Terminal terminal = this.getTerminal();
        return CRTModeTracker.isCRTModeEnabled(terminal);
    }
    
    /**
     * Sets or toggles CRT mode for this terminal
     * When called with no arguments, toggles CRT mode
     * When called with a boolean, sets CRT mode to that value
     * 
     * @param args Optional boolean to set CRT mode explicitly
     * @return The new CRT mode state
     * @throws LuaException if the terminal is not available or arguments are invalid
     */
    @LuaFunction
    public final boolean setCRT(Object... args) throws LuaException {
        Terminal terminal = this.getTerminal();
        
        if (args.length == 0) {
            // Toggle mode if no arguments
            return CRTModeTracker.toggleCRTMode(terminal);
        } else if (args[0] instanceof Boolean) {
            // Set mode explicitly if boolean provided
            boolean enabled = (Boolean) args[0];
            if (enabled) {
                CRTModeTracker.enableCRTMode(terminal);
            } else {
                CRTModeTracker.disableCRTMode(terminal);
            }
            return enabled;
        } else {
            throw new LuaException("Expected boolean or no argument");
        }
    }
}
