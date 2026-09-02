package com.humanoid.horror.replacement;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;

import java.util.UUID;

public class ReplacementEntity {

    private static FakePlayer replacement;

    private ReplacementEntity() {
    }

    public static FakePlayer create(ServerPlayer original) {

        if (original == null) {
            return null;
        }

        remove();

        ServerLevel level = original.serverLevel();

        UUID uuid = original.getUUID();

        String name = original.getGameProfile().getName();

        GameProfile profile =
                new GameProfile(uuid, name);

        profile.getProperties().putAll(
                original.getGameProfile().getProperties()
        );

        replacement = FakePlayerFactory.get(
                level,
                profile
        );

        replacement.moveTo(
                original.getX(),
                original.getY(),
                original.getZ(),
                original.getYRot(),
                original.getXRot()
        );

        replacement.setYHeadRot(
                original.getYHeadRot()
        );

        replacement.setHealth(
                original.getHealth()
        );

        if (!replacement.isAddedToWorld()) {
            level.addFreshEntity(replacement);
        }

        return replacement;
    }

    public static FakePlayer get() {
        return replacement;
    }

    public static boolean isActive() {
        return replacement != null
                && replacement.isAlive()
                && replacement.isAddedToWorld();
    }

    public static void remove() {

        if (replacement != null) {

            replacement.remove(
                    net.minecraft.world.entity.Entity.RemovalReason.DISCARDED
            );

            replacement = null;
        }
    }
}
