# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Cos'è

TvTimeNeverDies è un'app in stile TV Time scritta in **Kotlin Multiplatform + Compose Multiplatform**
(target: Android e iOS). Permette di cercare serie TV e film, segnare episodi visti, scrivere commenti
per episodio e tenere traccia di cosa si sta guardando. Login e dati utente sono su Firebase
(Authentication + Firestore). Le serie TV usano l'API TVmaze (senza chiave), gli anime usano l'API
Jikan (MyAnimeList) come fallback, i film usano TMDB (richiede API key).

## Comandi principali

```bash
# Build APK Android debug
./gradlew :androidApp:assembleDebug

# Installare sul device/emulatore collegato
./gradlew :androidApp:installDebug

# Test unità modulo condiviso (JVM/Android host test)
./gradlew :shared:testAndroidHostTest

# Test su simulatore iOS (richiede macOS/Xcode)
./gradlew :shared:iosSimulatorArm64Test

# Eseguire una singola classe/metodo di test (JUnit filter)
./gradlew :shared:testAndroidHostTest --tests "com.example.tvtimeneverdie.SharedCommonTest.example"
```

L'app iOS si apre ed esegue da Xcode aprendo la cartella `iosApp/`; non è verificabile su Windows.

### Setup locale necessario prima di buildare

Due file contenenti credenziali non sono nel repo (sono in `.gitignore`) e servono per compilare/eseguire
funzionalità reali:

- `androidApp/google-services.json` — copiare da `google-services.json.example` e sostituire con quello
  vero scaricato dalla Firebase Console. Senza questo la build Android fallisce (serve il plugin
  `google-services`).
- `shared/src/commonMain/kotlin/com/example/tvtimeneverdie/config/TmdbConfig.kt` — copiare da
  `TmdbConfig.kt.example` e inserire una API key TMDB gratuita. Se assente, l'app compila comunque ma
  le chiamate ai film falliscono a runtime con un errore visibile in UI (le funzionalità sulle serie TV
  restano utilizzabili).

In CI (`.github/workflows/build-apk.yml`) questi due file vengono generati da GitHub Secrets
(`GOOGLE_SERVICES_JSON_BASE64`, `TMDB_API_KEY`) prima della build.

## Struttura dei moduli

- `shared/` — tutto il codice condiviso: UI Compose Multiplatform, ViewModel, networking, repository,
  logica di dominio. È dove va fatta la quasi totalità del lavoro.
  - `commonMain` — codice comune a tutte le piattaforme (la stragrande maggioranza del codice vive qui).
  - `androidMain` / `iosMain` — implementazioni `actual` per le API dichiarate `expect` in `commonMain`
    (es. `GoogleAuthClient`, `PlatformContext`, `ZipExtractor`, `FilePicker`, `Time`). Quando serve una
    capability specifica di piattaforma (file picker nativo, estrazione zip, sign-in Google nativo,
    formattazione data), il pattern è: dichiarare `expect` in `commonMain` e implementare `actual` in
    `androidMain`/`iosMain`.
  - `androidHostTest` / `iosTest` / `commonTest` — test, rispettivamente Android-only, iOS-only e comuni.
- `androidApp/` — solo l'entry point Android (`MainActivity`, manifest, risorse, `google-services.json`).
- `iosApp/` — solo l'entry point iOS (progetto Xcode, `iOSApp.swift`, `ContentView.swift`). Manca ancora
  l'aggiunta del pacchetto Firebase iOS SDK via Swift Package Manager e di `GoogleService-Info.plist`
  (vedi commento in `iosApp/iosApp/iOSApp.swift`); Google Sign-In su iOS non è implementato.
- `firestore.rules` — regole di sicurezza Firestore, da pubblicare manualmente dalla console o via
  `firebase deploy --only firestore:rules`.

## Architettura (dentro `shared/src/commonMain/kotlin/com/example/tvtimeneverdie/`)

Flusso a livelli: `ui/screens/*` (Composable + ViewModel per schermata) → `data/repository/*` → `data/remote/*`
(client Ktor + DTO) o Firebase (Auth/Firestore direttamente dai repository). Nessun framework di DI: le
dipendenze condivise (repository) sono singleton lazy esposti da `di/AppContainer.kt`, istanziato a mano
nei ViewModel/Composable.

- `auth/` — `AuthRepository` (Firebase Auth: email/password + Google) e `GoogleAuthClient` (`expect/actual`,
  ottiene l'ID token Google dal flusso nativo di sign-in per passarlo a Firebase).
- `data/remote/` — client Ktor per le tre fonti dati esterne: `TvMazeApi` (serie TV), `JikanApi` (anime,
  fallback per ciò che TVmaze non copre), `TmdbApi` (film). Ogni fonte ha i propri DTO in `data/remote/dto/`
  e funzioni `toDomain()`/mapper (`Mappers.kt`, `JikanMappers.kt`, `TmdbMappers.kt`) che convertono verso i
  model di dominio comuni in `domain/model/`.
  - Le serie provenienti da Jikan condividono lo spazio ID con TVmaze tramite un offset
    (`JIKAN_SHOW_ID_OFFSET`, vedi `isJikanShowId`/uso in `TvShowRepository`): un `showId` sopra la soglia
    viene reinstradato verso Jikan invece che TVmaze. Tenerne conto in qualsiasi codice che manipola show ID.
- `data/repository/` — un repository per area (`TvShowRepository`, `MovieRepository`, `UserShowsRepository`,
  `UserMoviesRepository`, `CommentsRepository`, `AuthRepository`). I repository di catalogo (`TvShowRepository`,
  `MovieRepository`) cachano in memoria (mutex + map) le risposte delle API esterne; i repository "utente"
  leggono/scrivono su Firestore sotto il documento dell'utente autenticato.
- `data/gdpr/` — parsing dell'esportazione GDPR di TV Time (zip → CSV → match contro il catalogo
  TVmaze/Jikan). `ZipExtractor` è `expect/actual` (estrazione zip nativa per piattaforma). Punti da
  ricordare se si tocca questo import:
  - TV Time usa ID TheTVDB, incompatibili con gli ID TVmaze/Jikan di questa app: l'abbinamento avviene per
    nome (via `TvShowRepository.findBestMatch`), con conferma manuale utente prima di scrivere qualsiasi dato.
  - Per le serie, TV Time esporta solo un *conteggio* di episodi visti, non l'elenco esatto: l'import segna
    come visti i primi N episodi in ordine di stagione/numero (approssimazione).
  - Per i film, vengono importati come "visti" solo quelli il cui nome compare nei commenti/voti originali
    (TV Time non esporta una lista "da vedere" per i film).
  - Viene letto solo ciò che serve a popolare visti/da vedere; il resto del contenuto dello zip (commenti,
    statistiche, dati account) è ignorato e non persistito.
  - Su iOS l'import non è ancora implementato (serve un selettore di documenti nativo).
- `domain/model/` — model di dominio condivisi tra tutte le fonti dati e la UI (`Show`, `Episode`, `Movie`,
  `ShowProgress`, `EpisodeComment`, `AuthUser`).
- `ui/screens/<nome>/` — una cartella per schermata, ciascuna con `XScreen.kt` (Composable) e
  `XViewModel.kt`. Navigazione dichiarata in `ui/navigation/Destinations.kt` (route `@Serializable`,
  type-safe) e cablata in `ui/navigation/AppNavHost.kt` (`MainScreen`, con bottom bar Home/Cerca/Profilo).
  `App.kt` è la root Composable: mostra `LoginScreen` o `MainScreen` in base allo stato di
  `AuthRepository.currentUser`.
- `platform/` — capability native dietro `expect/actual` (es. `FilePicker`).
- `util/` — utility condivise con implementazioni per piattaforma dove serve (`Time`).

## Convenzioni

- Commenti/KDoc nel codice sono in italiano; segui la stessa lingua per coerenza.
- Nuove capability specifiche di piattaforma vanno dichiarate `expect` in `commonMain` e implementate
  `actual` in `androidMain`/`iosMain`, seguendo i file già presenti (`GoogleAuthClient`, `ZipExtractor`,
  `FilePicker`, `Time`) come modello.
- Nuove dipendenze condivise (repository, client) vanno esposte come singleton lazy in
  `di/AppContainer.kt`, non istanziate ad-hoc nelle schermate.
