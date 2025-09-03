package com.economyplugin.commands;

import com.economyplugin.EconomyPlugin;
import com.economyplugin.gui.AuctionGUI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AuctionCommand implements CommandExecutor, TabCompleter {
    
    private final EconomyPlugin plugin;
    private final AuctionGUI gui;
    
    public AuctionCommand(EconomyPlugin plugin) {
        this.plugin = plugin;
        this.gui = new AuctionGUI(plugin);
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cThis command can only be used by players!");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (args.length == 0) {
            // Open auction house GUI
            gui.openMainMenu(player);
            return true;
        }
        
        switch (args[0].toLowerCase()) {
            case "create":
                return handleCreateAuction(player, args);
            case "cancel":
                return handleCancelAuction(player, args);
            case "bid":
                return handleBid(player, args);
            case "list":
                return handleList(player, args);
            case "my":
                return handleMy(player, args);
            case "help":
                return handleHelp(player);
            default:
                player.sendMessage("§cUnknown subcommand. Use /auction help for help.");
                return true;
        }
    }
    
    private boolean handleCreateAuction(Player player, String[] args) {
        if (args.length < 3) {
            player.sendMessage("§cUsage: /auction create <starting_price> <duration_hours> [buyout_price]");
            return true;
        }
        
        // Check if player is holding an item
        ItemStack heldItem = player.getInventory().getItemInMainHand();
        if (heldItem.getType().isAir()) {
            player.sendMessage("§cYou must be holding an item to create an auction!");
            return true;
        }
        
        try {
            double startingPrice = Double.parseDouble(args[1]);
            int durationHours = Integer.parseInt(args[2]);
            double buyoutPrice = 0;
            
            if (args.length > 3) {
                buyoutPrice = Double.parseDouble(args[3]);
            }
            
            if (startingPrice <= 0) {
                player.sendMessage("§cStarting price must be greater than 0!");
                return true;
            }
            
            if (durationHours <= 0 || durationHours > 168) { // Max 7 days
                player.sendMessage("§cDuration must be between 1 and 168 hours!");
                return true;
            }
            
            if (buyoutPrice > 0 && buyoutPrice < startingPrice) {
                player.sendMessage("§cBuyout price must be higher than starting price!");
                return true;
            }
            
            if (plugin.getAuctionManager().createAuction(player, heldItem, startingPrice, buyoutPrice, durationHours)) {
                player.sendMessage("§aAuction created successfully!");
            } else {
                player.sendMessage("§cFailed to create auction. Make sure you have the item and enough money!");
            }
            
        } catch (NumberFormatException e) {
            player.sendMessage("§cInvalid number format!");
        }
        
        return true;
    }
    
    private boolean handleCancelAuction(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("§cUsage: /auction cancel <auction_id>");
            return true;
        }
        
        try {
            // For simplicity, we'll use a different approach since auction IDs are UUIDs
            // In a real implementation, you might want to show a list of player's auctions
            player.sendMessage("§cPlease use the GUI to cancel auctions. Use /auction to open the auction house.");
        } catch (Exception e) {
            player.sendMessage("§cInvalid auction ID!");
        }
        
        return true;
    }
    
    private boolean handleBid(Player player, String[] args) {
        if (args.length < 3) {
            player.sendMessage("§cUsage: /auction bid <auction_id> <amount>");
            return true;
        }
        
        try {
            double bidAmount = Double.parseDouble(args[2]);
            // For simplicity, we'll use a different approach since auction IDs are UUIDs
            player.sendMessage("§cPlease use the GUI to place bids. Use /auction to open the auction house.");
        } catch (NumberFormatException e) {
            player.sendMessage("§cInvalid bid amount!");
        }
        
        return true;
    }
    
    private boolean handleList(Player player, String[] args) {
        // Open browse auctions GUI
        gui.openBrowseAuctions(player, 0);
        return true;
    }
    
    private boolean handleMy(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("§cUsage: /auction my <auctions|bids>");
            return true;
        }
        
        switch (args[1].toLowerCase()) {
            case "auctions":
                gui.openMyAuctions(player, 0);
                break;
            case "bids":
                gui.openMyBids(player, 0);
                break;
            default:
                player.sendMessage("§cUnknown option. Use 'auctions' or 'bids'.");
                break;
        }
        
        return true;
    }
    
    private boolean handleHelp(Player player) {
        player.sendMessage("§6=== Auction House Commands ===");
        player.sendMessage("§e/auction §7- Open the auction house GUI");
        player.sendMessage("§e/auction create <price> <hours> [buyout] §7- Create an auction");
        player.sendMessage("§e/auction list §7- Browse all auctions");
        player.sendMessage("§e/auction my auctions §7- View your auctions");
        player.sendMessage("§e/auction my bids §7- View your bids");
        player.sendMessage("§e/auction help §7- Show this help message");
        return true;
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player)) {
            return new ArrayList<>();
        }
        
        if (args.length == 1) {
            return Arrays.asList("create", "cancel", "bid", "list", "my", "help");
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("my")) {
                return Arrays.asList("auctions", "bids");
            }
        }
        
        return new ArrayList<>();
    }
}
