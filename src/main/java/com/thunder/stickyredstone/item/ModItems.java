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

    public static final DeferredItem<BlockItem> STICKY_REDSTONE_WIRE =
            ITEMS.registerSimpleBlockItem("sticky_redstone_wire", ModBlocks.STICKY_REDSTONE_WIRE,
                new Item.Properties());

    private ModItems() {
    }
}
