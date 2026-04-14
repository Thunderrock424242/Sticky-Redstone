package com.thunder.stickyredstone.mixins.support;

import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;

public final class StickyRedstoneProperties {

    public static final DirectionProperty FACING = BlockStateProperties.FACING;

    private StickyRedstoneProperties() {
    }
}
