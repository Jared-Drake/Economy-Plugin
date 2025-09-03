package com.economyplugin.managers;

import com.economyplugin.EconomyPlugin;
import com.economyplugin.auction.AuctionItem;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class AuctionManager {
    
    private final EconomyPlugin plugin;
    private final Map<UUID, AuctionItem> activeAuctions;
    private final Map<UUID, List<UUID>> playerAuctions; // Player UUID -> List of auction UUIDs
    private final Map<UUID, List<UUID>> playerBids; // Player UUID -> List of auction UUIDs they've bid on
    private File auctionFile;
    private FileConfiguration auctionConfig;
    
    public AuctionManager(EconomyPlugin plugin) {
        this.plugin = plugin;
        this.activeAuctions = new ConcurrentHashMap<>();
        this.playerAuctions = new ConcurrentHashMap<>();
        this.playerBids = new ConcurrentHashMap<>();
        loadAuctions();
    }
    
    public boolean createAuction(Player seller, ItemStack item, double startingPrice, double buyoutPrice, int durationHours) {
        if (startingPrice <= 0) {
            return false;
        }
        
        if (buyoutPrice > 0 && buyoutPrice < startingPrice) {
            return false;
        }
        
        // Check if player has the item
        if (!seller.getInventory().containsAtLeast(item, item.getAmount())) {
            return false;
        }
        
        // Remove item from player's inventory
        seller.getInventory().removeItem(item);
        
        // Create auction
        UUID auctionId = UUID.randomUUID();
        AuctionItem auction = new AuctionItem(
            auctionId,
            seller.getUniqueId(),
            item.clone(),
            startingPrice,
            buyoutPrice,
            System.currentTimeMillis(),
            System.currentTimeMillis() + (durationHours * 60 * 60 * 1000L)
        );
        
        activeAuctions.put(auctionId, auction);
        
        // Track player's auctions
        playerAuctions.computeIfAbsent(seller.getUniqueId(), k -> new ArrayList<>()).add(auctionId);
        
        // Schedule auction expiration
        scheduleAuctionExpiration(auctionId, durationHours);
        
        saveAuctions();
        
        seller.sendMessage("§aAuction created successfully! Starting price: $" + startingPrice);
        return true;
    }
    
    public boolean placeBid(Player bidder, UUID auctionId, double bidAmount) {
        AuctionItem auction = activeAuctions.get(auctionId);
        if (auction == null) {
            bidder.sendMessage("§cAuction not found!");
            return false;
        }
        
        if (auction.isExpired()) {
            bidder.sendMessage("§cThis auction has expired!");
            return false;
        }
        
        if (auction.getSellerId().equals(bidder.getUniqueId())) {
            bidder.sendMessage("§cYou cannot bid on your own auction!");
            return false;
        }
        
        if (bidAmount <= auction.getCurrentBid()) {
            bidder.sendMessage("§cBid must be higher than current bid!");
            return false;
        }
        
        if (auction.getBuyoutPrice() > 0 && bidAmount >= auction.getBuyoutPrice()) {
            // Buyout
            return processBuyout(bidder, auction);
        }
        
        // Check if player has enough money
        if (plugin.getEconomy().getBalance(bidder) < bidAmount) {
            bidder.sendMessage("§cYou don't have enough money!");
            return false;
        }
        
        // Refund previous bidder if exists
        if (auction.getCurrentBidder() != null) {
            Player previousBidder = Bukkit.getPlayer(auction.getCurrentBidder());
            if (previousBidder != null) {
                plugin.getEconomy().depositPlayer(previousBidder, auction.getCurrentBid());
                previousBidder.sendMessage("§aYou have been outbid! Your bid has been refunded.");
            }
        }
        
        // Place new bid
        plugin.getEconomy().withdrawPlayer(bidder, bidAmount);
        auction.setCurrentBid(bidAmount);
        auction.setCurrentBidder(bidder.getUniqueId());
        
        // Track player's bids
        playerBids.computeIfAbsent(bidder.getUniqueId(), k -> new ArrayList<>()).add(auctionId);
        
        // Notify seller
        Player seller = Bukkit.getPlayer(auction.getSellerId());
        if (seller != null) {
            seller.sendMessage("§aSomeone bid $" + bidAmount + " on your auction!");
        }
        
        bidder.sendMessage("§aBid placed successfully! Amount: $" + bidAmount);
        saveAuctions();
        return true;
    }
    
    private boolean processBuyout(Player buyer, AuctionItem auction) {
        if (plugin.getEconomy().getBalance(buyer) < auction.getBuyoutPrice()) {
            buyer.sendMessage("§cYou don't have enough money for the buyout!");
            return false;
        }
        
        // Process buyout
        plugin.getEconomy().withdrawPlayer(buyer, auction.getBuyoutPrice());
        plugin.getEconomy().depositPlayer(Bukkit.getOfflinePlayer(auction.getSellerId()), auction.getBuyoutPrice());
        
        // Give item to buyer
        buyer.getInventory().addItem(auction.getItem());
        
        // Remove auction
        activeAuctions.remove(auction.getAuctionId());
        playerAuctions.get(auction.getSellerId()).remove(auction.getAuctionId());
        
        // Notify seller
        Player seller = Bukkit.getPlayer(auction.getSellerId());
        if (seller != null) {
            seller.sendMessage("§aYour auction was bought out for $" + auction.getBuyoutPrice() + "!");
        }
        
        buyer.sendMessage("§aYou bought out the auction for $" + auction.getBuyoutPrice() + "!");
        saveAuctions();
        return true;
    }
    
    public void cancelAuction(Player seller, UUID auctionId) {
        AuctionItem auction = activeAuctions.get(auctionId);
        if (auction == null) {
            seller.sendMessage("§cAuction not found!");
            return;
        }
        
        if (!auction.getSellerId().equals(seller.getUniqueId())) {
            seller.sendMessage("§cYou can only cancel your own auctions!");
            return;
        }
        
        // Refund current bidder if exists
        if (auction.getCurrentBidder() != null) {
            Player bidder = Bukkit.getPlayer(auction.getCurrentBidder());
            if (bidder != null) {
                plugin.getEconomy().depositPlayer(bidder, auction.getCurrentBid());
                bidder.sendMessage("§aAn auction you bid on has been cancelled. Your bid has been refunded.");
            }
        }
        
        // Return item to seller
        seller.getInventory().addItem(auction.getItem());
        
        // Remove auction
        activeAuctions.remove(auctionId);
        playerAuctions.get(seller.getUniqueId()).remove(auctionId);
        
        seller.sendMessage("§aAuction cancelled successfully!");
        saveAuctions();
    }
    
    private void scheduleAuctionExpiration(UUID auctionId, int durationHours) {
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            AuctionItem auction = activeAuctions.get(auctionId);
            if (auction != null && !auction.isExpired()) {
                expireAuction(auction);
            }
        }, durationHours * 60 * 60 * 20L); // Convert to ticks
    }
    
    private void expireAuction(AuctionItem auction) {
        if (auction.getCurrentBidder() != null) {
            // Auction was won
            Player winner = Bukkit.getPlayer(auction.getCurrentBidder());
            Player seller = Bukkit.getPlayer(auction.getSellerId());
            
            if (winner != null) {
                winner.getInventory().addItem(auction.getItem());
                winner.sendMessage("§aYou won an auction! Item delivered to your inventory.");
            }
            
            if (seller != null) {
                plugin.getEconomy().depositPlayer(seller, auction.getCurrentBid());
                seller.sendMessage("§aYour auction ended! You received $" + auction.getCurrentBid());
            }
        } else {
            // No bids, return item to seller
            Player seller = Bukkit.getPlayer(auction.getSellerId());
            if (seller != null) {
                seller.getInventory().addItem(auction.getItem());
                seller.sendMessage("§aYour auction expired with no bids. Item returned to your inventory.");
            }
        }
        
        // Remove auction
        activeAuctions.remove(auction.getAuctionId());
        playerAuctions.get(auction.getSellerId()).remove(auction.getAuctionId());
        saveAuctions();
    }
    
    public List<AuctionItem> getActiveAuctions() {
        return new ArrayList<>(activeAuctions.values());
    }
    
    public List<AuctionItem> getPlayerAuctions(UUID playerId) {
        List<UUID> auctionIds = playerAuctions.get(playerId);
        if (auctionIds == null) return new ArrayList<>();
        
        List<AuctionItem> auctions = new ArrayList<>();
        for (UUID auctionId : auctionIds) {
            AuctionItem auction = activeAuctions.get(auctionId);
            if (auction != null) {
                auctions.add(auction);
            }
        }
        return auctions;
    }
    
    public List<AuctionItem> getPlayerBids(UUID playerId) {
        List<UUID> auctionIds = playerBids.get(playerId);
        if (auctionIds == null) return new ArrayList<>();
        
        List<AuctionItem> auctions = new ArrayList<>();
        for (UUID auctionId : auctionIds) {
            AuctionItem auction = activeAuctions.get(auctionId);
            if (auction != null && auction.getCurrentBidder().equals(playerId)) {
                auctions.add(auction);
            }
        }
        return auctions;
    }
    
    public AuctionItem getAuction(UUID auctionId) {
        return activeAuctions.get(auctionId);
    }
    
    private void loadAuctions() {
        auctionFile = new File(plugin.getDataFolder(), "auctions.yml");
        if (!auctionFile.exists()) {
            plugin.saveResource("auctions.yml", false);
        }
        
        auctionConfig = YamlConfiguration.loadConfiguration(auctionFile);
        ConfigurationSection auctionsSection = auctionConfig.getConfigurationSection("auctions");
        
        if (auctionsSection != null) {
            for (String auctionIdStr : auctionsSection.getKeys(false)) {
                ConfigurationSection auctionSection = auctionsSection.getConfigurationSection(auctionIdStr);
                if (auctionSection != null) {
                    AuctionItem auction = AuctionItem.fromConfig(auctionSection);
                    if (auction != null && !auction.isExpired()) {
                        UUID auctionId = UUID.fromString(auctionIdStr);
                        activeAuctions.put(auctionId, auction);
                        
                        // Track player auctions
                        playerAuctions.computeIfAbsent(auction.getSellerId(), k -> new ArrayList<>()).add(auctionId);
                        
                        // Track player bids
                        if (auction.getCurrentBidder() != null) {
                            playerBids.computeIfAbsent(auction.getCurrentBidder(), k -> new ArrayList<>()).add(auctionId);
                        }
                    }
                }
            }
        }
    }
    
    private void saveAuctions() {
        auctionConfig.set("auctions", null);
        
        ConfigurationSection auctionsSection = auctionConfig.createSection("auctions");
        for (Map.Entry<UUID, AuctionItem> entry : activeAuctions.entrySet()) {
            ConfigurationSection auctionSection = auctionsSection.createSection(entry.getKey().toString());
            entry.getValue().saveToConfig(auctionSection);
        }
        
        try {
            auctionConfig.save(auctionFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save auctions: " + e.getMessage());
        }
    }
    
    public void cleanupExpiredAuctions() {
        Iterator<Map.Entry<UUID, AuctionItem>> iterator = activeAuctions.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, AuctionItem> entry = iterator.next();
            AuctionItem auction = entry.getValue();
            
            if (auction.isExpired()) {
                expireAuction(auction);
                iterator.remove();
            }
        }
    }
}
