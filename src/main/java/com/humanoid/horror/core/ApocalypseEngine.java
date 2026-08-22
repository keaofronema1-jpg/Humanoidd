package com.humanoid.horror.core;

import com.humanoid.horror.HumanoidMod;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;

public class ApocalypseEngine {

    private static final int TICKS_PER_DAY = 24000;

    public ApocalypseEngine() {
    }

    /**
     * SADECE GÜN SAYACINI VE NBT KAYDINI YÖNETEN SUNUCU TICK MANTIĞI
     */
    public static void serverTick(ServerLevel level) {
        if (level == null || level.isClientSide()) {
            return;
        }

        // Sadece Overworld'de çalışsın
        if (level.dimension() != Level.OVERWORLD) {
            return;
        }

        // /start verilmediyse zaman tutucu pasiftir
        if (!HumanoidMod.isStartTriggered) {
            ApocalypseSaveData.get(level).setStartGameTime(-1L);
            return;
        }

        long gameTime = level.getGameTime();
        ApocalypseSaveData saveData = ApocalypseSaveData.get(level);

        // /start ilk tetiklendiğinde başlangıç anını kaydet ve dosyaya yaz
        if (saveData.getStartGameTime() < 0) {
            saveData.setStartGameTime(gameTime);
        }

        // Gün Hesabı: Diğer sınıfların okuduğu currentDay değişkenine aktarılır
        int currentDay = (int) ((gameTime - saveData.getStartGameTime()) / TICKS_PER_DAY);
        HumanoidMod.currentDay = currentDay;
    }

    // ==========================================
    // DÜNYA KAYIT DOSYASI (NBT/SAVED DATA) SINIFI
    // ==========================================
    public static class ApocalypseSaveData extends SavedData {
        private static final String DATA_NAME = "humanoid_apocalypse";
        private long startGameTime = -1L;

        public static ApocalypseSaveData get(ServerLevel level) {
            return level.getDataStorage().computeIfAbsent(
                    ApocalypseSaveData::load,
                    ApocalypseSaveData::new,
                    DATA_NAME
            );
        }

        public ApocalypseSaveData() {
        }

        public static ApocalypseSaveData load(CompoundTag tag) {
            ApocalypseSaveData data = new ApocalypseSaveData();
            data.startGameTime = tag.getLong("StartGameTime");
            return data;
        }

        @Override
        public CompoundTag save(CompoundTag tag) {
            tag.putLong("StartGameTime", this.startGameTime);
            return tag;
        }

        public long getStartGameTime() {
            return startGameTime;
        }

        public void setStartGameTime(long time) {
            this.startGameTime = time;
            this.setDirty(); // Değişikliği diskteki dosyaya kaydet
        }
    }
}
