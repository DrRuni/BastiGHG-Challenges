# ChunkRandomizer Changelog

## [0.8.1] - Beta Update

### Added
- Integrated respawn support for ChunkRandomizer

### Changed
- Reworked the reset system and integrated it with the central reset management
- Changed the reset command to `/reset gamedata`
- Improved reset menus, buttons, icons, descriptions, and confirmation dialogs
- Revised chat and status messages for reset operations and world regeneration
- Improved coordination between player data, game data, and game world resets

### Fixed
- **Mob Spawning:** Fixed issues with spawning challenge mobs in water chunks
- **Chunk Randomization:** Blocks are now randomized before the challenge mob spawns

### Notes
- Improved reliability of player and world resets
- Optimized interaction between chunk randomization and mob spawning

## [0.8.0] - Beta – Initial Release

### Added
- Integrated ChunkRandomizer as the fourth challenge game in BastiGHG Challenges
- Random transformation of all eligible blocks within a chunk into a single block type
- Chunk-based WorldBorder system featuring initially peaceful challenge mobs
- WorldBorder removal after defeating the respective challenge mob
- Game start through the central `/challenge start` command
- Persistent game progress saving
- Ability to resume an ongoing challenge at any time, including after server restarts
- Game timer with on-screen display and saved time restoration
- Integration into the central lobby, game selection, and world settings
- Game data reset and complete world reset through the central reset voting system
- Commands:
    - `/chunkrandomizer reset` – Resets game progress
    - `/chunkrandomizer reset world` – Completely resets the game worlds

### Changed
- Optimized mob spawning and chunk transformation during beta development
- Adjusted world-saving operations to improve performance
- Improved reset handling and game state restoration

### Fixed
- Fixed issues with finding suitable mob spawn locations during development
- Fixed restoration of the original WorldBorder after server crashes
- Improved player inventory and game data reset handling

### Notes
- First public beta release of ChunkRandomizer
- Previously transformed chunks remain unchanged after a game data reset
- A complete world reset regenerates all ChunkRandomizer worlds
- Additional features and optimizations are planned