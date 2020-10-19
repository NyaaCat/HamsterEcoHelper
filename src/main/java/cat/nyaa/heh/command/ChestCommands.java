package cat.nyaa.heh.command;

import cat.nyaa.heh.HamsterEcoHelper;
import cat.nyaa.heh.I18n;
import cat.nyaa.heh.db.LocationConnection;
import cat.nyaa.heh.db.model.LocationDbModel;
import cat.nyaa.heh.db.model.LocationType;
import cat.nyaa.heh.utils.Utils;
import cat.nyaa.nyaacore.ILocalizer;
import cat.nyaa.nyaacore.Message;
import cat.nyaa.nyaacore.cmdreceiver.Arguments;
import cat.nyaa.nyaacore.cmdreceiver.CommandReceiver;
import cat.nyaa.nyaacore.cmdreceiver.SubCommand;
import me.crafter.mc.lockettepro.LockettePro;
import me.crafter.mc.lockettepro.LocketteProAPI;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;

import static cat.nyaa.heh.command.CommandUtils.filtered;

public class ChestCommands extends CommandReceiver implements ShortcutCommand{

    /**
     * @param plugin for logging purpose only
     * @param _i18n
     */
    public ChestCommands(Plugin plugin, ILocalizer _i18n) {
        super(plugin, _i18n);
    }

    @Override
    public String getHelpPrefix() {
        return "chest";
    }

    private static final String PERMISSION_CHEST_LOTTO = "heh.business.chest.lotto";
    private static final String PERMISSION_CHEST_REQ = "heh.business.chest.req";

    @SubCommand(value = "lotto", permission = PERMISSION_CHEST_LOTTO)
    public void onLotto(CommandSender sender, Arguments arguments){
        Player player = asPlayer(sender);
        Block targetBlock = player.getTargetBlockExact(10);
        if (targetBlock != null && !(targetBlock.getState() instanceof Chest)) {
           new Message(I18n.format("command.chest.error.not_chest")).send(sender);
           return;
        }
        Chest state = (Chest) targetBlock.getState();
        LocationConnection instance = LocationConnection.getInstance();
        LocationDbModel lottoChestForPlayer = instance.getLottoChestForPlayer(player);
        if (lottoChestForPlayer != null){
            if (!lottoChestForPlayer.getOwner().equals(player.getUniqueId()) && !player.isOp()) {
                new Message(I18n.format("command.chest.req.not_owner")).send(sender);
                return;
            }
            instance.updateChestLocation(player, LocationType.CHEST_LOTTO, state);
        } else{
            if (isLocked(targetBlock) && !isOwner(targetBlock, player)){
                new Message(I18n.format("command.chest.req.not_owner")).send(sender);
                return;
            }
            LocationDbModel chestModel = LocationConnection.getInstance().newLocationModel(LocationType.CHEST_LOTTO, player.getUniqueId(), state.getLocation());
            LocationConnection.getInstance().insertLocationModel(chestModel);
        }
        new Message(I18n.format("command.chest.lotto.success")).send(sender);
    }

    @SubCommand(value = "req", permission = PERMISSION_CHEST_REQ)
    public void onReq(CommandSender sender, Arguments arguments){
        Player player = asPlayer(sender);
        Block targetBlock = player.getTargetBlockExact(10);
        if (targetBlock != null && !(targetBlock.getState() instanceof Chest)) {
            new Message(I18n.format("command.chest.error.not_chest")).send(sender);
            return;
        }
        Chest state = (Chest) targetBlock.getState();
        LocationConnection instance = LocationConnection.getInstance();
        LocationDbModel lottoChestForPlayer = instance.getReqLocationModel(player.getUniqueId());
        if (lottoChestForPlayer != null){
            if (!lottoChestForPlayer.getOwner().equals(player.getUniqueId()) && !player.isOp()) {
                new Message(I18n.format("command.chest.req.not_owner")).send(sender);
                return;
            }
            instance.updateChestLocation(player, LocationType.CHEST_BUY, state);
        } else{
            if (isLocked(targetBlock) && !isOwner(targetBlock, player)){
                new Message(I18n.format("command.chest.req.not_owner")).send(sender);
                return;
            }
            LocationDbModel chestModel = LocationConnection.getInstance().newLocationModel(LocationType.CHEST_BUY, player.getUniqueId(), state.getLocation());
            LocationConnection.getInstance().insertLocationModel(chestModel);
        }
        new Message(I18n.format("command.chest.req.success")).send(sender);
    }

    @SubCommand(value = "remove", permission = PERMISSION_CHEST_REQ)
    public void onRemove(CommandSender sender, Arguments arguments){
        Player player = asPlayer(sender);
        Block targetBlock = player.getTargetBlockExact(10);
        if (targetBlock == null || !(targetBlock.getState() instanceof Chest)) {
            new Message(I18n.format("command.chest.error.not_chest")).send(sender);
            return;
        }
        Chest state = (Chest) targetBlock.getState();
        Location location = state.getLocation();
        LocationConnection instance = LocationConnection.getInstance();
        LocationDbModel dbModel = instance.getModelAt(location);
        if (dbModel == null){
            BlockFace relativeFace = Utils.getRelativeChestFace(targetBlock);
            Block relative = targetBlock.getRelative(relativeFace);
            if (!(relative.getState() instanceof Chest)){
                new Message(I18n.format("command.chest.remove.not_chest")).send(sender);
                return;
            }
            dbModel = instance.getModelAt(relative.getLocation());
            if (dbModel == null){
                new Message(I18n.format("command.chest.remove.not_chest")).send(sender);
                return;
            }
        }
        if (dbModel.getOwner().equals(player.getUniqueId()) || player.isOp()){
            instance.removeLocationModel(dbModel);
            new Message(I18n.format("command.chest.remove.success")).send(sender);
            return;
        }
        new Message(I18n.format("command.chest.remove.not_permitted")).send(sender);
    }

    public boolean isExisted(Block block){
        LocationConnection instance = LocationConnection.getInstance();
        BlockFace relativeFace = Utils.getRelativeChestFace(block);
        LocationDbModel modelAt = instance.getModelAt(block.getLocation());
        if (modelAt!=null){
            return true;
        }
        if (relativeFace != null){
            Block relative = block.getRelative(relativeFace);
            LocationDbModel modelAt1 = instance.getModelAt(relative.getLocation());
            if (modelAt1 != null){
                return true;
            }
        }
        return false;
    }

    private static Boolean locketteProEnabled = null;

    private static boolean isLocketteProEnabled(){
        if (locketteProEnabled == null) {
            try {
                LockettePro plugin = HamsterEcoHelper.getPlugin(LockettePro.class);
                locketteProEnabled = plugin.isEnabled();
            } catch (Throwable e) {
                locketteProEnabled = false;
            }
        }
        return locketteProEnabled;
    }

    public boolean isLocked(Block block){
        return isLocketteProEnabled() && LocketteProAPI.isLocked(block);
    }

    public boolean isOwner(Block block, Player player){
        return !isLocketteProEnabled() || LocketteProAPI.isOwner(block, player);
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
        return "hchest";
    }
}
