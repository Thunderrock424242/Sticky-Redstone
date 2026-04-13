package com.thunder.stickyredstone.core.client;

import com.thunder.stickyredstone.block.ModBlocks;
import com.thunder.stickyredstone.core.stickyredstone;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;

@EventBusSubscriber(modid = stickyredstone.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ModClientEvents {

    private ModClientEvents() {
    }

    @SubscribeEvent
    public static void registerBlockColors(RegisterColorHandlersEvent.Block event) {
        event.register(
                (state, level, pos, tintIndex) -> {
                    int vanilla = RedStoneWireBlock.getColorForPower(state.getValue(RedStoneWireBlock.POWER));

                    // Add a subtle slime-green bias so sticky wire keeps a vanilla-red look with a tiny green hint.
                    int red = (vanilla >> 16) & 0xFF;
                    int green = (vanilla >> 8) & 0xFF;
                    int blue = vanilla & 0xFF;

                    int slimeGreen = 0x76;
                    int mixedGreen = (green * 88 + slimeGreen * 12) / 100;

                    return (red << 16) | (mixedGreen << 8) | blue;
                },
                ModBlocks.STICKY_REDSTONE_WIRE.get()
        );
    }
}
