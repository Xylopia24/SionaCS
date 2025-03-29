package dev.xylopia.sionacs.integration.computercraft;

import dan200.computercraft.api.lua.IArguments;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.api.lua.MethodResult;
import dan200.computercraft.api.peripheral.AttachedComputerSet;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IDynamicPeripheral;
import dan200.computercraft.api.peripheral.IPeripheral;
import dev.xylopia.sionacs.integration.tardim.cc_api.TardimAPI;
import dev.xylopia.sionacs.integration.tardim.peripheral.TardimPeripheralBlockEntity;
import dev.xylopia.sionacs.utils.Constants;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * The peripheral implementation for TARDIM control.
 * This class serves as a bridge between ComputerCraft and the TARDIM functionality.
 */
public class TardimPeripheral implements IDynamicPeripheral {
    private static final Logger LOGGER = LoggerFactory.getLogger(Constants.MOD_ID + ":TardimPeripheral");
    
    private final TardimPeripheralBlockEntity blockEntity;
    private final TardimAPI api;
    private final AttachedComputerSet computers = new AttachedComputerSet();
    private final String[] methodNames;
    private final Map<String, Method> methods = new HashMap<>();
    
    public TardimPeripheral(TardimPeripheralBlockEntity blockEntity) {
        this.blockEntity = blockEntity;
        this.api = new TardimAPI(blockEntity);
        LOGGER.debug("Created new TARDIM peripheral for block entity at {}", blockEntity.getBlockPos());
        
        // Collect all @LuaFunction methods from the API
        methodNames = Arrays.stream(TardimAPI.class.getMethods())
            .filter(method -> method.isAnnotationPresent(LuaFunction.class))
            .map(Method::getName)
            .toArray(String[]::new);
        
        // Map method names to Method objects for faster lookup
        for (Method method : TardimAPI.class.getMethods()) {
            if (method.isAnnotationPresent(LuaFunction.class)) {
                methods.put(method.getName(), method);
            }
        }
    }
    
    @NotNull
    @Override
    public String getType() {
        return "tardim";
    }
    
    @Override
    public Set<String> getAdditionalTypes() {
        return Set.of("tardis");
    }
    
    @Override
    public boolean equals(@Nullable IPeripheral other) {
        if (this == other) return true;
        if (other == null || getClass() != other.getClass()) return false;
        TardimPeripheral that = (TardimPeripheral) other;
        return blockEntity.getBlockPos().equals(that.blockEntity.getBlockPos());
    }
    
    @Override
    public Object getTarget() {
        return this.blockEntity;
    }
    
    @Override
    public void attach(@NotNull IComputerAccess computer) {
        LOGGER.debug("Computer {} attached to TARDIM peripheral at {}", computer.getID(), blockEntity.getBlockPos());
        computers.add(computer);
    }
    
    @Override
    public void detach(@NotNull IComputerAccess computer) {
        LOGGER.debug("Computer {} detached from TARDIM peripheral at {}", computer.getID(), blockEntity.getBlockPos());
        computers.remove(computer);
    }
    
    @Override
    public String[] getMethodNames() {
        return methodNames;
    }
    
    @Override
    public MethodResult callMethod(IComputerAccess computer, ILuaContext context, int methodIndex, IArguments arguments) throws LuaException {
        // Get the method name from our array
        String methodName = methodNames[methodIndex];
        Method method = methods.get(methodName);
        
        if (method == null) {
            throw new LuaException("Method " + methodName + " does not exist");
        }
        
        try {
            // Get the number of parameters the method expects
            Class<?>[] paramTypes = method.getParameterTypes();
            Object[] params = new Object[paramTypes.length];
            
            // Convert Lua arguments to Java types
            for (int i = 0; i < params.length; i++) {
                if (i < arguments.count()) {
                    if (paramTypes[i] == String.class) {
                        params[i] = arguments.getString(i);
                    } else if (paramTypes[i] == boolean.class || paramTypes[i] == Boolean.class) {
                        params[i] = arguments.getBoolean(i);
                    } else if (paramTypes[i] == int.class || paramTypes[i] == Integer.class) {
                        params[i] = arguments.getInt(i);
                    } else if (paramTypes[i] == double.class || paramTypes[i] == Double.class) {
                        params[i] = arguments.getDouble(i);
                    } else {
                        params[i] = arguments.get(i);
                    }
                }
            }
            
            // Call the method on our API object
            Object result = method.invoke(api, params);
            
            // Return the result - null becomes an empty array
            return MethodResult.of(result);
        } catch (IllegalAccessException e) {
            LOGGER.error("Failed to access method {}", methodName, e);
            throw new LuaException("Cannot access method " + methodName);
        } catch (InvocationTargetException e) {
            // If the method threw a LuaException, re-throw it
            Throwable cause = e.getCause();
            if (cause instanceof LuaException) {
                throw (LuaException) cause;
            }
            
            LOGGER.error("Error executing method {}", methodName, e.getCause());
            throw new LuaException("Error in " + methodName + ": " + e.getCause().getMessage());
        }
    }
    
    /**
     * Broadcast an event to all attached computers.
     * Useful for notifying computers about TARDIS events like takeoff or landing.
     *
     * @param event The name of the event
     * @param args  Arguments to pass with the event
     */
    public void broadcastEvent(String event, Object... args) {
        computers.queueEvent(event, args);
    }
}
