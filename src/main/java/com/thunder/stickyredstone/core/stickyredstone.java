package com.thunder.stickyredstone.core;

import com.mojang.logging.LogUtils;
import com.thunder.stickyredstone.block.ModBlocks;
import com.thunder.stickyredstone.item.ModItems;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;

@Mod(stickyredstone.MOD_ID)
public class stickyredstone {

    public static final String MOD_ID = "stickyredstone";
    public static final Logger LOGGER = LogUtils.getLogger();

    public stickyredstone(IEventBus modEventBus) {
        LOGGER.info("stickyredstone loading...");

        ModBlocks.BLOCKS.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);

        LOGGER.info("stickyredstone loaded!");
    }
}
