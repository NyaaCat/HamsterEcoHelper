package cat.nyaa.hamsterecohelper;

import cat.nyaa.hamsterecohelper.enums.VaultType;
import cat.nyaa.nyaacore.configuration.PluginConfigure;
import org.bukkit.plugin.java.JavaPlugin;

public class Configuration extends PluginConfigure {

    @Override
    protected JavaPlugin getPlugin() {
        return HamsterEcoHelper.instance;
    }

    @Serializable
    public String language = "en_US";

    @Serializable(name = "balance.base")
    public double balanceBase = 10000;
    @Serializable(name = "balance.vault")
    public VaultType vaultType = VaultType.SYSTEM;
    @Serializable(name = "balance.player")
    public String vaultPlayer = "";

    @Serializable(name = "tax.market")
    public double marketTax = 10;
    @Serializable(name = "tax.signshop")
    public double signShopTax = 5;
    @Serializable(name = "tax.direct")
    public double directTax = 5;
    @Serializable(name = "tax.auction")
    public double auctionTax = 5;
    @Serializable(name = "tax.requisition")
    public double requisitionTax = 20;

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
    @Serializable(name = "fee.requsition.base")
    public double requsiTionFeeBase = 50;

    @Serializable(name = "limit.slots.market")
    public double limitSlotMarket = 5;
    @Serializable(name = "limit.slots.signshop")
    public double limitSlotSignshop = 100;
    @Serializable(name = "limit.signs")
    public double limitSigns = 3;
    @Serializable(name = "limit.frames")
    public double limitFrames = 12;
}
