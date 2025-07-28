# AutoPlacePatch [![Spigot Version](https://img.shields.io/badge/Spigot-1.8.8-orange.svg)](https://www.spigotmc.org/)
A lightweight Spigot plugin that detects and optionally prevents AutoPlace hacks in Minecraft 1.8.8. Originally developed for private use, now available to the public.

## Features
- Optimized detection of AutoPlace hacks
- Customizable staff alerts
- Configurable punishment system
- Minimal performance impact
- Written in Kotlin for modern development practices

## Current Bypasses
- AutoPlacing with slabs

## Installation
1. Download the latest release from the [releases page](https://github.com/quadflame/AutoPlacePatch/releases)
2. Place the `.jar` file in your server's `plugins` folder
3. Restart your server
4. The config file will be generated automatically

## Configuration
```yaml
patch:
  cancel: false

alerts:
  enabled: true
  message: "&c%player% has been flagged for autoplace!"
  permission: "autoplacepatch.alerts"

punishments:
  enabled: false
  command: "ban %player% 30d AutoPlace"
```

### Configuration Options
| Option              | Description                          | Default                                      |
|---------------------|--------------------------------------|----------------------------------------------|
| patch.cancel        | Cancel invalid block placements      | false                                        |
| alerts.enabled      | Toggle staff notifications           | true                                         |
| alerts.message      | Alert message with placeholders      | "&c%player% has been flagged for autoplace!" |
| alerts.permission   | Permission node for receiving alerts | "autoplacepatch.alerts"                      |
| punishments.enabled | Enable automatic punishments         | false                                        |
| punishments.command | Punishment command with placeholders | "ban %player% 30d AutoPlace"                 |

### Placeholders
- `%player%` - Player's name
- `%uuid%` - Player's UUID

## Support
If you encounter any issues or have suggestions, please:
1. Check the [known issues](https://github.com/quadflame/AutoPlacePatch/issues)
2. Create a new [issue](https://github.com/quadflame/AutoPlacePatch/issues/new)

## Contributing
1. Fork the repository
2. Create a new branch
3. Make your changes
4. Submit a pull request
