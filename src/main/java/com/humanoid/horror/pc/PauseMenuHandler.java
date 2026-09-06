package com.humanoid.horror.pc;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.network.chat.Component;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(
        modid = "humanoid",
        bus = Mod.EventBusSubscriber.Bus.FORGE,
        value = Dist.CLIENT
)
public class PauseMenuHandler {

    private PauseMenuHandler() {
    }

    @SubscribeEvent
    public static void onPauseMenuInit(
            ScreenEvent.Init.Post event
    ) {

        if (!(event.getScreen() instanceof PauseScreen)) {
            return;
        }

        /*
         * Pause menüsündeki bütün butonları kontrol et.
         */
        for (var child : event.getScreen().children()) {

            if (!(child instanceof AbstractWidget widget)) {
                continue;
            }

            Component message = widget.getMessage();

            if (message == null) {
                continue;
            }

            String text = message.getString();

            /*
             * QUIT GAME
             */
            if (isQuitGame(text)) {

                event.removeListener(widget);

                continue;
            }

            /*
             * OPEN TO LAN
             */
            if (isOpenToLan(text)) {

                event.removeListener(widget);
            }
        }
    }

    private static boolean isQuitGame(String text) {

        if (text == null) {
            return false;
        }

        String normalized =
                text.trim().toLowerCase();

        return normalized.equals("quit game")
                || normalized.equals("quit")
                || normalized.contains("quit game")
                || normalized.contains("oyundan çık")
                || normalized.contains("oyundan cik");
    }

    private static boolean isOpenToLan(String text) {

        if (text == null) {
            return false;
        }

        String normalized =
                text.trim().toLowerCase();

        return normalized.equals("open to lan")
                || normalized.contains("open to lan")
                || normalized.contains("lan'a aç")
                || normalized.contains("lan'a ac")
                || normalized.contains("lan'a açmak")
                || normalized.contains("lan'a acmak");
    }
}
