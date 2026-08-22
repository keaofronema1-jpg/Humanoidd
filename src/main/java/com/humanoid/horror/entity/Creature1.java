package com.humanoid.horror.entity;

import com.humanoid.horror.HumanoidMod;
import com.humanoid.horror.entity.ai.Creature1AI;
import com.humanoid.horror.network.JumpscarePacket;
import com.humanoid.horror.network.ModMessages;
import net.minecraft.core.BlockPos;
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
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.FluidState;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Creature1 extends PathfinderMob {

    private static final EntityDataAccessor<String> TARGET_NAME = SynchedEntityData.defineId(Creature1.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Integer> TARGET_DISTANCE = SynchedEntityData.defineId(Creature1.class, EntityDataSerializers.INT);

    private UUID targetUUID;
    private boolean jumpscareTetiklendi = false;

    // ZAMANLAYICI VE AŞAMA MANTIĞI
    private boolean isTimerActive = true; 
    private int timerTicks = 0;
    private int virtualDistanceTimer = 500; 

    public Creature1(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
    }

    @Override
    protected void defineSynchedData() {
        // 1.20.1 Forge uyumu için super çağrısı kaldırıldı
        this.entityData.define(TARGET_NAME, "Aranıyor...");
        this.entityData.define(TARGET_DISTANCE, 500); 
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 100.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.28D) 
                .add(Attributes.ATTACK_DAMAGE, 10.0D)
                .add(Attributes.FOLLOW_RANGE, 512.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new Creature1AI(this));
    }

    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide) return;

        // /start verilmediyse dondur
        if (!HumanoidMod.isStartTriggered) {
            return;
        }

        // ---- AŞAMA 1: ARKADAKİ ZAMANLAYICI (500 -> 92) ----
        if (isTimerActive) {
            this.setInvisible(true);
            this.setInvulnerable(true);
            this.setNoAi(true);

            timerTicks++;
            // Her 20 tick = 1 saniye
            if (timerTicks >= 20) {
                timerTicks = 0;
                virtualDistanceTimer--;

                this.setTargetDistance(virtualDistanceTimer);

                // 92'ye ulaştığında zamanlayıcı durur ve yaratık 92 blok ötede doğar!
                if (virtualDistanceTimer <= 92) {
                    spawnAtDistance(92.0D);
                }
            }
            return; 
        }

        // ---- AŞAMA 2: CANLI BLOK MESAFESİ (92'den Oyuncuya Kadar) ----
        ServerPlayer oyuncu = this.getCurrentTargetPlayer();
        if (oyuncu != null) {
            double mesafe = this.distanceTo(oyuncu);
            
            // Sayaç anlık olarak yaratığın geldiği blok mesafesini gösterir
            this.setTargetDistance((int) mesafe);

            if (mesafe <= 2.0 && !jumpscareTetiklendi) {
                jumpscareTetiklendi = true;

                ModMessages.sendToPlayer(new JumpscarePacket(), oyuncu);
                executeCurse(oyuncu);
            }
        }

        // Gece hızlanma mantığı
        boolean isNight = this.level().isNight();
        double speed = isNight ? 0.35D : 0.28D; 
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(speed);
    }

    @Override
    public boolean canStandOnFluid(FluidState state) {
        return !state.isEmpty();
    }

    // --- TARGET VE UUID YÖNETİMİ ---

    public ServerPlayer getCurrentTargetPlayer() {
        if (this.level().isClientSide) return null;

        if (this.targetUUID != null) {
            ServerPlayer player = (ServerPlayer) this.level().getPlayerByUUID(this.targetUUID);
            if (player != null && player.isAlive() && !player.isSpectator()) {
                return player;
            }
        }
        return selectNextTarget();
    }

    public ServerPlayer selectNextTarget() {
        if (this.level().isClientSide() || this.level().getServer() == null) return null;

        List<? extends ServerPlayer> players = this.level().getServer().getPlayerList().getPlayers();
        if (players.isEmpty()) {
            this.targetUUID = null;
            this.setTargetName("Yok");
            this.setTargetDistance(0);
            return null;
        }

        RandomSource rand = this.getRandom();
        ServerPlayer chosen = players.get(rand.nextInt(players.size()));
        this.targetUUID = chosen.getUUID();
        this.setTargetName(chosen.getScoreboardName());
        return chosen;
    }

    /**
     * Yaratığı hedefin tam belirlenen blok kadar uzağında sahaya indirir.
     */
    private void spawnAtDistance(double distance) {
        ServerPlayer target = selectNextTarget();
        if (target == null) {
            this.discard();
            return;
        }

        RandomSource rand = this.getRandom();
        double angle = rand.nextDouble() * Math.PI * 2;
        double newX = target.getX() + Math.cos(angle) * distance;
        double newZ = target.getZ() + Math.sin(angle) * distance;
        
        BlockPos checkPos = BlockPos.containing(newX, 0, newZ);
        int newY = this.level().getHeightmapPos(Heightmap.Types.WORLD_SURFACE, checkPos).getY();
        
        if (newY <= this.level().getMinBuildHeight()) {
            newY = (int) target.getY();
        }

        // Forge 1.20.1 güvenli taşıma
        this.moveTo(newX, newY, newZ, this.getYRot(), this.getXRot());
        this.setInvisible(false);
        this.setInvulnerable(false);
        this.setNoAi(false);
        
        this.isTimerActive = false;
    }

    /**
     * Jumpscare sonrası lanet uygulaması.
     */
    public void executeCurse(ServerPlayer player) {
        RandomSource rand = this.getRandom();

        // Envanterden 1 rastgele eşya silme
        List<ItemStack> inventory = player.getInventory().items;
        List<Integer> filledSlots = new ArrayList<>();
        for (int i = 0; i < inventory.size(); i++) {
            if (!inventory.get(i).isEmpty()) {
                filledSlots.add(i);
            }
        }
        if (!filledSlots.isEmpty()) {
            int randomSlotIndex = filledSlots.get(rand.nextInt(filledSlots.size()));
            inventory.set(randomSlotIndex, ItemStack.EMPTY);
        }

        // Zırh canını 1 bırakacak şekilde düşürme
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot.getType() == EquipmentSlot.Type.ARMOR) {
                ItemStack armor = player.getItemBySlot(slot);
                if (!armor.isEmpty() && armor.isDamageableItem()) {
                    armor.setDamageValue(Math.max(0, armor.getMaxDamage() - 1));
                }
            }
        }

        // Totem patlatma
        if (player.getMainHandItem().is(Items.TOTEM_OF_UNDYING)) {
            player.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
        } else if (player.getOffhandItem().is(Items.TOTEM_OF_UNDYING)) {
            player.setItemSlot(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
        }
    }

    public void setTargetName(String name) { this.entityData.set(TARGET_NAME, name); }
    public String getTargetName() { return this.entityData.get(TARGET_NAME); }
    public void setTargetDistance(int dist) { this.entityData.set(TARGET_DISTANCE, dist); }
    public int getTargetDistance() { return this.entityData.get(TARGET_DISTANCE); }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (this.targetUUID != null) tag.putUUID("TargetUUID", this.targetUUID);
        tag.putBoolean("IsTimerActive", this.isTimerActive);
        tag.putInt("TimerTicks", this.timerTicks);
        tag.putInt("VirtualDistanceTimer", this.virtualDistanceTimer);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.hasUUID("TargetUUID")) this.targetUUID = tag.getUUID("TargetUUID");
        this.isTimerActive = tag.getBoolean("IsTimerActive");
        this.timerTicks = tag.getInt("TimerTicks");
        this.virtualDistanceTimer = tag.getInt("VirtualDistanceTimer");
    }
}
