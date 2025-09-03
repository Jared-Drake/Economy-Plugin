package com.economyplugin.listeners;

import com.economyplugin.EconomyPlugin;
import com.economyplugin.auction.AuctionItem;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.UUID;

public class AuctionChatListener implements Listener {
    
    private final EconomyPlugin plugin;
    private final AuctionGUIListener guiListener;
    
    public AuctionChatListener(EconomyPlugin plugin, AuctionGUIListener guiListener) {
        this.plugin = plugin;
        this.guiListener = guiListener;
    }
    
    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage();
        
        // Check if player has a pending custom bid
        AuctionItem pendingAuction = guiListener.getPendingCustomBid(player.getUniqueId());
        if (pendingAuction == null) {
            return;
        }
        
        // Cancel the chat event to prevent the message from being sent
        event.setCancelled(true);
        
        // Handle the custom bid
        handleCustomBidInput(player, message, pendingAuction);
    }
    
    private void handleCustomBidInput(Player player, String input, AuctionItem auction) {
        // Remove the pending bid
        guiListener.removePendingCustomBid(player.getUniqueId());
        
        if (input.equalsIgnoreCase("cancel")) {
            player.sendMessage("§cCustom bid cancelled.");
            return;
        }
        
        try {
            double bidAmount = Double.parseDouble(input);
            
            // Validate the bid amount
            if (bidAmount <= auction.getCurrentBid()) {
                player.sendMessage("§cBid must be higher than the current bid of $" + auction.getCurrentBid() + "!");
                return;
            }
            
            if (bidAmount > plugin.getEconomy().getBalance(player)) {
                player.sendMessage("§cYou don't have enough money! Your balance: $" + plugin.getEconomy().getBalance(player));
                return;
            }
            
            // Place the bid
            if (plugin.getAuctionManager().placeBid(player, auction.getAuctionId(), bidAmount)) {
                player.sendMessage("§aCustom bid of $" + bidAmount + " placed successfully!");
            } else {
                player.sendMessage("§cFailed to place bid. The auction may have expired or been cancelled.");
            }
            
        } catch (NumberFormatException e) {
            player.sendMessage("§cInvalid bid amount! Please enter a valid number.");
        }
    }
}
