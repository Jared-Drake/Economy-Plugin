package com.economyplugin.listeners;

import com.economyplugin.EconomyPlugin;
import com.economyplugin.managers.JobManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

public class BlockBreakListener implements Listener {
    
    private final EconomyPlugin plugin;
    
    public BlockBreakListener(EconomyPlugin plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        JobManager jobManager = plugin.getJobManager();
        
        // Check if player has a mining job
        if (!jobManager.hasJob(player)) {
            return;
        }
        
        String jobName = jobManager.getPlayerJob(player);
        if (jobName == null || !jobName.equals("miner")) {
            return; // Only track mining jobs
        }
        
        // Check if player is using a pickaxe
        ItemStack tool = player.getInventory().getItemInMainHand();
        if (tool.getType().name().contains("PICKAXE")) {
            // Increment block breaking progress
            com.economyplugin.jobs.Job job = jobManager.getJob(jobName);
            if (job != null) {
                job.incrementBlockProgress(player);
                
                // Check if job is now complete
                if (job.hasCompletedBlockRequirement(player)) {
                    player.sendMessage("§a§lMining Job Complete! §fYou've broken 200 blocks with a pickaxe!");
                    player.sendMessage("§7Use §f/job complete §7to finish your job and collect your reward.");
                } else {
                    // Show progress update every 50 blocks
                    int progress = job.getBlockProgress(player);
                    if (progress % 50 == 0) {
                        player.sendMessage("§7Mining Progress: §f" + progress + "/200 blocks");
                    }
                }
            }
        }
    }
}





