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
public class Dimension2Fog {

    private static final ResourceLocation DIMENSION2 =
            new ResourceLocation(
                    "humanoid",
                    "dimension2"
            );

    @SubscribeEvent
    public static void onFogColor(
            ViewportEvent.ComputeFogColor event
    ) {

        Minecraft minecraft =
                Minecraft.getInstance();

        if (minecraft.level == null) {
            return;
        }

        if (!minecraft.level.dimension()
                .location()
                .equals(DIMENSION2)) {
            return;
        }

        event.setRed(0.0F);
        event.setGreen(0.0F);
        event.setBlue(0.0F);
    }

    @SubscribeEvent
    public static void onRenderFog(
            ViewportEvent.RenderFog event
    ) {

        Minecraft minecraft =
                Minecraft.getInstance();

        if (minecraft.level == null) {
            return;
        }

        if (!minecraft.level.dimension()
                .location()
                .equals(DIMENSION2)) {
            return;
        }

        event.setNearPlaneDistance(2.0F);
        event.setFarPlaneDistance(32.0F);

        event.setCanceled(true);
    }
}
