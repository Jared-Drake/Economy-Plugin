package com.economyplugin.managers;

import com.economyplugin.EconomyPlugin;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.advancement.Advancement;
import org.bukkit.advancement.AdvancementProgress;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;

public class AchievementManager {
    
    private final EconomyPlugin plugin;
    private final Map<String, Double> achievementRewards;
    private final Map<String, Boolean> playerAchievements; // player UUID -> achievement name -> completed
    
    public AchievementManager(EconomyPlugin plugin) {
        this.plugin = plugin;
        this.achievementRewards = new HashMap<>();
        this.playerAchievements = new HashMap<>();
        
        loadAchievementRewards();
    }
    
    private void loadAchievementRewards() {
        // Load from config
        ConfigurationSection rewardsSection = plugin.getConfig().getConfigurationSection("achievement-rewards");
        if (rewardsSection != null) {
            for (String achievement : rewardsSection.getKeys(false)) {
                double reward = rewardsSection.getDouble(achievement, 0.0);
                achievementRewards.put(achievement, reward);
            }
        } else {
            // Default achievement rewards
            createDefaultAchievementRewards();
        }
    }
    
    private void createDefaultAchievementRewards() {
        // Story mode achievements
        achievementRewards.put("minecraft:story/root", 100.0);
        achievementRewards.put("minecraft:story/mine_stone", 25.0);
        achievementRewards.put("minecraft:story/upgrade_tools", 50.0);
        achievementRewards.put("minecraft:story/smelt_iron", 75.0);
        achievementRewards.put("minecraft:story/obtain_armor", 100.0);
        achievementRewards.put("minecraft:story/defeat_zombie", 150.0);
        achievementRewards.put("minecraft:story/defeat_skeleton", 150.0);
        achievementRewards.put("minecraft:story/defeat_spider", 150.0);
        achievementRewards.put("minecraft:story/defeat_enderdragon", 1000.0);
        
        // Nether achievements
        achievementRewards.put("minecraft:nether/root", 200.0);
        achievementRewards.put("minecraft:nether/return_to_sender", 300.0);
        achievementRewards.put("minecraft:nether/find_fortress", 250.0);
        achievementRewards.put("minecraft:nether/uneasy_alliance", 500.0);
        
        // End achievements
        achievementRewards.put("minecraft:end/root", 300.0);
        achievementRewards.put("minecraft:end/kill_dragon", 1000.0);
        achievementRewards.put("minecraft:end/dragon_egg", 750.0);
        
        // Adventure achievements
        achievementRewards.put("minecraft:adventure/root", 150.0);
        achievementRewards.put("minecraft:adventure/kill_a_mob", 100.0);
        achievementRewards.put("minecraft:adventure/trade", 200.0);
        achievementRewards.put("minecraft:adventure/sleep_in_bed", 50.0);
        
        // Husbandry achievements
        achievementRewards.put("minecraft:husbandry/root", 100.0);
        achievementRewards.put("minecraft:husbandry/breed_an_animal", 150.0);
        achievementRewards.put("minecraft:husbandry/tame_an_animal", 200.0);
        achievementRewards.put("minecraft:husbandry/fishy_business", 175.0);
    }
    
    public void checkAchievements(Player player) {
        Iterator<Advancement> iterator = plugin.getServer().advancementIterator();
        while (iterator.hasNext()) {
            Advancement advancement = iterator.next();
            String advancementName = advancement.getKey().getKey();
            
            if (achievementRewards.containsKey(advancementName)) {
                AdvancementProgress progress = player.getAdvancementProgress(advancement);
                
                if (progress.isDone() && !hasPlayerCompletedAchievement(player, advancementName)) {
                    // Player just completed this achievement
                    giveAchievementReward(player, advancementName);
                    markAchievementCompleted(player, advancementName);
                }
            }
        }
    }
    
    private boolean hasPlayerCompletedAchievement(Player player, String achievementName) {
        String key = player.getUniqueId().toString() + ":" + achievementName;
        return playerAchievements.getOrDefault(key, false);
    }
    
    private void markAchievementCompleted(Player player, String achievementName) {
        String key = player.getUniqueId().toString() + ":" + achievementName;
        playerAchievements.put(key, true);
    }
    
    private void giveAchievementReward(Player player, String achievementName) {
        Double reward = achievementRewards.get(achievementName);
        if (reward != null && reward > 0) {
            Economy economy = plugin.getEconomy();
            economy.depositPlayer(player, reward);
            
            // Notify player
            player.sendMessage("§a§lAchievement Unlocked! §f" + achievementName);
            player.sendMessage("§6Reward: §e$" + reward);
            
            // Log the reward
            plugin.getLogger().info("Player " + player.getName() + " earned $" + reward + " for achievement: " + achievementName);
        }
    }
    
    public double getAchievementReward(String achievementName) {
        return achievementRewards.getOrDefault(achievementName, 0.0);
    }
    
    public Map<String, Double> getAllAchievementRewards() {
        return new HashMap<>(achievementRewards);
    }
    
    public void saveAchievementData() {
        // Save player achievement completion status
        // This would typically save to a database or file
    }
    
    public void loadAchievementData() {
        // Load player achievement completion status
        // This would typically load from a database or file
    }
    
    public Map<String, Boolean> getPlayerAchievementsMap() {
        return new HashMap<>(playerAchievements);
    }
    
    public void setPlayerAchievement(String key, boolean completed) {
        playerAchievements.put(key, completed);
    }
}
