package com.humanoid.horror.entity;

import com.humanoid.horror.HumanoidMod;
import com.humanoid.horror.entity.ai.Creature1AI;
import com.humanoid.horror.network.JumpscarePacket;
import com.humanoid.horror.network.ModMessages;
import com.humanoid.horror.registry.ModEntities;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FluidState;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Creature1 extends PathfinderMob {

    // =========================================================
    // SYNCHED DATA
    // =========================================================

    private static final EntityDataAccessor<String> TARGET_NAME =
            SynchedEntityData.defineId(
                    Creature1.class,
                    EntityDataSerializers.STRING
            );

    private static final EntityDataAccessor<Integer> TARGET_DISTANCE =
            SynchedEntityData.defineId(
                    Creature1.class,
                    EntityDataSerializers.INT
            );

    // =========================================================
    // 92 BLOK SPAWN
    // =========================================================

    private static boolean spawnedAt92 = false;

    private static final double SPAWN_DISTANCE = 92.0D;

    // =========================================================
    // TARGET
    // =========================================================

    private UUID targetUUID;

    // =========================================================
    // JUMPSCARE
    // =========================================================

    private boolean jumpscareTetiklendi = false;

    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public Creature1(
            EntityType<? extends PathfinderMob> type,
            Level level
    ) {
        super(type, level);
    }

    // =========================================================
    // SYNCHED DATA
    // =========================================================

    @Override
    protected void defineSynchedData() {

        super.defineSynchedData();

        this.entityData.define(
                TARGET_NAME,
                "Aranıyor..."
        );

        this.entityData.define(
                TARGET_DISTANCE,
                500
        );
    }

    // =========================================================
    // ATTRIBUTES
    // =========================================================

    public static AttributeSupplier.Builder createAttributes() {

        return Monster.createMonsterAttributes()

                .add(
                        Attributes.MAX_HEALTH,
                        100.0D
                )

                .add(
                        Attributes.MOVEMENT_SPEED,
                        0.28D
                )

                .add(
                        Attributes.ATTACK_DAMAGE,
                        10.0D
                )

                .add(
                        Attributes.FOLLOW_RANGE,
                        512.0D
                );
    }

    // =========================================================
    // GOALS
    // =========================================================

    @Override
    protected void registerGoals() {

        this.goalSelector.addGoal(
                0,
                new FloatGoal(this)
        );

        this.goalSelector.addGoal(
                1,
                new Creature1AI(this)
        );
    }

    // =========================================================
    // 92 BLOK SPAWN STATE
    // =========================================================

    public static void reset92Spawn() {
        spawnedAt92 = false;
    }

    public static boolean hasSpawnedAt92() {
        return spawnedAt92;
    }

    // =========================================================
    // SPAWN AT 92 BLOCKS
    // =========================================================

    public static Creature1 spawnAtDistance(
            ServerPlayer target
    ) {

        if (target == null) {
            return null;
        }

        if (target.getServer() == null) {
            return null;
        }

        if (spawnedAt92) {
            return null;
        }

        Level level = target.level();

        if (level.isClientSide) {
            return null;
        }

        /*
         * Oyuncunun baktığı yatay yönü alıyoruz.
         *
         * Creature1 oyuncunun TAM 92 blok arkasında
         * doğacak.
         */

        double lookX = target.getLookAngle().x;
        double lookZ = target.getLookAngle().z;

        double horizontalLength =
                Math.sqrt(
                        lookX * lookX
                                + lookZ * lookZ
                );

        /*
         * Oyuncu tamamen yukarı/aşağı bakıyorsa
         * yatay yön sıfıra yakın olabilir.
         *
         * Böyle durumda rastgele yatay yön kullanıyoruz.
         */

        if (horizontalLength < 0.001D) {

            RandomSource random =
                    target.getRandom();

            double angle =
                    random.nextDouble()
                            * Math.PI
                            * 2.0D;

            lookX = Math.cos(angle);
            lookZ = Math.sin(angle);

            horizontalLength = 1.0D;
        }

        lookX /= horizontalLength;
        lookZ /= horizontalLength;

        /*
         * Oyuncunun baktığı yönün TERSİ.
         */

        double spawnX =
                target.getX()
                        - lookX * SPAWN_DISTANCE;

        double spawnZ =
                target.getZ()
                        - lookZ * SPAWN_DISTANCE;

        /*
         * Aynı Y seviyesinde spawn ediyoruz.
         *
         * Böylece yatay mesafe tam 92 blok.
         */

        double spawnY =
                target.getY();

        Creature1 creature =
                ModEntities.CREATURE1
                        .get()
                        .create(level);

        if (creature == null) {
            return null;
        }

        creature.moveTo(
                spawnX,
                spawnY,
                spawnZ,
                target.getYRot(),
                0.0F
        );

        /*
         * Hedefi doğrudan /start ile seçilen
         * oyuncuya kilitliyoruz.
         */

        creature.setTargetPlayer(target);

        /*
         * Entity dünyaya başarıyla eklenirse
         * 92 spawn tamamlanmış sayılır.
         */

        if (!level.addFreshEntity(creature)) {
            return null;
        }

        spawnedAt92 = true;

        return creature;
    }

    // =========================================================
    // TICK
    // =========================================================

    @Override
    public void tick() {

        super.tick();

        if (this.level().isClientSide) {
            return;
        }

        // =====================================================
        // /START KONTROLÜ
        // =====================================================

        if (!HumanoidMod.isStartTriggered) {
            return;
        }

        // =====================================================
        // HEDEF
        // =====================================================

        ServerPlayer oyuncu =
                this.getCurrentTargetPlayer();

        if (oyuncu != null) {

            double mesafe =
                    this.distanceTo(oyuncu);

            this.setTargetDistance(
                    (int) mesafe
            );

            // =================================================
            // JUMPSCARE
            // =================================================

            if (
                    mesafe <= 2.0D
                            && !jumpscareTetiklendi
            ) {

                jumpscareTetiklendi = true;

                ModMessages.sendToPlayer(
                        new JumpscarePacket(),
                        oyuncu
                );

                executeCurse(oyuncu);
            }
        }

        // =====================================================
        // HIZ
        // =====================================================

        boolean isNight =
                this.level().isNight();

        double speed =
                isNight
                        ? 0.35D
                        : 0.28D;

        if (
                this.getAttribute(
                        Attributes.MOVEMENT_SPEED
                ) != null
        ) {

            this.getAttribute(
                    Attributes.MOVEMENT_SPEED
            ).setBaseValue(speed);
        }
    }

    // =========================================================
    // FLUID
    // =========================================================

    @Override
    public boolean canStandOnFluid(
            FluidState state
    ) {
        return !state.isEmpty();
    }

    // =========================================================
    // TARGET
    // =========================================================

    public ServerPlayer getCurrentTargetPlayer() {

        if (this.level().isClientSide) {
            return null;
        }

        if (this.targetUUID != null) {

            ServerPlayer player =
                    (ServerPlayer)
                            this.level()
                                    .getPlayerByUUID(
                                            this.targetUUID
                                    );

            if (
                    player != null
                            && player.isAlive()
                            && !player.isSpectator()
            ) {

                this.setTargetName(
                        player.getScoreboardName()
                );

                Creature1HUDState.setTargetName(
                        player.getScoreboardName()
                );

                return player;
            }
        }

        return selectNextTarget();
    }

    // =========================================================
    // SET TARGET PLAYER
    // =========================================================

    public void setTargetPlayer(
            ServerPlayer player
    ) {

        if (player == null) {
            return;
        }

        this.targetUUID =
                player.getUUID();

        String targetName =
                player.getScoreboardName();

        this.setTargetName(
                targetName
        );

        Creature1HUDState.setTargetName(
                targetName
        );
    }

    // =========================================================
    // SELECT TARGET
    // =========================================================

    public ServerPlayer selectNextTarget() {

        if (
                this.level().isClientSide
                        || this.level().getServer() == null
        ) {
            return null;
        }

        List<? extends ServerPlayer> players =
                this.level()
                        .getServer()
                        .getPlayerList()
                        .getPlayers();

        if (players.isEmpty()) {

            this.targetUUID = null;

            this.setTargetName("Yok");

            this.setTargetDistance(0);

            Creature1HUDState.setTargetName(
                    "Yok"
            );

            return null;
        }

        RandomSource rand =
                this.getRandom();

        ServerPlayer chosen =
                players.get(
                        rand.nextInt(
                                players.size()
                        )
                );

        this.targetUUID =
                chosen.getUUID();

        String targetName =
                chosen.getScoreboardName();

        this.setTargetName(
                targetName
        );

        Creature1HUDState.setTargetName(
                targetName
        );

        return chosen;
    }

    // =========================================================
    // CURSE
    // =========================================================

    public void executeCurse(
            ServerPlayer player
    ) {

        RandomSource rand =
                this.getRandom();

        List<ItemStack> inventory =
                player.getInventory().items;

        List<Integer> filledSlots =
                new ArrayList<>();

        for (
                int i = 0;
                i < inventory.size();
                i++
        ) {

            if (
                    !inventory
                            .get(i)
                            .isEmpty()
            ) {

                filledSlots.add(i);
            }
        }

        if (!filledSlots.isEmpty()) {

            int randomSlotIndex =
                    filledSlots.get(
                            rand.nextInt(
                                    filledSlots.size()
                            )
                    );

            inventory.set(
                    randomSlotIndex,
                    ItemStack.EMPTY
            );
        }

        for (
                EquipmentSlot slot :
                EquipmentSlot.values()
        ) {

            if (
                    slot.getType()
                            == EquipmentSlot.Type.ARMOR
            ) {

                ItemStack armor =
                        player.getItemBySlot(slot);

                if (
                        !armor.isEmpty()
                                && armor.isDamageableItem()
                ) {

                    armor.setDamageValue(
                            Math.max(
                                    0,
                                    armor.getMaxDamage() - 1
                            )
                    );
                }
            }
        }

        if (
                player.getMainHandItem()
                        .is(Items.TOTEM_OF_UNDYING)
        ) {

            player.setItemSlot(
                    EquipmentSlot.MAINHAND,
                    ItemStack.EMPTY
            );

        } else if (
                player.getOffhandItem()
                        .is(Items.TOTEM_OF_UNDYING)
        ) {

            player.setItemSlot(
                    EquipmentSlot.OFFHAND,
                    ItemStack.EMPTY
            );
        }
    }

    // =========================================================
    // TARGET NAME
    // =========================================================

    public void setTargetName(
            String name
    ) {

        if (name == null) {
            name = "";
        }

        this.entityData.set(
                TARGET_NAME,
                name
        );
    }

    public String getTargetName() {

        return this.entityData.get(
                TARGET_NAME
        );
    }

    // =========================================================
    // TARGET DISTANCE
    // =========================================================

    public void setTargetDistance(
            int dist
    ) {

        this.entityData.set(
                TARGET_DISTANCE,
                Math.max(0, dist)
        );
    }

    public int getTargetDistance() {

        return this.entityData.get(
                TARGET_DISTANCE
        );
    }

    // =========================================================
    // SAVE
    // =========================================================

    @Override
    public void addAdditionalSaveData(
            CompoundTag tag
    ) {

        super.addAdditionalSaveData(tag);

        if (this.targetUUID != null) {

            tag.putUUID(
                    "TargetUUID",
                    this.targetUUID
            );
        }

        tag.putInt(
                "TargetDistance",
                this.getTargetDistance()
        );
    }

    // =========================================================
    // LOAD
    // =========================================================

    @Override
    public void readAdditionalSaveData(
            CompoundTag tag
    ) {

        super.readAdditionalSaveData(tag);

        if (
                tag.hasUUID("TargetUUID")
        ) {

            this.targetUUID =
                    tag.getUUID(
                            "TargetUUID"
                    );
        }

        if (tag.contains("TargetDistance")) {

            this.setTargetDistance(
                    tag.getInt(
                            "TargetDistance"
                    )
            );
        }
    }
}
