package com.thunder.stickyredstone.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
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
import net.minecraft.world.level.block.state.properties.RedstoneSide;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class WallCeilingRedstoneBlock extends RedStoneWireBlock {

    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    public static final IntegerProperty POWER = BlockStateProperties.POWER;

    private static final VoxelShape SHAPE_FLOOR = Block.box(0, 0, 0, 16, 2, 16);
    private static final VoxelShape SHAPE_CEILING = Block.box(0, 14, 0, 16, 16, 16);
    private static final VoxelShape SHAPE_NORTH = Block.box(0, 0, 14, 16, 16, 16);
    private static final VoxelShape SHAPE_SOUTH = Block.box(0, 0, 0, 16, 16, 2);
    private static final VoxelShape SHAPE_WEST = Block.box(14, 0, 0, 16, 16, 16);
    private static final VoxelShape SHAPE_EAST = Block.box(0, 0, 0, 2, 16, 16);

    public WallCeilingRedstoneBlock(Properties properties) {
        super(properties);
        registerDefaultState(
                stateDefinition.any()
                        .setValue(FACING, Direction.UP)
                        .setValue(POWER, 0)
                        .setValue(NORTH, RedstoneSide.NONE)
                        .setValue(SOUTH, RedstoneSide.NONE)
                        .setValue(EAST, RedstoneSide.NONE)
                        .setValue(WEST, RedstoneSide.NONE)
        );
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FACING);
    }

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

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockPos placePos = context.getClickedPos();
        Direction attachedFace = Direction.UP;

        for (Direction direction : context.getNearestLookingDirections()) {
            Direction face = direction.getOpposite();
            BlockPos supportPos = placePos.relative(face.getOpposite());
            BlockState support = context.getLevel().getBlockState(supportPos);

            if (support.isFaceSturdy(context.getLevel(), supportPos, face)) {
                attachedFace = face;
                break;
            }
        }

        BlockState state = this.defaultBlockState().setValue(FACING, attachedFace);
        return calculateVisualConnections(context.getLevel(), placePos, state);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        Direction facing = state.getValue(FACING);

        if (direction == facing.getOpposite() && !neighborState.isFaceSturdy(level, neighborPos, facing)) {
            return Blocks.AIR.defaultBlockState();
        }

        // We ONLY calculate visual lines here. We do NOT call updatePower here anymore,
        // because it was overwriting the visual changes.
        if (level instanceof Level realLevel) {
            return calculateVisualConnections(realLevel, pos, state);
        }

        return state;
    }

    private BlockState calculateVisualConnections(Level level, BlockPos pos, BlockState state) {
        Direction facing = state.getValue(FACING);
        boolean connectN = false, connectS = false, connectE = false, connectW = false;

        if (facing == Direction.UP || facing == Direction.DOWN) {
            connectN = connectsTo(level, pos, Direction.NORTH);
            connectS = connectsTo(level, pos, Direction.SOUTH);
            connectE = connectsTo(level, pos, Direction.EAST);
            connectW = connectsTo(level, pos, Direction.WEST);
        } else if (facing == Direction.NORTH || facing == Direction.SOUTH) {
            connectN = connectsTo(level, pos, Direction.UP);
            connectS = connectsTo(level, pos, Direction.DOWN);
            connectE = connectsTo(level, pos, Direction.EAST);
            connectW = connectsTo(level, pos, Direction.WEST);
        } else if (facing == Direction.EAST || facing == Direction.WEST) {
            connectN = connectsTo(level, pos, Direction.UP);
            connectS = connectsTo(level, pos, Direction.DOWN);
            connectE = connectsTo(level, pos, Direction.NORTH);
            connectW = connectsTo(level, pos, Direction.SOUTH);
        }

        return state
                .setValue(NORTH, connectN ? RedstoneSide.SIDE : RedstoneSide.NONE)
                .setValue(SOUTH, connectS ? RedstoneSide.SIDE : RedstoneSide.NONE)
                .setValue(EAST, connectE ? RedstoneSide.SIDE : RedstoneSide.NONE)
                .setValue(WEST, connectW ? RedstoneSide.SIDE : RedstoneSide.NONE);
    }

    private boolean connectsTo(Level level, BlockPos pos, Direction dir) {
        BlockPos neighborPos = pos.relative(dir);
        BlockState neighborState = level.getBlockState(neighborPos);
        return neighborState.getBlock() instanceof RedStoneWireBlock ||
                neighborState.getBlock().canConnectRedstone(neighborState, level, neighborPos, dir.getOpposite());
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        Direction facing = state.getValue(FACING);
        BlockPos supportPos = pos.relative(facing.getOpposite());
        return level.getBlockState(supportPos).isFaceSturdy(level, supportPos, facing);
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (!level.isClientSide) {
            updatePower(level, pos, state);
        }
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean movedByPiston) {
        super.neighborChanged(state, level, pos, neighborBlock, neighborPos, movedByPiston);
        if (!level.isClientSide) {
            updatePower(level, pos, state);
        }
    }

    @Override
    public int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        int power = state.getValue(POWER);
        if (power == 0) return 0;

        Direction facing = state.getValue(FACING);
        if (direction == facing.getOpposite()) return 0;

        return power;
    }

    @Override
    public boolean canConnectRedstone(BlockState state, BlockGetter level, BlockPos pos, @Nullable Direction direction) {
        return true;
    }

    @Override
    public int getDirectSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return getSignal(state, level, pos, direction);
    }

    private void updatePower(Level level, BlockPos pos, BlockState oldState) {
        // ALWAYS get the absolute latest state from the world so we don't accidentally erase the visual lines
        BlockState currentState = level.getBlockState(pos);
        if (currentState.getBlock() != this) return;

        int currentPower = currentState.getValue(POWER);

        // TEMPORARY FIX: Set power to 0 briefly so it doesn't read its own signal echoing off solid blocks!
        // The '20' flag prevents nearby blocks from noticing this temporary drop to 0.
        level.setBlock(pos, currentState.setValue(POWER, 0), 20);

        int targetPower = calculateTargetPower(level, pos);

        if (targetPower == currentPower) {
            // Put it back to what it was
            level.setBlock(pos, currentState.setValue(POWER, currentPower), 2);
            return;
        }

        // Apply new power
        level.setBlock(pos, currentState.setValue(POWER, targetPower), 2);

        level.updateNeighborsAt(pos, this);
        for (Direction direction : Direction.values()) {
            level.updateNeighborsAt(pos.relative(direction), this);
        }
    }

    private int calculateTargetPower(Level level, BlockPos pos) {
        // Because we set our own power to 0 in updatePower(), this is now safe to call!
        int strongest = level.getBestNeighborSignal(pos);

        for (Direction direction : Direction.values()) {
            BlockPos neighborPos = pos.relative(direction);
            BlockState neighborState = level.getBlockState(neighborPos);

            if (neighborState.getBlock() instanceof RedStoneWireBlock) {
                strongest = Math.max(strongest, neighborState.getValue(POWER) - 1);
            }

            if (strongest >= 15) return 15;
        }

        return Mth.clamp(strongest, 0, 15);
    }
}