package cat.nyaa.HamsterEcoHelper.requisition;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import static cat.nyaa.HamsterEcoHelper.Configuration.*;

public class RequisitionSpecification{
    @Serializable
    public ItemStack itemTemplate = null;
    @Serializable
    public int minPurchasePrice = 10;
    @Serializable
    public int maxPurchasePrice = 100;
    @Serializable
    public int minAmount = 1;
    @Serializable
    public int maxAmount = -1; // `-1` means `unlimited`
    @Serializable
    public double randomWeight = 100;
    @Serializable
    public int timeoutTicks = 20*60*5; // 5 minutes

    public MatchingSpecification matchRule = new MatchingSpecification();

    public void loadFrom(ConfigurationSection s) {
        deserialize(s, this);
        if (itemTemplate == null)
            throw new IllegalArgumentException("RequisitionSpecification gets `null` item");
        matchRule = new MatchingSpecification();
        matchRule.loadFrom(s.getConfigurationSection("matchRule"));
    }

    public void dumpTo(ConfigurationSection s) {
        serialize(s, this);
        matchRule.dumpTo(s.createSection("matchRule"));
    }

    public static RequisitionSpecification fromConfig(ConfigurationSection s) {
        RequisitionSpecification tmp = new RequisitionSpecification();
        tmp.loadFrom(s);
        return tmp;
    }

    public class MatchingSpecification {
        @Serializable
        public boolean requireExact = false; // Require item to be exactly the same. e.g. can stack together. Ignore all other rules.
        @Serializable
        public int minDamageValue = -2; // `-1` means `unlimited`; `-2` means `same as template`
        @Serializable
        public int maxDamageValue = -2; // `-1` means `unlimited`; `-2` means `same as template`
        @Serializable
        public MatchingMode enchantMatch = MatchingMode.CONTAINS;
        @Serializable
        public MatchingMode loreMatch = MatchingMode.EXACT_TEXT;

        public void dumpTo(ConfigurationSection s) {
            serialize(s, this);
        }

        public void loadFrom(ConfigurationSection s) {
            if (s == null) return;
            deserialize(s, this);
        }
    }

    enum MatchingMode {
        EXACT,
        EXACT_TEXT, // ignore the control chars for strings.
        CONTAINS,
        ARBITRARY;
    }
}
