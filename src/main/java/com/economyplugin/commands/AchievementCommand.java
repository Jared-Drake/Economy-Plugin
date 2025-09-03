package com.economyplugin.commands;

import com.economyplugin.EconomyPlugin;
import com.economyplugin.managers.AchievementManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;

public class AchievementCommand implements CommandExecutor {
    
    private final EconomyPlugin plugin;
    
    public AchievementCommand(EconomyPlugin plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cThis command can only be used by players!");
            return true;
        }
        
        Player player = (Player) sender;
        AchievementManager achievementManager = plugin.getAchievementManager();
        
        if (args.length == 0) {
            showAchievementRewards(player, achievementManager);
        } else if (args[0].equalsIgnoreCase("list")) {
            showAllAchievements(player, achievementManager);
        } else {
            showSpecificAchievement(player, achievementManager, args[0]);
        }
        
        return true;
    }
    
    private void showAchievementRewards(Player player, AchievementManager achievementManager) {
        player.sendMessage("§6=== Achievement Rewards ===");
        player.sendMessage("§7Complete Minecraft achievements to earn money!");
        player.sendMessage("§7Use §f/achievement list §7to see all available rewards.");
        player.sendMessage("§7Use §f/achievement <name> §7for specific achievement info.");
        
        // Show some example rewards
        Map<String, Double> rewards = achievementManager.getAllAchievementRewards();
        int count = 0;
        for (Map.Entry<String, Double> entry : rewards.entrySet()) {
            if (count >= 5) break; // Show only first 5
            
            String achievementName = formatAchievementName(entry.getKey());
            double reward = entry.getValue();
            
            player.sendMessage(String.format("§7%s: §e$%.2f", achievementName, reward));
            count++;
        }
        
        if (rewards.size() > 5) {
            player.sendMessage("§7... and " + (rewards.size() - 5) + " more achievements!");
        }
    }
    
    private void showAllAchievements(Player player, AchievementManager achievementManager) {
        Map<String, Double> rewards = achievementManager.getAllAchievementRewards();
        
        player.sendMessage("§6=== All Achievement Rewards ===");
        
        // Group by category
        showCategoryRewards(player, "Story Mode", rewards, "minecraft:story/");
        showCategoryRewards(player, "Nether", rewards, "minecraft:nether/");
        showCategoryRewards(player, "The End", rewards, "minecraft:end/");
        showCategoryRewards(player, "Adventure", rewards, "minecraft:adventure/");
        showCategoryRewards(player, "Husbandry", rewards, "minecraft:husbandry/");
        
        player.sendMessage("§7Total Achievements: §f" + rewards.size());
        player.sendMessage("§7Total Possible Reward: §e$" + calculateTotalReward(rewards));
    }
    
    private void showCategoryRewards(Player player, String categoryName, Map<String, Double> rewards, String prefix) {
        player.sendMessage("§6" + categoryName + ":");
        
        int count = 0;
        for (Map.Entry<String, Double> entry : rewards.entrySet()) {
            if (entry.getKey().startsWith(prefix)) {
                String achievementName = formatAchievementName(entry.getKey());
                double reward = entry.getValue();
                
                player.sendMessage(String.format("§7- %s: §e$%.2f", achievementName, reward));
                count++;
            }
        }
        
        if (count == 0) {
            player.sendMessage("§7- No achievements in this category");
        }
    }
    
    private void showSpecificAchievement(Player player, AchievementManager achievementManager, String achievementName) {
        // Try to find the achievement by partial name
        Map<String, Double> rewards = achievementManager.getAllAchievementRewards();
        String foundAchievement = null;
        
        for (String key : rewards.keySet()) {
            if (key.toLowerCase().contains(achievementName.toLowerCase()) || 
                formatAchievementName(key).toLowerCase().contains(achievementName.toLowerCase())) {
                foundAchievement = key;
                break;
            }
        }
        
        if (foundAchievement == null) {
            player.sendMessage("§cAchievement not found: " + achievementName);
            player.sendMessage("§7Use §f/achievement list §7to see all available achievements.");
            return;
        }
        
        double reward = rewards.get(foundAchievement);
        String formattedName = formatAchievementName(foundAchievement);
        
        player.sendMessage("§6=== Achievement Information ===");
        player.sendMessage("§7Name: §f" + formattedName);
        player.sendMessage("§7Internal ID: §7" + foundAchievement);
        player.sendMessage("§7Reward: §e$" + reward);
        
        // Show category
        if (foundAchievement.startsWith("minecraft:story/")) {
            player.sendMessage("§7Category: §fStory Mode");
        } else if (foundAchievement.startsWith("minecraft:nether/")) {
            player.sendMessage("§7Category: §fNether");
        } else if (foundAchievement.startsWith("minecraft:end/")) {
            player.sendMessage("§7Category: §fThe End");
        } else if (foundAchievement.startsWith("minecraft:adventure/")) {
            player.sendMessage("§7Category: §fAdventure");
        } else if (foundAchievement.startsWith("minecraft:husbandry/")) {
            player.sendMessage("§7Category: §fHusbandry");
        } else {
            player.sendMessage("§7Category: §fOther");
        }
    }
    
    private String formatAchievementName(String achievementKey) {
        // Remove the minecraft: prefix and convert to readable format
        String name = achievementKey.replace("minecraft:", "");
        name = name.replace("_", " ");
        
        // Capitalize first letter of each word
        String[] words = name.split(" ");
        StringBuilder formatted = new StringBuilder();
        
        for (String word : words) {
            if (word.length() > 0) {
                formatted.append(Character.toUpperCase(word.charAt(0)));
                if (word.length() > 1) {
                    formatted.append(word.substring(1).toLowerCase());
                }
                formatted.append(" ");
            }
        }
        
        return formatted.toString().trim();
    }
    
    private String calculateTotalReward(Map<String, Double> rewards) {
        double total = 0.0;
        for (Double reward : rewards.values()) {
            total += reward;
        }
        return String.format("%.2f", total);
    }
}




