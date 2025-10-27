# Monster Hider

A RuneLite plugin that allows you to hide specific monsters by name using a configurable keybind toggle.

## Features

- **Keybind Toggle**: Quickly hide/show monsters with a single keypress (default: H key)
- **Name-based Filtering**: Hide monsters by their display name (supports partial matching)
- **Visual Feedback**: In-game chat messages confirm when hiding is enabled/disabled
- **Auto-hide Spawns**: Newly spawned monsters are automatically hidden when toggle is active
- **Default Visible**: All monsters are shown by default when the client loads

## Configuration

### Toggle Keybind
Set your preferred key or key combination to toggle monster hiding on/off.

### NPC Names
Enter a comma-separated list of monster names to hide. Examples:
- `Goblin` - Hides all goblins
- `Cow, Chicken, Rat` - Hides multiple monster types
- `Guard` - Hides all guards (case-insensitive, partial matching)

## Usage

1. Configure your monster name list in the plugin settings
2. Press your configured keybind (default: H) to toggle hiding
3. Monsters in your list will be hidden from view
4. Press the keybind again to show all monsters
5. The list can be updated at any time in the plugin configuration

## Use Cases

- **Training**: Hide distracting monsters while training on specific NPCs
- **PvM**: Reduce visual clutter in multi-NPC areas
- **Slayer**: Focus on your task monsters by hiding others
- **Skilling**: Hide aggressive monsters while skilling in dangerous areas