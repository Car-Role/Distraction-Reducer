# Distraction Reducer Plugin

## Overview
The Distraction Reducer plugin for RuneLite helps players focus on their skilling activities by reducing visual distractions. When enabled, it overlays a customizable black screen while you're engaged in specific skilling actions, allowing you to concentrate on your task without being distracted by the game's visuals.

## Support the Developer
If you find this plugin helpful and would like to support its development, you can buy the developer a coffee on Ko-fi:

[Support Car_role on Ko-fi](https://ko-fi.com/car_role)

Your support is greatly appreciated and helps maintain and improve the plugin!

## Features
- Supports multiple skilling activities:
  - Woodcutting
  - Fishing
  - Mining
  - Cooking
  - Herblore
  - Crafting
  - Fletching
  - Smithing
- Customizable overlay color and opacity
- Individual toggles for each supported skill
- Automatic detection of skilling activities
- Brief delay before activation to prevent flickering during short pauses

**Note:** Shooting stars are not yet supported by this plugin.

## Configuration
The plugin offers several configuration options:

### Skilling Toggles
Enable or disable the overlay for each supported skill:
- Woodcutting
- Fishing
- Mining
- Cooking
- Herblore
- Crafting
- Fletching
- Smithing

### Color Picker
- **Overlay Color**: Customize the color and opacity of the overlay

## How It Works
1. The plugin detects when you're performing a supported skilling activity by monitoring your character's animation.
2. If the corresponding skill toggle is enabled, the overlay will activate after a short delay (to prevent flickering during brief pauses).
3. The overlay will disappear shortly after you stop the skilling activity or become idle for more than 2 game ticks.

## Installation
1. Ensure you have RuneLite installed and updated to the latest version.
2. Open the Plugin Hub within RuneLite.
3. Search for "Distraction Reducer" and click "Install".

## Usage
1. Enable the plugin in RuneLite's plugin list.
2. Configure the settings to your preference.
3. Start skilling, and the overlay will automatically activate when appropriate.

## Support
If you encounter any issues or have suggestions for improvements, please open an issue on the GitHub repository or contact the plugin maintainer through the RuneLite Discord.

## Contributing
Contributions are welcome! If you'd like to contribute to the Distraction Reducer plugin, please fork the repository, make your changes, and submit a pull request.

## License
This plugin is released under the [BSD 2-Clause License](https://opensource.org/licenses/BSD-2-Clause).
