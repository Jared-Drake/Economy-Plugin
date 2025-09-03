package com.economyplugin.listeners;

import com.economyplugin.EconomyPlugin;
import com.economyplugin.managers.PlaytimeManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlaytimeListener implements Listener {
    
    private final EconomyPlugin plugin;
    
    public PlaytimeListener(EconomyPlugin plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        PlaytimeManager playtimeManager = plugin.getPlaytimeManager();
        playtimeManager.playerJoined(event.getPlayer());
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        PlaytimeManager playtimeManager = plugin.getPlaytimeManager();
        playtimeManager.playerLeft(event.getPlayer());
    }
}





