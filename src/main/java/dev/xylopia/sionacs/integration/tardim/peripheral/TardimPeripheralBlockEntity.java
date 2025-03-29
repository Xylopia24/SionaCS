package dev.xylopia.sionacs.integration.tardim.peripheral;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Import the correct TARDIM classes from the public version
import com.swdteam.tardim.TardimData;
import com.swdteam.tardim.TardimManager;

import dev.xylopia.sionacs.integration.tardim.TardimPeripheralRegistry;
import dev.xylopia.sionacs.utils.Constants;

/**
 * The block entity for the TARDIM peripheral block.
 * This handles storing state and providing peripheral functionality.
 */
public class TardimPeripheralBlockEntity extends BlockEntity {
    private static final Logger LOGGER = LoggerFactory.getLogger(Constants.MOD_ID + ":TardimPeripherals");
    
    private TardimData tardimData = null;
    private boolean cachedTardimValid = false;
    private long lastCheck = 0L;
    private int tardimId = -1;

    public TardimPeripheralBlockEntity(BlockPos pos, BlockState state) {
        super(TardimPeripheralRegistry.TARDIM_PERIPHERAL_BLOCK_ENTITY.get(), pos, state);
    }

    /**
     * Ticks the block entity.
     * Used to update internal state and check TARDIM connection.
     */
    public void tick(Level level, BlockPos pos, BlockState state) {
        if (level == null || level.isClientSide()) {
            return;
        }

        // Only check for TARDIM every 40 ticks (2 seconds) to avoid performance issues
        long currentTime = level.getGameTime();
        if (currentTime - lastCheck >= 40) {
            lastCheck = currentTime;
            updateTardimData();
        }
    }

    /**
     * Updates the cached TARDIM data by checking if we're in a TARDIM dimension
     * and finding the corresponding TARDIM data.
     */
    private void updateTardimData() {
        Level level = this.getLevel();
        if (level == null || level.isClientSide()) {
            cachedTardimValid = false;
            tardimData = null;
            return;
        }

        // Check if we're in a TARDIM dimension
        String dimensionKey = level.dimension().location().toString();
        
        // Log the dimension we're in for debugging
        LOGGER.debug("Current dimension: {}", dimensionKey);
        
        if (!dimensionKey.contains("tardim")) {
            LOGGER.debug("Not in a TARDIM dimension");
            cachedTardimValid = false;
            tardimData = null;
            return;
        }

        // Try to find which TARDIM we're in based on position
        try {
            // Get block position
            BlockPos pos = this.getBlockPos();
            
            // Use TardimManager directly to get TARDIM index from position
            // Note that INTERIOR_BOUNDS is now a field with value 96, not a config value
            int tardimIndex = TardimManager.getIDForXZ(pos.getX(), pos.getZ());
            
            LOGGER.debug("TARDIM index at position {},{}: {}", pos.getX(), pos.getZ(), tardimIndex);
            
            if (tardimIndex >= 0) {
                // Get the TARDIM data directly
                tardimData = TardimManager.getTardim(tardimIndex);
                tardimId = tardimIndex;
                
                // Check if it's a valid TARDIM
                if (tardimData != null && tardimData.getCurrentLocation() != null && tardimData.getOwnerName() != null) {
                    cachedTardimValid = true;
                    LOGGER.debug("Found valid TARDIM: Owner={}, ID={}", 
                            tardimData.getOwnerName(), tardimData.getId());
                } else {
                    LOGGER.debug("Invalid TARDIM data at index {}", tardimIndex);
                    cachedTardimValid = false;
                }
            } else {
                LOGGER.debug("No TARDIM found at position: {}", pos);
                cachedTardimValid = false;
                tardimData = null;
            }
        } catch (Exception e) {
            // If any error occurs, log it and set valid to false
            LOGGER.error("Failed to access TARDIM data: {}", e.getMessage());
            e.printStackTrace();
            cachedTardimValid = false;
            tardimData = null;
        }
    }

    /**
     * Gets the TARDIM data for this peripheral.
     * @return The TARDIM data or null if not in a valid TARDIM.
     */
    @Nullable
    public TardimData getTardimData() {
        if (!cachedTardimValid) {
            updateTardimData();
        }
        return tardimData;
    }

    /**
     * Gets the Block underneath this BlockEntity.
     * @return The associated Block.
     */
    public Block getBlock() {
        return this.getBlockState().getBlock();
    }

    /**
     * Checks if this peripheral is in a valid TARDIM.
     * @return true if in a valid TARDIM, false otherwise.
     */
    public boolean isInValidTardim() {
        if (!cachedTardimValid) {
            updateTardimData();
        }
        return cachedTardimValid;
    }

    @Override
    protected void saveAdditional(@SuppressWarnings("null") @NotNull CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("tardim_id", tardimId);
    }

    @Override
    public void load(@SuppressWarnings("null") @NotNull CompoundTag tag) {
        super.load(tag);
        this.tardimId = tag.getInt("tardim_id");
    }

    @Override
    public @NotNull CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        saveAdditional(tag);
        return tag;
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        load(pkt.getTag());
    }

    /**
     * Gets the ID of the TARDIM this peripheral is in.
     * @return The TARDIM ID or -1 if not in a TARDIM.
     */
    public int getTardimId() {
        return tardimId;
    }
}
