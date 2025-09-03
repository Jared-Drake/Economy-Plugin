package com.economyplugin.gui;

import com.economyplugin.EconomyPlugin;
import com.economyplugin.auction.AuctionItem;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AuctionGUI {
    
    private final EconomyPlugin plugin;
    private static final int ROWS = 6;
    private static final int SLOTS_PER_PAGE = 28; // 4 rows * 7 slots (excluding borders)
    
    public AuctionGUI(EconomyPlugin plugin) {
        this.plugin = plugin;
    }
    
    public void openMainMenu(Player player) {
        Inventory gui = Bukkit.createInventory(null, 9, "§8Auction House");
        
        // Browse Auctions
        ItemStack browseItem = createGuiItem(Material.BOOK, "§aBrowse Auctions", 
            "§7Click to view all active auctions");
        gui.setItem(2, browseItem);
        
        // My Auctions
        ItemStack myAuctionsItem = createGuiItem(Material.CHEST, "§eMy Auctions", 
            "§7View and manage your auctions");
        gui.setItem(4, myAuctionsItem);
        
        // My Bids
        ItemStack myBidsItem = createGuiItem(Material.GOLD_INGOT, "§6My Bids", 
            "§7View auctions you've bid on");
        gui.setItem(6, myBidsItem);
        
        player.openInventory(gui);
    }
    
    public void openBrowseAuctions(Player player, int page) {
        List<AuctionItem> auctions = plugin.getAuctionManager().getActiveAuctions();
        int totalPages = (int) Math.ceil((double) auctions.size() / SLOTS_PER_PAGE);
        
        if (page < 0) page = 0;
        if (page >= totalPages) page = totalPages - 1;
        
        Inventory gui = Bukkit.createInventory(null, ROWS * 9, "§8Browse Auctions - Page " + (page + 1));
        
        // Add auction items
        int startIndex = page * SLOTS_PER_PAGE;
        int endIndex = Math.min(startIndex + SLOTS_PER_PAGE, auctions.size());
        
        for (int i = startIndex; i < endIndex; i++) {
            AuctionItem auction = auctions.get(i);
            ItemStack auctionItem = createAuctionItem(auction);
            gui.setItem(i - startIndex + 10, auctionItem); // Start at row 2, slot 1
        }
        
        // Navigation buttons
        if (page > 0) {
            ItemStack prevButton = createGuiItem(Material.ARROW, "§aPrevious Page", "§7Click to go to previous page");
            gui.setItem(45, prevButton);
        }
        
        if (page < totalPages - 1) {
            ItemStack nextButton = createGuiItem(Material.ARROW, "§aNext Page", "§7Click to go to next page");
            gui.setItem(53, nextButton);
        }
        
        // Back button
        ItemStack backButton = createGuiItem(Material.BARRIER, "§cBack to Main Menu", "§7Click to return to main menu");
        gui.setItem(49, backButton);
        
        player.openInventory(gui);
    }
    
    public void openMyAuctions(Player player, int page) {
        List<AuctionItem> auctions = plugin.getAuctionManager().getPlayerAuctions(player.getUniqueId());
        int totalPages = (int) Math.ceil((double) auctions.size() / SLOTS_PER_PAGE);
        
        if (page < 0) page = 0;
        if (page >= totalPages) page = totalPages - 1;
        
        Inventory gui = Bukkit.createInventory(null, ROWS * 9, "§8My Auctions - Page " + (page + 1));
        
        // Add auction items
        int startIndex = page * SLOTS_PER_PAGE;
        int endIndex = Math.min(startIndex + SLOTS_PER_PAGE, auctions.size());
        
        for (int i = startIndex; i < endIndex; i++) {
            AuctionItem auction = auctions.get(i);
            ItemStack auctionItem = createMyAuctionItem(auction);
            gui.setItem(i - startIndex + 10, auctionItem);
        }
        
        // Navigation buttons
        if (page > 0) {
            ItemStack prevButton = createGuiItem(Material.ARROW, "§aPrevious Page", "§7Click to go to previous page");
            gui.setItem(45, prevButton);
        }
        
        if (page < totalPages - 1) {
            ItemStack nextButton = createGuiItem(Material.ARROW, "§aNext Page", "§7Click to go to next page");
            gui.setItem(53, nextButton);
        }
        
        // Back button
        ItemStack backButton = createGuiItem(Material.BARRIER, "§cBack to Main Menu", "§7Click to return to main menu");
        gui.setItem(49, backButton);
        
        player.openInventory(gui);
    }
    
    public void openMyBids(Player player, int page) {
        List<AuctionItem> auctions = plugin.getAuctionManager().getPlayerBids(player.getUniqueId());
        int totalPages = (int) Math.ceil((double) auctions.size() / SLOTS_PER_PAGE);
        
        if (page < 0) page = 0;
        if (page >= totalPages) page = totalPages - 1;
        
        Inventory gui = Bukkit.createInventory(null, ROWS * 9, "§8My Bids - Page " + (page + 1));
        
        // Add auction items
        int startIndex = page * SLOTS_PER_PAGE;
        int endIndex = Math.min(startIndex + SLOTS_PER_PAGE, auctions.size());
        
        for (int i = startIndex; i < endIndex; i++) {
            AuctionItem auction = auctions.get(i);
            ItemStack auctionItem = createMyBidItem(auction);
            gui.setItem(i - startIndex + 10, auctionItem);
        }
        
        // Navigation buttons
        if (page > 0) {
            ItemStack prevButton = createGuiItem(Material.ARROW, "§aPrevious Page", "§7Click to go to previous page");
            gui.setItem(45, prevButton);
        }
        
        if (page < totalPages - 1) {
            ItemStack nextButton = createGuiItem(Material.ARROW, "§aNext Page", "§7Click to go to next page");
            gui.setItem(53, nextButton);
        }
        
        // Back button
        ItemStack backButton = createGuiItem(Material.BARRIER, "§cBack to Main Menu", "§7Click to return to main menu");
        gui.setItem(49, backButton);
        
        player.openInventory(gui);
    }
    
    public void openAuctionDetails(Player player, AuctionItem auction) {
        Inventory gui = Bukkit.createInventory(null, 9, "§8Auction Details");
        
        // Auction item
        ItemStack item = auction.getItem().clone();
        ItemMeta meta = item.getItemMeta();
        List<String> lore = meta.getLore();
        if (lore == null) lore = new ArrayList<>();
        
        lore.add("");
        lore.add("§7Seller: §f" + auction.getSellerName());
        lore.add("§7Starting Price: §a$" + auction.getStartingPrice());
        if (auction.getBuyoutPrice() > 0) {
            lore.add("§7Buyout Price: §6$" + auction.getBuyoutPrice());
        }
        lore.add("§7Current Bid: §e$" + auction.getCurrentBid());
        lore.add("§7Current Bidder: §f" + auction.getCurrentBidderName());
        lore.add("§7Time Remaining: §c" + auction.getTimeRemainingString());
        
        meta.setLore(lore);
        item.setItemMeta(meta);
        gui.setItem(4, item);
        
        // Bid button
        ItemStack bidButton = createGuiItem(Material.GOLD_INGOT, "§aPlace Bid", "§7Click to place a bid");
        gui.setItem(6, bidButton);
        
        // Back button
        ItemStack backButton = createGuiItem(Material.BARRIER, "§cBack", "§7Click to go back");
        gui.setItem(2, backButton);
        
        player.openInventory(gui);
    }
    
    public void openBidMenu(Player player, AuctionItem auction) {
        Inventory gui = Bukkit.createInventory(null, 9, "§8Place Bid");
        
        // Current auction info
        ItemStack infoItem = createGuiItem(Material.PAPER, "§eAuction Info", 
            "§7Item: " + auction.getItem().getType().name(),
            "§7Current Bid: §e$" + auction.getCurrentBid(),
            "§7Your Balance: §a$" + plugin.getEconomy().getBalance(player));
        gui.setItem(4, infoItem);
        
        // Bid amount buttons
        double currentBid = auction.getCurrentBid();
        double[] bidAmounts = {
            currentBid + 1,
            currentBid + 5,
            currentBid + 10,
            currentBid + 50,
            currentBid + 100
        };
        
        gui.setItem(1, createGuiItem(Material.GOLD_NUGGET, "§aBid $" + bidAmounts[0], "§7Click to bid $" + bidAmounts[0]));
        gui.setItem(2, createGuiItem(Material.GOLD_NUGGET, "§aBid $" + bidAmounts[1], "§7Click to bid $" + bidAmounts[1]));
        gui.setItem(3, createGuiItem(Material.GOLD_NUGGET, "§aBid $" + bidAmounts[2], "§7Click to bid $" + bidAmounts[2]));
        gui.setItem(5, createGuiItem(Material.GOLD_INGOT, "§aBid $" + bidAmounts[3], "§7Click to bid $" + bidAmounts[3]));
        gui.setItem(6, createGuiItem(Material.GOLD_BLOCK, "§aBid $" + bidAmounts[4], "§7Click to bid $" + bidAmounts[4]));
        
        // Custom bid button
        ItemStack customBidButton = createGuiItem(Material.BOOK, "§6Custom Bid", 
            "§7Click to enter a custom bid amount",
            "§7Minimum bid: §e$" + (currentBid + 1));
        gui.setItem(7, customBidButton);
        
        // Buyout button (if available)
        if (auction.getBuyoutPrice() > 0) {
            ItemStack buyoutButton = createGuiItem(Material.DIAMOND, "§6Buyout $" + auction.getBuyoutPrice(), 
                "§7Click to buyout immediately");
            gui.setItem(8, buyoutButton);
        }
        
        // Back button
        ItemStack backButton = createGuiItem(Material.BARRIER, "§cBack", "§7Click to go back");
        gui.setItem(0, backButton);
        
        player.openInventory(gui);
    }
    
    private ItemStack createAuctionItem(AuctionItem auction) {
        ItemStack item = auction.getItem().clone();
        ItemMeta meta = item.getItemMeta();
        List<String> lore = meta.getLore();
        if (lore == null) lore = new ArrayList<>();
        
        lore.add("");
        lore.add("§7Seller: §f" + auction.getSellerName());
        lore.add("§7Starting Price: §a$" + auction.getStartingPrice());
        if (auction.getBuyoutPrice() > 0) {
            lore.add("§7Buyout Price: §6$" + auction.getBuyoutPrice());
        }
        lore.add("§7Current Bid: §e$" + auction.getCurrentBid());
        lore.add("§7Time Remaining: §c" + auction.getTimeRemainingString());
        lore.add("");
        lore.add("§aClick to view details");
        
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }
    
    private ItemStack createMyAuctionItem(AuctionItem auction) {
        ItemStack item = auction.getItem().clone();
        ItemMeta meta = item.getItemMeta();
        List<String> lore = meta.getLore();
        if (lore == null) lore = new ArrayList<>();
        
        lore.add("");
        lore.add("§7Starting Price: §a$" + auction.getStartingPrice());
        if (auction.getBuyoutPrice() > 0) {
            lore.add("§7Buyout Price: §6$" + auction.getBuyoutPrice());
        }
        lore.add("§7Current Bid: §e$" + auction.getCurrentBid());
        lore.add("§7Current Bidder: §f" + auction.getCurrentBidderName());
        lore.add("§7Time Remaining: §c" + auction.getTimeRemainingString());
        lore.add("");
        lore.add("§aClick to view details");
        lore.add("§cRight-click to cancel");
        
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }
    
    private ItemStack createMyBidItem(AuctionItem auction) {
        ItemStack item = auction.getItem().clone();
        ItemMeta meta = item.getItemMeta();
        List<String> lore = meta.getLore();
        if (lore == null) lore = new ArrayList<>();
        
        lore.add("");
        lore.add("§7Seller: §f" + auction.getSellerName());
        lore.add("§7Your Bid: §e$" + auction.getCurrentBid());
        lore.add("§7Time Remaining: §c" + auction.getTimeRemainingString());
        lore.add("");
        lore.add("§aClick to view details");
        lore.add("§6Click to increase bid");
        
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }
    
    private ItemStack createGuiItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        
        List<String> loreList = new ArrayList<>();
        for (String line : lore) {
            loreList.add(line);
        }
        meta.setLore(loreList);
        
        item.setItemMeta(meta);
        return item;
    }
}
