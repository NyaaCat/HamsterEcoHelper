package cat.nyaa.HamsterEcoHelper;

import cat.nyaa.HamsterEcoHelper.ads.AdsConfig;
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
    @Serializable(name = "ads.interval")
    public int ads_interval = 600;
    @Serializable(name = "ads.count_afk")
    public boolean ads_count_afk = false;
    @Serializable(name = "ads.count_self")
    public boolean ads_count_self = false;
    @Serializable(name = "ads.price")
    public int ads_price = 5;
    @Serializable(name = "ads.max_display")
    public int ads_max_display = 100;
    @Serializable(name = "ads.min_display")
    public int ads_min_display = 20;
    @Serializable(name = "ads.color")
    public boolean ads_color = true;
    @Serializable(name = "ads.formatting")
    public List<String> ads_formatting = new ArrayList<>();
    @Serializable(name = "ads.limit_total")
    public int ads_limit_total = 100;
    @Serializable(name = "ads.limit_text")
    public int ads_limit_text = 50;
    @Serializable(name = "lotto.force_locked")
    public boolean lotto_force_locked = false;
    @Serializable(name = "search.cooldown_tick")
    public long search_cooldown_tick = 200;
    @Serializable(name = "search.lore_additional_tick")
    public long search_lore_cooldown_tick = 200;
    @Serializable(name = "search.ench_additional_tick")
    public long search_ench_cooldown_tick = 200;


    public Map<String, Integer> signshop_sign_limit = new HashMap<>();
    public Map<String, Integer> signshop_slot_limit = new HashMap<>();
    public Map<String, Integer> marketSlot = new HashMap<>();
    public Map<String, Integer> ads_limit_group = new HashMap<>();
    @StandaloneConfig
    public AuctionConfig auctionConfig;
    @StandaloneConfig
    public RequisitionConfig requisitionConfig;
    @StandaloneConfig
    public VariablesConfig variablesConfig;
    @StandaloneConfig
    public AdsConfig adsConfig;

    public Configuration(HamsterEcoHelper plugin) {
        this.plugin = plugin;
        this.auctionConfig = new AuctionConfig(plugin);
        this.requisitionConfig = new RequisitionConfig(plugin);
        this.variablesConfig = new VariablesConfig(plugin);
        this.adsConfig = new AdsConfig(plugin);
    }

    @Override
    protected JavaPlugin getPlugin() {
        return plugin;
    }

    @Override
    public void deserialize(ConfigurationSection config) {
        ISerializable.deserialize(config, this);
        marketSlot = new HashMap<>();
        ConfigurationSection slotNumMap = config.getConfigurationSection("marketSlot");
        if (slotNumMap != null) {
            for (String group : slotNumMap.getKeys(false)) {
                marketSlot.put(group, slotNumMap.getInt(group));
            }
        }
        signshop_sign_limit = new HashMap<>();
        ConfigurationSection signNumMap = config.getConfigurationSection("signshop.sign_limit");
        if (signNumMap != null) {
            for (String group : signNumMap.getKeys(false)) {
                signshop_sign_limit.put(group, signNumMap.getInt(group));
            }
        }
        signshop_slot_limit = new HashMap<>();
        ConfigurationSection signShopSlotNumMap = config.getConfigurationSection("signshop.slot_limit");
        if (signShopSlotNumMap != null) {
            for (String group : signShopSlotNumMap.getKeys(false)) {
                signshop_slot_limit.put(group, signShopSlotNumMap.getInt(group));
            }
        }
        ads_limit_group = new HashMap<>();
        ConfigurationSection adsLimitNumMap = config.getConfigurationSection("ads.limit_group");
        if (adsLimitNumMap != null) {
            for (String group : adsLimitNumMap.getKeys(false)) {
                ads_limit_group.put(group, adsLimitNumMap.getInt(group));
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
        config.set("ads.limit_group", null);
        ConfigurationSection adsLimitNumMap = config.createSection("ads.limit_group");
        for (String group : ads_limit_group.keySet()) {
            adsLimitNumMap.set(group, ads_limit_group.get(group));
        }
    }
}
