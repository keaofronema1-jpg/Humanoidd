package com.humanoid.horror.client;

import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.world.phys.Vec3;

public class HumanoidDimensionEffects
        extends DimensionSpecialEffects {

    public HumanoidDimensionEffects() {

        super(
                Float.NaN,
                false,
                DimensionSpecialEffects.SkyType.NONE,
                false,
                false
        );
    }

    @Override
    public Vec3 getBrightnessDependentFogColor(
            Vec3 biomeFogColor,
            float daylight
    ) {

        return new Vec3(
                0.005D,
                0.005D,
                0.005D
        );
    }

    @Override
    public boolean isFoggyAt(
            int x,
            int z
    ) {

        return true;
    }
}
