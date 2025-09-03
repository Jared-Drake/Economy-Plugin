package com.economyplugin.commands;

import com.economyplugin.EconomyPlugin;
import com.economyplugin.managers.JobManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class JobInfoCommand implements CommandExecutor {
    
    private final EconomyPlugin plugin;
    
    public JobInfoCommand(EconomyPlugin plugin) {
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
        
        if (args.length == 0) {
            // Show current job info
            showCurrentJobInfo(player, jobManager);
        } else {
            // Show specific job info
            String jobName = args[0].toLowerCase();
            showSpecificJobInfo(player, jobManager, jobName);
        }
        
        return true;
    }
    
    private void showCurrentJobInfo(Player player, JobManager jobManager) {
        if (!jobManager.hasJob(player)) {
            player.sendMessage("§eYou don't have a job yet.");
            player.sendMessage("§7Use §f/jobs §7to see available jobs.");
            return;
        }
        
        String jobName = jobManager.getPlayerJob(player);
        com.economyplugin.jobs.Job job = jobManager.getJob(jobName);
        
        if (job != null) {
            showJobDetails(player, job, true);
        }
    }
    
    private void showSpecificJobInfo(Player player, JobManager jobManager, String jobName) {
        com.economyplugin.jobs.Job job = jobManager.getJob(jobName);
        
        if (job == null) {
            player.sendMessage("§cJob '" + jobName + "' not found!");
            player.sendMessage("§7Use §f/jobs §7to see available jobs.");
            return;
        }
        
        boolean isCurrentJob = jobManager.hasJob(player) && 
                              jobManager.getPlayerJob(player).equals(jobName);
        
        showJobDetails(player, job, isCurrentJob);
    }
    
    private void showJobDetails(Player player, com.economyplugin.jobs.Job job, boolean isCurrentJob) {
        player.sendMessage("§6=== Job Information ===");
        player.sendMessage("§7Name: §f" + job.getName());
        player.sendMessage("§7Description: §f" + job.getDescription());
        player.sendMessage("§7Type: §f" + job.getType().getDisplayName());
        player.sendMessage("§7Reward: §e$" + job.getReward());
        player.sendMessage("§7Cooldown: §f" + formatTime(job.getCooldown()));
        
        if (isCurrentJob) {
            player.sendMessage("§7Status: §aCurrent Job");
            
            if (job.canComplete(player)) {
                if (job.checkRequirements(player)) {
                    player.sendMessage("§a§l✓ Ready to complete!");
                    player.sendMessage("§7Use §f/job complete §7to finish your job.");
                } else {
                    player.sendMessage("§c§l✗ Requirements not met!");
                    showRequirementsWithProgress(player, job);
                }
            } else {
                long timeLeft = job.getTimeUntilAvailable(player);
                player.sendMessage("§c§l✗ On cooldown!");
                player.sendMessage("§7Time remaining: §f" + formatTime(timeLeft));
            }
        } else {
            player.sendMessage("§7Status: §eAvailable");
            player.sendMessage("§7Use §f/job join " + job.getName().toLowerCase() + " §7to get this job.");
        }
        
        if (isCurrentJob) {
            if (job.getType() == com.economyplugin.jobs.JobType.MINING) {
                showMiningProgress(player, job);
            } else {
                showRequirementsWithProgress(player, job);
            }
        } else {
            showRequirements(player, job);
        }
    }
    
    private void showRequirements(Player player, com.economyplugin.jobs.Job job) {
        player.sendMessage("§7Requirements:");
        for (java.util.Map.Entry<org.bukkit.Material, Integer> entry : job.getRequirements().entrySet()) {
            player.sendMessage("§7- §f" + entry.getValue() + "x " + entry.getKey().name());
        }
    }
    
    private void showRequirementsWithProgress(Player player, com.economyplugin.jobs.Job job) {
        player.sendMessage("§7Requirements Progress:");
        
        boolean allComplete = true;
        for (java.util.Map.Entry<org.bukkit.Material, Integer> entry : job.getRequirements().entrySet()) {
            org.bukkit.Material material = entry.getKey();
            int required = entry.getValue();
            
            // Count how many the player has
            int playerHas = 0;
            for (org.bukkit.inventory.ItemStack item : player.getInventory().getContents()) {
                if (item != null && item.getType() == material) {
                    playerHas += item.getAmount();
                }
            }
            
            // Determine status and color
            String status;
            String color;
            if (playerHas >= required) {
                status = "§a✓ Complete";
                color = "§a";
            } else {
                status = "§c✗ Incomplete";
                color = "§c";
                allComplete = false;
            }
            
            // Show progress bar
            int progressBars = Math.min(10, (int) ((double) playerHas / required * 10));
            StringBuilder progressBar = new StringBuilder("§7[");
            for (int i = 0; i < 10; i++) {
                if (i < progressBars) {
                    progressBar.append("§a█");
                } else {
                    progressBar.append("§7█");
                }
            }
            progressBar.append("§7]");
            
            player.sendMessage(String.format("§7- %sx %s %s", required, material.name(), status));
            player.sendMessage(String.format("  %s %s§7 (%d/%d)", progressBar.toString(), color, playerHas, required));
        }
        
        if (allComplete) {
            player.sendMessage("§a§l✓ All requirements met! You can complete this job!");
        } else {
            player.sendMessage("§c§l✗ Keep working to meet all requirements!");
        }
    }
    
    private void showMiningProgress(Player player, com.economyplugin.jobs.Job job) {
        int blocksBroken = job.getBlockProgress(player);
        int required = 200;
        
        player.sendMessage("§7Mining Progress:");
        
        // Show progress bar
        int progressBars = Math.min(10, (int) ((double) blocksBroken / required * 10));
        StringBuilder progressBar = new StringBuilder("§7[");
        for (int i = 0; i < 10; i++) {
            if (i < progressBars) {
                progressBar.append("§a█");
            } else {
                progressBar.append("§7█");
            }
        }
        progressBar.append("§7]");
        
        String status = blocksBroken >= required ? "§a✓ Complete" : "§c✗ Incomplete";
        String color = blocksBroken >= required ? "§a" : "§c";
        
        player.sendMessage(String.format("§7- Break %d blocks with pickaxe %s", required, status));
        player.sendMessage(String.format("  %s %s§7 (%d/%d)", progressBar.toString(), color, blocksBroken, required));
        
        if (blocksBroken >= required) {
            player.sendMessage("§a§l✓ Mining goal reached! You can complete this job!");
        } else {
            player.sendMessage("§c§l✗ Keep mining blocks with your pickaxe!");
        }
    }
    
    private String formatTime(long seconds) {
        if (seconds < 60) {
            return seconds + "s";
        } else if (seconds < 3600) {
            return (seconds / 60) + "m " + (seconds % 60) + "s";
        } else {
            long hours = seconds / 3600;
            long minutes = (seconds % 3600) / 60;
            return hours + "h " + minutes + "m";
        }
    }
}
