package com.humanoid.horror.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.client.loading.ForgeLoadingOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ForgeLoadingOverlay.class)
public class ForgeLoadingOverlayMixin {

    @Inject(
        method = "render",
        at = @At("HEAD"),
        cancellable = true
    )
    private void humanoid$replaceForgeLoadingScreen(
            GuiGraphics guiGraphics,
            int mouseX,
            int mouseY,
            float partialTick,
            CallbackInfo ci
    ) {

        int width = guiGraphics.guiWidth();
        int height = guiGraphics.guiHeight();

        // Forge'un bütün loading ekranını kapat.
        // Anvil, Memory, progress bar vs. artık çizilmeyecek.
        guiGraphics.fill(
                0,
                0,
                width,
                height,
                0xFF000000
        );

        ci.cancel();
    }
}
