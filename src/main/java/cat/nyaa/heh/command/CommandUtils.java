package cat.nyaa.heh.command;

import cat.nyaa.nyaacore.cmdreceiver.Arguments;
import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;

import java.util.List;
import java.util.stream.Collectors;

public class CommandUtils {

    public static List<String> getOnlinePlayers() {
        return Bukkit.getOnlinePlayers().stream().map(HumanEntity::getName).collect(Collectors.toList());
    }

    @SuppressWarnings("deprecation")
    public static List<String> filtered(Arguments arguments, List<String> completeStr) {
        String next = arguments.at(arguments.length() - 1);
        return completeStr.stream().filter(s -> s.startsWith(next)).collect(Collectors.toList());
    }
}
