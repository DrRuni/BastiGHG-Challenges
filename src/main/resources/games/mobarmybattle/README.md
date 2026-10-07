# MobArmyBattle

MobArmyBattle is a team-based Minecraft challenge game integrated into **BastiGHG Challenges – Fan Project**.

Two teams, **Red** and **Blue**, prepare separately by collecting resources, equipment and mobs.  
Each team creates attack waves for the opposing team before both teams enter the arena and fight through the enemy waves.

> This game is part of an unofficial fan project and is not affiliated with BastiGHG.

## Current Version

`v1.7 Stable`

## Features

- Two teams: **Red** and **Blue**
- Separate team worlds and Nether worlds
- Preparation phase for collecting resources, equipment and mobs
- Three configurable attack waves per team
- Automatic arena battle system
- Multiple arenas with GUI-based arena selection
- Includes the **Ancient City** arena
- Shared team backpacks
- Team equipment selection
- Optional block randomizer
- Chest randomizer
- GUI-based game and wave settings
- Arena monster compass
- Team and arena scoreboards
- Multiplayer support
- Integrated into the central challenge lobby
- Separate world, player and game data
- Persistent game progress
- Central world settings
- Reset voting system
- Arena, team-world and complete world reset support
- German and English language support
- In-game language selection using `/language`

## Commands

```text
/mobarmy
/mobarmy info
/mobarmy resume
/mobarmy gamesettings
/mobarmy reset
/mobarmy reset world
/mobarmy reset arena
/mobarmy reset teamwelt

/team join rot
/team join blau
/team leave

/mobstatus
/arenasummary
/language

## Changelog

### v1.7

#### Neu

- In **BastiGHG Challenges – Fan Project** integriert
- MobArmyBattle kann nun über die zentrale Lobby ausgewählt und geladen werden
- Sprachsystem für Deutsch und Englisch hinzugefügt
- Sprachauswahl über `/language` hinzugefügt

#### Verbessert

- Scoreboards überarbeitet und rote Score-Zahlen entfernt
- Texte, Titel und GUIs überarbeitet
- Code bereinigt und modernisiert
- Welt-, Spieler- und Spieldaten an das zentrale Challenge-System angepasst

#### Behoben

- Arena-Reset wurde bei laufendem Event teilweise nicht korrekt ausgeführt
- Kleinere Fehler bei Platzhaltern und Anzeigen behoben

#### Kompatibilität

- Benötigt **Paper 26.2**
- Benötigt **Java 25**
- Nicht kompatibel mit reinem Spigot/Bukkit

---

### v1.6

#### Neu

- Arenaauswahl in den Arenaeinstellungen hinzugefügt
- Zweite Arena **„Ancient City“** hinzugefügt

#### Verbessert

- Arena-Scoreboard überarbeitet und wieder funktionsfähig
- Arena-Reset überarbeitet: Spieler werden nun zurück zur Wave-Auswahl teleportiert, um dort neu zu starten
- Spawn-Konfiguration und Arena-Spawnpunkte überarbeitet
- Weltenstruktur an Paper/Minecraft **26.x** angepasst
- Ready-System intern von einzelnen Spielern auf Teams umgestellt

#### Geändert

- Funktion zum Neustarten der Waves deaktiviert

---

### v1.5

#### Neu

- Kistenrandomizer hinzugefügt
- Spawnreihenfolge entspricht nun der Klick-Reihenfolge
- Auf Paper/API 26.2 alpha aktualisiert
- Template-Datei-System eingeführt

#### Verbessert

- Startcountdown überarbeitet
- Startlogik verbessert

#### Behoben

- Mobspawning in Lobby und Arena entfernt
- Arena und Lobby umgestaltet

---

### v1.4

#### Hinzugefügt

- Arena-Welt-Reset hinzugefügt
- Team-Scoreboard unterstützt nun Offline-Spieler
- Neue Reset-Befehle hinzugefügt:
  - `/reset arena`
  - `/reset lobby`
  - `/reset teamworld`
  - `/reset playerdata`

#### Geändert

- Dateimanager neu strukturiert
- Textausgaben im Spiel überarbeitet
- Konsolenausgaben überarbeitet
- Interne Projektstruktur verbessert

---

### v1.3

#### Neu

- World-Arena von der Lobby getrennt
- Vorbereitung für spätere zusätzliche Arenen hinzugefügt
- Team-Equipment-GUI für Event-Ausrüstung hinzugefügt

#### Verbessert

- System auf getrennte Lobby- und Arena-Welten umgestellt
- Teleport-Logik für Lobby und Arena überarbeitet
- Resume-Funktion an die getrennten Welten angepasst
- Respawn-Ablauf verbessert
- Sound-Handling überarbeitet

#### Behoben

- Mehrere Bugs im Zusammenhang mit Sounds behoben
- Fehler bei Teleports zwischen Lobby und Arena behoben
- Probleme mit Resume behoben
- Fehler beim Respawn behoben
- Weitere kleinere Bugs beseitigt

---

### v1.2

#### Neu

- Welteneinstellungen hinzugefügt
- Arena-Kompass-Option erweitert

#### Verbessert

- Arena-Reset verbessert
- GUIs verbessert
- Funktionen übersichtlicher neu angeordnet
- Code angepasst und für Paper/Minecraft 26.1 aktualisiert

#### Behoben

- Mehrere Bugs behoben
- Kleinere Fehler im Ablauf und bei den Einstellungen beseitigt

#### Kompatibilität

- Benötigt **Paper 26.1.2**
- Benötigt **Java 25**
- Nicht kompatibel mit reinem Spigot/Bukkit

---

### v1.1

#### Neu

- Willkommensnachricht hinzugefügt
- Weitere abbaubare Items in der Arena hinzugefügt

#### Verbessert

- KeepInventory funktioniert nun auch bei Welt-Neugenerierung korrekt
- Spawnpunkte überarbeitet und verbessert
- Resume-Funktion erweitert und an die Spawnpunkte angepasst
- Reset-Game überarbeitet
- Neue, stabilere Scoreboard-Logik
- Konsolen-Logausgabe erweitert und verbessert

#### Behoben

- Fehler bei der Erzeugung von Netherportalen behoben
- Verschiedene kleinere Bugs behoben

#### Kompatibilität

- Benötigt **Paper 1.21.5**
- Benötigt **Java 21**
- Getestet bis 1.21.11
- Nicht kompatibel mit reinem Spigot/Bukkit