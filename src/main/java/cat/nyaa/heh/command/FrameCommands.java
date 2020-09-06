package cat.nyaa.heh.command;

import cat.nyaa.heh.I18n;
import cat.nyaa.heh.business.signshop.ItemFrameShop;
import cat.nyaa.nyaacore.ILocalizer;
import cat.nyaa.nyaacore.Message;
import cat.nyaa.nyaacore.cmdreceiver.Arguments;
import cat.nyaa.nyaacore.cmdreceiver.CommandReceiver;
import cat.nyaa.nyaacore.cmdreceiver.SubCommand;
import cat.nyaa.nyaacore.utils.RayTraceUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;

import static cat.nyaa.heh.command.CommandUtils.filtered;

public class FrameCommands extends CommandReceiver implements ShortcutCommand{

    /**
     * @param plugin for logging purpose only
     * @param _i18n
     */
    public FrameCommands(Plugin plugin, ILocalizer _i18n) {
        super(plugin, _i18n);
    }

    @Override
    public String getHelpPrefix() {
        return "shop";
    }
    private static final String PERMISSION_FRAME = "heh.business.frame";

    @SubCommand(value = "set", permission = PERMISSION_FRAME)
    public void onSet(CommandSender sender, Arguments arguments){
        Player player = asPlayer(sender);
        Entity targetEntity = RayTraceUtils.getTargetEntity(player, 10);
        if (!(targetEntity instanceof ItemFrame)){
            new Message(I18n.format("command.frame.set.not_frame")).send(sender);
            return;
        }
        ItemFrame f = (ItemFrame) targetEntity;
        ItemStack item = f.getItem();
        if (f.isFixed() || !item.getType().isAir()){
            new Message(I18n.format("command.frame.set.invalid_frame")).send(sender);
            return;
        }
        ItemFrameShop itemFrameShop = new ItemFrameShop(player, f);
        ItemFrameShop.newFrameShop(itemFrameShop);
        new Message(I18n.format("command.frame.set.success")).send(sender);
    }

    @SubCommand(value = "remove", permission = PERMISSION_FRAME)
    public void onRemove(CommandSender sender, Arguments arguments){

    }

    public List<String> sampleCompleter(CommandSender sender, Arguments arguments) {
        List<String> completeStr = new ArrayList<>();
        switch (arguments.remains()) {
            case 1:
                break;
        }
        return filtered(arguments, completeStr);
    }

    @Override
    public String getShortcutName() {
        return "hFrame";
    }
}
