package com.humanoid.horror.client.renderer;

import com.humanoid.horror.entity.PhotoScareEntity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class PhotoScareRenderer
        extends EntityRenderer<PhotoScareEntity> {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation(
                    "humanoid",
                    "textures/entity/photo_scare.png"
            );

    public PhotoScareRenderer(
            EntityRendererProvider.Context context
    ) {

        super(context);

        this.shadowRadius = 0.0F;
    }

    @Override
    public void render(
            PhotoScareEntity entity,
            float entityYaw,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight
    ) {

        Minecraft minecraft =
                Minecraft.getInstance();

        if (minecraft.player == null) {
            return;
        }

        poseStack.pushPose();

        /*
         * Entity'nin bulunduğu noktaya git.
         */
        poseStack.translate(
                0.0D,
                1.0D,
                0.0D
        );

        /*
         * Her zaman oyuncunun kamerasına dön.
         *
         * Böylece fotoğraf hangi taraftan görülürse
         * görülsün oyuncuya dönük olur.
         */
        poseStack.mulPose(
                minecraft.getEntityRenderDispatcher()
                        .cameraOrientation()
        );

        /*
         * Fotoğraf boyutu.
         */
        poseStack.scale(
                2.0F,
                2.0F,
                2.0F
        );

        VertexConsumer consumer =
                buffer.getBuffer(
                        RenderType.entityTranslucent(
                                TEXTURE
                        )
                );

        /*
         * Basit billboard quad.
         */
        float halfWidth = 0.5F;
        float height = 1.0F;

        int overlay = 0;

        vertex(
                poseStack,
                consumer,
                -halfWidth,
                0.0F,
                0.0F,
                0.0F,
                1.0F,
                packedLight,
                overlay
        );

        vertex(
                poseStack,
                consumer,
                halfWidth,
                0.0F,
                0.0F,
                1.0F,
                1.0F,
                packedLight,
                overlay
        );

        vertex(
                poseStack,
                consumer,
                halfWidth,
                height,
                0.0F,
                1.0F,
                0.0F,
                packedLight,
                overlay
        );

        vertex(
                poseStack,
                consumer,
                -halfWidth,
                height,
                0.0F,
                0.0F,
                0.0F,
                packedLight,
                overlay
        );

        poseStack.popPose();
    }

    private static void vertex(
            PoseStack poseStack,
            VertexConsumer consumer,
            float x,
            float y,
            float z,
            float u,
            float v,
            int light,
            int overlay
    ) {

        PoseStack.Pose pose =
                poseStack.last();

        consumer.vertex(
                pose.pose(),
                x,
                y,
                z
        )
                .color(
                        255,
                        255,
                        255,
                        255
                )
                .uv(u, v)
                .overlayCoords(overlay)
                .uv2(light)
                .normal(
                        pose.normal(),
                        0.0F,
                        0.0F,
                        1.0F
                )
                .endVertex();
    }

    @Override
    public ResourceLocation getTextureLocation(
            PhotoScareEntity entity
    ) {

        return TEXTURE;
    }
}
