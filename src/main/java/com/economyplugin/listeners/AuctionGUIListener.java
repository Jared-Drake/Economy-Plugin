package com.economyplugin.listeners;

import com.economyplugin.EconomyPlugin;
import com.economyplugin.auction.AuctionItem;
import com.economyplugin.gui.AuctionGUI;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AuctionGUIListener implements Listener {
    
    private final EconomyPlugin plugin;
    private final AuctionGUI gui;
    private final Map<UUID, Integer> playerPages; // Player UUID -> Current page
    private final Map<UUID, AuctionItem> selectedAuctions; // Player UUID -> Selected auction for bidding
    private final Map<UUID, AuctionItem> pendingCustomBids; // Player UUID -> Auction waiting for custom bid input
    
    public AuctionGUIListener(EconomyPlugin plugin) {
        this.plugin = plugin;
        this.gui = new AuctionGUI(plugin);
        this.playerPages = new HashMap<>();
        this.selectedAuctions = new HashMap<>();
        this.pendingCustomBids = new HashMap<>();
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getWhoClicked();
        String title = event.getView().getTitle();
        
        if (!title.contains("Auction House") && !title.contains("Browse Auctions") && 
            !title.contains("My Auctions") && !title.contains("My Bids") && 
            !title.contains("Auction Details") && !title.contains("Place Bid")) {
            return;
        }
        
        event.setCancelled(true);
        
        if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) {
            return;
        }
        
        ItemStack clickedItem = event.getCurrentItem();
        
        // Main menu
        if (title.equals("§8Auction House")) {
            handleMainMenuClick(player, clickedItem);
        }
        // Browse auctions
        else if (title.contains("Browse Auctions")) {
            handleBrowseAuctionsClick(player, clickedItem, title);
        }
        // My auctions
        else if (title.contains("My Auctions")) {
            handleMyAuctionsClick(player, clickedItem, title);
        }
        // My bids
        else if (title.contains("My Bids")) {
            handleMyBidsClick(player, clickedItem, title);
        }
        // Auction details
        else if (title.equals("§8Auction Details")) {
            handleAuctionDetailsClick(player, clickedItem);
        }
        // Place bid
        else if (title.equals("§8Place Bid")) {
            handlePlaceBidClick(player, clickedItem);
        }
    }
    
    private void handleMainMenuClick(Player player, ItemStack clickedItem) {
        if (clickedItem.getType() == Material.BOOK) {
            // Browse Auctions
            playerPages.put(player.getUniqueId(), 0);
            gui.openBrowseAuctions(player, 0);
        } else if (clickedItem.getType() == Material.CHEST) {
            // My Auctions
            playerPages.put(player.getUniqueId(), 0);
            gui.openMyAuctions(player, 0);
        } else if (clickedItem.getType() == Material.GOLD_INGOT) {
            // My Bids
            playerPages.put(player.getUniqueId(), 0);
            gui.openMyBids(player, 0);
        }
    }
    
    private void handleBrowseAuctionsClick(Player player, ItemStack clickedItem, String title) {
        int currentPage = playerPages.getOrDefault(player.getUniqueId(), 0);
        
        if (clickedItem.getType() == Material.ARROW) {
            if (clickedItem.getItemMeta().getDisplayName().contains("Previous")) {
                gui.openBrowseAuctions(player, currentPage - 1);
                playerPages.put(player.getUniqueId(), currentPage - 1);
            } else if (clickedItem.getItemMeta().getDisplayName().contains("Next")) {
                gui.openBrowseAuctions(player, currentPage + 1);
                playerPages.put(player.getUniqueId(), currentPage + 1);
            }
        } else if (clickedItem.getType() == Material.BARRIER) {
            gui.openMainMenu(player);
        } else {
            // Auction item clicked
            AuctionItem auction = getAuctionFromItem(clickedItem);
            if (auction != null) {
                selectedAuctions.put(player.getUniqueId(), auction);
                gui.openAuctionDetails(player, auction);
            }
        }
    }
    
    private void handleMyAuctionsClick(Player player, ItemStack clickedItem, String title) {
        int currentPage = playerPages.getOrDefault(player.getUniqueId(), 0);
        
        if (clickedItem.getType() == Material.ARROW) {
            if (clickedItem.getItemMeta().getDisplayName().contains("Previous")) {
                gui.openMyAuctions(player, currentPage - 1);
                playerPages.put(player.getUniqueId(), currentPage - 1);
            } else if (clickedItem.getItemMeta().getDisplayName().contains("Next")) {
                gui.openMyAuctions(player, currentPage + 1);
                playerPages.put(player.getUniqueId(), currentPage + 1);
            }
        } else if (clickedItem.getType() == Material.BARRIER) {
            gui.openMainMenu(player);
        } else {
            // Auction item clicked
            AuctionItem auction = getAuctionFromItem(clickedItem);
            if (auction != null) {
                selectedAuctions.put(player.getUniqueId(), auction);
                gui.openAuctionDetails(player, auction);
            }
        }
    }
    
    private void handleMyBidsClick(Player player, ItemStack clickedItem, String title) {
        int currentPage = playerPages.getOrDefault(player.getUniqueId(), 0);
        
        if (clickedItem.getType() == Material.ARROW) {
            if (clickedItem.getItemMeta().getDisplayName().contains("Previous")) {
                gui.openMyBids(player, currentPage - 1);
                playerPages.put(player.getUniqueId(), currentPage - 1);
            } else if (clickedItem.getItemMeta().getDisplayName().contains("Next")) {
                gui.openMyBids(player, currentPage + 1);
                playerPages.put(player.getUniqueId(), currentPage + 1);
            }
        } else if (clickedItem.getType() == Material.BARRIER) {
            gui.openMainMenu(player);
        } else {
            // Auction item clicked
            AuctionItem auction = getAuctionFromItem(clickedItem);
            if (auction != null) {
                selectedAuctions.put(player.getUniqueId(), auction);
                gui.openBidMenu(player, auction);
            }
        }
    }
    
    private void handleAuctionDetailsClick(Player player, ItemStack clickedItem) {
        AuctionItem auction = selectedAuctions.get(player.getUniqueId());
        if (auction == null) {
            player.closeInventory();
            return;
        }
        
        if (clickedItem.getType() == Material.GOLD_INGOT) {
            // Place bid button
            gui.openBidMenu(player, auction);
        } else if (clickedItem.getType() == Material.BARRIER) {
            // Back button
            gui.openMainMenu(player);
            selectedAuctions.remove(player.getUniqueId());
        }
    }
    
    private void handlePlaceBidClick(Player player, ItemStack clickedItem) {
        AuctionItem auction = selectedAuctions.get(player.getUniqueId());
        if (auction == null) {
            player.closeInventory();
            return;
        }
        
        if (clickedItem.getType() == Material.BARRIER) {
            // Back button
            gui.openAuctionDetails(player, auction);
        } else if (clickedItem.getType() == Material.DIAMOND) {
            // Buyout button
            if (plugin.getAuctionManager().placeBid(player, auction.getAuctionId(), auction.getBuyoutPrice())) {
                player.closeInventory();
                selectedAuctions.remove(player.getUniqueId());
            }
        } else if (clickedItem.getType() == Material.BOOK) {
            // Custom bid button
            handleCustomBid(player, auction);
        } else if (clickedItem.getType() == Material.GOLD_NUGGET || 
                   clickedItem.getType() == Material.GOLD_INGOT || 
                   clickedItem.getType() == Material.GOLD_BLOCK) {
            // Bid button
            String displayName = clickedItem.getItemMeta().getDisplayName();
            if (displayName.contains("Bid $")) {
                try {
                    double bidAmount = Double.parseDouble(displayName.split("\\$")[1]);
                    if (plugin.getAuctionManager().placeBid(player, auction.getAuctionId(), bidAmount)) {
                        player.closeInventory();
                        selectedAuctions.remove(player.getUniqueId());
                    }
                } catch (NumberFormatException e) {
                    player.sendMessage("§cInvalid bid amount!");
                }
            }
        }
    }
    
    private void handleCustomBid(Player player, AuctionItem auction) {
        player.closeInventory();
        player.sendMessage("§6=== Custom Bid ===");
        player.sendMessage("§7Current bid: §e$" + auction.getCurrentBid());
        player.sendMessage("§7Your balance: §a$" + plugin.getEconomy().getBalance(player));
        player.sendMessage("§7Minimum bid: §e$" + (auction.getCurrentBid() + 1));
        if (auction.getBuyoutPrice() > 0) {
            player.sendMessage("§7Buyout price: §6$" + auction.getBuyoutPrice());
        }
        player.sendMessage("§ePlease type your bid amount in chat (or 'cancel' to cancel):");
        
        // Store the auction for the chat input
        pendingCustomBids.put(player.getUniqueId(), auction);
    }
    
    private AuctionItem getAuctionFromItem(ItemStack item) {
        // This is a simplified method - in a real implementation, you might want to store
        // auction data in the item's metadata or use a more sophisticated system
        // For now, we'll search through active auctions to find a match
        for (AuctionItem auction : plugin.getAuctionManager().getActiveAuctions()) {
            if (auction.getItem().getType() == item.getType() && 
                auction.getItem().getAmount() == item.getAmount()) {
                return auction;
            }
        }
        return null;
    }
    
    public AuctionGUI getGui() {
        return gui;
    }
    
    public AuctionItem getPendingCustomBid(UUID playerId) {
        return pendingCustomBids.get(playerId);
    }
    
    public void removePendingCustomBid(UUID playerId) {
        pendingCustomBids.remove(playerId);
    }
}
