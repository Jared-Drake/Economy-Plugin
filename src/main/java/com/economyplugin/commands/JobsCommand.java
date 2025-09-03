package com.economyplugin.commands;

import com.economyplugin.EconomyPlugin;
import com.economyplugin.managers.JobManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class JobsCommand implements CommandExecutor {
    
    private final EconomyPlugin plugin;
    
    public JobsCommand(EconomyPlugin plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cThis command can only be used by players!");
            return true;
        }
        
        Player player = (Player) sender;
        JobManager jobManager = plugin.getJobManager();
        
        player.sendMessage("§6=== Available Jobs ===");
        
        for (com.economyplugin.jobs.Job job : jobManager.getAvailableJobs()) {
            String status = "§aAvailable";
            if (jobManager.hasJob(player) && jobManager.getPlayerJob(player).equals(job.getName())) {
                status = "§eCurrent Job";
            }
            
            player.sendMessage(String.format("§7%s §f- §7%s §f(%s)", 
                job.getName(), job.getDescription(), status));
        }
        
        if (!jobManager.hasJob(player)) {
            player.sendMessage("§7Use §f/job join <jobname> §7to get a job.");
        } else {
            player.sendMessage("§7Use §f/job info §7to see your current job details.");
        }
        
        return true;
    }
}




