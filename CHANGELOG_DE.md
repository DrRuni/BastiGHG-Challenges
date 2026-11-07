# Changelog

## Challenge-Changelogs

- ⚔️ [MobArmyBattle](docs/game-changelogs/CHANGELOG-MobArmyBattle-de.md)
- 🌍 [LevelBorder](docs/game-changelogs/CHANGELOG-LevelBorder-de.md)
- 🧩 [LevelBlock](docs/game-changelogs/CHANGELOG-LevelBlock-de.md)
- 🎲 [ChunkRandomizer](docs/game-changelogs/CHANGELOG-ChunkRandomizer-de.md)

---

### [1.1.0] - Feature-Update

### Hinzugefügt
- ChunkRandomizer v0.8 Beta als viertes Challenge-Game integriert
- Automatische Speicherung der Spielerdaten zur Absicherung bei Serverabstürzen
- Verbesserte Wiederherstellung von Spielerdaten nach Serverabstürzen
- Neue Befehle /chunkrandomizer reset und /chunkrandomizer reset world
  Geändert
- Zentrale Spiel-, Welt- und Reset-Verwaltung für ChunkRandomizer erweitert
- Speicherung von Spielerdaten und Wiederherstellung des Spielstands verbessert
- Welteinstellungen und Reset-Abstimmung für ChunkRandomizer erweitert
  Behoben
- Zuverlässigkeit beim Zurücksetzen von Welten und Spielerdaten verbessert
  Enthaltene Games
  - MobArmyBattle v1.7.2
  - LevelBorder v1.2.1
  - LevelBlock v0.9.1 Beta
  - ChunkRandomizer v0.8 Beta

## [1.0.1] - Stabile Version

### Geändert
- Aktualisiert auf Paper/Minecraft 26.3
- Reset-System für alle Challenges überarbeitet
- Spielerinventare werden beim Zurücksetzen der Spielerdaten geleert
- Spieler werden beim Betreten einer Challenge oder bei der Rückkehr in die zentrale Lobby automatisch in den Überlebensmodus versetzt
- Warnhinweise zum Verlust des Inventarinhalts bei Reset-Bestätigungen hinzugefügt
- Interne Spielversionen aktualisiert:
  - MobArmyBattle v1.7.2 Stable
  - LevelBorder v1.2.1 Stable
  - LevelBlock v0.9.1 Beta

### Behoben
- **MobArmyBattle:** Spieler-Reset aus der zentralen Lobby korrigiert
- **LevelBorder:** Fehler bei der flüssigen Erweiterung der Weltgrenze und der falschen Startgröße (3×3) nach einem Reset behoben
- **LevelBorder:** Scoreboard-Probleme behoben; das Scoreboard ist jetzt standardmäßig ausgeblendet und kann über eine INFO-Option eingeblendet werden
- **LevelBorder:** Übersetzungsfehler bei Reset-Abstimmungen behoben
- **LevelBorder & LevelBlock:** Ein vollständiger Reset führt jetzt die spielspezifischen Reset-Funktionen korrekt aus

### Hinweise
- Zuverlässigkeit des Reset-Systems und allgemeines Spielverhalten verbessert
- Weitere Verbesserungen am Reset-System sind in Arbeit und werden in v1.0.2 fortgesetzt

## [1.0.0] - Stabile Version

### Hinzugefügt
- Einheitliche Versionsanzeige für alle Challenges über `plugin.yml`

### Geändert
- WorldSettings in das zentrale Einstellungsmenü integriert
- Verwaltung und Entladen der Spielwelten verbessert
- Menüführung und Integration der Spiele verbessert
- MobArmyBattle auf Version 1.7.1 aktualisiert
- Internen Code bereinigt und veraltete Methoden entfernt

### Behoben
- Probleme beim Entladen von Welten behoben (`canUnload()`)
- Fehler beim Arena-Bauschutz nach dem Wechsel zwischen Challenges behoben
- Positionierungs- und Anzeigeprobleme des Lobby-Logos behoben
- **MobArmyBattle:** Fehlerhafte Cube-Kill-Zählung und Scoreboard-Anzeige korrigiert
- Verschiedene Fehler beim Wechsel zwischen Spielen und der Lobby behoben

### Hinweise
- Erste stabile Version von BastiGHG Challenges
- Enthält MobArmyBattle v1.7.1, LevelBorder v1.2 und LevelBlock v0.9

## [0.9.9] - Beta

### Hinzugefügt
- Zentrale Reset-Verwaltung für MobArmyBattle mit vier individuellen Reset-Optionen
- Separate Reset-Optionen für Spielfortschritt, Teamwelten, Arenawelt und sämtliche Spielwelten
- Abstimmungsbestätigung für alle Reset-Vorgänge im MobArmyBattle-Reset-Menü
- Anzeige von Details zu Reset-Vorgängen im Abstimmungs-GUI

### Geändert
- Zentrales ResetManagementGUI überarbeitet, um spielspezifische Reset-Optionen zu unterstützen
- Layout, Button-Symbole und Beschreibungen des Reset-Menüs verbessert
- Übersetzungen des Reset-Menüs im zentralen Sprachsystem des Core zusammengeführt
- Teleportierung der Spieler während Welt-Resets verbessert
- Teamwelten werden jetzt mit einem gemeinsamen zufälligen Seed neu generiert
- Abstimmung zwischen den Reset-Managern des Core und von MobArmyBattle verbessert

### Behoben
- Entladen und Neugenerieren von Welten verbessert
- Schutz vor sich überschneidenden Welt-Reset-Vorgängen
- Veraltete und redundante Reset-Methoden entfernt
- Verschiedene kleinere Unstimmigkeiten im Reset-Menü und bei der Anzeige behoben

### Hinweise
- Beta-Version

## [0.9.8] - Beta

### Hinzugefügt
- Deutsche und englische Sprachunterstützung im zentralen Plugin und in den einzelnen Challenge-Spielen erweitert
- Sprachabhängige Arenanamen und Beschreibungen für MobArmyBattle
- Zusätzliche Lade- und Statusmeldungen für LevelBorder und LevelBlock

### Geändert
- Sprachsystem für Core, MobArmyBattle, LevelBorder und LevelBlock überarbeitet und vereinheitlicht
- WorldSettings und spielspezifische Einstellungen auf das zentrale Sprachsystem umgestellt
- Sprachverarbeitung für die Arena-Einstellungen von MobArmyBattle verbessert
- Interne Lademeldungen und Statusanzeigen vereinheitlicht
- Mehrere fest im Code hinterlegte Texte durch Spracheinträge ersetzt

### Behoben
- Fehlende oder fehlerhafte Spracheinträge im zentralen WorldSettings-Menü korrigiert
- Fehler behoben, durch den Arenanamen und Beschreibungen trotz ausgewählter englischer Sprache auf Deutsch angezeigt wurden
- Fehlerhafte Sprachausgabe in den Arena-Auswahlmenüs korrigiert
- Verschiedene kleinere Sprach- und Anzeigeprobleme behoben

### Hinweise
- Beta-Version
- Weitere Multiplayer-Tests, Codebereinigungen und Fehlerbehebungen sind vor Version 1.0.0 geplant

## [0.9.1] - Beta

### Hinzugefügt
- Zentraler `/info`-Befehl für alle Challenge-Spiele
- Informations-GUI im Spiel mit Spielbeschreibungen, Funktionen und Befehlen
- Spielinformationssystem auf Grundlage der README-Dateien
- Anklickbare `[INFO]`-Nachricht nach Abschluss des Ladevorgangs eines Spiels
- Zentrale Willkommensnachricht beim Betreten der Challenge-Lobby
- Zentraler Weltschutz für die Hauptlobby und die MobArmyBattle-Lobby
- Dauerhafte Nachtzeit in der zentralen Challenge-Lobby

### Geändert
- Einheitliche Verwendung spielspezifischer Farben in Lobby-Anzeigen und Informationsmeldungen
- Game-README-Dateien und Projektdokumentation aktualisiert

### Behoben
- Anzeigefehler des Lobby-Logos nach dem Laden des Resource Packs behoben
- Problem bei der Neuerstellung der Versionsanzeige in der zentralen Lobby behoben

### Hinweise
- Beta-Version
- Weitere Sprachintegration, Bereinigung der Befehle und Multiplayer-Tests vor Version 1.0.0 geplant

## [0.9.0] - Beta

### Hinzugefügt
- MobArmyBattle
- LevelBorder
- LevelBlock
- Zentrale Lobby und Spielauswahl
- Zentrales WorldSettings-System
- Getrennte Spieler- und Spieldaten
- Reset- und Abstimmungssystem
- Lobby-Anzeigen und interaktive Buttons
- Deutsche und englische Sprachunterstützung

### Geändert
- Die einzelnen Challenge-Projekte in einem zentralen Plugin zusammengeführt

### Hinweise
- Beta-Version
- Größere Multiplayer-Tests und zusätzliche Fehlerbehebungen sind vor Version 1.0.0 geplant
