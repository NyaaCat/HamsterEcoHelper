package cat.nyaa.HamsterEcoHelper.quest.gui;

import cat.nyaa.HamsterEcoHelper.HamsterEcoHelper;
import cat.nyaa.HamsterEcoHelper.I18n;
import cat.nyaa.HamsterEcoHelper.utils.database.tables.quest.QuestEntry;
import cat.nyaa.HamsterEcoHelper.utils.database.tables.quest.QuestStation;
import cat.nyaa.nyaacore.Message;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class QuestStationGui extends PaginatedListGui {
    // static methods to make gui unique for each station
    private static final Map<String, QuestStationGui> cachedGui = new HashMap<>();
    public static QuestStationGui getStationGui(QuestStation station) {
        QuestStationGui gui = cachedGui.get(station.id);
        if (gui == null) {
            gui = new QuestStationGui(station);
            cachedGui.put(station.id, gui);
        }
        return gui;
    }

    // non-static members

    private final String stationId;
    private QuestStationGui(QuestStation station) {
        super("Quest Station");
        stationId = station.id;
        contentChanged();
    }

    @Override
    protected PairList<String, ItemStack> getFullGuiContent() {
        PairList<String, ItemStack> pair = new PairList<>();
        List<QuestEntry> quests = HamsterEcoHelper.instance.database.query(QuestEntry.class).whereEq("station_id", stationId).select();
        for (QuestEntry e : quests) {
            pair.put(e.id, buildQuestIcon(e));
        }
        return pair;
    }

    @Override
    protected void itemClicked(Player player, String itemKey, boolean isShiftClicked) {
        QuestEntry e;
        try {
            e = HamsterEcoHelper.instance.database.query(QuestEntry.class).whereEq("id", itemKey).selectUnique();
        } catch (Exception ex) {
            e = null;
        }
        if (e == null || !e.claimable) {
            player.sendMessage(I18n.format("user.quest.menu.not_available"));
            return;
        }
        if (!isShiftClicked) {
            player.sendMessage("NOT IMPLEMENTED");
            // QuestCommon.claimQuest(player, e)
        } else {
            player.sendMessage(I18n.format("user.quest.menu.name", e.questName));
            player.sendMessage(I18n.format("user.quest.menu.desc", e.questDescription));
            switch (e.targetType) {
                case OTHER: player.sendMessage(I18n.format("user.quest.menu.target_other")); break;
                case ITEM: for (ItemStack i : e.targetItems) new Message(I18n.format("user.quest.menu.target_item")).append(i).send(player); break;
                default: throw new RuntimeException("IllegalTargetType");
            }
            switch (e.rewardType) {
                case ITEM: for (ItemStack i : e.rewardItem)  new Message(I18n.format("user.quest.menu.reward_item")).append(i).send(player); break;
                case MONEY: player.sendMessage(I18n.format("user.quest.menu.reward_money", e.rewardMoney)); break;
                case NONE: player.sendMessage(I18n.format("user.quest.menu.reward_none")); break;
                default: throw new RuntimeException("IllegalTargetType");
            }
            player.sendMessage(I18n.format("user.quest.menu.time_limit", e.questTimeLimit.toString()));
            player.sendMessage(I18n.format("user.quest.menu.available_before", e.questExpire.toString()));
        }
        Bukkit.getServer().getScheduler().runTask(HamsterEcoHelper.instance,player::closeInventory);
    }

    @Override
    protected void guiCompletelyClosed() {
        cachedGui.remove(stationId);
    }

    private ItemStack buildQuestIcon(QuestEntry e) {
        ItemStack i = new ItemStack(e.claimable?e.getIconMaterial():Material.BARRIER);
        ItemMeta m = i.getItemMeta();
        m.setDisplayName(I18n.format("user.quest.menu.title"+(e.claimable?"":"_not_available"), e.questName));
        List<String> lore = new ArrayList<>();
        lore.add(e.questDescription);
        if (e.claimable) {
            lore.add(I18n.format("user.quest.menu.shift_click"));
        } else {
            lore.add(I18n.format("user.quest.menu.not_available"));
        }
        m.setLore(lore);
        i.setItemMeta(m);
        return i;
    }
}
