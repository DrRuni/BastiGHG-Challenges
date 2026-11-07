# ChunkRandomizer Changelog

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
  - `/chunkrandomizer reset`zum Zurücksetzen des Spielfortschritts
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