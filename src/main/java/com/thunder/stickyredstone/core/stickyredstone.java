package com.thunder.stickyredstone.core;

import com.mojang.logging.LogUtils;
import com.thunder.stickyredstone.block.ModBlocks;
import com.thunder.stickyredstone.item.ModItems;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import org.slf4j.Logger;

@Mod(stickyredstone.MOD_ID)
public class stickyredstone {

    public static final String MOD_ID = "stickyredstone";
    public static final Logger LOGGER = LogUtils.getLogger();

    public stickyredstone(IEventBus modEventBus) {
        LOGGER.info("stickyredstone loading...");

        ModBlocks.BLOCKS.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);

        modEventBus.addListener(stickyredstone::onClientSetup);

        LOGGER.info("stickyredstone loaded!");
    }

    private static void onClientSetup(final FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.WALL_REDSTONE_WIRE.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.STICKY_REDSTONE_WIRE.get(), RenderType.cutout());
        });
    }
}
