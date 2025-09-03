package com.economyplugin.auction;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class AuctionItem {
    
    private final UUID auctionId;
    private final UUID sellerId;
    private final ItemStack item;
    private final double startingPrice;
    private final double buyoutPrice;
    private final long startTime;
    private final long endTime;
    private double currentBid;
    private UUID currentBidder;
    
    public AuctionItem(UUID auctionId, UUID sellerId, ItemStack item, double startingPrice, 
                       double buyoutPrice, long startTime, long endTime) {
        this.auctionId = auctionId;
        this.sellerId = sellerId;
        this.item = item;
        this.startingPrice = startingPrice;
        this.buyoutPrice = buyoutPrice;
        this.startTime = startTime;
        this.endTime = endTime;
        this.currentBid = startingPrice;
        this.currentBidder = null;
    }
    
    public AuctionItem(UUID auctionId, UUID sellerId, ItemStack item, double startingPrice, 
                       double buyoutPrice, long startTime, long endTime, double currentBid, UUID currentBidder) {
        this.auctionId = auctionId;
        this.sellerId = sellerId;
        this.item = item;
        this.startingPrice = startingPrice;
        this.buyoutPrice = buyoutPrice;
        this.startTime = startTime;
        this.endTime = endTime;
        this.currentBid = currentBid;
        this.currentBidder = currentBidder;
    }
    
    public UUID getAuctionId() {
        return auctionId;
    }
    
    public UUID getSellerId() {
        return sellerId;
    }
    
    public ItemStack getItem() {
        return item;
    }
    
    public double getStartingPrice() {
        return startingPrice;
    }
    
    public double getBuyoutPrice() {
        return buyoutPrice;
    }
    
    public long getStartTime() {
        return startTime;
    }
    
    public long getEndTime() {
        return endTime;
    }
    
    public double getCurrentBid() {
        return currentBid;
    }
    
    public void setCurrentBid(double currentBid) {
        this.currentBid = currentBid;
    }
    
    public UUID getCurrentBidder() {
        return currentBidder;
    }
    
    public void setCurrentBidder(UUID currentBidder) {
        this.currentBidder = currentBidder;
    }
    
    public boolean isExpired() {
        return System.currentTimeMillis() > endTime;
    }
    
    public long getTimeRemaining() {
        return Math.max(0, endTime - System.currentTimeMillis());
    }
    
    public String getTimeRemainingString() {
        long timeRemaining = getTimeRemaining();
        if (timeRemaining <= 0) {
            return "Expired";
        }
        
        long hours = timeRemaining / (1000 * 60 * 60);
        long minutes = (timeRemaining % (1000 * 60 * 60)) / (1000 * 60);
        long seconds = (timeRemaining % (1000 * 60)) / 1000;
        
        if (hours > 0) {
            return String.format("%dh %dm %ds", hours, minutes, seconds);
        } else if (minutes > 0) {
            return String.format("%dm %ds", minutes, seconds);
        } else {
            return String.format("%ds", seconds);
        }
    }
    
    public String getSellerName() {
        return Bukkit.getOfflinePlayer(sellerId).getName();
    }
    
    public String getCurrentBidderName() {
        if (currentBidder == null) {
            return "None";
        }
        return Bukkit.getOfflinePlayer(currentBidder).getName();
    }
    
    public void saveToConfig(ConfigurationSection section) {
        section.set("auctionId", auctionId.toString());
        section.set("sellerId", sellerId.toString());
        section.set("item", item);
        section.set("startingPrice", startingPrice);
        section.set("buyoutPrice", buyoutPrice);
        section.set("startTime", startTime);
        section.set("endTime", endTime);
        section.set("currentBid", currentBid);
        if (currentBidder != null) {
            section.set("currentBidder", currentBidder.toString());
        }
    }
    
    public static AuctionItem fromConfig(ConfigurationSection section) {
        try {
            UUID auctionId = UUID.fromString(section.getString("auctionId"));
            UUID sellerId = UUID.fromString(section.getString("sellerId"));
            ItemStack item = section.getItemStack("item");
            double startingPrice = section.getDouble("startingPrice");
            double buyoutPrice = section.getDouble("buyoutPrice");
            long startTime = section.getLong("startTime");
            long endTime = section.getLong("endTime");
            double currentBid = section.getDouble("currentBid");
            
            UUID currentBidder = null;
            if (section.contains("currentBidder")) {
                currentBidder = UUID.fromString(section.getString("currentBidder"));
            }
            
            return new AuctionItem(auctionId, sellerId, item, startingPrice, buyoutPrice, 
                                 startTime, endTime, currentBid, currentBidder);
        } catch (Exception e) {
            return null;
        }
    }
}
