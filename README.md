# Economy Plugin

A custom Bukkit plugin I made for my personal server, runs for paper servers specifically designed for version 1.21.8

## Features

### 🏢 Job System
### 🏆 Achievement Rewards
### ⏰ Playtime Rewards
### 💰 Economy Integration
### 🏪 Auction House

## Dependencies

### Required
- **Paper/Spigot 1.20+** - Server software
- **Vault** - Economy API

### Optional (Soft Dependencies)
- **Essentials** - For additional economy features
- **QuickShop-Hikari** - For shop integration

## Commands

### Job Commands
- `/job` - View current job information
- `/job info` - Show detailed job info
- `/job list` - List available jobs
- `/job join <jobname>` - Join a specific job
- `/job quit` - Quit current job
- `/job complete` - Complete current job

### Information Commands
- `/jobs` - List all available jobs
- `/jobinfo [jobname]` - Get detailed information about a job
- `/playtime` - Check your playtime and rewards
- `/achievement` - View achievement rewards
- `/achievement list` - List all achievements
- `/achievement <name>` - Get specific achievement info

### Auction House Commands
- `/auction` - Open the auction house GUI
- `/auction create <price> <hours> [buyout]` - Create an auction (hold item)
- `/auction list` - Browse all auctions
- `/auction my auctions` - View your auctions
- `/auction my bids` - View your bids
- `/auction help` - Show auction help

## Building from Source

1. **Clone the Repository**
   ```bash
   git clone <repository-url>
   cd economy-plugin
   ```

2. **Build with Maven**
   ```bash
   mvn clean package
   ```

3. **Install the Plugin**
   - Copy the generated JAR from `target/` to your server's `plugins/` folder
