package com.economyplugin.jobs;

public enum JobType {
    MINING("Mining"),
    FARMING("Farming"),
    HUNTING("Hunting"),
    FISHING("Fishing"),
    CRAFTING("Crafting"),
    BUILDING("Building"),
    EXPLORATION("Exploration");
    
    private final String displayName;
    
    JobType(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}




