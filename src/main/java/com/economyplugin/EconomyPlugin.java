package com.economyplugin;

import com.economyplugin.commands.*;
import com.economyplugin.listeners.*;
import com.economyplugin.managers.*;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class EconomyPlugin extends JavaPlugin {
    
    private static EconomyPlugin instance;
    private Economy economy;
    private JobManager jobManager;
    private AchievementManager achievementManager;
    private PlaytimeManager playtimeManager;
    private DataManager dataManager;
    private AuctionManager auctionManager;
    
    @Override
    public void onEnable() {
        instance = this;
        
        // Save default config
        saveDefaultConfig();
        
        // Setup Vault economy
        if (!setupEconomy()) {
            getLogger().severe("Vault not found! Disabling plugin.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        
        // Initialize managers
        dataManager = new DataManager(this);
        jobManager = new JobManager(this);
        achievementManager = new AchievementManager(this);
        playtimeManager = new PlaytimeManager(this);
        auctionManager = new AuctionManager(this);
        
        // Load all data
        dataManager.loadAllData();
        
        // Register commands
        registerCommands();
        
        // Register listeners
        registerListeners();
        
        // Start playtime tracking
        playtimeManager.startPlaytimeTracking();
        
        getLogger().info("EconomyPlugin has been enabled!");
    }
    
    @Override
    public void onDisable() {
        if (playtimeManager != null) {
            playtimeManager.stopPlaytimeTracking();
        }
        
        if (dataManager != null) {
            dataManager.saveAllData();
        }
        
        getLogger().info("EconomyPlugin has been disabled!");
    }
    
    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        
        economy = rsp.getProvider();
        return economy != null;
    }
    
    private void registerCommands() {
        getCommand("job").setExecutor(new JobCommand(this));
        getCommand("jobs").setExecutor(new JobsCommand(this));
        getCommand("jobinfo").setExecutor(new JobInfoCommand(this));
        getCommand("playtime").setExecutor(new PlaytimeCommand(this));
        getCommand("achievement").setExecutor(new AchievementCommand(this));
        getCommand("adminpay").setExecutor(new AdminPayCommand(this));
        getCommand("adminpay").setTabCompleter(new AdminPayCommand(this));
        getCommand("auction").setExecutor(new AuctionCommand(this));
        getCommand("auction").setTabCompleter(new AuctionCommand(this));
    }
    
    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new BlockBreakListener(this), this);
        getServer().getPluginManager().registerEvents(new AchievementListener(this), this);
        getServer().getPluginManager().registerEvents(new PlaytimeListener(this), this);
        
        AuctionGUIListener auctionGUIListener = new AuctionGUIListener(this);
        getServer().getPluginManager().registerEvents(auctionGUIListener, this);
        getServer().getPluginManager().registerEvents(new AuctionChatListener(this, auctionGUIListener), this);
    }
    
    // Getters
    public static EconomyPlugin getInstance() {
        return instance;
    }
    
    public Economy getEconomy() {
        return economy;
    }
    
    public JobManager getJobManager() {
        return jobManager;
    }
    
    public AchievementManager getAchievementManager() {
        return achievementManager;
    }
    
    public PlaytimeManager getPlaytimeManager() {
        return playtimeManager;
    }
    
    public DataManager getDataManager() {
        return dataManager;
    }
    
    public AuctionManager getAuctionManager() {
        return auctionManager;
    }
}
