package com.humanoid.horror.system;

import com.humanoid.horror.entity.Creature3;
import com.humanoid.horror.entity.ModEntities;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.AbstractChestBlock;
import net.minecraft.world.level.block.AbstractFurnaceBlock;
import net.minecraft.world.level.block.AbstractGlassBlock;
import net.minecraft.world.level.block.BarrelBlock;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BlockState;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber
public class HomeTrackerManager {

    public static boolean ACTIVE = false;

    private static final Map<UUID, BlockPos> playerHomes =
            new HashMap<>();

    /*
     * Her oyuncu için aktif bir cam baskını Creature3'ü.
     * Böylece aynı olayın her tick tekrar oluşturulması engellenir.
     */
    private static final Map<UUID, Creature3> windowCreatures =
            new HashMap<>();

    /*
     * Oyuncunun Creature3'e bakıp bakmadığını kontrol etmek için.
     */
    private static final Map<UUID, Boolean> windowTriggered =
            new HashMap<>();

    // =========================================================
    // PLAYER TICK
    // =========================================================

    @SubscribeEvent
    public static void onPlayerTick(
            TickEvent.PlayerTickEvent event
    ) {

        if (!ACTIVE
                || event.phase != TickEvent.Phase.END) {
            return;
        }

        if (event.player == null
                || event.player.level().isClientSide()) {
            return;
        }

        if (event.player instanceof ServerPlayer player) {

            ServerLevel level =
                    player.serverLevel();

            /*
             * Her 15 saniyede bir ev taraması.
             */
            if (level.getGameTime() % 300 == 0) {
                calculateHomeScore(
                        player,
                        level
                );
            }

            /*
             * Daha önce cam baskını için yaratık
             * oluşturulduysa oyuncunun ona bakıp
             * bakmadığını kontrol et.
             */
            checkWindowCreatureLook(
                    player,
                    level
            );
        }
    }

    // =========================================================
    // CAM KONTROLÜ
    // =========================================================

    private static boolean isAnyGlass(
            BlockState state,
            Block block
    ) {

        return block instanceof AbstractGlassBlock
                || block instanceof IronBarsBlock
                || state.is(Blocks.GLASS);
    }

    // =========================================================
    // EV PUANI
    // =========================================================

    private static void calculateHomeScore(
            ServerPlayer player,
            ServerLevel level
    ) {

        if (player == null || level == null) {
            return;
        }

        BlockPos playerPos =
                player.blockPosition();

        if (!level.hasChunkAt(playerPos)) {
            return;
        }

        int score = 0;

        boolean hasGlass = false;
        boolean hasDoor = false;

        BlockPos glassPos = null;
        BlockPos bedPos = null;

        // =====================================================
        // 15 x 15 x 15 TARAMA
        // =====================================================

        for (BlockPos pos : BlockPos.betweenClosed(
                playerPos.offset(-7, -7, -7),
                playerPos.offset(7, 7, 7)
        )) {

            BlockState state =
                    level.getBlockState(pos);

            Block block =
                    state.getBlock();

            // -------------------------------------------------
            // YATAK
            // -------------------------------------------------

            if (state.is(BlockTags.BEDS)
                    || block instanceof BedBlock) {

                score += 50;

                if (bedPos == null) {
                    bedPos = pos.immutable();
                }
            }

            // -------------------------------------------------
            // SANDIK / VARİL
            // -------------------------------------------------

            if (block instanceof AbstractChestBlock
                    || block instanceof BarrelBlock) {

                score += 10;
            }

            // -------------------------------------------------
            // FIRIN
            // -------------------------------------------------

            if (block instanceof AbstractFurnaceBlock) {
                score += 10;
            }

            // -------------------------------------------------
            // CRAFTING TABLE
            // -------------------------------------------------

            if (state.is(Blocks.CRAFTING_TABLE)) {
                score += 10;
            }

            // -------------------------------------------------
            // CAM
            // -------------------------------------------------

            if (isAnyGlass(state, block)) {

                hasGlass = true;

                if (glassPos == null) {
                    glassPos = pos.immutable();
                }
            }

            // -------------------------------------------------
            // KAPI
            // -------------------------------------------------

            if (state.is(BlockTags.DOORS)
                    || block instanceof DoorBlock) {

                hasDoor = true;
            }
        }

        // =====================================================
        // KAPI BONUSU
        // =====================================================

        if (hasDoor) {
            score = (int) (score * 1.5D);
        }

        // =====================================================
        // EV
        // =====================================================

        if (score >= 80) {

            playerHomes.put(
                    player.getUUID(),
                    bedPos != null
                            ? bedPos
                            : playerPos.immutable()
            );

            // =================================================
            // 1 / 900 CAM OLAYI
            // =================================================

            if (hasGlass
                    && glassPos != null
                    && !hasActiveWindowCreature(player)
                    && level.getRandom().nextInt(900) == 0) {

                BlockPos spawnPos =
                        findWindowSpawnPosition(
                                level,
                                glassPos,
                                playerPos
                        );

                /*
                 * Olay iptal edilmiyor.
                 *
                 * Uygun zemin bulunamazsa bile camın
                 * dışındaki güvenli boş pozisyon kullanılır.
                 */
                if (spawnPos == null) {

                    spawnPos =
                            findFallbackWindowPosition(
                                    level,
                                    glassPos,
                                    playerPos
                            );
                }

                triggerWindowStareEvent(
                        level,
                        player,
                        glassPos,
                        spawnPos
                );
            }
        }
    }

    // =========================================================
    // CAMIN DIŞINDA SPAWN POZİSYONU
    // =========================================================

    private static BlockPos findWindowSpawnPosition(
            ServerLevel level,
            BlockPos glassPos,
            BlockPos playerPos
    ) {

        /*
         * Oyuncu -> cam yönünü bul.
         *
         * Creature3 camın EVİN DIŞ tarafında
         * olacak.
         */

        int dx =
                Integer.signum(
                        glassPos.getX()
                                - playerPos.getX()
                );

        int dz =
                Integer.signum(
                        glassPos.getZ()
                                - playerPos.getZ()
                );

        if (dx == 0 && dz == 0) {
            dz = 1;
        }

        /*
         * Camın dış tarafı.
         */
        BlockPos outsideBase =
                glassPos.offset(
                        dx * 2,
                        0,
                        dz * 2
                );

        /*
         * Cam sütununun yüksekliğini bul.
         *
         * Amaç:
         * Creature3'ün kafa + gövdesi camın
         * görüş alanına gelsin.
         */
        int bottomY =
                glassPos.getY();

        int topY =
                glassPos.getY();

        for (int i = 1; i <= 8; i++) {

            BlockPos up =
                    glassPos.above(i);

            if (isAnyGlass(
                    level.getBlockState(up),
                    level.getBlockState(up).getBlock()
            )) {

                topY = up.getY();

            } else {
                break;
            }
        }

        /*
         * Camın ortasına yakın yükseklik.
         *
         * Creature3'ün gövde + kafa kısmının
         * camdan görünmesini sağlar.
         */
        int desiredY =
                bottomY
                        + Math.max(
                                0,
                                (topY - bottomY) / 2
                        );

        /*
         * Önce tam hedef Y seviyesini dene.
         */
        for (int horizontal = 0;
             horizontal <= 2;
             horizontal++) {

            BlockPos horizontalPos =
                    outsideBase.offset(
                            dx * horizontal,
                            0,
                            dz * horizontal
                    );

            for (int yOffset = -2;
                 yOffset <= 2;
                 yOffset++) {

                int y =
                        desiredY + yOffset;

                BlockPos feet =
                        new BlockPos(
                                horizontalPos.getX(),
                                y,
                                horizontalPos.getZ()
                        );

                if (!isSpawnSpaceClear(
                        level,
                        feet
                )) {
                    continue;
                }

                /*
                 * Zemin varsa normal şekilde
                 * ayakları yere basar.
                 */
                BlockState below =
                        level.getBlockState(
                                feet.below()
                        );

                if (below.blocksMotion()) {
                    return feet.immutable();
                }
            }
        }

        return null;
    }

    // =========================================================
    // FALLBACK SPAWN
    // =========================================================

    private static BlockPos findFallbackWindowPosition(
            ServerLevel level,
            BlockPos glassPos,
            BlockPos playerPos
    ) {

        int dx =
                Integer.signum(
                        glassPos.getX()
                                - playerPos.getX()
                );

        int dz =
                Integer.signum(
                        glassPos.getZ()
                                - playerPos.getZ()
                );

        if (dx == 0 && dz == 0) {
            dz = 1;
        }

        /*
         * Camın hemen dışından başlayarak
         * daha geniş bir alan ara.
         */
        for (int distance = 2;
             distance <= 5;
             distance++) {

            BlockPos base =
                    glassPos.offset(
                            dx * distance,
                            0,
                            dz * distance
                    );

            /*
             * Camın yüksekliğini takip et.
             */
            for (int y = glassPos.getY() - 2;
                 y <= glassPos.getY() + 5;
                 y++) {

                BlockPos feet =
                        new BlockPos(
                                base.getX(),
                                y,
                                base.getZ()
                        );

                if (isSpawnSpaceClear(
                        level,
                        feet
                )) {

                    /*
                     * Zemin varsa zemine bas.
                     */
                    if (level.getBlockState(
                            feet.below()
                    ).blocksMotion()) {

                        return feet.immutable();
                    }
                }
            }
        }

        /*
         * Oyuncunun yaptığı yapı tamamen havadaysa
         * olay yine iptal edilmez.
         *
         * Bu durumda camın dış tarafındaki boş
         * konumu kullan.
         */
        return glassPos.offset(
                dx * 2,
                0,
                dz * 2
        ).immutable();
    }

    // =========================================================
    // SPAWN ALANI BOŞ MU?
    // =========================================================

    private static boolean isSpawnSpaceClear(
            ServerLevel level,
            BlockPos feet
    ) {

        BlockPos body =
                feet.above();

        BlockPos head =
                feet.above(2);

        /*
         * Creature3'ün ayak, gövde ve kafa
         * alanı camın içine gömülmemeli.
         */
        return isPassable(
                level,
                feet
        )
                && isPassable(
                        level,
                        body
                )
                && isPassable(
                        level,
                        head
                );
    }

    private static boolean isPassable(
            ServerLevel level,
            BlockPos pos
    ) {

        BlockState state =
                level.getBlockState(pos);

        return state.isAir()
                || !state.blocksMotion();
    }

    // =========================================================
    // CREATURE3 OLUŞTUR
    // =========================================================

    private static void triggerWindowStareEvent(
            ServerLevel level,
            ServerPlayer target,
            BlockPos glassPos,
            BlockPos spawnPos
    ) {

        if (target == null) {
            return;
        }

        if (ModEntities.CREATURE3 == null
                || ModEntities.CREATURE3.get() == null) {
            return;
        }

        Creature3 creature =
                ModEntities.CREATURE3
                        .get()
                        .create(level);

        if (creature == null) {
            return;
        }

        // =====================================================
        // CREATURE3 POZİSYONU
        // =====================================================

        /*
         * Camın dışına yerleştir.
         *
         * Oyuncuya dönük olacak.
         */
        creature.moveTo(
                spawnPos.getX() + 0.5D,
                spawnPos.getY(),
                spawnPos.getZ() + 0.5D,
                target.getYRot() + 180.0F,
                0.0F
        );

        /*
         * ŞU ANDA TARGET VERİLMİYOR.
         *
         * Böylece Creature3ChaseGoal hemen
         * oyuncuyu kovalamaya başlamaz.
         */
        level.addFreshEntity(creature);

        windowCreatures.put(
                target.getUUID(),
                creature
        );

        windowTriggered.put(
                target.getUUID(),
                false
        );
    }

    // =========================================================
    // CREATURE3 OYUNCUYA BAKIYOR MU?
    // =========================================================

    private static void checkWindowCreatureLook(
            ServerPlayer player,
            ServerLevel level
    ) {

        Creature3 creature =
                windowCreatures.get(
                        player.getUUID()
                );

        if (creature == null) {
            return;
        }

        if (!creature.isAlive()) {

            windowCreatures.remove(
                    player.getUUID()
            );

            windowTriggered.remove(
                    player.getUUID()
            );

            return;
        }

        if (Boolean.TRUE.equals(
                windowTriggered.get(
                        player.getUUID()
                )
        )) {
            return;
        }

        /*
         * Oyuncunun gözlerinden Creature3'e
         * doğru yön.
         */
        Vec3 playerEyes =
                player.getEyePosition();

        Vec3 directionToCreature =
                creature.position()
                        .add(0.0D, 1.0D, 0.0D)
                        .subtract(playerEyes)
                        .normalize();

        Vec3 playerLook =
                player.getLookAngle()
                        .normalize();

        /*
         * 0.98 = oldukça dar görüş açısı.
         *
         * Yani oyuncunun gerçekten Creature3'e
         * bakması gerekiyor.
         */
        double dot =
                playerLook.dot(
                        directionToCreature
                );

        if (dot >= 0.98D) {

            windowTriggered.put(
                    player.getUUID(),
                    true
            );

            executeWindowBreakEvent(
                    level,
                    player
            );

            /*
             * Creature3 artık normal hedefini
             * alabilir ve kovalamaya başlayabilir.
             */
            creature.setTarget(player);

            windowCreatures.remove(
                    player.getUUID()
            );

            windowTriggered.remove(
                    player.getUUID()
            );
        }
    }

    // =========================================================
    // CAM BASKINI
    // =========================================================

    private static void executeWindowBreakEvent(
            ServerLevel level,
            ServerPlayer target
    ) {

        BlockPos center =
                target.blockPosition();

        for (BlockPos pos : BlockPos.betweenClosed(
                center.offset(-10, -5, -10),
                center.offset(10, 5, 10)
        )) {

            if (!level.hasChunkAt(pos)) {
                continue;
            }

            BlockState state =
                    level.getBlockState(pos);

            Block block =
                    state.getBlock();

            // -------------------------------------------------
            // CAMLARI KIR
            // -------------------------------------------------

            if (isAnyGlass(state, block)) {

                level.destroyBlock(
                        pos,
                        false
                );
            }

            // -------------------------------------------------
            // SANDIK / VARİL ÜSTÜNDE ATEŞ
            // -------------------------------------------------

            if (block instanceof AbstractChestBlock
                    || block instanceof BarrelBlock) {

                BlockPos firePos =
                        pos.above();

                if (level.isEmptyBlock(firePos)) {

                    BlockState fireState =
                            BaseFireBlock.getState(
                                    level,
                                    firePos
                            );

                    level.setBlock(
                            firePos,
                            fireState,
                            3
                    );
                }
            }
        }
    }

    // =========================================================
    // AKTİF CREATURE3 VAR MI?
    // =========================================================

    private static boolean hasActiveWindowCreature(
            ServerPlayer player
    ) {

        Creature3 creature =
                windowCreatures.get(
                        player.getUUID()
                );

        return creature != null
                && creature.isAlive();
    }

    // =========================================================
    // OYUNCUNUN EVİNİ AL
    // =========================================================

    public static BlockPos getPlayerHome(
            UUID playerUUID
    ) {

        return playerHomes.get(
                playerUUID
        );
    }
}
