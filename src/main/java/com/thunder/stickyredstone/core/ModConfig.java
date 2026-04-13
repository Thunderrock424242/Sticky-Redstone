package com.thunder.stickyredstone.core;

import net.neoforged.neoforge.common.ModConfigSpec;

public class ModConfig {

    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue VANILLA_DUST_WALLS_AND_CEILINGS = BUILDER
            .comment("When true, vanilla redstone dust places sticky wire on floors/walls/ceilings. When false, use the mod's sticky wire item.")
            .define("vanillaDustWallsAndCeilings", false);

    public static final ModConfigSpec SPEC = BUILDER.build();

    private ModConfig() {
    }
}
