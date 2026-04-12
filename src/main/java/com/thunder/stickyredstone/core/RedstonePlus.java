package com.thunder.stickyredstone.core;

import com.mojang.logging.LogUtils;
import com.redstoneplus.block.ModBlocks;
import com.redstoneplus.item.ModItems;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;

@Mod(RedstonePlus.MOD_ID)
public class RedstonePlus {

    public static final String MOD_ID = "redstoneplus";
    public static final Logger LOGGER = LogUtils.getLogger();

    public RedstonePlus(IEventBus modEventBus) {
        LOGGER.info("RedstonePlus loading...");

        ModBlocks.BLOCKS.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);

        LOGGER.info("RedstonePlus loaded!");
    }
}
