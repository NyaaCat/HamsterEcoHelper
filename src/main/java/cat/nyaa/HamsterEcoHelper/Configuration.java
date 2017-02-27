package cat.nyaa.HamsterEcoHelper;

import cat.nyaa.HamsterEcoHelper.auction.AuctionConfig;
import cat.nyaa.HamsterEcoHelper.balance.VariablesConfig;
import cat.nyaa.HamsterEcoHelper.requisition.RequisitionConfig;
import cat.nyaa.utils.ISerializable;
import cat.nyaa.utils.PluginConfigure;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
    @Serializable
    public int balance_SaveIntervalTicks = 12000;
    @Serializable(name = "death_penalty.worlds")
    public List<String> death_penalty_worlds = new ArrayList<>();
    @Serializable(name = "death_penalty.penalty.min")
    public int death_penalty_min = 100;
    @Serializable(name = "death_penalty.penalty.max")
    public int death_penalty_max = 1000;
    @Serializable(name = "death_penalty.penalty.percent")
    public int death_penalty_percent = 10;
    @Serializable(name = "signshop.tax")
    public int signshop_tax = 0;
    public Map<String, Integer> signshop_sign_limit = new HashMap<>();
    public Map<String, Integer> signshop_slot_limit = new HashMap<>();
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
        signshop_sign_limit = new HashMap<>();
        ConfigurationSection signNumMap = plugin.getConfig().getConfigurationSection("signshop.sign_limit");
        if (signNumMap != null) {
            for (String group : signNumMap.getKeys(false)) {
                signshop_sign_limit.put(group, signNumMap.getInt(group));
            }
        }
        signshop_slot_limit = new HashMap<>();
        ConfigurationSection signShopSlotNumMap = plugin.getConfig().getConfigurationSection("signshop.slot_limit");
        if (signShopSlotNumMap != null) {
            for (String group : signShopSlotNumMap.getKeys(false)) {
                signshop_slot_limit.put(group, signShopSlotNumMap.getInt(group));
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
        config.set("signshop.sign_limit", null);
        ConfigurationSection signNumMap = config.createSection("signshop.sign_limit");
        for (String group : signshop_sign_limit.keySet()) {
            signNumMap.set(group, signshop_sign_limit.get(group));
        }
        config.set("signshop.slot_limit", null);
        ConfigurationSection signShopSlotNumMap = config.createSection("signshop.slot_limit");
        for (String group : signshop_slot_limit.keySet()) {
            signShopSlotNumMap.set(group, signshop_slot_limit.get(group));
        }
    }
}
