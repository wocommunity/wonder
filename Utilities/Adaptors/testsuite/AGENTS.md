# WebObjects Adaptor Test Suite

Dieses Verzeichnis enthält eine eigenständige, automatisierte Testsuite für den gemeinsamen C-Core des WebObjects-Adaptors (`Adaptor/`), der sowohl für das Apache 2.4-Modul (`mod_WebObjects.so`) als auch das IIS-Modul verwendet wird.

---

## 1. Übersicht & Zielsetzung

Die Testsuite dient dazu:
1. **Race Conditions im Multithreading isoliert nachzuweisen** (z. B. Abstürze durch Heap-Korruption / Double-Free oder abgeschnittene Header wie `x-webobjects-loa`).
2. **Streaming-Uploads mit großen Payloads (>1 MB)** unter hoher Nebenläufigkeit automatisiert zu testen.
3. **Autark ohne externe Dienste** zu laufen: Die Konfiguration wird aus einer lokalen XML-Datei (`file://.../test_woadaptor.xml`) statt von `wotaskd` bezogen, und die WebObjects-Instanz wird durch einen integrierten Dummy-Server simuliert.
4. **Crashes, Deadlocks und Header-Korruptionen sicher abzufangen** und sauber als Test-Fehlschläge auszugeben.

---

## 2. Komponenten & Dateien

| Datei | Beschreibung |
|---|---|
| `test_woadaptor.xml` | WebObjects-Konfigurationsdatei (`WOConfig.xml`). Definiert eine Applikation `TestApp` mit einer Instanz auf `127.0.0.1:8999`. |
| `dummy_app_server.py` | Multithreaded Python HTTP-1.1-Server. Verarbeitet simultane 1MB+ Streaming-Uploads, simuliert Keep-Alive und antwortet mit WebObjects-Headern (`x-webobjects-loadaverage: 1.25`). |
| `test_wastring.c` | **Tier 1 (Unit Test):** Simuliert die exakte Nebenläufigkeit zwischen Header-Erstellung (`response.c`) und Transaction-ID-Generierung (`transaction.c`). Erkennt Nullbyte-Einfügungen an Index 16 (`x-webobjects-loa`), fängt Signal-Crashes ab. |
| `test_adaptor_runner.c` | **Tier 2 (Integration Test):** Initialisiert den vollständigen Adaptor-Core mit `test_woadaptor.xml` und sendet parallele 1.125 MB POST-Requests über `tr_handleRequest` und die Streaming-Callback-Funktion `test_getMoreContent`. |
| `Makefile` | Kompiliert die Test-Binaries in zwei Varianten: Multithreaded (korrektes Locking) und Single-Threaded (reproduziert den bisherigen Build-Zustand). |
| `run_tests.sh` | Orchestrierungs-Skript. Startet/stoppt den Dummy-Server, führt Tests mit Timeouts aus, überwacht Exit-Codes und druckt eine strukturierte Zusammenfassung. |

---

## 3. Kompilierte Test-Binaries

Das `Makefile` erzeugt 4 Binaries:

1. **`test_wastring`**:
   - Kompiliert mit `-DMULTITHREADED_ADAPTOR -DTHREAD_MODEL=2`.
   - Testet den String-Cache unter Pthread-Synchronisation.
2. **`test_wastring_single`**:
   - Kompiliert mit `-DSINGLE_THREADED_ADAPTOR` (wie bisher das Apache2.4 Makefile).
   - Demonstriert die Race Condition: führt durch fehlende Locks innerhalb von Sekunden zu Endlosschleifen (Zyklen in `cache->next`) oder Double-Free-Crashes.
3. **`test_adaptor_runner`**:
   - Vollständiger Adaptor-Runner mit Streaming von 1.125 MB POST-Uploads unter Pthreads.
4. **`test_adaptor_runner_single`**:
   - Adaptor-Runner mit `-DSINGLE_THREADED_ADAPTOR`.
   - Demonstriert Crashes (`SIGSEGV` / `SIGABRT`) unter Last, wenn Apache mit Worker- oder Event-MPM betrieben wird.

---

## 4. Benutzung & Ausführung

### A. Vollständige Testsuite ausführen (Standard)

```bash
./run_tests.sh
```

**Ablauf:**
1. Baut alle Binaries via `make all`.
2. Startet den Dummy-Server im Hintergrund auf Port `8999`.
3. Führt `test_wastring` aus (5000 Iterationen, 16 Threads).
4. Führt `test_adaptor_runner` aus (200 parallele 1.125 MB Streaming-Uploads).
5. Beendet den Dummy-Server zuverlässig und gibt die Test-Zusammenfassung aus.

### B. Bug-Reproduktionsmodus (Fehler & Crashes demonstrieren)

```bash
./run_tests.sh --reproduce
```

Dieser Modus führt die mit `-DSINGLE_THREADED_ADAPTOR` gebauten Binaries aus:
- Fängt Timeouts / Deadlocks ab (wenn Zyklen in der Cache-Liste entstehen).
- Fängt Speicherabstürze (`SIGSEGV`, `SIGABRT`) über Signal-Handler ab.
- Markiert die Tests sauber als `FAILED` und zeigt die Crash-Ursachen an, ohne dass das System unkontrolliert abstürzt.

### C. Manuelle Ausführung einzelner Tests

```bash
# Unit-Test mit individueller Anzahl an Iterationen und Threads:
# Syntax: ./test_wastring [Iterationen_pro_Thread] [Thread_Anzahl]
./test_wastring 10000 20

# End-to-End-Runner (erfordert laufenden dummy_app_server.py):
python3 dummy_app_server.py --port 8999 &
./test_adaptor_runner 50 10  # 10 Threads mit je 50 Requests (500 Requests à 1.125 MB)
kill %1
```

---

## 5. Signal- & Crash-Handling

Wenn Threads ungeschützt auf den String-Cache zugreifen, schlägt die macOS-Speicherverwaltung (`libmalloc`) mit `BUG IN CLIENT OF LIBMALLOC: memory corruption of free block` (`SIGABRT`) oder `SIGSEGV` zu.

In `test_wastring.c` und `test_adaptor_runner.c` sind Signal-Handler für `SIGABRT`, `SIGSEGV` und `SIGBUS` integriert:
- Verhindert das unkontrollierte Aufpoppen des System-Crash-Reporters.
- Gibt eine formatierte Fehlermeldung auf `stderr` aus.
- Beendet den Prozess mit Exit-Code `2`.
- `run_tests.sh` fängt diesen Exit-Code sowie Timeouts (Exit `124`) ab und bewertet den Test automatisch als `FAILED`.

---

## 6. Zu behebende Schwachstellen im Code (Fix-Plan)

1. **`Apache2.4/Makefile`**:
   - Aktuell: `-DSINGLE_THREADED_ADAPTOR` schaltet jegliches Mutex-Locking aus.
   - Fix: Auf `-DMULTITHREADED_ADAPTOR` umstellen.
2. **`Adaptor/Platform.h`**:
   - Aktuell: `THREAD_MODEL = PTHREADS` ist nur für `SOLARIS` und `HPUX` definiert. Für `MACOS` und `LINUX` ist `THREAD_MODEL` undefiniert (0).
   - Fix: `#if defined(LINUX) || defined(MACOS)` ergänzen.
3. **`Adaptor/wastring.c`**:
   - Aktuell: `str_free()` durchläuft Listen teilweise ungelockt, und `while (str != cache)` liest `cache` ohne Mutex.
   - Fix: Die gesamte Listen-Freigabe und Cache-Verkettung atomar kapseln.
4. **`Adaptor/nbsocket.c`**:
   - Aktuell: `reset()` liest Restdaten vom Socket; bei `result > 0` loggt es nur eine Warnung, gibt aber `TR_OK` zurück -> verschmutzter Socket wandert zurück in den Connection-Pool.
   - Fix: Bei Restdaten `TR_ERROR` liefern, damit der Socket geschlossen wird.
5. **`Adaptor/request.c`**:
   - Aktuell: `value[(*iov)->iov_len - 1]` bei leeren Headern erzeugt Buffer-Underflow (`value[-1]`).
   - Fix: Länge vor dem Zugriff auf `> 0` prüfen.
