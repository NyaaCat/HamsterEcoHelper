package cat.nyaa.HamsterEcoHelper.ads;


import cat.nyaa.HamsterEcoHelper.HamsterEcoHelper;
import cat.nyaa.HamsterEcoHelper.I18n;
import cat.nyaa.nyaautils.api.events.HamsterEcoHelperTransactionApiEvent;
import cat.nyaa.nyaacore.CommandReceiver;
import cat.nyaa.nyaacore.LanguageRepository;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AdsCommands extends CommandReceiver {
    private HamsterEcoHelper plugin;

    public AdsCommands(Object plugin, LanguageRepository i18n) {
        super((HamsterEcoHelper) plugin, i18n);
        this.plugin = (HamsterEcoHelper) plugin;
    }

    @Override
    public String getHelpPrefix() {
        return "ads";
    }

    @SubCommand(value = "add", permission = "heh.ads")
    public void add(CommandSender sender, Arguments args) {
        if (args.length() >= 4) {
            Player player = asPlayer(sender);
            String adText = args.next();
            int displayAmount = args.nextInt();
            if (plugin.config.adsConfig.adsDataList.size() >= plugin.config.ads_limit_total) {
                msg(sender, "user.ads.add.full");
                return;
            }
            if (plugin.adsManager.getPlayerAdsCount(player) >= plugin.adsManager.getLimit(player)) {
                msg(sender, "user.ads.add.fail");
                return;
            }
            if (ChatColor.stripColor(adText).length() > plugin.config.ads_limit_text) {
                msg(sender, "user.ads.message_too_long", plugin.config.ads_limit_text);
                return;
            }
            if (displayAmount < plugin.config.ads_min_display || displayAmount > plugin.config.ads_max_display) {
                msg(sender, "user.ads.add.invalid_amount",
                        plugin.config.ads_min_display, plugin.config.ads_max_display);
                return;
            }
            int totalPrice = displayAmount * plugin.config.ads_price;
            boolean confirm = args.length() == 5 && (args.next().equalsIgnoreCase("confirm"));
            if (!confirm) {
                msg(sender, "user.ads.add.price", plugin.config.ads_price, totalPrice);
                msg(sender, "user.ads.add.preview");
                msg(sender, "user.ads.message", sender.getName(), AdsManager.getMessage(adText));
                if (!plugin.eco.enoughMoney(player, totalPrice)) {
                    msg(sender, "user.warn.no_enough_money");
                } else {
                    msg(sender, "user.ads.add.confirm", adText, displayAmount);
                }
            } else {
                if (!plugin.eco.enoughMoney(player, totalPrice)) {
                    msg(sender, "user.warn.no_enough_money");
                    return;
                } else {
                    plugin.config.adsConfig.pos++;
                    plugin.config.adsConfig.adsDataList.put(plugin.config.adsConfig.pos,
                            new AdsData(plugin.config.adsConfig.pos, player.getUniqueId(), adText, displayAmount));
                    HamsterEcoHelperTransactionApiEvent event = new HamsterEcoHelperTransactionApiEvent(totalPrice);
                    plugin.getServer().getPluginManager().callEvent(event);
                    plugin.eco.withdraw(player, totalPrice);
                    plugin.logger.info(I18n.format("log.info.ads_add",
                            plugin.config.adsConfig.pos, displayAmount, adText, player.getName()));
                    plugin.config.adsConfig.save();
                    msg(sender, "user.ads.add.success");
                }
            }
        } else {
            msg(sender, "manual.ads.add.usage");
        }
    }

    @SubCommand(value = "list", permission = "heh.ads")
    public void list(CommandSender sender, Arguments args) {
        Player player = asPlayer(sender);
        msg(sender, "user.ads.list.line_0");
        msg(sender, "user.ads.list.line_1");
        for (AdsData ad : plugin.config.adsConfig.adsDataList.values()) {
            if (player.getUniqueId().equals(ad.getUUID())) {
                msg(sender, "user.ads.list.line_2", ad.id, AdsManager.getMessage(ad.text), ad.displayed, ad.display_total);
            }
        }
    }

    @SubCommand(value = "revoke", permission = "heh.ads")
    public void revoke(CommandSender sender, Arguments args) {
        if (args.length() == 3) {
            Player player = asPlayer(sender);
            int adID = args.nextInt();
            if (plugin.config.adsConfig.adsDataList.containsKey(adID)) {
                AdsData ad = plugin.config.adsConfig.adsDataList.get(adID);
                if (player.getUniqueId().equals(ad.getUUID()) || player.hasPermission("heh.admin")) {
                    msg(sender, "user.ads.revoke.success");
                    plugin.logger.info(I18n.format("log.info.ads_remove", ad.id, ad.text,
                            plugin.getServer().getOfflinePlayer(ad.getUUID()).getName()));
                    plugin.config.adsConfig.adsDataList.remove(adID);
                    plugin.config.adsConfig.save();
                    return;
                } else {
                    msg(sender, "user.ads.revoke.no_permission");
                    return;
                }
            }
        }
    }

    @SubCommand(value = "mute", permission = "heh.ads")
    public void mute(CommandSender sender, Arguments args) {
        Player player = asPlayer(sender);
        if (!plugin.config.adsConfig.muteList.contains(player.getUniqueId().toString())) {
            msg(sender, "user.ads.mute");
            plugin.config.adsConfig.muteList.add(player.getUniqueId().toString());
        }
    }

    @SubCommand(value = "unmute", permission = "heh.ads")
    public void unmute(CommandSender sender, Arguments args) {
        Player player = asPlayer(sender);
        if (plugin.config.adsConfig.muteList.contains(player.getUniqueId().toString())) {
            msg(sender, "user.ads.unmute");
            plugin.config.adsConfig.muteList.remove(player.getUniqueId().toString());
        }
    }
}
