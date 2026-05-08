package org.system.nobanedItem;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BiTabCompleter implements TabCompleter {

    private static final List<String> SUBCOMMANDS = Arrays.asList(
            "itemadd", "itemremove", "itemlist",
            "adminadd", "adminremove", "adminlist",
            "reload"
    );

    private final NoBanedItem plugin;

    public BiTabCompleter(NoBanedItem plugin) {
        this.plugin = plugin;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("nobaneditem.admin")) {
            return List.of();
        }

        if (args.length == 1) {
            return filterByPrefix(args[0], SUBCOMMANDS);
        }

        if (args.length == 2) {
            return switch (args[0].toLowerCase()) {
                case "itemremove" -> filterByPrefix(args[1], plugin.getBannedItemsCache());
                case "adminremove" -> filterByPrefix(args[1], plugin.getAdminsCache());
                default -> List.of();
            };
        }

        return List.of();
    }

    private List<String> filterByPrefix(String prefix, Iterable<String> candidates) {
        List<String> result = new ArrayList<>();
        String lowerPrefix = prefix.toLowerCase();
        for (String candidate : candidates) {
            if (candidate.toLowerCase().startsWith(lowerPrefix)) {
                result.add(candidate);
            }
        }
        return result;
    }
}
