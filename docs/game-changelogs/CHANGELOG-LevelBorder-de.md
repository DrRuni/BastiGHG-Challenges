# LevelBorder Changelog

## [1.2.1] - Fehlerbehebungen

### Geändert
- Auf Paper/Minecraft 26.3 aktualisiert

### Geändert
- Das Scoreboard ist beim Start der Challenge jetzt standardmäßig ausgeblendet
- INFO-Hinweis zum Einblenden des Scoreboards hinzugefügt

### Behoben
- Fehler bei der flüssigen Erweiterung der Weltgrenze behoben
- Fehlende Spieler im Scoreboard beim Start der Challenge korrigiert
- Fehlerhafte Startgröße der Weltgrenze (1×1 statt 3×3) nach einem Reset behoben
- Übersetzungsfehler bei Reset-Abstimmungen behoben
- Vollständiger Reset führt jetzt die spielspezifische Reset-Funktion korrekt aus

## [1.2.0]

### Hinzugefügt
- Neues Optionsmenü, erreichbar über `/optionen` oder `/levelborder optionen`
- Zugriff auf Challenge-Einstellungen nur für OPs
- KeepInventory ein- und ausschaltbar
- Steuerung für Start, Stopp, Reset und Mittelpunkt der Weltgrenze
- Anpassung der Weltgrenzengröße per Links- und Rechtsklick

### Geändert
- Paper API auf 26.2 aktualisiert
- Handhabung von Netherportalen deutlich verbessert
- Ablauf beim Reset und Start verbessert
- Mittelpunkt der Weltgrenze wird jetzt sofort aktualisiert
- Kleinere Codebereinigungen und API-Anpassungen

### Behoben
- Spielerliste im Scoreboard korrigiert

## [1.1.1]

### Geändert
- Internen Code bereinigt
- API-Warnungen reduziert

## [1.1.0]

### Hinzugefügt
- Eigenes Mob-Spawning-System
- Konfigurierbare Einstellungen für das Mob-Spawning
- Mob-Spawning unter Berücksichtigung der Weltgrenze
- Unterstützung für das Spawnen von Mobs außerhalb der Challenge-Weltgrenze

### Geändert
- Paper API auf 26.1.2 aktualisiert
- Auf Java 25 aktualisiert
- Mob-Spawning bei kleinen Weltgrenzen verbessert

### Hinweise
- Das eigene Mob-Spawning-System gleicht die Einschränkungen des normalen Minecraft-Spawnsystems außerhalb der WorldBorder aus

## [1.0.0] - Erste Veröffentlichung

### Hinzugefügt
- Erste Veröffentlichung von LevelBorder
- Unterstützung für Paper 1.21.10
