package com.github.standobyte.v1_21_4_stuff.missingmethods;

import net.minecraft.util.FastColor;

public class ARGB {

    public static int white(float alpha) {
        return FastColor.as8BitChannel(alpha) << 24 | 0xFFFFFF;
    }
}
