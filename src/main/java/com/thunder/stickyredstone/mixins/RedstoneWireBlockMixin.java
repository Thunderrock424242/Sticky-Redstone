package com.thunder.stickyredstone.mixins;

import com.thunder.stickyredstone.mixins.support.StickyRedstoneProperties;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.RedstoneSide;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RedStoneWireBlock.class)
public abstract class RedstoneWireBlockMixin {

    @Unique
    private static final VoxelShape STICKYREDSTONE_SHAPE_FLOOR = Block.box(0, 0, 0, 16, 2, 16);
    @Unique
    private static final VoxelShape STICKYREDSTONE_SHAPE_CEILING = Block.box(0, 14, 0, 16, 16, 16);
    @Unique
    private static final VoxelShape STICKYREDSTONE_SHAPE_NORTH = Block.box(0, 0, 14, 16, 16, 16);
    @Unique
    private static final VoxelShape STICKYREDSTONE_SHAPE_SOUTH = Block.box(0, 0, 0, 16, 16, 2);
    @Unique
    private static final VoxelShape STICKYREDSTONE_SHAPE_WEST = Block.box(14, 0, 0, 16, 16, 16);
    @Unique
    private static final VoxelShape STICKYREDSTONE_SHAPE_EAST = Block.box(0, 0, 0, 2, 16, 16);

    @Invoker("registerDefaultState")
    protected abstract void stickyredstone$invokeRegisterDefaultState(BlockState state);

    @Inject(method = "createBlockStateDefinition", at = @At("TAIL"))
    private void stickyredstone$addFacing(StateDefinition.Builder<Block, BlockState> builder, CallbackInfo ci) {
        builder.add(StickyRedstoneProperties.FACING);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void stickyredstone$setDefaultFacing(Block.Properties properties, CallbackInfo ci) {
        this.stickyredstone$invokeRegisterDefaultState(((RedStoneWireBlock) (Object) this).defaultBlockState().setValue(StickyRedstoneProperties.FACING, Direction.UP));
    }

    @Inject(method = "getShape", at = @At("HEAD"), cancellable = true)
    private void stickyredstone$getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context, CallbackInfoReturnable<VoxelShape> cir) {
        Direction facing = state.getValue(StickyRedstoneProperties.FACING);
        VoxelShape shape = switch (facing) {
            case DOWN -> STICKYREDSTONE_SHAPE_CEILING;
            case NORTH -> STICKYREDSTONE_SHAPE_NORTH;
            case SOUTH -> STICKYREDSTONE_SHAPE_SOUTH;
            case WEST -> STICKYREDSTONE_SHAPE_WEST;
            case EAST -> STICKYREDSTONE_SHAPE_EAST;
            default -> STICKYREDSTONE_SHAPE_FLOOR;
        };
        cir.setReturnValue(shape);
    }

    @Inject(method = "canSurvive", at = @At("HEAD"), cancellable = true)
    private void stickyredstone$canSurvive(BlockState state, LevelReader level, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        Direction facing = state.getValue(StickyRedstoneProperties.FACING);
        BlockPos supportPos = pos.relative(facing.getOpposite());
        cir.setReturnValue(level.getBlockState(supportPos).isFaceSturdy(level, supportPos, facing));
    }

    @Inject(method = "updateShape", at = @At("HEAD"), cancellable = true)
    private void stickyredstone$dropIfSupportLost(BlockState state, Direction direction, BlockState neighborState, net.minecraft.world.level.LevelAccessor level, BlockPos pos, BlockPos neighborPos, CallbackInfoReturnable<BlockState> cir) {
        Direction facing = state.getValue(StickyRedstoneProperties.FACING);
        if (direction == facing.getOpposite() && !neighborState.isFaceSturdy(level, neighborPos, facing)) {
            cir.setReturnValue(Blocks.AIR.defaultBlockState());
        }
    }

    @Inject(method = "getStateForPlacement", at = @At("RETURN"), cancellable = true)
    private void stickyredstone$setFacingFromClickedFace(net.minecraft.world.item.context.BlockPlaceContext context, CallbackInfoReturnable<BlockState> cir) {
        BlockState state = cir.getReturnValue();
        if (state == null) {
            return;
        }
        Direction clickedFace = context.getClickedFace();
        cir.setReturnValue(state.setValue(StickyRedstoneProperties.FACING, clickedFace));
    }

    @Inject(
            method = "calculateShape(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)Lnet/minecraft/world/level/block/state/BlockState;",
            at = @At("RETURN"),
            cancellable = true
    )
    private void stickyredstone$rotateVisualConnections(BlockGetter level, BlockPos pos, BlockState state, CallbackInfoReturnable<BlockState> cir) {
        BlockState shaped = cir.getReturnValue();
        Direction facing = state.getValue(StickyRedstoneProperties.FACING);
        if (facing == Direction.UP || facing == Direction.DOWN) {
            cir.setReturnValue(shaped);
            return;
        }

        boolean connectA;
        boolean connectB;
        boolean connectC;
        boolean connectD;

        if (facing == Direction.NORTH || facing == Direction.SOUTH) {
            connectA = stickyredstone$connectsTo(level, pos, Direction.UP);
            connectB = stickyredstone$connectsTo(level, pos, Direction.DOWN);
            connectC = stickyredstone$connectsTo(level, pos, Direction.EAST);
            connectD = stickyredstone$connectsTo(level, pos, Direction.WEST);
        } else {
            connectA = stickyredstone$connectsTo(level, pos, Direction.UP);
            connectB = stickyredstone$connectsTo(level, pos, Direction.DOWN);
            connectC = stickyredstone$connectsTo(level, pos, Direction.NORTH);
            connectD = stickyredstone$connectsTo(level, pos, Direction.SOUTH);
        }

        cir.setReturnValue(
                shaped.setValue(RedStoneWireBlock.NORTH, connectA ? RedstoneSide.SIDE : RedstoneSide.NONE)
                        .setValue(RedStoneWireBlock.SOUTH, connectB ? RedstoneSide.SIDE : RedstoneSide.NONE)
                        .setValue(RedStoneWireBlock.EAST, connectC ? RedstoneSide.SIDE : RedstoneSide.NONE)
                        .setValue(RedStoneWireBlock.WEST, connectD ? RedstoneSide.SIDE : RedstoneSide.NONE)
        );
    }

    @Unique
    private boolean stickyredstone$connectsTo(BlockGetter level, BlockPos pos, Direction direction) {
        BlockPos neighborPos = pos.relative(direction);
        BlockState neighborState = level.getBlockState(neighborPos);
        return neighborState.getBlock() instanceof RedStoneWireBlock
                || neighborState.getBlock().canConnectRedstone(neighborState, level, neighborPos, direction.getOpposite());
    }
}
