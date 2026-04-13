package com.thunder.stickyredstone.core;

import com.mojang.logging.LogUtils;
import com.thunder.stickyredstone.block.ModBlocks;
import com.thunder.stickyredstone.item.ModItems;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import org.slf4j.Logger;

@Mod(stickyredstone.MOD_ID)
public class stickyredstone {

    public static final String MOD_ID = "stickyredstone";
    public static final Logger LOGGER = LogUtils.getLogger();

    public stickyredstone(IEventBus modEventBus, ModContainer modContainer) {
        LOGGER.info("stickyredstone loading...");

        ModBlocks.BLOCKS.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        modContainer.registerConfig(ModConfig.Type.COMMON, com.thunder.stickyredstone.core.ModConfig.SPEC);
        modEventBus.addListener(this::addCreative);

        LOGGER.info("stickyredstone loaded!");
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (!com.thunder.stickyredstone.core.ModConfig.VANILLA_DUST_WALLS_AND_CEILINGS.get()
                && event.getTabKey() == CreativeModeTabs.REDSTONE_BLOCKS) {
            event.accept(ModItems.STICKY_REDSTONE_WIRE);
        }
    }
}
