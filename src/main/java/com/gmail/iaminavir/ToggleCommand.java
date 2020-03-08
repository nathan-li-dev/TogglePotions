package com.gmail.iaminavir;


import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class ToggleCommand implements CommandExecutor {

    private final TogglePotions plugin;

    public ToggleCommand(TogglePotions p){
        plugin = p;
    }

    private void sendConfigMessage(CommandSender recipient, String path){
        String message = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString(path));
        if(recipient instanceof Player) {
            if (plugin.getConfig().getBoolean("messages.actionBar"))
                ((Player)recipient).spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(message));
            else
                recipient.sendMessage(message);
        }
        else
            recipient.sendMessage(message);
    }

    private void togglePotion(Player player, PotionEffectType type, int level) {

        // Ex. togglepotions.fire_resistance.4 or togglepotions.fire_resistance.*
        // lowercase from spigot api

        if (player.hasPermission(String.format("togglepotions.%s.%d", type.getName().toLowerCase(), level)) || player.hasPermission(String.format("togglepotions.%s.*", type.getName().toLowerCase())) || player.hasPermission("togglepotions.all.*") || player.hasPermission("togglepotions.all." + String.valueOf(level))) {
            if (player.hasPotionEffect(type)) {
                if (player.getPotionEffect(type).getAmplifier() >= level - 1) {

                    sendConfigMessage(player, "messages.toggledOff");
                    player.removePotionEffect(type);
                    if(plugin.getConfig().getBoolean("sounds.enable"))
                        player.playSound(player.getLocation(), Sound.valueOf(plugin.getConfig().getString("sounds.toggleOff.sound")), (float)plugin.getConfig().getDouble("sounds.toggleOff.volume"), (float)plugin.getConfig().getDouble("sounds.toggleOff.pitch"));

                    return;
                }
            }

            sendConfigMessage(player, "messages.toggledOn");
            if(plugin.getConfig().getBoolean("sounds.enable"))
                player.playSound(player.getLocation(), Sound.valueOf(plugin.getConfig().getString("sounds.toggleOn.sound")), (float)plugin.getConfig().getDouble("sounds.toggleOn.volume"), (float)plugin.getConfig().getDouble("sounds.toggleOn.pitch"));

            player.addPotionEffect(new PotionEffect(type, Integer.MAX_VALUE, level - 1), true);
        }

        else{
            sendConfigMessage(player, "messages.noPermission");
        }
    }

    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("reload")) {
                if (commandSender.hasPermission("togglepotion.reload")) {
                    plugin.reloadConfig();
                    plugin.registerAssociations();
                    sendConfigMessage(commandSender, "messages.reloaded");
                }
                else{
                    sendConfigMessage(commandSender, "messages.noPermission");
                }
                return true;
            }
        }

        if(commandSender instanceof Player) {
            if (commandSender.hasPermission("togglepotion.use")) {
                PotionEffectType type = null;
                int level = 0;

                if (args.length == 0) {

                    sendConfigMessage(commandSender, "messages.usage");
                    return true;
                }
                if (args.length >= 1) {
                    type = plugin.getPotionAssociations().get(args[0].toUpperCase());
                }
                if (args.length >= 2) {
                    try {
                        level = Integer.valueOf(args[1]);
                    } catch (NumberFormatException e) {
                        sendConfigMessage(commandSender, "messages.invalidNumberInput");
                    }
                }

                try {
                    togglePotion((Player) commandSender, type, level);
                } catch (NullPointerException e) {
                    sendConfigMessage(commandSender, "messages.invalidPotionInput");
                }
            }
            else{
                sendConfigMessage(commandSender, "messages.noPermission");
            }
        }

        return true;
    }
}
