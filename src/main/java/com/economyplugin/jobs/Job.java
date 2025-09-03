package com.economyplugin.jobs;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class Job {
    
    private String name;
    private String description;
    private JobType type;
    private Map<Material, Integer> requirements;
    private double reward;
    private int cooldown; // in seconds
        private Map<String, Long> playerCooldowns; // player UUID -> last completion time
    private Map<String, Integer> playerBlockProgress; // player UUID -> blocks broken for mining jobs

    public Job(String name, String description, JobType type, Map<Material, Integer> requirements, double reward, int cooldown) {
        this.name = name;
        this.description = description;
        this.type = type;
        this.requirements = requirements;
        this.reward = reward;
        this.cooldown = cooldown;
        this.playerCooldowns = new HashMap<>();
        this.playerBlockProgress = new HashMap<>();
    }
    
    public boolean canComplete(Player player) {
        String playerUUID = player.getUniqueId().toString();
        long lastCompletion = playerCooldowns.getOrDefault(playerUUID, 0L);
        long currentTime = System.currentTimeMillis() / 1000;
        
        // If lastCompletion is 0, it means the player has never completed this job before
        // In this case, they should be able to complete it
        if (lastCompletion == 0L) {
            return true;
        }
        
        long timeSinceLastCompletion = currentTime - lastCompletion;
        return timeSinceLastCompletion >= cooldown;
    }
    
    public boolean checkRequirements(Player player) {
        for (Map.Entry<Material, Integer> requirement : requirements.entrySet()) {
            Material material = requirement.getKey();
            int amount = requirement.getValue();
            
            int playerHas = 0;
            for (org.bukkit.inventory.ItemStack item : player.getInventory().getContents()) {
                if (item != null && item.getType() == material) {
                    playerHas += item.getAmount();
                }
            }
            
            if (playerHas < amount) {
                return false;
            }
        }
        return true;
    }
    
    public void complete(Player player) {
        String playerUUID = player.getUniqueId().toString();
        playerCooldowns.put(playerUUID, System.currentTimeMillis() / 1000);
        
        // For mining jobs, don't remove inventory items since they don't have inventory requirements
        if (type != JobType.MINING) {
            // Remove required items for non-mining jobs
            for (Map.Entry<Material, Integer> requirement : requirements.entrySet()) {
                Material material = requirement.getKey();
                int amount = requirement.getValue();
                
                org.bukkit.inventory.ItemStack[] contents = player.getInventory().getContents();
                int remaining = amount;
                
                for (int i = 0; i < contents.length && remaining > 0; i++) {
                    if (contents[i] != null && contents[i].getType() == material) {
                        if (contents[i].getAmount() <= remaining) {
                            remaining -= contents[i].getAmount();
                            contents[i] = null;
                        } else {
                            contents[i].setAmount(contents[i].getAmount() - remaining);
                            remaining = 0;
                        }
                    }
                }
                
                player.getInventory().setContents(contents);
            }
        }
        
        // For mining jobs, reset the block progress after completion
        if (type == JobType.MINING) {
            playerBlockProgress.put(playerUUID, 0);
        }
    }
    
    public long getTimeUntilAvailable(Player player) {
        String playerUUID = player.getUniqueId().toString();
        long lastCompletion = playerCooldowns.getOrDefault(playerUUID, 0L);
        long currentTime = System.currentTimeMillis() / 1000;
        long timePassed = currentTime - lastCompletion;
        
        return Math.max(0, cooldown - timePassed);
    }
    
    // Getters
    public String getName() {
        return name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public JobType getType() {
        return type;
    }
    
    public Map<Material, Integer> getRequirements() {
        return requirements;
    }
    
    public double getReward() {
        return reward;
    }
    
    public int getCooldown() {
        return cooldown;
    }
    
    public Map<String, Long> getPlayerCooldowns() {
        return playerCooldowns;
    }
    
    public void setPlayerCooldowns(Map<String, Long> playerCooldowns) {
        this.playerCooldowns = playerCooldowns;
    }
    
    // Block breaking progress methods
    public void incrementBlockProgress(Player player) {
        String playerUUID = player.getUniqueId().toString();
        int currentProgress = playerBlockProgress.getOrDefault(playerUUID, 0);
        playerBlockProgress.put(playerUUID, currentProgress + 1);
    }
    
    public int getBlockProgress(Player player) {
        String playerUUID = player.getUniqueId().toString();
        return playerBlockProgress.getOrDefault(playerUUID, 0);
    }
    
    public boolean hasCompletedBlockRequirement(Player player) {
        // For mining jobs, check if they've broken enough blocks
        if (type == JobType.MINING) {
            return getBlockProgress(player) >= 200; // 200 blocks required
        }
        // For other jobs, use the old inventory requirement system
        return checkRequirements(player);
    }
    
    public Map<String, Integer> getPlayerBlockProgressMap() {
        return new HashMap<>(playerBlockProgress);
    }
    
    public void setPlayerBlockProgress(Map<String, Integer> blockProgress) {
        this.playerBlockProgress = blockProgress;
    }
    
    public void resetPlayerData(String playerUUID) {
        playerCooldowns.remove(playerUUID);
        playerBlockProgress.remove(playerUUID);
    }
}
