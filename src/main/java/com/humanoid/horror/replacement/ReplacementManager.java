package com.humanoid.horror.replacement;

import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber
public class ReplacementManager {

    /*
     * 3 Minecraft günü
     * 20 saniye
     */
    private static final long TRIGGER_TIME =
            72000L + 400L;

    /*
     * Replacement'ın yaşam süresi:
     *
     * 60 saniye = 1200 tick
     */
    private static final long REPLACEMENT_LIFETIME =
            1200L;

    /*
     * Gerçek oyuncu bağlandıktan
     * 1.5 saniye sonra atılır.
     */
    private static final long LOGIN_DELAY =
            30L;

    /*
     * %10 şans
     */
    private static final float CHANCE =
            0.25F;

    /*
     * Bir Replacement aktifken
     * yeni Replacement başlatılmaz.
     */
    private static boolean replacementActive =
            false;

    /*
     * Son Replacement'ın
     * bitiş zamanı.
     */
    private static long replacementEndTime =
            -1L;

    /*
     * Son kontrol edilen dünya zamanı.
     *
     * 3 gün + 20 saniyeye gelindiğinde
     * sadece o döngü için bir kez %10 şans denenir.
     */
    private static long lastTriggerCheck =
            -1L;

    /*
     * Replacement olarak kullanılan
     * gerçek oyuncunun UUID'si.
     */
    private static UUID replacementUUID =
            null;

    /*
     * Yeniden bağlanan oyuncuların
     * 1.5 saniye sonra atılma zamanları.
     */
    private static final Map<UUID, Long> pendingLogins =
            new HashMap<>();

    private ReplacementManager() {
    }

    @SubscribeEvent
    public static void onServerTick(
            TickEvent.ServerTickEvent event
    ) {

        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        MinecraftServer server =
                event.getServer();

        if (server == null) {
            return;
        }

        /*
         * =====================================================
         * REPLACEMENT AKTİFSE
         * =====================================================
         */

        if (replacementActive) {

            /*
             * 60 saniye doldu mu?
             */
            if (getCurrentGameTime(server)
                    >= replacementEndTime) {

                finishReplacement();

                return;
            }

            /*
             * Replacement yaşadığı sürece
             * aynı hesapla tekrar giriş yapan
             * gerçek oyuncuyu kontrol et.
             */
            processPendingLogins(server);

            return;
        }

        /*
         * =====================================================
         * YENİ DÖNGÜ
         * =====================================================
         */

        List<ServerPlayer> players =
                server.getPlayerList()
                        .getPlayers();

        /*
         * En az 2 gerçek oyuncu gerekiyor.
         */
        if (players.size() < 2) {
            return;
        }

        ServerPlayer reference =
                players.get(0);

        if (reference == null) {
            return;
        }

        long gameTime =
                reference.serverLevel()
                        .getGameTime();

        /*
         * 3 gün + 20 saniyeye daha gelmediysek
         * hiçbir şey yapma.
         */
        if (gameTime < TRIGGER_TIME) {
            return;
        }

        /*
         * Bu zaman diliminde şans zaten
         * kontrol edildiyse tekrar kontrol etme.
         */
        if (lastTriggerCheck == TRIGGER_TIME) {
            return;
        }

        /*
         * Bu döngünün %10 kontrolü yapıldı.
         */
        lastTriggerCheck = TRIGGER_TIME;

        /*
         * %10 tutmadı.
         *
         * Replacement başlamaz.
         *
         * Bir sonraki döngü için yeni 3 gün + 20 saniye
         * zamanı beklenir.
         */
        if (reference.serverLevel()
                .getRandom()
                .nextFloat() >= CHANCE) {

            return;
        }

        /*
         * =====================================================
         * %10 TUTTU
         * =====================================================
         */

        ServerPlayer selected =
                players.get(
                        reference.serverLevel()
                                .getRandom()
                                .nextInt(
                                        players.size()
                                )
                );

        if (selected == null) {
            return;
        }

        /*
         * Seçilen oyuncuyu kaydet.
         */
        replacementUUID =
                selected.getUUID();

        ReplacementPlayer.setTarget(
                selected.getUUID(),
                selected.getGameProfile()
                        .getName()
        );

        /*
         * =====================================================
         * REPLACEMENT OLUŞTUR
         * =====================================================
         */

        ReplacementEntity.create(selected);

        if (!ReplacementEntity.isActive()) {

            ReplacementPlayer.clearTarget();

            replacementUUID = null;

            return;
        }

        /*
         * Replacement artık aktif.
         */
        replacementActive = true;

        /*
         * Tam olarak 60 saniye sonra bitecek.
         */
        replacementEndTime =
                gameTime
                        + REPLACEMENT_LIFETIME;

        /*
         * Seçilen gerçek oyuncu o anda
         * sunucudaysa bağlantısını kes.
         *
         * Böylece Replacement onun yerine geçer.
         */
        selected.connection.disconnect(
                Component.literal(
                        "Connection lost."
                )
        );

        System.out.println(
                "[The Lost Place] Replacement started: "
                        + ReplacementPlayer.getTargetName()
                        + " | Lifetime: 60 seconds"
        );
    }

    /*
     * =========================================================
     * OYUNCU SUNUCUYA GİRİNCE
     * =========================================================
     */

    @SubscribeEvent
    public static void onPlayerLoggedIn(
            PlayerEvent.PlayerLoggedInEvent event
    ) {

        if (!replacementActive) {
            return;
        }

        if (!(event.getEntity()
                instanceof ServerPlayer)) {

            return;
        }

        ServerPlayer player =
                (ServerPlayer) event.getEntity();

        /*
         * Sadece Replacement'ın kullandığı
         * hesap etkilenir.
         */
        if (replacementUUID == null) {
            return;
        }

        if (!player.getUUID()
                .equals(replacementUUID)) {

            return;
        }

        MinecraftServer server =
                player.getServer();

        if (server == null) {
            return;
        }

        long gameTime =
                player.serverLevel()
                        .getGameTime();

        /*
         * 1.5 saniye sonra at.
         */
        pendingLogins.put(
                player.getUUID(),
                gameTime + LOGIN_DELAY
        );
    }

    /*
     * =========================================================
     * BEKLEYEN GİRİŞLER
     * =========================================================
     */

    private static void processPendingLogins(
            MinecraftServer server
    ) {

        if (pendingLogins.isEmpty()) {
            return;
        }

        long currentTime =
                getCurrentGameTime(server);

        pendingLogins.entrySet()
                .removeIf(entry -> {

                    UUID uuid =
                            entry.getKey();

                    long kickTime =
                            entry.getValue();

                    /*
                     * 1.5 saniye daha dolmadı.
                     */
                    if (currentTime < kickTime) {
                        return false;
                    }

                    ServerPlayer player =
                            server.getPlayerList()
                                    .getPlayer(uuid);

                    if (player != null) {

                        player.connection.disconnect(
                                Component.literal(
                                        "This account is already connected from another location."
                                )
                        );
                    }

                    return true;
                });
    }

    /*
     * =========================================================
     * REPLACEMENT'I TAMAMEN BİTİR
     * =========================================================
     */

    private static void finishReplacement() {

        System.out.println(
                "[The Lost Place] Replacement expired: "
                        + ReplacementPlayer.getTargetName()
        );

        /*
         * Bekleyen girişleri temizle.
         */
        pendingLogins.clear();

        /*
         * AI'yi sıfırla.
         */
        ReplacementAI.reset();

        /*
         * FakePlayer tamamen kaldır.
         */
        ReplacementEntity.remove();

        /*
         * Seçilen oyuncu bilgisini temizle.
         */
        ReplacementPlayer.clearTarget();

        /*
         * Replacement durumu kapat.
         */
        replacementActive = false;

        replacementUUID = null;

        replacementEndTime = -1L;

        /*
         * ÖNEMLİ:
         *
         * Burada yeni Replacement başlatılmıyor.
         *
         * Sistem tekrar 3 gün + 20 saniyelik
         * yeni döngüyü bekliyor.
         */
    }

    /*
     * =========================================================
     * OYUN ZAMANI
     * =========================================================
     */

    private static long getCurrentGameTime(
            MinecraftServer server
    ) {

        List<ServerPlayer> players =
                server.getPlayerList()
                        .getPlayers();

        if (players.isEmpty()) {
            return 0L;
        }

        return players.get(0)
                .serverLevel()
                .getGameTime();
    }

    /*
     * =========================================================
     * MANUEL RESET
     * =========================================================
     */

    public static void reset() {

        pendingLogins.clear();

        ReplacementAI.reset();

        ReplacementEntity.remove();

        ReplacementPlayer.clearTarget();

        replacementActive = false;

        replacementUUID = null;

        replacementEndTime = -1L;

        lastTriggerCheck = -1L;
    }

    /*
     * =========================================================
     * DURUM
     * =========================================================
     */

    public static boolean isReplacementActive() {
        return replacementActive
                && ReplacementEntity.isActive();
    }

    public static UUID getReplacementUUID() {
        return replacementUUID;
    }

    public static long getReplacementEndTime() {
        return replacementEndTime;
    }
}
