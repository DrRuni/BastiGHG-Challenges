
# MobArmyBattle Changelog

## [1.7.1] - Fehlerbehebungen

### Behoben
- Schwerwiegenden Fehler bei der fehlerhaften Cube-Kill-Zählung behoben
- Cube-Kills werden jetzt korrekt erfasst und im Scoreboard angezeigt
- Interne Kill-Verarbeitung und Scoreboard-Aktualisierungen verbessert

## [1.7.0]

### Hinzugefügt
- Deutsche und englische Sprachunterstützung
- Sprachauswahl über `/language`

### Geändert
- Scoreboards überarbeitet und rote Punktzahlen entfernt
- Texte, Titel und GUIs verbessert
- Internen Code bereinigt und modernisiert

### Behoben
- Probleme beim Arena-Reset während laufender Events behoben
- Kleinere Platzhalter- und Anzeigeprobleme behoben

## [1.6.0] - Stabile Version

### Hinzugefügt
- Arenaauswahl in den Arena-Einstellungen
- Neue Ancient-City-Arena

### Geändert
- Arena-Scoreboard überarbeitet
- Beim Arena-Reset werden Spieler jetzt zur Wellenauswahl zurückteleportiert
- Spawn-Konfiguration und Arena-Spawnpunkte verbessert
- Weltstruktur für Paper/Minecraft 26.x aktualisiert
- Internes Bereitschaftssystem von einzelnen Spielern auf Teams umgestellt
- Funktion zum Neustarten von Wellen deaktiviert

### Behoben
- Funktionsprobleme des Arena-Scoreboards behoben

## [1.5.0]

### Hinzugefügt
- Kisten-Randomizer
- Spawn-Reihenfolge richtet sich jetzt nach der Klickreihenfolge
- Vorlagendateisystem

### Geändert
- Start-Countdown und Spielinitialisierung verbessert
- Arena und Lobby neu gestaltet
- Mob-Spawning in Lobby und Arena deaktiviert

## [1.4.0]

### Hinzugefügt
- Reset der Arenawelt
- Unterstützung für Offline-Spieler in Team-Scoreboards
- Neue Reset-Befehle:
  - `/reset arena`
  - `/reset lobby`
  - `/reset teamworld`
  - `/reset playerdata`
### Geändert
- Dateiverwaltung neu strukturiert
- Nachrichten im Spiel und Konsolenausgaben verbessert
- Interne Projektstruktur verbessert

## [1.3.0]

### Hinzugefügt
- Separate Arenawelt, unabhängig von der Lobby
- Grundlage für zusätzliche Arenen
- Teamausrüstungs-GUI für die Event-Ausrüstung

### Geändert
- Verwaltung der Lobby- und Arenawelten überarbeitet
- Teleportierung und Respawn-Verarbeitung verbessert
- Resume-Funktion für getrennte Welten aktualisiert
- Sound-Verarbeitung verbessert

### Behoben
- Sound-Probleme behoben
- Teleportierungsprobleme zwischen Lobby und Arena behoben
- Probleme mit Resume und Respawn behoben
- Verschiedene kleinere Fehler behoben

## [1.2.0]

### Hinzugefügt
- WorldSettings-System
- Erweiterte Optionen für den Arena-Kompass

### Geändert
- Arena-Reset-Funktion verbessert
- GUIs und interne Codeorganisation verbessert
- Kompatibilität für Paper/Minecraft 26.1 aktualisiert

### Behoben
- Verschiedene Probleme mit dem Spielablauf und den Einstellungen behoben

## [1.1.0]

### Hinzugefügt
- Willkommensnachricht
- Zusätzliche zerstörbare Gegenstände in der Arena

### Geändert
- KeepInventory-Verarbeitung während der Weltneugenerierung verbessert
- Spawnpunkte überarbeitet
- Resume-Funktion erweitert
- Spiel-Reset-System verbessert
- Stabilere Scoreboard-Logik eingeführt
- Konsolenprotokollierung verbessert

### Behoben
- Fehler bei der Netherportal-Generierung behoben
- Verschiedene kleinere Fehler behoben
