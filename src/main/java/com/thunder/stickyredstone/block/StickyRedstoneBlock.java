package com.thunder.stickyredstone.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * StickyRedstoneBlock
 *
 * A variant of {@link WallCeilingRedstoneBlock} crafted from
 * Redstone Dust + Slime Ball.
 *
 * Extra behaviour over normal wall/ceiling redstone:
 *
 *   1. STICKY SURVIVAL  – The wire sticks to ANY block face, even non-sturdy
 *      ones such as glass panes, fences, or other transparent blocks.
 *      It will NOT break when the supporting block is destroyed; instead it
 *      "floats" in air until the player removes it.
 *
 *   2. BOOST ON SLIME   – When placed on a Slime Block the wire transmits
 *      power at full strength (15) regardless of its own stored power, letting
 *      you use slime as an amplifier/relay surface.
 *
 *   3. VISUAL DIFFERENCE – Uses its own texture/model (sticky_redstone_wire*)
 *      so players can tell it apart from ordinary wall redstone at a glance.
 */
public class StickyRedstoneBlock extends WallCeilingRedstoneBlock {

    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    public static final IntegerProperty   POWER  = BlockStateProperties.POWER;

    // Same thin-slab shapes as the parent – reused from superclass constants
    private static final VoxelShape SHAPE_DOWN  = Block.box(0,  0, 0, 16,  2, 16);
    private static final VoxelShape SHAPE_UP    = Block.box(0, 14, 0, 16, 16, 16);
    private static final VoxelShape SHAPE_NORTH = Block.box(0,  0, 0, 16, 16,  2);
    private static final VoxelShape SHAPE_SOUTH = Block.box(0,  0,14, 16, 16, 16);
    private static final VoxelShape SHAPE_WEST  = Block.box(0,  0, 0,  2, 16, 16);
    private static final VoxelShape SHAPE_EAST  = Block.box(14, 0, 0, 16, 16, 16);

    public StickyRedstoneBlock(Properties properties) {
        super(properties);
        registerDefaultState(
            stateDefinition.any()
                .setValue(FACING, Direction.DOWN)
                .setValue(POWER, 0)
        );
    }

    // -----------------------------------------------------------------------
    // Sticky survival: clings even to non-solid faces and survives in mid-air
    // -----------------------------------------------------------------------

    /**
     * Sticky redstone always "survives" – it won't pop off by itself.
     * If there's something to cling to, great; if not, it just floats.
     */
    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return true; // sticky – never pops off
    }

    /**
     * Sticky redstone can be placed on ANY face (glass, leaves, fences, air …).
     * We only return null if the placement pos is already occupied.
     */
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction clickedFace = context.getClickedFace();

        // Grab the parent's connection-property state (ignore its placement check)
        BlockState base = super.getStateForPlacement(context);

        // If super returned null (couldn't survive), construct a minimal state manually
        if (base == null) {
            base = defaultBlockState();
        }

        return base.setValue(FACING, clickedFace);
    }

    /**
     * Override updateShape to NOT destroy the wire when its supporting block
     * is removed – sticky redstone floats in place.
     */
    @Override
    public BlockState updateShape(
            BlockState state,
            Direction direction,
            BlockState neighborState,
            LevelAccessor level,
            BlockPos pos,
            BlockPos neighborPos) {

        // Skip the parent's "break if support removed" check.
        // Still call RedStoneWireBlock's super for connection updates,
        // but skip the survivability destruction from WallCeilingRedstoneBlock.
        return RedStoneWireBlock.class.cast(this)
                .updateShape(state, direction, neighborState, level, pos, neighborPos);
        // Note: because Java doesn't allow calling grandparent methods directly,
        // we let the base RedStoneWireBlock handle connection property updates
        // without the face-check destruction our parent adds.
    }

    // -----------------------------------------------------------------------
    // Slime boost: full-power when on a slime block
    // -----------------------------------------------------------------------

    @Override
    public int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        Direction facing = state.getValue(FACING);

        // No signal back into the surface (same as parent)
        if (direction == facing.getOpposite()) return 0;

        // Check if we're sitting on a slime block → amplify to 15
        BlockPos supportPos = pos.relative(facing.getOpposite());
        BlockState support  = level.getBlockState(supportPos);
        if (support.is(net.minecraft.world.level.block.Blocks.SLIME_BLOCK)) {
            return 15;
        }

        return state.getValue(POWER);
    }

    @Override
    public int getDirectSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        Direction facing = state.getValue(FACING);
        if (direction != facing) return 0;

        BlockPos supportPos = pos.relative(facing.getOpposite());
        if (level.getBlockState(supportPos).is(net.minecraft.world.level.block.Blocks.SLIME_BLOCK)) {
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
            case UP    -> SHAPE_UP;
            case NORTH -> SHAPE_NORTH;
            case SOUTH -> SHAPE_SOUTH;
            case WEST  -> SHAPE_WEST;
            case EAST  -> SHAPE_EAST;
            default    -> SHAPE_DOWN;
        };
    }
}
