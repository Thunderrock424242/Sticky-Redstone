package com.thunder.stickyredstone.core;

import net.neoforged.neoforge.common.ModConfigSpec;

public class ModConfig {

    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec SPEC = BUILDER.build();

    private ModConfig() {
    }
}
