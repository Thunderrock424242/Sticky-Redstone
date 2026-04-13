package com.thunder.stickyredstone.core;

import com.thunder.stickyredstone.block.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

@EventBusSubscriber(modid = stickyredstone.MOD_ID)
public class VanillaDustPlacementHandler {

    private VanillaDustPlacementHandler() {
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (!ModConfig.VANILLA_DUST_WALLS_AND_CEILINGS.get()) {
            return;
        }

        Level level = event.getLevel();
        Player player = event.getEntity();
        InteractionHand hand = event.getHand();
        ItemStack held = player.getItemInHand(hand);

        if (!held.is(Items.REDSTONE)) {
            return;
        }

        Direction clickedFace = event.getFace();
        if (clickedFace == null) {
            return;
        }

        UseOnContext useContext = new UseOnContext(player, hand, event.getHitVec());
        BlockPlaceContext placeContext = new BlockPlaceContext(useContext);

        BlockPos clickedPos = event.getPos();
        BlockState clickedState = level.getBlockState(clickedPos);
        BlockPos placePos = clickedState.canBeReplaced(placeContext) ? clickedPos : clickedPos.relative(clickedFace);

        BlockState replaceState = level.getBlockState(placePos);
        if (!replaceState.canBeReplaced(placeContext)) {
            return;
        }

        BlockState placeState = ModBlocks.STICKY_REDSTONE_WIRE.get().defaultBlockState()
                .setValue(com.thunder.stickyredstone.block.WallCeilingRedstoneBlock.FACING, clickedFace);

        if (!placeState.canSurvive(level, placePos)) {
            return;
        }

        if (level.isClientSide) {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.SUCCESS);
            return;
        }

        if (!level.setBlock(placePos, placeState, 3)) {
            return;
        }

        SoundType soundType = placeState.getSoundType(level, placePos, player);
        level.playSound(null, placePos, soundType.getPlaceSound(), SoundSource.BLOCKS,
                (soundType.getVolume() + 1.0F) / 2.0F, soundType.getPitch() * 0.8F);

        if (!player.getAbilities().instabuild) {
            held.shrink(1);
        }

        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
    }
}
