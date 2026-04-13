package com.thunder.stickyredstone.block;

/**
 * Sticky-redstone item block behavior.
 *
 * Acts like regular redstone power behavior, while retaining the mod's
 * multi-surface placement/support logic from {@link WallCeilingRedstoneBlock}.
 */
public class StickyRedstoneBlock extends WallCeilingRedstoneBlock {

    public StickyRedstoneBlock(Properties properties) {
        super(properties);
    }
}
