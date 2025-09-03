package com.economyplugin.managers;

import com.economyplugin.EconomyPlugin;
import com.economyplugin.jobs.Job;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DataManager {
    
    private final EconomyPlugin plugin;
    private final File dataFile;
    private FileConfiguration dataConfig;
    
    public DataManager(EconomyPlugin plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "data.yml");
        loadData();
    }
    
    private void loadData() {
        if (!dataFile.exists()) {
            plugin.saveResource("data.yml", false);
        }
        
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);
    }
    
    public void saveAllData() {
        saveJobData();
        saveAchievementData();
        savePlaytimeData();
        saveDataFile();
    }
    
    private void saveJobData() {
        JobManager jobManager = plugin.getJobManager();
        
        // Save player jobs
        ConfigurationSection playerJobsSection = dataConfig.createSection("player-jobs");
        for (Map.Entry<UUID, String> entry : jobManager.getPlayerJobsMap().entrySet()) {
            playerJobsSection.set(entry.getKey().toString(), entry.getValue());
        }
        
        // Save job cooldowns
        ConfigurationSection jobCooldownsSection = dataConfig.createSection("job-cooldowns");
        for (Job job : jobManager.getAvailableJobs()) {
            ConfigurationSection jobSection = jobCooldownsSection.createSection(job.getName());
            for (Map.Entry<String, Long> entry : job.getPlayerCooldowns().entrySet()) {
                jobSection.set(entry.getKey(), entry.getValue());
            }
        }
        
        // Save block progress for mining jobs
        ConfigurationSection blockProgressSection = dataConfig.createSection("job-block-progress");
        for (Job job : jobManager.getAvailableJobs()) {
            if (job.getType() == com.economyplugin.jobs.JobType.MINING) {
                ConfigurationSection jobSection = blockProgressSection.createSection(job.getName());
                Map<String, Integer> playerBlockProgress = job.getPlayerBlockProgressMap();
                for (Map.Entry<String, Integer> entry : playerBlockProgress.entrySet()) {
                    jobSection.set(entry.getKey(), entry.getValue());
                }
            }
        }
    }
    
    private void saveAchievementData() {
        AchievementManager achievementManager = plugin.getAchievementManager();
        
        // Save player achievement completion status
        ConfigurationSection achievementsSection = dataConfig.createSection("player-achievements");
        for (Map.Entry<String, Boolean> entry : achievementManager.getPlayerAchievementsMap().entrySet()) {
            achievementsSection.set(entry.getKey(), entry.getValue());
        }
    }
    
    private void savePlaytimeData() {
        PlaytimeManager playtimeManager = plugin.getPlaytimeManager();
        
        // Save total playtime
        ConfigurationSection playtimeSection = dataConfig.createSection("playtime");
        for (Map.Entry<UUID, Long> entry : playtimeManager.getTotalPlaytimeMap().entrySet()) {
            playtimeSection.set(entry.getKey().toString(), entry.getValue());
        }
        
        // Save last reward times
        ConfigurationSection rewardTimesSection = dataConfig.createSection("last-reward-times");
        for (Map.Entry<UUID, Long> entry : playtimeManager.getLastRewardTimeMap().entrySet()) {
            rewardTimesSection.set(entry.getKey().toString(), entry.getValue());
        }
        
        // Save playtime tiers
        ConfigurationSection tiersSection = dataConfig.createSection("playtime-tiers");
        for (Map.Entry<UUID, Integer> entry : playtimeManager.getPlaytimeTierMap().entrySet()) {
            tiersSection.set(entry.getKey().toString(), entry.getValue());
        }
    }
    
    private void saveDataFile() {
        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save data file: " + e.getMessage());
        }
    }
    
    public void loadAllData() {
        loadJobData();
        loadAchievementData();
        loadPlaytimeData();
    }
    
    private void loadJobData() {
        JobManager jobManager = plugin.getJobManager();
        
        // Load player jobs
        ConfigurationSection playerJobsSection = dataConfig.getConfigurationSection("player-jobs");
        if (playerJobsSection != null) {
            for (String uuidStr : playerJobsSection.getKeys(false)) {
                try {
                    UUID uuid = UUID.fromString(uuidStr);
                    String jobName = playerJobsSection.getString(uuidStr);
                    jobManager.setPlayerJob(uuid, jobName);
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Invalid UUID in data file: " + uuidStr);
                }
            }
        }
        
        // Load job cooldowns
        ConfigurationSection jobCooldownsSection = dataConfig.getConfigurationSection("job-cooldowns");
        if (jobCooldownsSection != null) {
            for (String jobName : jobCooldownsSection.getKeys(false)) {
                Job job = jobManager.getJob(jobName);
                if (job != null) {
                    ConfigurationSection jobSection = jobCooldownsSection.getConfigurationSection(jobName);
                    if (jobSection != null) {
                        Map<String, Long> cooldowns = new HashMap<>();
                        for (String playerUUID : jobSection.getKeys(false)) {
                            cooldowns.put(playerUUID, jobSection.getLong(playerUUID));
                        }
                        job.setPlayerCooldowns(cooldowns);
                    }
                }
            }
        }
        
        // Load block progress for mining jobs
        ConfigurationSection blockProgressSection = dataConfig.getConfigurationSection("job-block-progress");
        if (blockProgressSection != null) {
            for (String jobName : blockProgressSection.getKeys(false)) {
                Job job = jobManager.getJob(jobName);
                if (job != null && job.getType() == com.economyplugin.jobs.JobType.MINING) {
                    ConfigurationSection jobSection = blockProgressSection.getConfigurationSection(jobName);
                    if (jobSection != null) {
                        Map<String, Integer> blockProgress = new HashMap<>();
                        for (String playerUUID : jobSection.getKeys(false)) {
                            blockProgress.put(playerUUID, jobSection.getInt(playerUUID));
                        }
                        job.setPlayerBlockProgress(blockProgress);
                    }
                }
            }
        }
    }
    
    private void loadAchievementData() {
        AchievementManager achievementManager = plugin.getAchievementManager();
        
        // Load player achievement completion status
        ConfigurationSection achievementsSection = dataConfig.getConfigurationSection("player-achievements");
        if (achievementsSection != null) {
            for (String key : achievementsSection.getKeys(false)) {
                boolean completed = achievementsSection.getBoolean(key);
                achievementManager.setPlayerAchievement(key, completed);
            }
        }
    }
    
    private void loadPlaytimeData() {
        PlaytimeManager playtimeManager = plugin.getPlaytimeManager();
        
        // Load total playtime
        ConfigurationSection playtimeSection = dataConfig.getConfigurationSection("playtime");
        if (playtimeSection != null) {
            for (String uuidStr : playtimeSection.getKeys(false)) {
                try {
                    UUID uuid = UUID.fromString(uuidStr);
                    long playtime = playtimeSection.getLong(uuidStr);
                    playtimeManager.setTotalPlaytime(uuid, playtime);
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Invalid UUID in data file: " + uuidStr);
                }
            }
        }
        
        // Load last reward times
        ConfigurationSection rewardTimesSection = dataConfig.getConfigurationSection("last-reward-times");
        if (rewardTimesSection != null) {
            for (String uuidStr : rewardTimesSection.getKeys(false)) {
                try {
                    UUID uuid = UUID.fromString(uuidStr);
                    long lastReward = rewardTimesSection.getLong(uuidStr);
                    playtimeManager.setLastRewardTime(uuid, lastReward);
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Invalid UUID in data file: " + uuidStr);
                }
            }
        }
        
        // Load playtime tiers
        ConfigurationSection tiersSection = dataConfig.getConfigurationSection("playtime-tiers");
        if (tiersSection != null) {
            for (String uuidStr : tiersSection.getKeys(false)) {
                try {
                    UUID uuid = UUID.fromString(uuidStr);
                    int tier = tiersSection.getInt(uuidStr);
                    playtimeManager.setPlaytimeTier(uuid, tier);
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Invalid UUID in data file: " + uuidStr);
                }
            }
        }
    }
}
