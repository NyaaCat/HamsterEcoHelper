package cat.nyaa.HamsterEcoHelper.balance;

import cat.nyaa.HamsterEcoHelper.CommandHandler;
import cat.nyaa.HamsterEcoHelper.HamsterEcoHelper;
import cat.nyaa.nyaacore.CommandReceiver;
import cat.nyaa.nyaacore.LanguageRepository;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BalanceCommands extends CommandReceiver<HamsterEcoHelper> {
    private HamsterEcoHelper plugin;

    public BalanceCommands(Object plugin, LanguageRepository i18n) {
        super((HamsterEcoHelper) plugin, i18n);
        this.plugin = (HamsterEcoHelper) plugin;
    }

    @CommandHandler.SubCommand(value = "pay", permission = "heh.balance.pay")
    public void pay(CommandSender sender, CommandHandler.Arguments args) {
        if (args.length() >= 5) {
            double amount = 0.0D;
            String playerName = "";
            String type = args.next().toLowerCase();
            if (type.equals("amount")) {
                amount = args.nextDouble("#.##");
                playerName = args.next();
            } else if (type.equals("percent")) {
                double percent = args.nextDouble();
                amount = (plugin.balanceAPI.getBalance() / 100) * percent;
                playerName = args.next();
                double min = args.length() >= 6 ? args.nextDouble("#.##") : -1;
                double max = args.length() == 7 ? args.nextDouble("#.##") : -1;
                if (max != -1 && amount > max) {
                    amount = max;
                }
                if (min != -1 && amount < min) {
                    amount = min;
                }
            } else {
                msg(sender, "manual.balance.pay.usage");
                return;
            }
            if (!(amount > 0.0D)) {
                msg(sender, "user.error.not_int");
                return;
            }
            Player player = Bukkit.getPlayer(playerName);
            if (player != null) {
                plugin.balanceAPI.withdraw(amount);
                plugin.eco.deposit(player, amount);
                msg(sender, "user.balance.pay", amount, playerName);
                msg(player, "user.balance.pay_notice", amount);
            } else {
                msg(sender, "user.info.player_not_found", playerName);
            }
            return;
        }
        msg(sender, "manual.balance.pay.usage");
    }

    @Override
    public String getHelpPrefix() {
        return "balance";
    }

    @CommandHandler.SubCommand(value = "take", permission = "heh.balance.take")
    public void take(CommandSender sender, CommandHandler.Arguments args) {
        if (args.length() != 4) {
            msg(sender, "manual.balance.take.usage");
            return;
        }
        String playerName = args.next();
        double amount = 0.0D;
        amount = args.nextDouble("#.##");
        if (!(amount > 0.0D)) {
            msg(sender, "user.error.not_int");
            return;
        }
        Player player = Bukkit.getPlayer(playerName);
        if (player != null) {
            if (plugin.eco.withdraw(player, amount)) {
                plugin.balanceAPI.deposit(amount);
                msg(sender, "user.balance.take", amount, playerName);
                msg(player, "user.balance.take_notice", amount);
            } else {
                msg(sender, "user.balance.take_fail", playerName);
            }
        } else {
            msg(sender, "user.info.player_not_found", playerName);
            return;
        }
    }

    @CommandHandler.SubCommand(value = "view", permission = "heh.balance.view")
    public void viewbalance(CommandSender sender, CommandHandler.Arguments args) {
        msg(sender, "user.balance.current_balance", plugin.balanceAPI.getBalance());
    }
}
