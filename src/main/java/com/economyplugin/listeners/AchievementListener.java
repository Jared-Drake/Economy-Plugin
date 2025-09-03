package com.economyplugin.listeners;

import com.economyplugin.EconomyPlugin;
import com.economyplugin.managers.AchievementManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;

public class AchievementListener implements Listener {
    
    private final EconomyPlugin plugin;
    
    public AchievementListener(EconomyPlugin plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onPlayerAdvancementDone(PlayerAdvancementDoneEvent event) {
        AchievementManager achievementManager = plugin.getAchievementManager();
        
        // Check achievements and give rewards
        achievementManager.checkAchievements(event.getPlayer());
    }
}





