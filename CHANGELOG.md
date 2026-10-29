# Changelog

## Challenge Changelogs

- ⚔️ [MobArmyBattle](game-changelogs/CHANGELOG-MobArmyBattle.md)
- 🌍 [LevelBorder](game-changelogs/CHANGELOG-LevelBorder.md)
- 🧩 [LevelBlock](game-changelogs/CHANGELOG-LevelBlock.md)

---

## [1.0.1] - Stable Release

### Changed
- Updated to Paper/Minecraft 26.3
- Reworked the reset system across all challenges
- Updated internal game versions:
    - MobArmyBattle v1.7.2 Stable
    - LevelBorder v1.2.1 Stable
    - LevelBlock v0.9.1 Beta

### Fixed
- **MobArmyBattle:** Fixed player reset from the central lobby
- **LevelBorder:** Fixed smooth border expansion and incorrect border size (3×3) after reset
- **LevelBorder:** Fixed scoreboard issues; scoreboard is now hidden by default with an INFO option to show it
- **LevelBorder:** Fixed translation errors in reset voting
- **LevelBorder & LevelBlock:** Full reset now correctly executes the game-specific reset functions

### Notes
- Improved reset reliability and overall game behavior
- Reset system improvements are still in progress and will continue in v1.0.2

## [1.0.0] - Stable Release

### Added
- Unified version display for all challenges using `plugin.yml`

### Changed
- Integrated WorldSettings into the central Settings menu
- Improved game world management and unloading
- Improved menu navigation and game integration
- Updated MobArmyBattle to version 1.7.1
- Cleaned up internal code and removed obsolete methods

### Fixed
- Fixed world unloading issues (`canUnload()`)
- Fixed arena build protection after switching challenges
- Fixed lobby logo positioning and display issues
- **MobArmyBattle:** Fixed incorrect Cube kill counting and scoreboard display
- Fixed various bugs when switching between games and the lobby

### Notes
- First stable release of BastiGHG Challenges
- Includes MobArmyBattle V1.7.1, LevelBorder V1.2, and LevelBlock V0.9

## [0.9.9] - Beta

### Added
- Centralized reset management for MobArmyBattle with four individual reset options
- Separate reset options for game progress, team worlds, arena world, and all game worlds
- Voting confirmation for all reset operations in the MobArmyBattle reset menu
- Reset operation details displayed in the voting GUI

### Changed
- Reworked the central ResetManagementGUI to support game-specific reset options
- Improved reset menu layout, button icons, and descriptions
- Centralized reset menu translations in the Core language system
- Improved player teleport handling during world resets
- Team worlds now regenerate using a shared random seed
- Improved coordination between Core and MobArmyBattle reset managers

### Fixed
- Improved world unloading and regeneration handling
- Protection against overlapping world reset operations
- Removed obsolete and redundant reset methods
- Various smaller reset menu and display inconsistencies

### Notes
- Beta release

## [0.9.8] - Beta

### Added
- Extended German and English language support across the central plugin and individual challenge games
- Language-dependent arena names and descriptions for MobArmyBattle
- Additional loading and status messages for LevelBorder and LevelBlock

### Changed
- Reworked and unified the language system for Core, MobArmyBattle, LevelBorder, and LevelBlock
- Updated WorldSettings and game-specific settings to use the centralized language system
- Improved language handling for MobArmyBattle arena settings
- Internal loading messages and status displays standardized
- Several hardcoded texts replaced with language entries

### Fixed
- Missing or incorrect language entries in the central WorldSettings menu
- Arena names and descriptions remaining in German when English was selected
- Incorrect language output in arena selection menus
- Various smaller language and display inconsistencies

### Notes
- Beta release
- Further multiplayer testing, cleanup, and bug fixes are planned before version 1.0.0

## [0.9.1] - Beta

### Added
- Central `/info` command for all challenge games
- In-game information GUI featuring game descriptions, features, and commands
- Game information system based on README files
- Clickable `[INFO]` message upon completion of game loading
- Central welcome message when entering the challenge lobby
- Central world protection for the main lobby and the MobArmyBattle lobby
- Permanent night-time setting for the central challenge lobby

### Changed
- Consistent use of game-specific colors in lobby displays and info messages
- Updated game README files and project documentation

### Fixed
- Lobby logo display error after loading the resource pack
- Issue with regenerating the version display in the central lobby

### Notes
- Beta release
- Further language integration, command cleanup, and multiplayer testing planned before version 1.0.0

## [0.9.0] - Beta

### Added
- MobArmyBattle
- LevelBorder
- LevelBlock
- Central lobby and game selection
- Central WorldSettings system
- Separate player and game data
- Reset and voting system
- Lobby displays and interactive buttons
- German and English language support

### Changed
- Integrated the individual challenge projects into one central plugin

### Notes
- Beta release
- Larger multiplayer tests and additional bug fixes are planned before version 1.0.0