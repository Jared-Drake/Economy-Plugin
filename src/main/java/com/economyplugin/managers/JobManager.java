package com.economyplugin.managers;

import com.economyplugin.EconomyPlugin;
import com.economyplugin.jobs.Job;
import com.economyplugin.jobs.JobType;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.configuration.ConfigurationSection;

import java.util.*;

public class JobManager {
    
    private final EconomyPlugin plugin;
    private final Map<String, Job> jobs;
    private final Map<UUID, String> playerJobs; // player UUID -> job name
    
    public JobManager(EconomyPlugin plugin) {
        this.plugin = plugin;
        this.jobs = new HashMap<>();
        this.playerJobs = new HashMap<>();
        
        loadJobs();
        loadPlayerJobs();
    }
    
    private void loadJobs() {
        // Create default jobs
        createDefaultJobs();
        
        // Load from config if available
        ConfigurationSection jobsSection = plugin.getConfig().getConfigurationSection("jobs");
        if (jobsSection != null) {
            for (String jobName : jobsSection.getKeys(false)) {
                ConfigurationSection jobSection = jobsSection.getConfigurationSection(jobName);
                if (jobSection != null) {
                    loadJobFromConfig(jobName, jobSection);
                }
            }
        }
    }
    
    private void createDefaultJobs() {
        // Mining job - Count blocks broken with pickaxe
        Map<Material, Integer> miningReqs = new HashMap<>();
        // Empty requirements - will be handled by block breaking events
        jobs.put("miner", new Job("Miner", "Break 200 blocks with a pickaxe to earn money", JobType.MINING, miningReqs, 250.0, 300));
        
        // Farming job
        Map<Material, Integer> farmingReqs = new HashMap<>();
        farmingReqs.put(Material.WHEAT, 16);
        farmingReqs.put(Material.CARROT, 16);
        farmingReqs.put(Material.POTATO, 16);
        jobs.put("farmer", new Job("Farmer", "Grow crops to earn money", JobType.FARMING, farmingReqs, 250.0, 180));
        
        // Hunting job
        Map<Material, Integer> huntingReqs = new HashMap<>();
        huntingReqs.put(Material.BEEF, 8);
        huntingReqs.put(Material.PORKCHOP, 8);
        huntingReqs.put(Material.CHICKEN, 8);
        jobs.put("hunter", new Job("Hunter", "Hunt animals to earn money", JobType.HUNTING, huntingReqs, 250.0, 240));
        
        // Fishing job
        Map<Material, Integer> fishingReqs = new HashMap<>();
        fishingReqs.put(Material.COD, 5);
        fishingReqs.put(Material.SALMON, 5);
        fishingReqs.put(Material.TROPICAL_FISH, 2);
        jobs.put("fisher", new Job("Fisher", "Catch fish to earn money", JobType.FISHING, fishingReqs, 250.0, 200));
    }
    
    private void loadJobFromConfig(String jobName, ConfigurationSection jobSection) {
        String description = jobSection.getString("description", "No description");
        String typeStr = jobSection.getString("type", "MINING");
        JobType type = JobType.valueOf(typeStr.toUpperCase());
        double reward = jobSection.getDouble("reward", 50.0);
        int cooldown = jobSection.getInt("cooldown", 300);
        
        Map<Material, Integer> requirements = new HashMap<>();
        ConfigurationSection reqsSection = jobSection.getConfigurationSection("requirements");
        if (reqsSection != null) {
            for (String materialStr : reqsSection.getKeys(false)) {
                Material material = Material.valueOf(materialStr.toUpperCase());
                int amount = reqsSection.getInt(materialStr);
                requirements.put(material, amount);
            }
        }
        
        jobs.put(jobName, new Job(jobName, description, type, requirements, reward, cooldown));
    }
    
    private void loadPlayerJobs() {
        // Load from data manager
        // This would typically load from a database or file
    }
    
    public boolean assignJob(Player player, String jobName) {
        if (!jobs.containsKey(jobName)) {
            return false;
        }
        
        // Check if player already has a job
        if (playerJobs.containsKey(player.getUniqueId())) {
            return false;
        }
        
        playerJobs.put(player.getUniqueId(), jobName);
        savePlayerJobs();
        return true;
    }
    
    public boolean quitJob(Player player) {
        if (!playerJobs.containsKey(player.getUniqueId())) {
            return false;
        }
        
        String jobName = playerJobs.get(player.getUniqueId());
        Job job = jobs.get(jobName);
        
        // Reset player's job data when they quit
        if (job != null) {
            String playerUUID = player.getUniqueId().toString();
            job.resetPlayerData(playerUUID);
        }
        
        playerJobs.remove(player.getUniqueId());
        savePlayerJobs();
        return true;
    }
    
    public String getPlayerJob(Player player) {
        return playerJobs.get(player.getUniqueId());
    }
    
    public boolean completeJob(Player player) {
        String jobName = getPlayerJob(player);
        if (jobName == null) {
            plugin.getLogger().warning("Player " + player.getName() + " tried to complete job but has no job assigned");
            return false;
        }
        
        Job job = jobs.get(jobName);
        if (job == null) {
            plugin.getLogger().severe("Player " + player.getName() + " has job '" + jobName + "' but job not found in jobs list");
            return false;
        }
        
        if (!job.canComplete(player)) {
            plugin.getLogger().info("Player " + player.getName() + " tried to complete job '" + jobName + "' but it's on cooldown");
            return false;
        }
        
        if (!job.hasCompletedBlockRequirement(player)) {
            plugin.getLogger().info("Player " + player.getName() + " tried to complete job '" + jobName + "' but requirements not met");
            return false;
        }
        
        // Complete the job
        job.complete(player);
        
        // Give reward
        Economy economy = plugin.getEconomy();
        economy.depositPlayer(player, job.getReward());
        
        // Log successful completion
        plugin.getLogger().info("Player " + player.getName() + " completed job '" + jobName + "' and received $" + job.getReward());
        
        // Save data
        savePlayerJobs();
        saveJobData();
        
        return true;
    }
    
    public List<Job> getAvailableJobs() {
        return new ArrayList<>(jobs.values());
    }
    
    public Job getJob(String jobName) {
        return jobs.get(jobName);
    }
    
    public boolean hasJob(Player player) {
        return playerJobs.containsKey(player.getUniqueId());
    }
    
    private void savePlayerJobs() {
        // Save to data manager
        // This would typically save to a database or file
    }
    
    private void saveJobData() {
        // Save job cooldowns and other data
        // This would typically save to a database or file
    }
    
    public void saveAllData() {
        savePlayerJobs();
        saveJobData();
    }
    
    public Map<UUID, String> getPlayerJobsMap() {
        return new HashMap<>(playerJobs);
    }
    
    public void setPlayerJob(UUID playerUUID, String jobName) {
        playerJobs.put(playerUUID, jobName);
    }
}
