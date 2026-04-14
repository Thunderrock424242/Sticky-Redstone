package com.thunder.stickyredstone.mixins;

import com.thunder.stickyredstone.block.ModBlocks;
import com.thunder.stickyredstone.block.WallCeilingRedstoneBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockItem.class)
public abstract class RedstoneItemMixin {

    @Shadow
    public abstract Block getBlock();

    @Inject(method = "useOn", at = @At("HEAD"), cancellable = true)
    private void stickyredstone$placeOnWallsAndCeilings(UseOnContext context, CallbackInfoReturnable<InteractionResult> cir) {
        if (getBlock() != Blocks.REDSTONE_WIRE) {
            return;
        }

        Direction clickedFace = context.getClickedFace();
        if (clickedFace == Direction.UP) {
            return;
        }

        Level level = context.getLevel();
        BlockPos clickedPos = context.getClickedPos();
        BlockPlaceContext placeContext = new BlockPlaceContext(context);

        BlockState clickedState = level.getBlockState(clickedPos);
        BlockPos placePos = clickedState.canBeReplaced(placeContext) ? clickedPos : clickedPos.relative(clickedFace);

        BlockState replaceState = level.getBlockState(placePos);
        if (!replaceState.canBeReplaced(placeContext)) {
            return;
        }

        BlockState placeState = ModBlocks.STICKY_REDSTONE_WIRE.get().defaultBlockState()
                .setValue(WallCeilingRedstoneBlock.FACING, clickedFace);

        if (!placeState.canSurvive(level, placePos)) {
            return;
        }

        if (level.isClientSide) {
            cir.setReturnValue(InteractionResult.SUCCESS);
            return;
        }

        if (!level.setBlock(placePos, placeState, 3)) {
            return;
        }

        Player player = context.getPlayer();
        SoundType soundType = placeState.getSoundType(level, placePos, player);
        level.playSound(player, placePos, soundType.getPlaceSound(), SoundSource.BLOCKS,
                (soundType.getVolume() + 1.0F) / 2.0F, soundType.getPitch() * 0.8F);

        if (player != null && !player.getAbilities().instabuild) {
            context.getItemInHand().shrink(1);
        }

        cir.setReturnValue(InteractionResult.SUCCESS);
    }
}
