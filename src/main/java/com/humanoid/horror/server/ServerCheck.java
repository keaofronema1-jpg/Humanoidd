package com.humanoid.horror.server;

import com.humanoid.horror.check.Check;

public class ServerCheck {

    public static void checkEnvironment() {

        boolean isServer;

        try {
            Class.forName("net.minecraft.client.Minecraft");
            isServer = false;
        } catch (ClassNotFoundException e) {
            isServer = true;
        }

        if (!isServer) {
            Check.verifyPlatform();
        } else {
            System.out.println(
                "[HORROR-SERVER] Running on Dedicated Server. OS locks bypassed."
            );
        }
    }
}