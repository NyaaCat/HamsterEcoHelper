package cat.nyaa.heh.command;

import cat.nyaa.heh.I18n;
import cat.nyaa.heh.business.auction.Auction;
import cat.nyaa.heh.utils.EcoUtils;
import cat.nyaa.nyaacore.ILocalizer;
import cat.nyaa.nyaacore.Message;
import cat.nyaa.nyaacore.cmdreceiver.Arguments;
import cat.nyaa.nyaacore.cmdreceiver.BadCommandException;
import cat.nyaa.nyaacore.cmdreceiver.CommandReceiver;
import cat.nyaa.nyaacore.cmdreceiver.SubCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;

import static cat.nyaa.heh.command.CommandUtils.filtered;

public class BidCommand extends CommandReceiver implements ShortcutCommand{
    /**
     * @param plugin for logging purpose only
     * @param _i18n
     */
    public BidCommand(Plugin plugin, ILocalizer _i18n) {
        super(plugin, _i18n);
    }
    private static final String PERMISSION_BID = "heh.business.bid";

    @SubCommand(isDefaultCommand = true, permission = PERMISSION_BID, tabCompleter = "bidCompleter")
    public void onBid(CommandSender sender, Arguments arguments){
        Auction auction = Auction.currentAuction();
        Player player = asPlayer(sender);
        if (auction == null){
            new Message(I18n.format("command.bid.no_auction")).send(sender);
            return;
        }
        if(auction.getItem().getOwner().equals(player.getUniqueId())){
            new Message(I18n.format("command.bid.self_bid")).send(sender);
            return;
        }
        double offer;
        double currentMinOffer = auction.hasOffer() ? auction.getCurrentOffer() : auction.getBasePrice();
        double minOffer;
        if (auction.hasOffer()){
            minOffer = currentMinOffer + Math.max(auction.getStepPrice(), 1);
        }else {
            minOffer = auction.getBasePrice();
        }
        String input = arguments.nextString();
        if (input.equals("min")){
            offer = minOffer;
        }else {
            try{
                offer = Double.parseDouble(input);
            }catch (NumberFormatException e){
                throw new BadCommandException();
            }
        }
        if (offer == -1 || offer < auction.getBasePrice()) {
            new Message(I18n.format("command.bid.invalid_price", minOffer)).send(sender);
            return;
        }
        if (!EcoUtils.getInstance().getEco().has(player, offer)) {
            new Message(I18n.format("command.bid.insufficient_funds")).send(sender);
            return;
        }
        auction.onBid(player.getUniqueId(), offer);
    }

    public List<String> bidCompleter(CommandSender sender, Arguments arguments) {
        List<String> completeStr = new ArrayList<>();
        switch (arguments.remains()) {
            case 1:
                completeStr.add("<price>");
                completeStr.add("min");
                break;
        }
        return filtered(arguments, completeStr);
    }

    @Override
    public String getHelpPrefix() {
        return "bid";
    }

    @Override
    public String getShortcutName() {
        return "hbid";
    }
}
