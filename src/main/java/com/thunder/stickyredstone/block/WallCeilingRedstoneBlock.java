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

/**
 * Multi-surface redstone wire that can be placed on floors, walls, and ceilings.
 *
 * Key differences from vanilla RedStoneWireBlock:
 *   - Uses FACING (a DirectionProperty) to track which surface it's attached to.
 *   - canSurvive() checks ANY solid face, not just the floor.
 *   - getStateForPlacement() picks the face from the player's click direction.
 *   - Signal output is directed along the plane of the surface.
 *
 * The block is intentionally kept thin (1/16 slab on the attachment face)
 * so it looks like a painted wire on the surface.
 */
public class WallCeilingRedstoneBlock extends RedStoneWireBlock {

    /** Which face this wire is glued to (the face BEHIND the wire, not the outward normal). */
    public static final DirectionProperty FACING = BlockStateProperties.FACING;

    /** Power level 0–15 (inherited from RedStoneWireBlock via POWER property). */
    public static final IntegerProperty POWER = BlockStateProperties.POWER;

    // Thin flat shapes for each orientation (2px thick slab on the surface)
    private static final VoxelShape SHAPE_DOWN  = Block.box(0, 0, 0, 16, 2, 16);   // floor
    private static final VoxelShape SHAPE_UP    = Block.box(0, 14, 0, 16, 16, 16); // ceiling
    private static final VoxelShape SHAPE_NORTH = Block.box(0, 0, 0, 16, 16, 2);   // south wall face
    private static final VoxelShape SHAPE_SOUTH = Block.box(0, 0, 14, 16, 16, 16); // north wall face
    private static final VoxelShape SHAPE_WEST  = Block.box(0, 0, 0, 2, 16, 16);   // east wall face
    private static final VoxelShape SHAPE_EAST  = Block.box(14, 0, 0, 16, 16, 16); // west wall face

    public WallCeilingRedstoneBlock(Properties properties) {
        super(properties);
        // Default: attached to the floor (same as vanilla redstone)
        registerDefaultState(
            stateDefinition.any()
                .setValue(FACING, Direction.DOWN)
                .setValue(POWER, 0)
        );
    }

    // -----------------------------------------------------------------------
    // Block state
    // -----------------------------------------------------------------------

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder); // adds POWER + wire connection props
        builder.add(FACING);
    }

    // -----------------------------------------------------------------------
    // Visual shape: a thin slab sitting on the attachment face
    // -----------------------------------------------------------------------

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(FACING)) {
            case UP    -> SHAPE_UP;
            case NORTH -> SHAPE_NORTH;
            case SOUTH -> SHAPE_SOUTH;
            case WEST  -> SHAPE_WEST;
            case EAST  -> SHAPE_EAST;
            default    -> SHAPE_DOWN;   // DOWN = floor, same as vanilla
        };
    }

    // -----------------------------------------------------------------------
    // Placement
    // -----------------------------------------------------------------------

    /**
     * Called when the player right-clicks a face to place the block.
     * We record which face was clicked so we know what surface to attach to.
     */
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        // The face the player clicked is the surface we attach to
        Direction clickedFace = context.getClickedFace();
        BlockPos attachPos = context.getClickedPos().relative(clickedFace.getOpposite());

        // Verify that surface is solid enough to hold the wire
        BlockState support = context.getLevel().getBlockState(attachPos);
        if (!support.isFaceSturdy(context.getLevel(), attachPos, clickedFace)) {
            return null; // can't place here – NeoForge will cancel placement
        }

        BlockState baseState = super.getStateForPlacement(context);
        if (baseState == null) return null;

        return baseState.setValue(FACING, clickedFace);
    }

    // -----------------------------------------------------------------------
    // Survival / neighbor updates
    // -----------------------------------------------------------------------

    /**
     * The wire survives as long as its backing surface is solid.
     */
    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        Direction facing = state.getValue(FACING);
        BlockPos supportPos = pos.relative(facing.getOpposite());
        return level.getBlockState(supportPos).isFaceSturdy(level, supportPos, facing);
    }

    /**
     * If the block we are attached to is removed, destroy the wire.
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

        // If the support block was removed, drop the wire
        if (direction == facing.getOpposite()) {
            if (!neighborState.isFaceSturdy(level, neighborPos, facing)) {
                return Blocks.AIR.defaultBlockState();
            }
        }

        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    // -----------------------------------------------------------------------
    // Redstone signal direction
    // -----------------------------------------------------------------------

    /**
     * Emit redstone power toward any adjacent block, respecting the orientation.
     * On a wall, signals travel up/down and sideways along the wall; they also
     * pass through to the block directly in front (the outward normal side).
     */
    @Override
    public int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        int power = state.getValue(POWER);
        if (power == 0) return 0;

        Direction facing = state.getValue(FACING);

        // Don't emit back into the supporting wall/floor/ceiling
        if (direction == facing.getOpposite()) return 0;

        return power;
    }

    @Override
    public int getDirectSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        // Direct signal only on the outward face (the face "above" the wire)
        Direction facing = state.getValue(FACING);
        return direction == facing ? state.getValue(POWER) : 0;
    }
}
