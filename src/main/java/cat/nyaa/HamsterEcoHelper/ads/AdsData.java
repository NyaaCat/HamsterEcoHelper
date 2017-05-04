package cat.nyaa.HamsterEcoHelper.ads;

import cat.nyaa.nyaacore.configuration.ISerializable;

import java.util.UUID;

public class AdsData implements ISerializable {
    @Serializable
    public int id;
    @Serializable
    public String playerUUID;
    @Serializable
    public String text;
    @Serializable
    public int display_total;
    @Serializable
    public int displayed;

    public AdsData() {
    }

    public AdsData(int id, UUID player, String adsText, int total) {
        this.id = id;
        setUUID(player);
        this.text = adsText;
        this.display_total = total;
        this.displayed = 0;
    }

    public UUID getUUID() {
        return UUID.fromString(playerUUID);
    }

    public void setUUID(UUID uuid) {
        this.playerUUID = uuid.toString();
    }

}
