# LevelBorder Changelog

## [1.2.2] - Update Release

### Changed
- Reworked the reset system and integrated it with the central reset management
- Changed the reset command to `/reset gamedata`
- Improved reset menus, buttons, icons, descriptions, and confirmation dialogs
- Revised chat and status messages for reset operations and world regeneration
- Improved coordination between player data, game data, and world border resets

### Fixed
- **Portal Registration:** Nether portals are no longer registered if no valid portal is created when attempting to ignite one
- **Respawn:** Fixed issues with respawn anchors in the Nether
- **Lobby Logo:** Fixed unwanted logo displays when switching between the Overworld and Nether

### Notes
- Improved reliability of player and world resets

## [1.2.1] - Bug Fix Release

### Changed
- Updated to Paper/Minecraft 26.3

### Changed
- Scoreboard is now hidden by default when starting the challenge
- Added an INFO notification to show the scoreboard

### Fixed
- Fixed WorldBorder expansion not updating smoothly
- Fixed missing players in the scoreboard when starting the challenge
- Fixed incorrect starting border size (1×1 instead of 3×3) after a reset
- Fixed translation errors in reset voting
- Fixed full reset not executing the game-specific reset functionality

## [1.2.0]

### Added
- New options GUI accessible via `/optionen` or `/levelborder optionen`
- OP-only access to challenge settings
- KeepInventory toggle
- Start, stop, reset, and border center controls
- Border size adjustment using left-click and right-click

### Changed
- Updated Paper API to 26.2
- Significantly improved Nether portal handling
- Improved reset and start flow
- Border center now updates immediately
- Minor code cleanup and API adjustments

### Fixed
- Fixed scoreboard player list

## [1.1.1]

### Changed
- Cleaned up internal code
- Reduced API warnings

## [1.1.0]

### Added
- Custom mob spawning system
- Configurable mob spawning settings
- Border-aware mob spawning
- Support for spawning mobs outside the challenge border

### Changed
- Updated Paper API to 26.1.2
- Updated to Java 25
- Improved mob spawning for small borders

### Notes
- Custom mob spawning compensates for vanilla spawning restrictions outside the WorldBorder

## [1.0.0] - Initial Release

### Added
- First release of LevelBorder
- Support for Paper 1.21.10