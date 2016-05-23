package cat.nyaa.HamsterEcoHelper.data;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import static cat.nyaa.HamsterEcoHelper.Configuration.*;

public class RequisitionSpecification implements ISerializable{
    @Serializable
    public ItemStack itemTemplate = null;
    @Serializable
    public int minPurchasePrice = 10;
    @Serializable
    public int maxPurchasePrice = 100;
    @Serializable
    public double randomWeight = 100;

    @Serializable
    public int minDamageValue = -2; // `-1` means `unlimited`; `-2` means `same as template`
    @Serializable
    public int maxDamageValue = -2; // `-1` means `unlimited`; `-2` means `same as template`
    @Serializable
    public int minAmount = 1;
    @Serializable
    public int maxAmount = 1; // `-1` means `unlimited`
    @Serializable
    public int timeoutTicks = 20*60*5; // 5 minutes
    @Serializable
    public MatchingMode enchantMatch = MatchingMode.CONTAINS;
    @Serializable
    public MatchingMode loreMatch = MatchingMode.EXACT_TEXT;

    @Override
    public void loadFrom(ConfigurationSection s) {
        deserialize(s, this);
        if (itemTemplate == null)
            throw new IllegalArgumentException("RequisitionSpecification gets `null` item");
    }

    @Override
    public void dumpTo(ConfigurationSection s) {
        serialize(s, this);
    }

    public static RequisitionSpecification fromConfig(ConfigurationSection s) {
        RequisitionSpecification tmp = new RequisitionSpecification();
        tmp.loadFrom(s);
        return tmp;
    }

    enum MatchingMode {
        EXACT,
        EXACT_TEXT, // ignore the control chars for strings.
        CONTAINS,
        ARBITRARY;
    }
}
