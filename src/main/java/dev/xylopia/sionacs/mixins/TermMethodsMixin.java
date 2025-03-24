package dev.xylopia.sionacs.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.core.apis.TermMethods;
import dan200.computercraft.core.terminal.Terminal;
import dev.xylopia.sionacs.computercraft.apis.TermExtensions;

/**
 * Mixin to add CRT effect toggle functionality to TermMethods
 * This makes the functions available in Lua via the term API
 */
@Mixin(TermMethods.class)
public abstract class TermMethodsMixin {
    
    /**
     * Gets the target terminal - abstract method in TermMethods that we need to use
     */
    public abstract Terminal getTerminal() throws LuaException;
    
    /**
     * Sets whether CRT effect is enabled for this terminal
     * @param enabled true to enable CRT effect, false to disable
     * @return always true (for Lua function chaining)
     */
    @Unique
    @LuaFunction
    public final boolean setCRT(boolean enabled) throws LuaException {
        Terminal terminal = this.getTerminal();
        synchronized(terminal) {
            TermExtensions.setCRTEnabled(terminal, enabled);
        }
        return true;
    }
    
    /**
     * Gets whether CRT effect is enabled for this terminal
     * @return true if CRT effect is enabled, false otherwise
     */
    @Unique
    @LuaFunction
    public final boolean getCRT() throws LuaException {
        return TermExtensions.isCRTEnabled(this.getTerminal());
    }
}
