package com.frazbi.realTime;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class RealTimeCommand implements CommandExecutor, TabCompleter {
    
    private final RealTime plugin;
    
    public RealTimeCommand(RealTime plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }
        
        switch (args[0].toLowerCase()) {
            case "enable":
                if (!sender.hasPermission("realtime.admin")) {
                    sender.sendMessage(ChatColor.RED + "У вас нет прав для выполнения этой команды!");
                    return true;
                }
                plugin.setSyncEnabled(true);
                sender.sendMessage(ChatColor.GREEN + "RealTime включён!");
                return true;
                
            case "disable":
                if (!sender.hasPermission("realtime.admin")) {
                    sender.sendMessage(ChatColor.RED + "У вас нет прав для выполнения этой команды!");
                    return true;
                }
                plugin.setSyncEnabled(false);
                sender.sendMessage(ChatColor.YELLOW + "RealTime выключен!");
                return true;
                
            case "reload":
                if (!sender.hasPermission("realtime.admin")) {
                    sender.sendMessage(ChatColor.RED + "У вас нет прав для выполнения этой команды!");
                    return true;
                }
                plugin.loadConfig();
                if (plugin.isSyncEnabled()) {
                    plugin.startTimeSync();
                }
                sender.sendMessage(ChatColor.GREEN + "Конфигурация перезагружена!");
                return true;
                
            case "status":
                if (!sender.hasPermission("realtime.status")) {
                    sender.sendMessage(ChatColor.RED + "У вас нет прав для выполнения этой команды!");
                    return true;
                }
                LocalTime realTime = LocalTime.now();
                sender.sendMessage(ChatColor.GOLD + "=== RealTime Статус ===");
                sender.sendMessage(ChatColor.YELLOW + "Состояние: " + 
                    (plugin.isSyncEnabled() ? ChatColor.GREEN + "Включён" : ChatColor.RED + "Выключен"));
                sender.sendMessage(ChatColor.YELLOW + "Реальное время: " + ChatColor.WHITE + 
                    String.format("%02d:%02d:%02d", realTime.getHour(), realTime.getMinute(), realTime.getSecond()));
                return true;
                
            default:
                sendHelp(sender);
                return true;
        }
    }
    
    private void sendHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "=== RealTime Команды ===");
        sender.sendMessage(ChatColor.YELLOW + "/realtime enable" + ChatColor.WHITE + " - Включить синхронизацию");
        sender.sendMessage(ChatColor.YELLOW + "/realtime disable" + ChatColor.WHITE + " - Выключить синхронизацию");
        sender.sendMessage(ChatColor.YELLOW + "/realtime reload" + ChatColor.WHITE + " - Перезагрузить конфиг");
        sender.sendMessage(ChatColor.YELLOW + "/realtime status" + ChatColor.WHITE + " - Показать статус");
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            completions.add("enable");
            completions.add("disable");
            completions.add("reload");
            completions.add("status");
            
            // Фильтруем по введённому тексту
            String input = args[0].toLowerCase();
            completions.removeIf(s -> !s.toLowerCase().startsWith(input));
        }
        
        return completions;
    }
}
