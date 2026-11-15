# ChunkRandomizer Changelog

## [0.8.1] - Beta Update

### Hinzugefügt
- Respawn-Unterstützung für ChunkRandomizer integriert

### Geändert
- Reset-System überarbeitet und an die zentrale Reset-Verwaltung angepasst
- Reset-Befehl auf `/reset gamedata` geändert
- Reset-Menüs, Buttons, Symbole, Beschreibungen und Bestätigungsdialoge verbessert
- Chat- und Statusmeldungen bei Reset-Vorgängen und der Neugenerierung von Spielwelten überarbeitet
- Zurücksetzen von Spielerdaten, Spieldaten und Spielwelten besser aufeinander abgestimmt

### Behoben
- **Mob-Spawning:** Fehler beim Erzeugen von Challenge-Mobs in Wasser-Chunks behoben
- **Chunk-Randomisierung:** Blöcke werden jetzt zuerst randomisiert, bevor der Challenge-Mob erscheint

### Hinweise
- Verbesserte Zuverlässigkeit bei Spieler- und Welt-Resets
- Optimiertes Zusammenspiel zwischen Chunk-Randomisierung und Mob-Spawning

## [0.8.0] - Beta – Erstveröffentlichung

### Hinzugefügt
- ChunkRandomizer als viertes Challenge-Game in BastiGHG Challenges integriert
- Zufällige Umwandlung aller geeigneten Blöcke eines Chunks in einen einheitlichen Blocktyp
- Chunkbasiertes WorldBorder-System mit zunächst friedlichen Challenge-Mobs
- Freigabe der Weltgrenze nach dem Besiegen des jeweiligen Challenge-Mobs
- Spielstart über den zentralen Befehl `/challenge start`
- Dauerhafte Speicherung des Spielfortschritts
- Jederzeitiges Fortsetzen einer begonnenen Challenge, auch nach einem Serverneustart
- Timer mit Zeitanzeige und Wiederherstellung der Spielzeit
- Integration in die zentrale Lobby, Spielauswahl und Welteinstellungen
- Datenreset und vollständiger Weltreset über das zentrale Reset-Abstimmungssystem
- Befehle:
  - `/chunkrandomizer reset` – zum Zurücksetzen des Spielfortschritts
  - `/chunkrandomizer reset world` zum vollständigen Zurücksetzen der Spielwelten

### Geändert
- Mob-Spawning und Chunkumwandlung während der Beta-Entwicklung optimiert
- Speichervorgänge der Spielwelten zur Verbesserung der Performance angepasst
- Reset-Verarbeitung und Wiederherstellung des Spielstatus verbessert

### Behoben
- Probleme bei der Ermittlung geeigneter Mob-Spawnpositionen während der Entwicklung behoben
- Wiederherstellung der ursprünglichen Weltgrenze nach Serverabstürzen korrigiert
- Zurücksetzen von Spielerinventaren und Spieldaten verbessert

### Hinweise
- Erste öffentliche Beta-Version von ChunkRandomizer
- Bereits umgewandelte Chunks bleiben beim Datenreset erhalten
- Ein vollständiger Weltreset generiert alle ChunkRandomizer-Welten neu
- Weitere Funktionen und Optimierungen sind geplant