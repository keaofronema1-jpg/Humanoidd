package com.humanoid.horror.client;

import com.mojang.blaze3d.systems.RenderSystem;

public class HorrorClientManager {

    // Korku sisteminin aktif olup olmadığını tutar
    public static boolean isHorrorActive = false;

    public HorrorClientManager() {
        super();
    }

    // --- /start VERİLDİĞİNDE ÇAĞRILAN ANA METOT ---
    public static void activateClientHorror() {
        isHorrorActive = true;
    }

    // --- SİS MOTORU ---
    // Korku sistemi aktifse sis uygulanır.
    public static void renderHorrorFog() {

        if (!isHorrorActive) {
            return;
        }

        // Sis 4 bloktan itibaren başlar
        RenderSystem.setShaderFogStart(4.0F);

        // Sis 15 blokta sona ulaşır
        RenderSystem.setShaderFogEnd(15.0F);
    }

    // --- İSİM ETİKETİ KONTROLÜ ---
    // İsimler hiçbir zaman gösterilmez.
    public static boolean shouldShowNames() {
        return false;
    }
}
