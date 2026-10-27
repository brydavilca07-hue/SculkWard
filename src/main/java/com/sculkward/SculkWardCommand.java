package com.sculkward;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class SculkWardCommand implements CommandExecutor, TabCompleter {

    private static final String PERMISSION = "sculkward.admin";
    private static final List<String> SUBCOMMANDS = List.of("give", "list", "reload");
    private static final List<String> GROUPS = List.of("set", "tools", "all");

    private final SculkWardPlugin plugin;
    private final ItemManager items;

    public SculkWardCommand(SculkWardPlugin plugin, ItemManager items) {
        this.plugin = plugin;
        this.items = items;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission(PERMISSION)) {
            sender.sendMessage(plugin.msg("no-permission"));
            return true;
        }
        if (args.length == 0) {
            sendHelp(sender, label);
            return true;
        }

        switch (args[0].toLowerCase(Locale.ROOT)) {
            case "reload" -> {
                plugin.reloadAll();
                sender.sendMessage(plugin.msg("reloaded"));
            }
            case "list" -> {
                sender.sendMessage(Component.text("Piezas Sculk Ward:", NamedTextColor.AQUA));
                for (SculkItem item : SculkItem.values()) {
                    sender.sendMessage(Component.text(" - " + item.getId() + "  ", NamedTextColor.GRAY)
                            .append(Component.text(item.getDisplayName(), NamedTextColor.DARK_AQUA)));
                }
                sender.sendMessage(Component.text(" Grupos: set, tools, all", NamedTextColor.GRAY));
            }
            case "give" -> give(sender, args);
            default -> sendHelp(sender, label);
        }
        return true;
    }

    private void sendHelp(CommandSender sender, String label) {
        sender.sendMessage(Component.text("/" + label + " give <jugador> <ítem|set|tools|all> [cantidad]", NamedTextColor.AQUA));
        sender.sendMessage(Component.text("/" + label + " list", NamedTextColor.AQUA));
        sender.sendMessage(Component.text("/" + label + " reload", NamedTextColor.AQUA));
    }

    private void give(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(plugin.msg("usage-give"));
            return;
        }

        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            sender.sendMessage(plugin.msg("player-not-found", "player", args[1]));
            return;
        }

        String what = args[2].toLowerCase(Locale.ROOT);
        List<SculkItem> toGive = new ArrayList<>();
        String label;

        switch (what) {
            case "set" -> {
                for (SculkItem item : SculkItem.values()) {
                    if (item.isArmor()) {
                        toGive.add(item);
                    }
                }
                label = "el set de armadura Sculk Ward";
            }
            case "tools" -> {
                for (SculkItem item : SculkItem.values()) {
                    if (!item.isArmor()) {
                        toGive.add(item);
                    }
                }
                label = "las herramientas Sculk Ward";
            }
            case "all" -> {
                toGive.addAll(List.of(SculkItem.values()));
                label = "todo el equipo Sculk Ward";
            }
            default -> {
                SculkItem single = SculkItem.fromId(what);
                if (single == null) {
                    sender.sendMessage(plugin.msg("unknown-item"));
                    return;
                }
                toGive.add(single);
                label = single.getDisplayName();
            }
        }

        int amount = 1;
        if (args.length >= 4) {
            try {
                amount = Math.max(1, Math.min(16, Integer.parseInt(args[3])));
            } catch (NumberFormatException ignored) {
                amount = 1;
            }
        }

        for (int i = 0; i < amount; i++) {
            for (SculkItem item : toGive) {
                giveOne(target, items.create(item));
            }
        }

        sender.sendMessage(plugin.msg("given", "item", label, "player", target.getName()));
        if (!sender.equals(target)) {
            target.sendMessage(plugin.msg("received", "item", label));
        }
    }

    private void giveOne(Player target, ItemStack stack) {
        Map<Integer, ItemStack> leftovers = target.getInventory().addItem(stack);
        for (ItemStack left : leftovers.values()) {
            target.getWorld().dropItemNaturally(target.getLocation(), left);
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission(PERMISSION)) {
            return List.of();
        }

        List<String> options = new ArrayList<>();
        if (args.length == 1) {
            options.addAll(SUBCOMMANDS);
        } else if (args.length == 2 && args[0].equalsIgnoreCase("give")) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                options.add(player.getName());
            }
        } else if (args.length == 3 && args[0].equalsIgnoreCase("give")) {
            for (SculkItem item : SculkItem.values()) {
                options.add(item.getId());
            }
            options.addAll(GROUPS);
        } else if (args.length == 4 && args[0].equalsIgnoreCase("give")) {
            options.addAll(List.of("1", "2", "4", "8", "16"));
        }

        String typed = args[args.length - 1].toLowerCase(Locale.ROOT);
        List<String> result = new ArrayList<>();
        for (String option : options) {
            if (option.toLowerCase(Locale.ROOT).startsWith(typed)) {
                result.add(option);
            }
        }
        return result;
    }
}
