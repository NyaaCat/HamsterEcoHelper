package cat.nyaa.HamsterEcoHelper;

import cat.nyaa.HamsterEcoHelper.auction.AuctionConfig;
import cat.nyaa.HamsterEcoHelper.balance.VariablesConfig;
import cat.nyaa.HamsterEcoHelper.requisition.RequisitionConfig;
import cat.nyaa.utils.ISerializable;
import cat.nyaa.utils.PluginConfigure;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;

public class Configuration extends PluginConfigure {
    private final HamsterEcoHelper plugin;

    @Serializable
    public String language = "en_US";
    @Serializable
    public int auctionIntervalTicks = 60 * 60 * 20; // 1 hour
    @Serializable
    public int requisitionIntervalTicks = 60 * 60 * 20; // 1 hour
    @Serializable
    public int auctionMaxDelayTicks = 2000;
    @Serializable
    public int requisitionMaxDelayTicks = 2000;
    @Serializable
    public int auctionMinimalPlayer = 5;
    @Serializable
    public int requisitionMinimalPlayer = 2;
    @Serializable
    public int requisitionHintInterval = 600; // ticks; 30 seconds; negative value to disable
    @Serializable
    public boolean marketPlaySound = true;
    @Serializable
    public boolean marketBroadcast = true;
    @Serializable
    public int marketBroadcastCooldown = 120;
    @Serializable
    public int market_tax = 5;
    @Serializable
    public int market_offer_fee = 10;
    @Serializable
    public int market_placement_fee = 1;
    @Serializable
    public int playerAuctionTimeoutTicks = 1000;
    @Serializable
    public int playerAuctionCooldownTicks = 3000;
    @Serializable
    public int playerAuctionCommissionFee = 0;
    @Serializable
    public int playerRequisitionTimeoutTicks = 1000;
    @Serializable
    public int playerRequisitionCooldownTicks = 3000;
    @Serializable
    public boolean enable_balance = false;

    public Map<String, Integer> marketSlot = new HashMap<>();
    @StandaloneConfig
    public AuctionConfig auctionConfig;
    @StandaloneConfig
    public RequisitionConfig requisitionConfig;
    @StandaloneConfig
    public VariablesConfig variablesConfig;

    public Configuration(HamsterEcoHelper plugin) {
        this.plugin = plugin;
        this.auctionConfig = new AuctionConfig(plugin);
        this.requisitionConfig = new RequisitionConfig(plugin);
        this.variablesConfig = new VariablesConfig(plugin);
    }

    public void loadFromPlugin() {
        deserialize(plugin.getConfig());
    }

    public void saveToPlugin() {
        serialize(plugin.getConfig());
        plugin.saveConfig();
    }

    @Override
    protected JavaPlugin getPlugin() {
        return plugin;
    }

    @Override
    public void deserialize(ConfigurationSection config) {
        ISerializable.deserialize(config, this);
        marketSlot = new HashMap<>();
        ConfigurationSection slotNumMap = plugin.getConfig().getConfigurationSection("marketSlot");
        if (slotNumMap != null) {
            for (String group : slotNumMap.getKeys(false)) {
                marketSlot.put(group, slotNumMap.getInt(group));
            }
        }
    }

    @Override
    public void serialize(ConfigurationSection config) {
        ISerializable.serialize(config, this);
        config.set("marketSlot", null);
        ConfigurationSection slotMap = config.createSection("marketSlot");
        for (String group : marketSlot.keySet()) {
            slotMap.set(group, marketSlot.get(group));
        }
    }
}
