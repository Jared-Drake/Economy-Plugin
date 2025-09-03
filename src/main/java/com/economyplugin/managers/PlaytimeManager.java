package com.economyplugin.managers;

import com.economyplugin.EconomyPlugin;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlaytimeManager {
    
    private final EconomyPlugin plugin;
    private final Map<UUID, Long> playerJoinTimes; // player UUID -> join timestamp
    private final Map<UUID, Long> totalPlaytime; // player UUID -> total playtime in seconds
    private final Map<UUID, Long> lastRewardTime; // player UUID -> last reward timestamp
    private final Map<UUID, Integer> playtimeTier; // player UUID -> current tier
    
    private BukkitTask playtimeTask;
    private final int rewardInterval; // seconds between rewards
    private final double baseReward; // base reward per interval
    private final double tierMultiplier; // multiplier per tier
    private final double maxReward; // maximum reward per interval
    
    public PlaytimeManager(EconomyPlugin plugin) {
        this.plugin = plugin;
        this.playerJoinTimes = new HashMap<>();
        this.totalPlaytime = new HashMap<>();
        this.lastRewardTime = new HashMap<>();
        this.playtimeTier = new HashMap<>();
        
        // Load config values
        this.rewardInterval = plugin.getConfig().getInt("playtime.reward-interval", 300); // 5 minutes default
        this.baseReward = plugin.getConfig().getDouble("playtime.base-reward", 10.0);
        this.tierMultiplier = plugin.getConfig().getDouble("playtime.tier-multiplier", 1.5);
        this.maxReward = plugin.getConfig().getDouble("playtime.max-reward", 25.0);
        
        loadPlaytimeData();
    }
    
    public void startPlaytimeTracking() {
        playtimeTask = new BukkitRunnable() {
            @Override
            public void run() {
                checkPlaytimeRewards();
            }
        }.runTaskTimer(plugin, 20L * rewardInterval, 20L * rewardInterval); // Convert seconds to ticks
    }
    
    public void stopPlaytimeTracking() {
        if (playtimeTask != null) {
            playtimeTask.cancel();
        }
    }
    
    public void playerJoined(Player player) {
        UUID playerUUID = player.getUniqueId();
        playerJoinTimes.put(playerUUID, System.currentTimeMillis());
        
        // Initialize playtime data if new player
        if (!totalPlaytime.containsKey(playerUUID)) {
            totalPlaytime.put(playerUUID, 0L);
            lastRewardTime.put(playerUUID, System.currentTimeMillis());
            playtimeTier.put(playerUUID, 1);
        }
    }
    
    public void playerLeft(Player player) {
        UUID playerUUID = player.getUniqueId();
        Long joinTime = playerJoinTimes.get(playerUUID);
        
        if (joinTime != null) {
            long sessionTime = (System.currentTimeMillis() - joinTime) / 1000; // Convert to seconds
            long currentTotal = totalPlaytime.getOrDefault(playerUUID, 0L);
            totalPlaytime.put(playerUUID, currentTotal + sessionTime);
            
            // Remove join time
            playerJoinTimes.remove(playerUUID);
            
            // Save data
            savePlaytimeData();
        }
    }
    
    private void checkPlaytimeRewards() {
        long currentTime = System.currentTimeMillis();
        
        for (Player player : Bukkit.getOnlinePlayers()) {
            UUID playerUUID = player.getUniqueId();
            Long lastReward = lastRewardTime.get(playerUUID);
            
            if (lastReward != null) {
                long timeSinceLastReward = (currentTime - lastReward) / 1000;
                
                if (timeSinceLastReward >= rewardInterval) {
                    givePlaytimeReward(player);
                    lastRewardTime.put(playerUUID, currentTime);
                    
                    // Update tier based on total playtime
                    updatePlaytimeTier(player);
                }
            }
        }
    }
    
    private void givePlaytimeReward(Player player) {
        UUID playerUUID = player.getUniqueId();
        int tier = playtimeTier.getOrDefault(playerUUID, 1);
        
        // Calculate base reward with tier multiplier
        double calculatedReward = baseReward * Math.pow(tierMultiplier, tier - 1);
        
        // Cap the reward at the configured maximum
        double reward = Math.min(calculatedReward, maxReward);
        
        Economy economy = plugin.getEconomy();
        economy.depositPlayer(player, reward);
        
        // Notify player
        player.sendMessage("§6§lPlaytime Reward! §e$" + String.format("%.2f", reward));
        player.sendMessage("§7Current tier: §f" + tier);
        
        // If reward was capped, show the original calculated amount
        if (calculatedReward > maxReward) {
            player.sendMessage("§7Reward capped at $" + String.format("%.2f", maxReward) + " (would have been $" + String.format("%.2f", calculatedReward) + ")");
        }
        
        // Log the reward
        plugin.getLogger().info("Player " + player.getName() + " earned $" + String.format("%.2f", reward) + " for playtime (Tier " + tier + ")");
    }
    
    private void updatePlaytimeTier(Player player) {
        UUID playerUUID = player.getUniqueId();
        long totalTime = totalPlaytime.getOrDefault(playerUUID, 0L);
        int currentTier = playtimeTier.getOrDefault(playerUUID, 1);
        
        // Calculate new tier based on total playtime
        int newTier = calculateTier(totalTime);
        
        if (newTier > currentTier) {
            playtimeTier.put(playerUUID, newTier);
            player.sendMessage("§a§lNew Playtime Tier! §fYou are now Tier " + newTier);
            player.sendMessage("§7You will now earn more money per interval!");
        }
    }
    
    private int calculateTier(long totalPlaytimeSeconds) {
        // Tier calculation: every 2 hours (7200 seconds) = new tier
        return Math.min((int) (totalPlaytimeSeconds / 7200) + 1, 10); // Cap at tier 10
    }
    
    public long getPlayerPlaytime(Player player) {
        UUID playerUUID = player.getUniqueId();
        long totalTime = totalPlaytime.getOrDefault(playerUUID, 0L);
        
        // Add current session time if player is online
        Long joinTime = playerJoinTimes.get(playerUUID);
        if (joinTime != null) {
            long sessionTime = (System.currentTimeMillis() - joinTime) / 1000;
            totalTime += sessionTime;
        }
        
        return totalTime;
    }
    
    public int getPlayerTier(Player player) {
        UUID playerUUID = player.getUniqueId();
        return playtimeTier.getOrDefault(playerUUID, 1);
    }
    
    public long getTimeUntilNextReward(Player player) {
        UUID playerUUID = player.getUniqueId();
        Long lastReward = lastRewardTime.get(playerUUID);
        
        if (lastReward == null) {
            return 0;
        }
        
        long timeSinceLastReward = (System.currentTimeMillis() - lastReward) / 1000;
        return Math.max(0, rewardInterval - timeSinceLastReward);
    }
    
    private void loadPlaytimeData() {
        // Load from data manager
        // This would typically load from a database or file
    }
    
    private void savePlaytimeData() {
        // Save to data manager
        // This would typically save to a database or file
    }
    
    public void saveAllData() {
        savePlaytimeData();
    }
    
    public Map<UUID, Long> getTotalPlaytimeMap() {
        return new HashMap<>(totalPlaytime);
    }
    
    public Map<UUID, Long> getLastRewardTimeMap() {
        return new HashMap<>(lastRewardTime);
    }
    
    public Map<UUID, Integer> getPlaytimeTierMap() {
        return new HashMap<>(playtimeTier);
    }
    
    public void setTotalPlaytime(UUID playerUUID, long playtime) {
        totalPlaytime.put(playerUUID, playtime);
    }
    
    public void setLastRewardTime(UUID playerUUID, long lastReward) {
        lastRewardTime.put(playerUUID, lastReward);
    }
    
    public void setPlaytimeTier(UUID playerUUID, int tier) {
        playtimeTier.put(playerUUID, tier);
    }
}
