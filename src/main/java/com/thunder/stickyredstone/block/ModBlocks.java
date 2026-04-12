package com.thunder.stickyredstone.block;

import com.thunder.stickyredstone.core.stickyredstone;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlocks {

    public static final DeferredRegister.Blocks BLOCKS =
            DeferredRegister.createBlocks(stickyredstone.MOD_ID);

    /**
     * Wall & Ceiling Redstone Wire
     * Behaves identically to vanilla redstone dust but can be placed on any face.
     */
    public static final DeferredBlock<WallCeilingRedstoneBlock> WALL_REDSTONE_WIRE =
            BLOCKS.register("wall_redstone_wire", () ->
                new WallCeilingRedstoneBlock(
                    BlockBehaviour.Properties.of()
                        .mapColor(MapColor.FIRE)
                        .noCollission()
                        .instabreak()
                        .sound(SoundType.GRAVEL)
                        .lightLevel(state ->
                            state.getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.POWER) > 0 ? 1 : 0)
                )
            );

    /**
     * Sticky Redstone Wire
     * Crafted from 1× Redstone Dust + 1× Slime Ball.
     * Clings to any surface and never falls off. Emits full power on slime blocks.
     */
    public static final DeferredBlock<StickyRedstoneBlock> STICKY_REDSTONE_WIRE =
            BLOCKS.register("sticky_redstone_wire", () ->
                new StickyRedstoneBlock(
                    BlockBehaviour.Properties.of()
                        .mapColor(MapColor.SLIME)
                        .noCollission()
                        .instabreak()
                        .sound(SoundType.SLIME_BLOCK)
                        .lightLevel(state ->
                            state.getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.POWER) > 0 ? 1 : 0)
                )
            );
}
