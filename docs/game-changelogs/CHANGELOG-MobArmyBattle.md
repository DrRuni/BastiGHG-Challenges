# MobArmyBattle Changelog

## [1.7.1] - Bug Fix Release

### Fixed
- Fixed a major issue with incorrect Cube kill counting
- Cube kills are now correctly tracked and displayed on the scoreboard
- Improved internal kill handling and scoreboard updates

## [1.7.0]

### Added
- German and English language support
- Language selection via `/language`

### Changed
- Reworked scoreboards and removed red score numbers
- Improved texts, titles, and GUIs
- Cleaned up and modernized internal code

### Fixed
- Fixed arena reset issues during active events
- Fixed minor placeholder and display issues

## [1.6.0] - Stable

### Added
- Arena selection in the arena settings
- New Ancient City arena

### Changed
- Reworked arena scoreboard
- Arena reset now teleports players back to wave selection
- Improved spawn configuration and arena spawn points
- Updated world structure for Paper/Minecraft 26.x
- Changed the internal ready system from players to teams
- Disabled wave restart functionality

### Fixed
- Fixed arena scoreboard functionality

## [1.5.0]

### Added
- Chest Randomizer
- Spawn order now follows the click order
- Template file system

### Changed
- Improved start countdown and game initialization
- Redesigned arena and lobby
- Disabled mob spawning in lobby and arena

## [1.4.0]

### Added
- Arena world reset
- Offline player support for team scoreboards
- New reset commands:
    - `/reset arena`
    - `/reset lobby`
    - `/reset teamworld`
    - `/reset playerdata`

### Changed
- Restructured file management
- Improved in-game messages and console output
- Improved internal project structure

## [1.3.0]

### Added
- Separate arena world independent of the lobby
- Foundation for additional arenas
- Team Equipment GUI for event equipment

### Changed
- Reworked lobby and arena world management
- Improved teleportation and respawn handling
- Updated Resume functionality for separate worlds
- Improved sound handling

### Fixed
- Fixed sound-related issues
- Fixed teleportation issues between lobby and arena
- Fixed Resume and respawn issues
- Various minor bug fixes

## [1.2.0]

### Added
- WorldSettings system
- Extended arena compass options

### Changed
- Improved arena reset functionality
- Improved GUIs and internal code organization
- Updated compatibility for Paper/Minecraft 26.1

### Fixed
- Fixed various game flow and settings issues

## [1.1.0]

### Added
- Welcome message
- Additional breakable items in the arena

### Changed
- Improved KeepInventory handling during world regeneration
- Reworked spawn points
- Extended Resume functionality
- Improved game reset system
- Introduced more stable scoreboard logic
- Improved console logging

### Fixed
- Fixed Nether portal generation
- Various minor bug fixes