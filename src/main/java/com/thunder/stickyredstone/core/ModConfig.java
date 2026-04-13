package com.thunder.stickyredstone.core;

import net.neoforged.neoforge.common.ModConfigSpec;

public class ModConfig {

    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue VANILLA_DUST_WALLS_AND_CEILINGS = BUILDER
            .comment("Allow vanilla redstone dust item to place Sticky Redstone's wall/ceiling wire block when clicking walls or ceilings.")
            .define("vanillaDustWallsAndCeilings", true);

    public static final ModConfigSpec SPEC = BUILDER.build();

    private ModConfig() {
    }
}
