# LevelBlock

LevelBlock ist ein Minecraft-Challenge-Game innerhalb von
BastiGHG Challenges – Fan Project.

Zu Beginn steht nur ein sehr kleiner Bereich zur Verfügung.
Spieler können ihre Erfahrungslevel ausgeben, um weitere Blöcke
freizuschalten.

Ein ausgegebenes Level schaltet einen weiteren Block frei.

## Features

- Blockweise Erweiterung der Welt
- Multiplayer-Unterstützung
- Overworld, Nether und End
- Persistenter Spielfortschritt
- Separate Spieler- und Weltdaten
- Zentrale WorldSettings
- Reset-System

## Commands

- `/levelblock`
- `/levelblock info`
- `/levelblock start`
- `/levelblock gamesettings`
- `/levelblock reset`
- `/levelblock reset world`

## Changelog

### v0.9 Beta

#### Added
- Multiworld support for Overworld, Nether and End
- LevelBlock progression in Nether and End
- Custom Nether portal handling
- Portal return to already unlocked Overworld areas

#### Changed
- Improved border updates after portal travel and height changes
- Improved multiworld data handling

#### Fixed
- Fixed portals sending players to the normal Nether
- Fixed border display not updating correctly after dimension changes

### v0.8

#### Added
- Integration into BastiGHG Challenges – Fan Project
- Game selection and loading through the central lobby
- Game settings menu
- Reset voting system
- Persistent game progress