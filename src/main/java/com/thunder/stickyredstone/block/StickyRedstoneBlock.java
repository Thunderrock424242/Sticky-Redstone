package com.thunder.stickyredstone.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;

/**
 * StickyRedstoneBlock
 *
 * A variant of {@link WallCeilingRedstoneBlock} crafted from
 * Redstone Dust + Slime Ball.
 *
 * Extra behavior over normal wall/ceiling redstone:
 *
 *   1. STICKY SURVIVAL  – The wire sticks to any block face, including
 *      non-sturdy targets such as panes/fences/leaves, and can float.
 *
 *   2. BOOST ON SLIME   – When attached to a slime block, transmitted
 *      power is forced to full strength (15).
 *
 *   3. VISUAL DIFFERENCE – Uses sticky-specific models so players can
 *      distinguish it from regular wall/ceiling wire.
 */
public class StickyRedstoneBlock extends WallCeilingRedstoneBlock {

    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    public static final IntegerProperty POWER = BlockStateProperties.POWER;

    private static final VoxelShape SHAPE_FLOOR = Block.box(0, 0, 0, 16, 2, 16);
    private static final VoxelShape SHAPE_CEILING = Block.box(0, 14, 0, 16, 16, 16);
    private static final VoxelShape SHAPE_NORTH = Block.box(0, 0, 0, 16, 16, 2);
    private static final VoxelShape SHAPE_SOUTH = Block.box(0, 0, 14, 16, 16, 16);
    private static final VoxelShape SHAPE_WEST = Block.box(0, 0, 0, 2, 16, 16);
    private static final VoxelShape SHAPE_EAST = Block.box(14, 0, 0, 16, 16, 16);

    public StickyRedstoneBlock(Properties properties) {
        super(properties);
        registerDefaultState(
            stateDefinition.any()
                .setValue(FACING, Direction.UP)
                .setValue(POWER, 0)
        );
    }

    // -----------------------------------------------------------------------
    // Sticky survival: clings even to non-solid faces and survives in mid-air
    // -----------------------------------------------------------------------

    /**
     * Sticky redstone always survives; it won't pop off by itself.
     */
    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return true;
    }

    /**
     * Sticky redstone can be placed on any clicked face.
     */
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction clickedFace = context.getClickedFace();
        BlockState base = super.getStateForPlacement(context);

        // If parent rejects placement due to non-sturdy support,
        // sticky wire still places using default state.
        if (base == null) {
            base = defaultBlockState();
        }

        return base.setValue(FACING, clickedFace);
    }

    /**
     * Sticky wire does not drop when its supporting face disappears.
     * We bypass the parent drop-on-support-loss rule for that one side.
     */
    @Override
    public BlockState updateShape(
            BlockState state,
            Direction direction,
            BlockState neighborState,
            LevelAccessor level,
            BlockPos pos,
            BlockPos neighborPos) {
        Direction facing = state.getValue(FACING);

        if (direction == facing.getOpposite() && !neighborState.isFaceSturdy(level, neighborPos, facing)) {
            return state;
        }

        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    // -----------------------------------------------------------------------
    // Slime boost: full-power when on a slime block
    // -----------------------------------------------------------------------

    @Override
    public int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        Direction facing = state.getValue(FACING);

        // No signal back into the surface.
        if (direction == facing.getOpposite()) return 0;

        BlockPos supportPos = pos.relative(facing.getOpposite());
        BlockState support = level.getBlockState(supportPos);
        if (support.is(Blocks.SLIME_BLOCK)) {
            return 15;
        }

        return state.getValue(POWER);
    }

    /**
     * Allow vanilla dust and components to recognize this block as a redstone connection target.
     */
    @Override
    public boolean canConnectRedstone(BlockState state, BlockGetter level, BlockPos pos, @Nullable Direction direction) {
        if (direction == null) return true;
        return direction != state.getValue(FACING).getOpposite();
    }

    @Override
    public int getDirectSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        Direction facing = state.getValue(FACING);
        if (direction != facing) return 0;

        BlockPos supportPos = pos.relative(facing.getOpposite());
        if (level.getBlockState(supportPos).is(Blocks.SLIME_BLOCK)) {
            return 15;
        }

        return state.getValue(POWER);
    }

    // -----------------------------------------------------------------------
    // Shape
    // -----------------------------------------------------------------------

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(FACING)) {
            case DOWN -> SHAPE_CEILING;
            case NORTH -> SHAPE_NORTH;
            case SOUTH -> SHAPE_SOUTH;
            case WEST -> SHAPE_WEST;
            case EAST -> SHAPE_EAST;
            default -> SHAPE_FLOOR;
        };
    }
}
