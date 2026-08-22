package com.humanoid.horror.system;

import com.humanoid.horror.entity.Creature3;
import com.humanoid.horror.entity.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber
public class HomeTrackerManager {

    public static boolean ACTIVE = false;
    private static final Map<UUID, BlockPos> playerHomes = new HashMap<>();

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (!ACTIVE || event.phase != TickEvent.Phase.END) return;
        if (event.player == null || event.player.level().isClientSide()) return;

        if (event.player instanceof ServerPlayer player) {
            ServerLevel level = player.serverLevel();
            
            // Her 15 saniyede bir (300 tick)
            if (level.getGameTime() % 300 == 0) {
                calculateHomeScore(player, level);
            }
        }
    }
private static boolean isAnyGlass(BlockState state, Block block) {
    return block instanceof AbstractGlassBlock
            || block instanceof IronBarsBlock
            || state.is(Blocks.GLASS);
}
    private static void calculateHomeScore(ServerPlayer player, ServerLevel level) {
        if (player == null || level == null) return;

        BlockPos playerPos = player.blockPosition();
        if (!level.hasChunkAt(playerPos)) return;

        int score = 0;
        boolean hasGlass = false;
        boolean hasDoor = false;
        BlockPos glassPos = null;
        BlockPos bedPos = null;

        // Alan taraması (15x15x15 küp)
        for (BlockPos pos : BlockPos.betweenClosed(playerPos.offset(-7, -7, -7), playerPos.offset(7, 7, 7))) {
            BlockState state = level.getBlockState(pos);
            Block block = state.getBlock();

            if (state.is(BlockTags.BEDS) || block instanceof BedBlock) {
                score += 50;
                if (bedPos == null) bedPos = pos.immutable();
            }

            if (block instanceof AbstractChestBlock || block instanceof BarrelBlock) score += 10;
            if (block instanceof AbstractFurnaceBlock) score += 10;
            if (state.is(Blocks.CRAFTING_TABLE)) score += 10;

            if (isAnyGlass(state, block)) {
                hasGlass = true;
                if (glassPos == null) glassPos = pos.immutable();
            }

            if (state.is(BlockTags.DOORS) || block instanceof DoorBlock) {
                hasDoor = true;
            }
        }

        if (hasDoor) score = (int) (score * 1.5);

        if (score >= 80) {
            playerHomes.put(player.getUUID(), bedPos != null ? bedPos : playerPos.immutable());

            // Cam baskını şansı
            if (hasGlass && glassPos != null && level.getRandom().nextInt(900) == 0) {
                BlockPos safeSpawnPos = findSafeSpawnPos(level, glassPos, playerPos);
                if (safeSpawnPos != null) {
                    triggerWindowStareEvent(level, player, safeSpawnPos);
                }
            }
        }
    }

    private static boolean isAnyGlass(BlockState state, Block block) {
        return block instanceof AbstractGlassBlock || 
               block instanceof GlassPaneBlock || 
               block instanceof IronBarsBlock || 
               state.is(Blocks.GLASS);
    }

    private static BlockPos findSafeSpawnPos(ServerLevel level, BlockPos glassPos, BlockPos playerPos) {
        int dx = Integer.signum(glassPos.getX() - playerPos.getX());
        int dz = Integer.signum(glassPos.getZ() - playerPos.getZ());
        if (dx == 0 && dz == 0) dz = 1;

        BlockPos targetPos = glassPos.offset(dx * 2, 0, dz * 2);

        for (int y = -1; y <= 2; y++) {
            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++) {
                    BlockPos checkPos = targetPos.offset(x, y, z);
                    if (!level.hasChunkAt(checkPos)) continue;

                    BlockPos headPos = checkPos.above();
                    BlockPos groundPos = checkPos.below();

                    boolean isFeetPassable = level.isEmptyBlock(checkPos) || !level.getBlockState(checkPos).blocksMotion();
                    boolean isHeadPassable = level.isEmptyBlock(headPos) || !level.getBlockState(headPos).blocksMotion();
                    boolean isGroundSolid = level.getBlockState(groundPos).blocksMotion();

                    if (isFeetPassable && isHeadPassable && isGroundSolid) {
                        return checkPos.immutable();
                    }
                }
            }
        }
        return null;
    }

    private static void triggerWindowStareEvent(ServerLevel level, ServerPlayer target, BlockPos spawnPos) {
        if (ModEntities.CREATURE3 == null || ModEntities.CREATURE3.get() == null) return;

        // 1. Creature3'ü Doğur
        Creature3 creature = ModEntities.CREATURE3.get().create(level);
        if (creature != null) {
            creature.moveTo(
                    spawnPos.getX() + 0.5D,
                    spawnPos.getY(),
                    spawnPos.getZ() + 0.5D,
                    target.getYRot() + 180.0F,
                    0.0F
            );
            creature.setTarget(target);
            level.addFreshEntity(creature);
        }

        // 2. Camları Patlat ve Sandık Üstlerinde Ateş Yak
        BlockPos center = target.blockPosition();
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-10, -5, -10), center.offset(10, 5, 10))) {
            if (!level.hasChunkAt(pos)) continue;

            BlockState state = level.getBlockState(pos);
            Block block = state.getBlock();

            if (isAnyGlass(state, block)) {
                level.destroyBlock(pos, false);
            }

            if (block instanceof AbstractChestBlock || block instanceof BarrelBlock) {
                BlockPos firePos = pos.above();
                if (level.isEmptyBlock(firePos)) {
                    BlockState fireState = BaseFireBlock.getState(level, firePos);
                    level.setBlock(firePos, fireState, 3);
                }
            }
        }
    }

    public static BlockPos getPlayerHome(UUID playerUUID) {
        return playerHomes.get(playerUUID);
    }
}
