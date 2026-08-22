package com.humanoid.horror;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(
        modid = HumanoidMod.MOD_ID,
        value = Dist.CLIENT
)
public class ClientHUDOverlay {

    private static final ResourceLocation JUMPSCARE =
            new ResourceLocation(
                    HumanoidMod.MOD_ID,
                    "textures/gui/jumpscare.png"
            );

    private static boolean aktif = false;

    // 60 tick = 3 saniye
    private static int timer = 0;

    /**
     * Jumpscare'ı başlatır.
     */
    public static void jumpscareBaslat() {
        aktif = true;
        timer = 60;
    }

    /**
     * Client tick.
     *
     * Timer burada azaltılır.
     * Böylece render FPS'inden bağımsız olarak
     * yaklaşık tam 3 saniye sürer.
     */
    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {

        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        if (!aktif) {
            return;
        }

        timer--;

        if (timer <= 0) {
            timer = 0;
            aktif = false;
        }
    }

    /**
     * Jumpscare görüntüsünü ekrana çizer.
     */
    @SubscribeEvent
    public static void onRender(RenderGuiOverlayEvent.Post event) {

        if (!aktif) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();

        if (mc.player == null) {
            return;
        }

        GuiGraphics gui = event.getGuiGraphics();

        int width = mc.getWindow().getGuiScaledWidth();
        int height = mc.getWindow().getGuiScaledHeight();

        // Texture'ı tüm ekranı kaplayacak şekilde çiz.
        gui.blit(
                JUMPSCARE,
                0,
                0,
                0,
                0,
                width,
                height,
                width,
                height
        );
    }
}