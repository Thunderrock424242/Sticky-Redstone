package com.thunder.stickyredstone.item;

import com.thunder.stickyredstone.block.ModBlocks;
import com.thunder.stickyredstone.core.stickyredstone;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {

    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(stickyredstone.MOD_ID);

    /**
     * Item form of sticky redstone wire.
     * Crafting recipe: 1× Redstone Dust + 1× Slime Ball → 1× Sticky Redstone Wire
     * (see data/redstoneplus/recipes/sticky_redstone_wire.json)
     */
    public static final DeferredItem<BlockItem> STICKY_REDSTONE_WIRE =
            ITEMS.registerSimpleBlockItem("sticky_redstone_wire", ModBlocks.STICKY_REDSTONE_WIRE,
                new Item.Properties());
}
