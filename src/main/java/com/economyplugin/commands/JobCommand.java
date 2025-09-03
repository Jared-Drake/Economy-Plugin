package com.economyplugin.commands;

import com.economyplugin.EconomyPlugin;
import com.economyplugin.managers.JobManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class JobCommand implements CommandExecutor {
    
    private final EconomyPlugin plugin;
    
    public JobCommand(EconomyPlugin plugin) {
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
            showJobInfo(player, jobManager);
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "info":
                showJobInfo(player, jobManager);
                break;
            case "list":
                showAvailableJobs(player, jobManager);
                break;
            case "join":
                if (args.length < 2) {
                    player.sendMessage("§cUsage: /job join <jobname>");
                    return true;
                }
                joinJob(player, jobManager, args[1]);
                break;
            case "quit":
                quitJob(player, jobManager);
                break;
            case "complete":
                completeJob(player, jobManager);
                break;
            default:
                player.sendMessage("§cUnknown subcommand. Use: /job [info|list|join|quit|complete]");
                break;
        }
        
        return true;
    }
    
    private void showJobInfo(Player player, JobManager jobManager) {
        if (!jobManager.hasJob(player)) {
            player.sendMessage("§eYou don't have a job yet.");
            player.sendMessage("§7Use §f/job list §7to see available jobs.");
            player.sendMessage("§7Use §f/job join <jobname> §7to get a job.");
            return;
        }
        
        String jobName = jobManager.getPlayerJob(player);
        com.economyplugin.jobs.Job job = jobManager.getJob(jobName);
        
        if (job != null) {
            player.sendMessage("§6=== Job Information ===");
            player.sendMessage("§7Current Job: §f" + job.getName());
            player.sendMessage("§7Description: §f" + job.getDescription());
            player.sendMessage("§7Type: §f" + job.getType().getDisplayName());
            player.sendMessage("§7Reward: §e$" + job.getReward());
            player.sendMessage("§7Cooldown: §f" + formatTime(job.getCooldown()));
            
            // For mining jobs, show block progress
            if (job.getType() == com.economyplugin.jobs.JobType.MINING) {
                int blockProgress = job.getBlockProgress(player);
                player.sendMessage("§7Blocks Broken: §f" + blockProgress + "/200");
            }
            
            if (job.canComplete(player)) {
                // Use the appropriate requirement check method based on job type
                boolean requirementsMet = (job.getType() == com.economyplugin.jobs.JobType.MINING) 
                    ? job.hasCompletedBlockRequirement(player) 
                    : job.checkRequirements(player);
                
                if (requirementsMet) {
                    player.sendMessage("§a§l✓ Ready to complete!");
                    player.sendMessage("§7Use §f/job complete §7to finish your job.");
                } else {
                    player.sendMessage("§c§l✗ Requirements not met!");
                    if (job.getType() == com.economyplugin.jobs.JobType.MINING) {
                        // For mining jobs, show block progress
                        int progress = job.getBlockProgress(player);
                        player.sendMessage("§7Keep breaking blocks with a pickaxe!");
                        player.sendMessage("§7Progress: §f" + progress + "/200 blocks");
                    } else {
                        showRequirementsWithProgress(player, job);
                    }
                }
            } else {
                long timeLeft = job.getTimeUntilAvailable(player);
                player.sendMessage("§c§l✗ On cooldown!");
                player.sendMessage("§7Time remaining: §f" + formatTime(timeLeft));
            }
        }
    }
    
    private void showAvailableJobs(Player player, JobManager jobManager) {
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
        }
    }
    
    private void joinJob(Player player, JobManager jobManager, String jobName) {
        if (jobManager.hasJob(player)) {
            player.sendMessage("§cYou already have a job! Use §f/job quit §cto quit first.");
            return;
        }
        
        if (jobManager.assignJob(player, jobName)) {
            player.sendMessage("§a§lJob Assigned! §fYou are now a " + jobName + "!");
            player.sendMessage("§7Use §f/job info §7to see your job details.");
        } else {
            player.sendMessage("§cCould not assign job '" + jobName + "'. Job may not exist.");
        }
    }
    
    private void quitJob(Player player, JobManager jobManager) {
        if (!jobManager.hasJob(player)) {
            player.sendMessage("§cYou don't have a job to quit!");
            return;
        }
        
        if (jobManager.quitJob(player)) {
            player.sendMessage("§a§lJob Quit! §fYou are now unemployed.");
            player.sendMessage("§7Use §f/job list §7to see available jobs.");
        } else {
            player.sendMessage("§cCould not quit job. Please try again.");
        }
    }
    
    private void completeJob(Player player, JobManager jobManager) {
        if (!jobManager.hasJob(player)) {
            player.sendMessage("§cYou don't have a job to complete!");
            return;
        }
        
        if (jobManager.completeJob(player)) {
            String jobName = jobManager.getPlayerJob(player);
            com.economyplugin.jobs.Job job = jobManager.getJob(jobName);
            
            if (job != null) {
                player.sendMessage("§a§lJob Completed! §fGreat work!");
                player.sendMessage("§6Reward: §e$" + job.getReward());
                player.sendMessage("§7Job will be available again in §f" + formatTime(job.getCooldown()));
            }
        } else {
            player.sendMessage("§cCould not complete job. Check requirements and cooldown.");
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
