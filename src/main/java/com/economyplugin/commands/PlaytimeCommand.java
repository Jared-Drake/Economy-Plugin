package com.economyplugin.commands;

import com.economyplugin.EconomyPlugin;
import com.economyplugin.managers.PlaytimeManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PlaytimeCommand implements CommandExecutor {
    
    private final EconomyPlugin plugin;
    
    public PlaytimeCommand(EconomyPlugin plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cThis command can only be used by players!");
            return true;
        }
        
        Player player = (Player) sender;
        PlaytimeManager playtimeManager = plugin.getPlaytimeManager();
        
        showPlaytimeInfo(player, playtimeManager);
        
        return true;
    }
    
    private void showPlaytimeInfo(Player player, PlaytimeManager playtimeManager) {
        long totalPlaytime = playtimeManager.getPlayerPlaytime(player);
        int currentTier = playtimeManager.getPlayerTier(player);
        long timeUntilNextReward = playtimeManager.getTimeUntilNextReward(player);
        
        player.sendMessage("§6=== Playtime Information ===");
        player.sendMessage("§7Total Playtime: §f" + formatPlaytime(totalPlaytime));
        player.sendMessage("§7Current Tier: §f" + currentTier);
        
        if (timeUntilNextReward > 0) {
            player.sendMessage("§7Next Reward In: §f" + formatTime(timeUntilNextReward));
        } else {
            player.sendMessage("§a§l✓ Ready for reward!");
        }
        
        // Show tier information
        showTierInfo(player, currentTier);
        
        // Show next tier progress
        showNextTierProgress(player, totalPlaytime, currentTier);
    }
    
    private void showTierInfo(Player player, int currentTier) {
        player.sendMessage("§7Tier Information:");
        player.sendMessage("§7- Tier 1: §e$10.00 §7per interval");
        
        for (int tier = 2; tier <= Math.min(currentTier + 2, 10); tier++) {
            double reward = 10.0 * Math.pow(1.5, tier - 1);
            String status = tier <= currentTier ? "§aUnlocked" : "§7Locked";
            player.sendMessage(String.format("§7- Tier %d: §e$%.2f §7per interval (%s)", 
                tier, reward, status));
        }
        
        if (currentTier < 10) {
            player.sendMessage("§7- Next tier unlocks in: §f" + formatTime(getTimeToNextTier(currentTier)));
        }
    }
    
    private void showNextTierProgress(Player player, long totalPlaytime, int currentTier) {
        if (currentTier >= 10) {
            player.sendMessage("§a§lCongratulations! §fYou've reached the maximum tier!");
            return;
        }
        
        long timeForCurrentTier = (currentTier - 1) * 7200L; // 2 hours per tier
        long timeForNextTier = currentTier * 7200L;
        long progress = totalPlaytime - timeForCurrentTier;
        long required = timeForNextTier - timeForCurrentTier;
        
        if (progress > 0) {
            double percentage = (double) progress / required * 100;
            player.sendMessage("§7Progress to Tier " + (currentTier + 1) + ": §f" + 
                String.format("%.1f", percentage) + "%");
            player.sendMessage("§7Time remaining: §f" + formatTime(required - progress));
        }
    }
    
    private long getTimeToNextTier(int currentTier) {
        if (currentTier >= 10) return 0;
        return 7200L; // 2 hours
    }
    
    private String formatPlaytime(long seconds) {
        if (seconds < 60) {
            return seconds + " seconds";
        } else if (seconds < 3600) {
            long minutes = seconds / 60;
            long remainingSeconds = seconds % 60;
            return minutes + "m " + remainingSeconds + "s";
        } else if (seconds < 86400) {
            long hours = seconds / 3600;
            long minutes = (seconds % 3600) / 60;
            return hours + "h " + minutes + "m";
        } else {
            long days = seconds / 86400;
            long hours = (seconds % 86400) / 3600;
            return days + "d " + hours + "h";
        }
    }
    
    private String formatTime(long seconds) {
        if (seconds < 60) {
            return seconds + "s";
        } else if (seconds < 3600) {
            return (seconds / 60) + "m " + (seconds % 60) + "s";
        } else {
            long hours = seconds / 3600;
            long minutes = (seconds % 3600) / 60;
            return hours + "h " + minutes + "m";
        }
    }
}




