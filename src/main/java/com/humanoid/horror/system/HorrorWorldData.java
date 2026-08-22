package com.humanoid.horror.system;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;

public class HorrorWorldData extends SavedData {

    private static final String DATA_NAME = "humanoid_horror_data";

    public boolean isLocked = false;
    public long lockStartGameTime = 0L;

    public HorrorWorldData() {
    }

    public static HorrorWorldData load(CompoundTag tag) {
        HorrorWorldData data = new HorrorWorldData();

        data.isLocked = tag.getBoolean("IsLocked");
        data.lockStartGameTime = tag.getLong("LockStartGameTime");

        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        tag.putBoolean("IsLocked", this.isLocked);
        tag.putLong("LockStartGameTime", this.lockStartGameTime);

        return tag;
    }

    public static HorrorWorldData get(Level level) {

        if (level == null) {
            return new HorrorWorldData();
        }

        return level.getServer()
                .overworld()
                .getDataStorage()
                .computeIfAbsent(
                        HorrorWorldData::load,
                        HorrorWorldData::new,
                        DATA_NAME
                );
    }
}
