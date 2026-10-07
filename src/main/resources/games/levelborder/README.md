# LevelBorder

LevelBorder is a Minecraft challenge game integrated into **BastiGHG Challenges – Fan Project**.

The challenge starts with a very small WorldBorder. The border grows based on the combined experience levels of all players. The more levels the players collect, the larger the playable area becomes.

> This game is part of an unofficial fan project and is not affiliated with BastiGHG.

## Current Version

`v1.1 Stable`

## Features

- WorldBorder grows based on the combined player levels
- Shared progression for all players
- Multiplayer support
- Overworld, Nether and End support
- 1:1 Nether border handling
- Integrated into the central challenge lobby
- Separate world and player data
- Persistent game progress
- Central world settings
- Game settings menu
- Reset voting system
- Complete world reset support
- Custom mob spawning support for small border areas

## Commands

```text
/levelborder
/levelborder info
/levelborder status
/levelborder start
/levelborder stop
/levelborder set <size>
/levelborder center
/levelborder gamesettings
/levelborder reset
/levelborder reset world

## Changelog

### v1.1

#### Added

- Updated to Paper 26.1.2
- Improved mob spawning outside the border
- Adjusted vanilla spawning behavior so mobs can spawn reliably even in small border areas
- Added an additional spawn fix for the area where Minecraft normally prevents mobs from spawning too close to players

### v1.0

#### Added

- Initial release of LevelBorder
- Support for Minecraft 1.21.10