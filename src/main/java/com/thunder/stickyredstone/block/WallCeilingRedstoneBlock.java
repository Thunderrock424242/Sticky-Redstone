package com.thunder.stickyredstone.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;

/**
 * Multi-surface redstone wire that can be placed on floors, walls, and ceilings.
 *
 * Key differences from vanilla {@link RedStoneWireBlock}:
 * <ul>
 *   <li>Uses {@link #FACING} to track the outward normal of the attached surface.</li>
 *   <li>{@link #canSurvive(BlockState, LevelReader, BlockPos)} checks any sturdy face.</li>
 *   <li>{@link #getStateForPlacement(BlockPlaceContext)} attaches to the clicked face.</li>
 *   <li>Signal output works on floor, wall, and ceiling placements.</li>
 * </ul>
 *
 * The block stays visually thin (2px) so it reads like painted redstone on surfaces.
 */
public class WallCeilingRedstoneBlock extends RedStoneWireBlock {

    /** Outward normal from the surface this wire is attached to. */
    public static final DirectionProperty FACING = BlockStateProperties.FACING;

    /** Power level 0–15 (inherited from RedStoneWireBlock via POWER property). */
    public static final IntegerProperty POWER = BlockStateProperties.POWER;

    // Thin flat shapes for each orientation (2px thick slab on the surface)
    private static final VoxelShape SHAPE_FLOOR = Block.box(0, 0, 0, 16, 2, 16);
    private static final VoxelShape SHAPE_CEILING = Block.box(0, 14, 0, 16, 16, 16);
    private static final VoxelShape SHAPE_NORTH = Block.box(0, 0, 0, 16, 16, 2);
    private static final VoxelShape SHAPE_SOUTH = Block.box(0, 0, 14, 16, 16, 16);
    private static final VoxelShape SHAPE_WEST = Block.box(0, 0, 0, 2, 16, 16);
    private static final VoxelShape SHAPE_EAST = Block.box(14, 0, 0, 16, 16, 16);

    public WallCeilingRedstoneBlock(Properties properties) {
        super(properties);
        // Default = floor-attached wire (surface normal points up).
        registerDefaultState(
            stateDefinition.any()
                .setValue(FACING, Direction.UP)
                .setValue(POWER, 0)
        );
    }

    // -----------------------------------------------------------------------
    // Block state
    // -----------------------------------------------------------------------

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FACING);
    }

    // -----------------------------------------------------------------------
    // Visual shape: a thin slab sitting on the attachment face
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

    // -----------------------------------------------------------------------
    // Placement
    // -----------------------------------------------------------------------

    /**
     * Called when the player right-clicks a face to place the block.
     * The clicked face becomes this wire's attached-surface normal.
     */
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockPos placePos = context.getClickedPos();

        // Prefer clicked face first, then fallback to nearest looking directions like vanilla placement.
        for (Direction direction : context.getNearestLookingDirections()) {
            Direction attachFace = direction.getOpposite();
            BlockPos supportPos = placePos.relative(attachFace.getOpposite());
            BlockState support = context.getLevel().getBlockState(supportPos);

            if (support.isFaceSturdy(context.getLevel(), supportPos, attachFace)) {
                return defaultBlockState().setValue(FACING, attachFace);
            }
        }

        return null;
    }

    // -----------------------------------------------------------------------
    // Survival / neighbor updates
    // -----------------------------------------------------------------------

    /**
     * The wire survives as long as its backing surface is sturdy.
     */
    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        Direction facing = state.getValue(FACING);
        BlockPos supportPos = pos.relative(facing.getOpposite());
        return level.getBlockState(supportPos).isFaceSturdy(level, supportPos, facing);
    }

    /**
     * If the support face disappears, regular wall/ceiling wire drops.
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
            return Blocks.AIR.defaultBlockState();
        }

        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    // -----------------------------------------------------------------------
    // Redstone signal direction
    // -----------------------------------------------------------------------

    /**
     * Emit redstone power toward all sides except back into the supporting surface.
     * This allows floor↔wall↔ceiling transitions and corner propagation.
     */
    @Override
    public int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        int power = state.getValue(POWER);
        if (power == 0) return 0;

        Direction facing = state.getValue(FACING);
        if (direction == facing.getOpposite()) return 0;

        return power;
    }

    /**
     * Allow vanilla dust and components to recognize this block as a redstone connection target.
     */
    @Override
    public boolean canConnectRedstone(BlockState state, BlockGetter level, BlockPos pos, @Nullable Direction direction) {
        if (direction == null) return true;
        return direction != state.getValue(FACING).getOpposite();
    }

    /**
     * Direct power is emitted from the outward face.
     */
    @Override
    public int getDirectSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        Direction facing = state.getValue(FACING);
        return direction == facing ? state.getValue(POWER) : 0;
    }
}
