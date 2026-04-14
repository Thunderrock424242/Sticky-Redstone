package com.thunder.stickyredstone.core;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;

@Mod(stickyredstone.MOD_ID)
public class stickyredstone {

    public static final String MOD_ID = "stickyredstone";
    public static final Logger LOGGER = LogUtils.getLogger();

    public stickyredstone(IEventBus modEventBus, ModContainer modContainer) {
        LOGGER.info("stickyredstone loading...");

        LOGGER.info("stickyredstone loaded!");
    }
}
