package dev.xylopia.sionacs.integration.computercraft;

import dan200.computercraft.api.ForgeComputerCraftAPI;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.api.peripheral.IPeripheralProvider;
import dev.xylopia.sionacs.integration.tardim.peripheral.TardimPeripheralBlockEntity;
import dev.xylopia.sionacs.utils.Constants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles registration of ComputerCraft integration components
 */
public class ComputerCraftRegistration {
    private static final Logger LOGGER = LoggerFactory.getLogger(Constants.MOD_ID + ":ComputerCraft");

    /**
     * Register all ComputerCraft integration components
     */
    public static void register(IEventBus eventBus) {
        // Register setup event to initialize peripheral providers
        eventBus.addListener(ComputerCraftRegistration::setup);
        LOGGER.info("Initialized ComputerCraft integration");
    }
    
    /**
     * Setup ComputerCraft integration
     */
    private static void setup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            // Register TARDIM peripheral provider using the proper Forge API
            LOGGER.info("Registering TARDIM peripheral provider");
            
            // Create and register our IPeripheralProvider implementation
            ForgeComputerCraftAPI.registerPeripheralProvider(new IPeripheralProvider() {
                @Override
                public @NotNull LazyOptional<IPeripheral> getPeripheral(@NotNull Level world, @NotNull BlockPos pos, @NotNull Direction side) {
                    BlockEntity blockEntity = world.getBlockEntity(pos);
                    
                    // First case: Our peripheral block entity
                    if (blockEntity instanceof TardimPeripheralBlockEntity tardimBlockEntity) {
                        // Always provide the peripheral, even if not in a valid TARDIM
                        // The peripheral itself will check and throw LuaException for invalid operations
                        LOGGER.debug("Creating peripheral for TardimPeripheralBlockEntity at {}", pos);
                        return LazyOptional.of(() -> new TardimPeripheral(tardimBlockEntity));
                    }
                    
                    return LazyOptional.empty();
                }
            });
            
            LOGGER.info("ComputerCraft integration setup complete");
        });
    }
}
