# Economy Plugin

A custom Bukkit plugin for Paper Minecraft servers that provides an economy system with jobs, achievement rewards, and playtime rewards. This plugin is designed to be compatible with Essentials, Vault, and QuickShop-Hikari.

## Features

### 🏢 Job System
- **Multiple Job Types**: Mining, Farming, Hunting, Fishing, Crafting, Building, and Exploration
- **Item Requirements**: Jobs require specific items to complete
- **Cooldown System**: Jobs have cooldowns to prevent abuse
- **Automatic Rewards**: Players earn money upon job completion
- **Job Management**: Players can join, quit, and view job information

### 🏆 Achievement Rewards
- **Automatic Detection**: Automatically detects when players complete Minecraft achievements
- **Configurable Rewards**: Customizable reward amounts for each achievement
- **Multiple Categories**: Story Mode, Nether, End, Adventure, and Husbandry achievements
- **Instant Payout**: Rewards are given immediately upon achievement completion

### ⏰ Playtime Rewards
- **Periodic Rewards**: Players earn money at regular intervals while playing
- **Tier System**: Higher playtime unlocks higher reward tiers
- **Progressive Scaling**: Rewards increase with each tier
- **Automatic Tracking**: Tracks player join/quit times automatically

### 💰 Economy Integration
- **Vault Compatibility**: Works with any economy plugin that supports Vault
- **Essentials Support**: Compatible with Essentials economy
- **QuickShop Integration**: Works alongside QuickShop-Hikari

### 🏪 Auction House
- **Item Auctions**: Players can auction items with starting prices and optional buyout prices
- **Bidding System**: Real-time bidding with automatic refunds for outbid players
- **GUI Interface**: User-friendly graphical interface for browsing and managing auctions
- **Time Management**: Configurable auction durations with automatic expiration
- **Player Management**: View your own auctions and bids
- **Notifications**: Get notified when outbid or when someone bids on your auctions

## Dependencies

### Required
- **Paper/Spigot 1.20+** - Server software
- **Vault** - Economy API

### Optional (Soft Dependencies)
- **Essentials** - For additional economy features
- **QuickShop-Hikari** - For shop integration

## Installation

1. **Download the Plugin**
   - Download the latest JAR file from releases
   - Place it in your server's `plugins` folder

2. **Install Dependencies**
   - Install Vault plugin
   - Install an economy plugin (like EssentialsX)
   - Restart your server

3. **Configure the Plugin**
   - Edit `plugins/EconomyPlugin/config.yml` to customize settings
   - Restart your server again

4. **Verify Installation**
   - Check console for successful plugin loading
   - Test commands in-game

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

## Configuration

### Jobs
Jobs are configured in `config.yml` under the `jobs` section. You can override default jobs or add custom ones:

```yaml
jobs:
  custom_miner:
    description: "Advanced mining job"
    type: "MINING"
    reward: 200.0
    cooldown: 600
    requirements:
      DIAMOND_ORE: 3
      EMERALD_ORE: 2
```

### Achievement Rewards
Configure achievement rewards in the `achievement-rewards` section:

```yaml
achievement-rewards:
  minecraft:story/defeat_enderdragon: 1000.0
  minecraft:nether/uneasy_alliance: 500.0
```

### Playtime Rewards
Adjust playtime reward settings:

```yaml
playtime:
  reward-interval: 300  # 5 minutes
  base-reward: 10.0     # Base reward amount
  tier-multiplier: 1.5  # Multiplier per tier
```

### Auction House
Configure auction house settings:

```yaml
auction:
  max-duration: 168      # Maximum auction duration (hours)
  min-duration: 1         # Minimum auction duration (hours)
  max-auctions-per-player: 10  # Max auctions per player
  allow-buyout: true      # Allow buyout prices
  min-bid-increment: 1.0  # Minimum bid increase
  fee-percentage: 5.0     # Auction house fee (%)
  notify-outbid: true     # Notify when outbid
  notify-seller: true      # Notify seller of bids
```

## Default Jobs

The plugin comes with several pre-configured jobs:

- **Miner**: Collect ores (Diamond, Iron, Coal)
- **Farmer**: Grow crops (Wheat, Carrots, Potatoes)
- **Hunter**: Hunt animals (Beef, Porkchop, Chicken)
- **Fisher**: Catch fish (Cod, Salmon, Tropical Fish)

## Achievement Categories

- **Story Mode**: Basic progression achievements
- **Nether**: Nether dimension achievements
- **The End**: End dimension achievements
- **Adventure**: Exploration and combat achievements
- **Husbandry**: Animal and farming achievements

## Playtime Tiers

Players progress through tiers based on total playtime:

- **Tier 1**: $10.00 per interval (0-2 hours)
- **Tier 2**: $15.00 per interval (2-4 hours)
- **Tier 3**: $22.50 per interval (4-6 hours)
- **Tier 4**: $33.75 per interval (6-8 hours)
- And so on...

## Auction House Usage

### Creating an Auction
1. Hold the item you want to auction in your hand
2. Use `/auction create <starting_price> <duration_hours> [buyout_price]`
3. The item will be removed from your inventory and listed for auction

### Bidding on Auctions
1. Use `/auction` to open the auction house GUI
2. Browse available auctions
3. Click on an auction to view details
4. Click "Place Bid" to bid on the item
5. Choose from preset bid amounts or click "Custom Bid" to enter your own amount
6. If using custom bid, type your bid amount in chat when prompted

### Managing Your Auctions
- Use `/auction my auctions` to view your active auctions
- Use `/auction my bids` to view auctions you've bid on
- Auctions automatically expire and process when time runs out

### Custom Bidding
- Click the "Custom Bid" button in the bid menu to enter your own bid amount
- Type your bid amount in chat when prompted
- Type "cancel" to cancel the custom bid
- Your bid must be higher than the current bid and within your available balance

## Permissions

- `economyplugin.job` - Access to job commands
- `economyplugin.jobs` - View available jobs
- `economyplugin.jobinfo` - View job information
- `economyplugin.playtime` - Check playtime
- `economyplugin.achievement` - View achievements
- `economyplugin.auction` - Access to auction house features
- `economyplugin.admin` - Admin commands

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

## Troubleshooting

### Common Issues

1. **Plugin won't start**
   - Check that Vault is installed
   - Verify server version compatibility
   - Check console for error messages

2. **Economy not working**
   - Ensure Vault and an economy plugin are installed
   - Check that the economy plugin is properly configured

3. **Jobs not working**
   - Verify job configuration in config.yml
   - Check that required items exist in the game

4. **Achievements not giving rewards**
   - Ensure the achievement names match exactly
   - Check that the player has the required permissions

### Debug Mode

Enable debug mode in `config.yml`:

```yaml
general:
  debug: true
```

This will provide additional logging information in the console.

## Support

For support and bug reports:
- Check the console for error messages
- Verify your configuration
- Ensure all dependencies are properly installed
- Check that you're using a compatible server version

## License

This plugin is provided as-is for educational and server use purposes.

## Contributing

Contributions are welcome! Please feel free to submit issues, feature requests, or pull requests.

---

**Note**: This plugin is designed for Paper/Spigot 1.20+ servers. Make sure your server meets the minimum requirements before installation.





