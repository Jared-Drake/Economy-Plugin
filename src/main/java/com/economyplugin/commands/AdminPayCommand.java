package com.economyplugin.commands;

import com.economyplugin.EconomyPlugin;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AdminPayCommand implements CommandExecutor, TabCompleter {
    
    private final EconomyPlugin plugin;
    
    public AdminPayCommand(EconomyPlugin plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Check if sender has permission
        if (!sender.hasPermission("economyplugin.admin.pay")) {
            sender.sendMessage("§c§lError! §fYou don't have permission to use this command.");
            return true;
        }
        
        // Check if sender is a player (for balance checking) or console
        boolean isPlayer = sender instanceof Player;
        Player senderPlayer = isPlayer ? (Player) sender : null;
        
        if (args.length < 2) {
            sender.sendMessage("§c§lUsage: §f/adminpay <player> <amount>");
            sender.sendMessage("§7This command allows operators to pay any amount to players.");
            return true;
        }
        
        // Get target player
        Player targetPlayer = Bukkit.getPlayer(args[0]);
        if (targetPlayer == null) {
            sender.sendMessage("§c§lError! §fPlayer '" + args[0] + "' not found or not online.");
            return true;
        }
        
        // Parse amount
        double amount;
        try {
            amount = Double.parseDouble(args[1]);
        } catch (NumberFormatException e) {
            sender.sendMessage("§c§lError! §fInvalid amount: " + args[1]);
            return true;
        }
        
        if (amount <= 0) {
            sender.sendMessage("§c§lError! §fAmount must be greater than 0.");
            return true;
        }
        
        // Check if sender is trying to pay themselves (allow for operators)
        if (isPlayer && targetPlayer.equals(senderPlayer)) {
            sender.sendMessage("§e§lNote: §fYou are paying yourself. This will add money to your account.");
        }
        
        // Get economy instance
        Economy economy = plugin.getEconomy();
        if (economy == null) {
            sender.sendMessage("§c§lError! §fEconomy system not available.");
            return true;
        }
        
        // If sender is a player, check their balance (but allow negative for admins)
        if (isPlayer) {
            double senderBalance = economy.getBalance(senderPlayer);
            if (senderBalance < amount) {
                sender.sendMessage("§e§lNote: §fYou don't have enough money in your balance.");
                sender.sendMessage("§7As an operator, you can still complete this transaction.");
                sender.sendMessage("§7Your balance will go negative: §c$" + String.format("%.2f", senderBalance - amount));
            }
        }
        
        // Perform the payment
        boolean success = false;
        if (isPlayer) {
            if (targetPlayer.equals(senderPlayer)) {
                // Self-payment: just deposit the money directly
                economy.depositPlayer(targetPlayer, amount);
                success = true;
            } else {
                // Payment to other player: withdraw from sender first
                success = economy.withdrawPlayer(senderPlayer, amount).transactionSuccess();
                if (success) {
                    // Then deposit to target
                    economy.depositPlayer(targetPlayer, amount);
                }
            }
        } else {
            // Console sender: just deposit to target (no withdrawal needed)
            economy.depositPlayer(targetPlayer, amount);
            success = true;
        }
        
        if (success) {
            // Success messages
            sender.sendMessage("§a§lSuccess! §fPaid §e$" + String.format("%.2f", amount) + " §fto §f" + targetPlayer.getName());
            
            if (isPlayer) {
                double newBalance = economy.getBalance(senderPlayer);
                sender.sendMessage("§7Your new balance: §f$" + String.format("%.2f", newBalance));
            }
            
            targetPlayer.sendMessage("§a§lPayment Received! §fYou received §e$" + String.format("%.2f", amount) + " §ffrom §f" + sender.getName());
            
            // Log the transaction
            plugin.getLogger().info("Admin payment: " + sender.getName() + " paid $" + String.format("%.2f", amount) + " to " + targetPlayer.getName());
            
        } else {
            sender.sendMessage("§c§lError! §fFailed to complete the payment transaction.");
        }
        
        return true;
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            // Tab complete player names
            String partialName = args[0].toLowerCase();
            completions.addAll(Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(partialName))
                    .collect(Collectors.toList()));
        } else if (args.length == 2) {
            // Tab complete common amounts
            String partialAmount = args[1].toLowerCase();
            String[] commonAmounts = {"100", "500", "1000", "5000", "10000", "50000", "100000"};
            for (String amount : commonAmounts) {
                if (amount.startsWith(partialAmount)) {
                    completions.add(amount);
                }
            }
        }
        
        return completions;
    }
}
