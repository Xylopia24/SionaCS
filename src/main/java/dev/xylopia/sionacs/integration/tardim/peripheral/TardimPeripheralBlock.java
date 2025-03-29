package dev.xylopia.sionacs.integration.tardim.peripheral;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import dev.xylopia.sionacs.integration.tardim.TardimPeripheralRegistry;

/**
 * The main TARDIM peripheral block.
 * This block provides computerized control for TARDIS functionality when placed inside a TARDIM.
 */
@SuppressWarnings("unused")
public class TardimPeripheralBlock extends Block implements EntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    public TardimPeripheralBlock() {
        super(Properties.of()
            .mapColor(MapColor.COLOR_BLUE)
            .strength(2.5F, 6.0F)
            .sound(SoundType.METAL)
            .requiresCorrectToolForDrops()
            .pushReaction(PushReaction.BLOCK)
            .noOcclusion());
            
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(@SuppressWarnings("null") StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(@SuppressWarnings("null") BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@SuppressWarnings("null") @NotNull BlockPos pos, @SuppressWarnings("null") @NotNull BlockState state) {
        return new TardimPeripheralBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@SuppressWarnings("null") Level level, @SuppressWarnings("null") BlockState state, @SuppressWarnings("null") BlockEntityType<T> type) {
        return type == TardimPeripheralRegistry.TARDIM_PERIPHERAL_BLOCK_ENTITY.get() ? 
            (lvl, pos, blockState, blockEntity) -> ((TardimPeripheralBlockEntity)blockEntity).tick(lvl, pos, blockState) : null;
    }
}
