package com.humanoid.horror.client;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(
        modid = "humanoid",
        bus = Mod.EventBusSubscriber.Bus.FORGE,
        value = Dist.CLIENT
)
public class HumanoidDimensionFog {

    private static final ResourceLocation HUMANOID_DIMENSION =
            new ResourceLocation(
                    "humanoid",
                    "humanoid_dimension"
            );

    @SubscribeEvent
    public static void onRenderFog(
            ViewportEvent.RenderFog event
    ) {

        Minecraft minecraft =
                Minecraft.getInstance();

        if (minecraft.level == null) {
            return;
        }

        if (!minecraft.level
                .dimension()
                .location()
                .equals(HUMANOID_DIMENSION)) {

            return;
        }

        event.setNearPlaneDistance(2.0F);
        event.setFarPlaneDistance(32.0F);

        event.setCanceled(true);
    }
}
