# LevelBlock Changelog

## [0.9.2] - Beta Update

### Changed
- Reworked the reset system and integrated it with the central reset management
- Changed the reset command to `/reset gamedata`
- Improved reset menus, buttons, icons, descriptions, and confirmation dialogs
- Revised chat and status messages for reset operations and world regeneration
- Improved coordination between player data and game data resets

### Fixed
- **Teleportation:** Fixed duplicate teleportation when leaving LevelBlock and returning to the central lobby
- **Respawn:** Fixed issues with respawn anchors in the Nether
- **Lobby Logo:** Fixed unwanted logo displays when switching between the Overworld and Nether

### Notes
- Improved reliability of player and world resets

## [0.9.1] - Beta Bug Fix Release

### Changed
- Updated to Paper/Minecraft 26.3

### Fixed
- Fixed full reset not executing the game-specific reset functionality
- Improved reset handling and game state synchronization

## [0.9.0] - Beta

### Added
- Multiworld support for Overworld, Nether, and End
- LevelBlock progression in Nether and End
- Custom Nether portal handling
- Portal return to previously unlocked Overworld areas

### Changed
- Improved border updates after portal travel and height changes
- Improved multiworld data management

### Fixed
- Fixed portals incorrectly sending players to the vanilla Nether
- Fixed border display issues after dimension changes

## [0.8.0]

### Added
- Integration into BastiGHG Challenges – Fan Project
- Game selection and loading through the central lobby
- Game settings menu
- Reset voting system
- Persistent game progress