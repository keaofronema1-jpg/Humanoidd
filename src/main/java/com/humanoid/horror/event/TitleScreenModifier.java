package com.humanoid.horror.event;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(modid = "humanoid", value = Dist.CLIENT)
public class TitleScreenModifier {

    @SubscribeEvent
    public static void onScreenInit(ScreenEvent.Init.Post event) {
        if (event.getScreen() instanceof TitleScreen screen) {
            List<GuiEventListener> toRemove = new ArrayList<>();

            // Ekrandaki tüm bileşenleri tarıyoruz
            for (GuiEventListener listener : screen.children()) {
                if (listener instanceof Button button) {
                    String message = button.getMessage().getString();
                    
                    boolean isAllowed = message.contains("Singleplayer") || 
                                        message.contains("Multiplayer") || 
                                        message.contains("Options") ||
                                        message.contains("Tek Oyunculu") || 
                                        message.contains("Çok Oyunculu") || 
                                        message.contains("Seçenekler");
                    
                    if (!isAllowed) {
                        toRemove.add(button);
                    }
                }
            }

            // İstenmeyen butonları doğrudan ekranın (screen) çocuk listesinden siliyoruz
            for (GuiEventListener listener : toRemove) {
                screen.children().remove(listener);
                screen.renderables.remove(listener);
            }
        }
    }
}
