package cat.nyaa.HamsterEcoHelper.quest;

import cat.nyaa.HamsterEcoHelper.HamsterEcoHelper;
import cat.nyaa.HamsterEcoHelper.I18n;
import cat.nyaa.HamsterEcoHelper.utils.Utils;
import cat.nyaa.HamsterEcoHelper.utils.database.tables.quest.QuestEntry;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.librazy.nyaautils_lang_checker.LangKey;
import org.librazy.nyaautils_lang_checker.LangKeyType;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.UUID;

import static cat.nyaa.HamsterEcoHelper.quest.QuestWizard.State.*;

public class QuestWizard implements Listener {
    private final Player player;
    private final UUID station;
    private final int timeout;
    private final QuestEntry entry;
    private State state;
    private Timer timer;

    private final boolean reallyTakeItem;

    private class Timer extends BukkitRunnable {
        boolean cancelled = false;

        public Timer(int timeout) {
            this.runTaskLater(HamsterEcoHelper.instance, timeout*20L);
        }

        @Override
        public synchronized void cancel() throws IllegalStateException {
            super.cancel();
            this.cancelled = true;
        }

        @Override
        public void run() {
            if (cancelled) {
                this.cancel();
            } else {
                cancelWizard();
            }
        }
    }

    @LangKey(type = LangKeyType.SUFFIX)
    enum State {
        WAITING_NAME,
        WAITING_DESCRIPTION,

        WAITING_PREREQ_TYPE,
        WAITING_PREREQ_MONEY,
        WAITING_PREREQ_ITEM,

        WAITING_EARLY_REWARD_ITEM,

        WAITING_TARGET_TYPE,
        WAITING_TARGET_ITEM,

        WAITING_REWARD_TYPE,
        WAITING_REWARD_ITEM,
        WAITING_REWARD_MONEY,

        WAITING_TIME_LIMIT,
        WAITING_EXPIRE_IN,

        WAITING_IS_RECURRENT,
        WAITING_CLAIM_LIMIT,
        WAITING_ENABLED,

        FINISH,
        CANCEL;
    }

    public QuestWizard(String stationUUID, Player p, int timeoutSeconds) {
        player = p;
        station = UUID.fromString(stationUUID);
        timeout = timeoutSeconds;
        entry = new QuestEntry();
        entry.id = UUID.randomUUID().toString();
        entry.publisher = p.getUniqueId().toString();
        entry.stationId = stationUUID;
        state = State.WAITING_NAME;
        p.sendMessage(I18n.format("user.quest.wizard." + state.name().toLowerCase()));
        Bukkit.getServer().getPluginManager().registerEvents(this, HamsterEcoHelper.instance);
        timer = new Timer(timeout);
        this.reallyTakeItem = !p.hasPermission("heh.quest.admin");
    }

    private void cancelWizard() {
        state = CANCEL;
        player.sendMessage(I18n.format("user.quest.wizard.cancelled"));
        HandlerList.unregisterAll(this);
        if (reallyTakeItem) { // give items back
            for (ItemStack item : entry.rewardItem) {
                Utils.giveItem(player, item);
            }
            HamsterEcoHelper.instance.eco.deposit(player, entry.rewardMoney);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerChat(PlayerChatEvent ev) {
        if (state == CANCEL) return;
        if (!player.getUniqueId().equals(ev.getPlayer().getUniqueId())) return;
        timer.cancel();
        ev.setCancelled(true);
        String input = ev.getMessage();
        if ("cancel".equalsIgnoreCase(input)) {
            cancelWizard();
            return;
        }

        switch (state) {
            case WAITING_NAME:
                entry.questName = input;
                state = State.WAITING_DESCRIPTION;
                break;
            case WAITING_DESCRIPTION:
                entry.questDescription = input;
                state = State.WAITING_TARGET_TYPE;
                break;
            case WAITING_TARGET_TYPE:
                if ("item".equalsIgnoreCase(input)) {
                    entry.targetType = QuestEntry.QuestType.ITEM;
                    state = State.WAITING_TARGET_ITEM;
                } else {
                    entry.targetType = QuestEntry.QuestType.OTHER;
                    state = State.WAITING_REWARD_TYPE;
                }
                break;
            case WAITING_TARGET_ITEM:
                if ("end".equalsIgnoreCase(input)) {
                    if (entry.targetItems == null || entry.targetItems.size() <= 0) {
                        player.sendMessage(I18n.format("user.quest.wizard.at_least_one"));
                    } else {
                        state = State.WAITING_REWARD_TYPE;
                    }
                } else {
                    ItemStack stack = player.getInventory().getItemInMainHand();
                    if (stack == null || stack.getType() == Material.AIR) {
                        player.sendMessage(I18n.format("user.quest.wizard.hold_item_plz"));
                    } else {
                        entry.targetItems.add(stack.clone());
                    }
                }
                break;
            case WAITING_REWARD_TYPE:
                if ("item".equalsIgnoreCase(input)) {
                    entry.rewardType = QuestEntry.QuestType.ITEM;
                    state = State.WAITING_REWARD_ITEM;
                } else if ("money".equalsIgnoreCase(input)) {
                    entry.rewardType = QuestEntry.QuestType.MONEY;
                    state = State.WAITING_REWARD_MONEY;
                } else {
                    entry.rewardType = QuestEntry.QuestType.NONE;
                    state = WAITING_TIME_LIMIT;
                }
                break;
            case WAITING_REWARD_ITEM:
                if ("end".equalsIgnoreCase(input)) {
                    if (entry.rewardItem == null || entry.rewardItem.size() <= 0) {
                        player.sendMessage(I18n.format("user.quest.wizard.at_least_one"));
                    } else {
                        state = WAITING_TIME_LIMIT;
                    }
                } else {
                    ItemStack stack = player.getInventory().getItemInMainHand();
                    if (stack == null || stack.getType() == Material.AIR) {
                        player.sendMessage(I18n.format("user.quest.wizard.hold_item_plz"));
                    } else {
                        entry.rewardItem.add(stack.clone());
                        if (reallyTakeItem) {
                            player.getInventory().setItemInMainHand(new ItemStack(Material.AIR));
                        }
                    }
                }
                break;
            case WAITING_REWARD_MONEY:
                try {
                    double money = Double.parseDouble(input);
                    entry.rewardMoney = money;
                    state = WAITING_TIME_LIMIT;
                    HamsterEcoHelper.instance.eco.withdraw(player, money);
                } catch (NumberFormatException ex) {
                    player.sendMessage(I18n.format("user.quest.wizard.invalid_number"));
                }
                break;
            case WAITING_TIME_LIMIT:
                try {
                    entry.questTimeLimit = Duration.parse(input);
                    state = WAITING_EXPIRE_IN;
                } catch (DateTimeParseException ex) {
                    player.sendMessage(I18n.format("user.quest.wizard.invalid_time"));
                }
                break;
            case WAITING_EXPIRE_IN:
                try {
                    Duration dur = Duration.parse(input);
                    entry.questExpire = ZonedDateTime.now().plus(dur);
                    state = FINISH;
                } catch (DateTimeParseException ex) {
                    player.sendMessage(I18n.format("user.quest.wizard.invalid_time"));
                }
                break;
            case FINISH:
                entry.claimable = true;
                entry.iconMaterial = Material.BOOK_AND_QUILL.name();
                HamsterEcoHelper.instance.database.query(QuestEntry.class).insert(entry);
                HandlerList.unregisterAll(this);
                player.sendMessage(I18n.format("user.quest.wizard.added"));
                return;
            default:
                throw new IllegalStateException();
        }
        player.sendMessage(I18n.format("user.quest.wizard." + state.name().toLowerCase()));
        timer = new Timer(timeout);
    }
}
