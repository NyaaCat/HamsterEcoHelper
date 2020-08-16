package cat.nyaa.heh;

import cat.nyaa.heh.enums.VaultType;
import cat.nyaa.nyaacore.configuration.PluginConfigure;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.LinkedHashMap;
import java.util.Map;

public class Configuration extends PluginConfigure {



    @Override
    protected JavaPlugin getPlugin() {
        return HamsterEcoHelper.plugin;
    }

    @Serializable
    public String language = "en_US";

    @Serializable(name = "balance.base")
    public double balanceBase = 10000;
    @Serializable(name = "balance.vault")
    public VaultType vaultType = VaultType.SYSTEM;
    @Serializable(name = "balance.player")
    public String vaultPlayer = "";

    @Serializable(name = "tax")
    public Map<String, Double> taxRateMap = new LinkedHashMap<>();

    @Serializable(name = "fee.market.base")
    public double marketFeeBase = 100;
    @Serializable(name = "fee.market.storage")
    public double marketFeeStorage = 10;
    @Serializable(name = "fee.signshop.base")
    public double signShopFeeBase = 0;
    @Serializable(name = "fee.direct.base")
    public double directFeeBase = 50;
    @Serializable(name = "fee.auction.base")
    public double auctionFeeBase = 100;
    @Serializable(name = "fee.requisition.base")
    public double requisitionFeeBase = 50;

    @Serializable(name = "limit.slots.market")
    public double limitSlotMarket = 5;
    @Serializable(name = "limit.slots.signshop")
    public double limitSlotSignshop = 100;
    @Serializable(name = "limit.signs")
    public double limitSigns = 3;
    @Serializable(name = "limit.frames")
    public double limitFrames = 12;

    @Serializable(name = "auction.interval")
    public int auctionStepInterval = 200;
    @Serializable(name = "requisition.duration")
    public int requisitionDuration = 3600;
}
