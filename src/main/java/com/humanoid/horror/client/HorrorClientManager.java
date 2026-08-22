.class public Lcom/humanoid/horror/client/HorrorClientManager;
.super Ljava/lang/Object;

# Sistemin aktif olup olmadığını tutan sinsi anahtarımız
.field public static isHorrorActive:Z

.method public static constructor <clinit>()V
    .registers 1
    const/4 v0, 0x0
    sput-boolean v0, Lcom/humanoid/horror/client/HorrorClientManager;->isHorrorActive:Z
    return-void
.end method

.method public constructor <init>()V
    .registers 1
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V
    return-void
.end method

# --- TETİKLENMEYİ BEKLEYEN ANA METOT ---
.method public static activateClientHorror()V
    .registers 1
    
    const/4 v0, 0x1
    sput-boolean v0, Lcom/humanoid/horror/client/HorrorClientManager;->isHorrorActive:Z

    return-void
.end method

# --- SİS MOTORU KANCASI (FogRenderer İçin) ---
.method public static renderHorrorFog()V
    .registers 2
    
    sget-boolean v0, Lcom/humanoid/horror/client/HorrorClientManager;->isHorrorActive:Z
    if-nez v0, :out

    # Fog Start: Sisin tatlı tatlı başladığı yer (Yaklaşık 3-4 blok sonrası)
    const/high16 v0, 0x40800000 # Float: 4.0F
    invoke-static {v0}, Lcom/mojang/blaze3d/systems/RenderSystem;->setShaderFogStart(F)V

    # Fog End: 10 blok sonrasını hafifçe seçebilsinler diye sınırı 15.0F yaptık kanka!
    const/high16 v0, 0x41700000 # Float: 15.0F
    invoke-static {v0}, Lcom/mojang/blaze3d/systems/RenderSystem;->setShaderFogEnd(F)V

:out
    return-void
.end method

# --- İSİM ETİKETİ GİZLEME KANCASI (PlayerRenderer İçin) ---
.method public static shouldShowNames()Z
    .registers 1
    
    sget-boolean v0, Lcom/humanoid/horror/client/HorrorClientManager;->isHorrorActive:Z
    
    if-eqz v0, :hide

    const/4 v0, 0x1
    return v0

:hide
    const/4 v0, 0x0 # İsimleri kapat!
    return v0
.end method
