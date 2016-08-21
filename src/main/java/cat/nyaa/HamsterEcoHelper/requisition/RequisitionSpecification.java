package cat.nyaa.HamsterEcoHelper.requisition;

import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Repairable;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static cat.nyaa.HamsterEcoHelper.Configuration.*;

public class RequisitionSpecification {
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
    public int timeoutTicks = 20 * 60 * 5; // 5 minutes

    public MatchingSpecification matchRule = new MatchingSpecification();

    public static RequisitionSpecification fromConfig(ConfigurationSection s) {
        RequisitionSpecification tmp = new RequisitionSpecification();
        tmp.loadFrom(s);
        return tmp;
    }

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

    enum MatchingMode {
        EXACT,
        EXACT_TEXT, // ignore the control chars for strings.
        CONTAINS,
        CONTAINS_TEXT,  // ignore the control chars for strings.
        ARBITRARY;
    }

    public class MatchingSpecification {
        @Serializable
        public boolean requireExact = false; // Require item to be exactly the same. e.g. can stack together. Ignore all other rules.
        @Serializable
        public int minDamageValue = -2; // `-1` means `arbitrary`; `-2` means `same as template`
        @Serializable
        public int maxDamageValue = -2; // `-1` means `arbitrary`; `-2` means `same as template`
        @Serializable
        public MatchingMode enchantMatch = MatchingMode.CONTAINS;
        @Serializable
        public MatchingMode loreMatch = MatchingMode.EXACT_TEXT;
        @Serializable
        public MatchingMode nameMatch = MatchingMode.ARBITRARY;
        @Serializable
        public MatchingMode repairCostMatch = MatchingMode.EXACT;

        public void dumpTo(ConfigurationSection s) {
            serialize(s, this);
        }

        public void loadFrom(ConfigurationSection s) {
            if (s == null) return;
            deserialize(s, this);
        }

        public boolean matches(ItemStack anotherItem) {
            ItemStack base = itemTemplate.clone();
            ItemStack given = anotherItem.clone();
            base.setAmount(1);
            given.setAmount(1);
            if (requireExact) return base.equals(given);
            if (!base.getType().equals(given.getType())) return false;

            if (repairCostMatch == MatchingMode.EXACT) {
                if (base.getItemMeta() instanceof Repairable && given.getItemMeta() instanceof Repairable) {
                    int cost1 = ((Repairable) given.getItemMeta()).getRepairCost();
                    int cost2 = ((Repairable) base.getItemMeta()).getRepairCost();
                    if (cost1 != cost2) return false;
                } else if (base.getItemMeta() instanceof Repairable || given.getItemMeta() instanceof Repairable) {
                    return false;
                }
            }

            int baseDamage = base.getDurability();
            int givenDamage = given.getDurability();
            if (minDamageValue == -2 && givenDamage < baseDamage) return false;
            if (minDamageValue >= 0 && givenDamage < minDamageValue) return false;
            if (maxDamageValue == -2 && givenDamage > baseDamage) return false;
            if (maxDamageValue >= 0 && givenDamage > maxDamageValue) return false;

            String baseDisplay = getDisplayName(base);
            String givenDisplay = getDisplayName(given);
            if (nameMatch == MatchingMode.EXACT && !baseDisplay.equals(givenDisplay)) return false;
            if (nameMatch == MatchingMode.EXACT_TEXT && !ChatColor.stripColor(baseDisplay).equals(ChatColor.stripColor(givenDisplay)))
                return false;
            if (nameMatch == MatchingMode.CONTAINS && !givenDisplay.contains(baseDisplay)) return false;
            if (nameMatch == MatchingMode.CONTAINS_TEXT && !ChatColor.stripColor(givenDisplay).contains(ChatColor.stripColor(baseDisplay)))
                return false;

            Map<Enchantment, Integer> baseEnch = base.getEnchantments();
            Map<Enchantment, Integer> givenEnch = given.getEnchantments();
            if (enchantMatch == MatchingMode.EXACT || enchantMatch == MatchingMode.EXACT_TEXT) {
                if (!baseEnch.equals(givenEnch)) return false;
            } else if (enchantMatch == MatchingMode.CONTAINS || enchantMatch == MatchingMode.CONTAINS_TEXT) {
                for (Map.Entry<Enchantment, Integer> e : baseEnch.entrySet()) {
                    if (!givenEnch.containsKey(e.getKey()) || givenEnch.get(e.getKey()) < e.getValue())
                        return false;
                }
            }

            String[] baseLore = getLore(base);
            String[] givenLore = getLore(given);
            if (loreMatch == MatchingMode.EXACT && !Arrays.deepEquals(baseLore, givenLore)) return false;
            if (loreMatch == MatchingMode.CONTAINS && !containStrArr(givenLore, baseLore, false)) return false;
            if (loreMatch == MatchingMode.EXACT_TEXT) {
                for (int i = 0; i < baseLore.length; i++) baseLore[i] = ChatColor.stripColor(baseLore[0]);
                for (int i = 0; i < baseLore.length; i++) baseLore[i] = ChatColor.stripColor(baseLore[0]);
                if (!Arrays.deepEquals(baseLore, givenLore)) return false;
            }
            if (loreMatch == MatchingMode.CONTAINS_TEXT && !containStrArr(givenLore, baseLore, true)) return false;

            return true;
        }

        private String getDisplayName(ItemStack i) {
            if (i.hasItemMeta() && i.getItemMeta().hasDisplayName()) return i.getItemMeta().getDisplayName();
            return i.getType().name();
        }

        private String[] getLore(ItemStack i) {
            if (!i.hasItemMeta() || !i.getItemMeta().hasLore()) return new String[0];
            return i.getItemMeta().getLore().toArray(new String[0]);
        }

        private boolean containStrArr(String[] sample, String[] pattern, boolean stripColor) {
            Set<String> sampleSet = new HashSet<>();
            for (String s : sample) {
                sampleSet.add(stripColor ? ChatColor.stripColor(s) : s);
            }
            for (String s : pattern) {
                if (!sampleSet.contains(s))
                    return false;
            }
            return true;
        }

    }
}
