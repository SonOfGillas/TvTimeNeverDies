This is a Kotlin Multiplatform project targeting Android, iOS.

* [/iosApp](./iosApp/iosApp) contains an iOS application. Even if you’re sharing your UI with Compose Multiplatform,
  you need this entry point for your iOS app. This is also where you should add SwiftUI code for your project.

* [/shared](./shared/src) is for code that will be shared across your Compose Multiplatform applications.
  It contains several subfolders:
  - [commonMain](./shared/src/commonMain/kotlin) is for code that’s common for all targets.
  - Other folders are for Kotlin code that will be compiled for only the platform indicated in the folder name.
    For example, if you want to use Apple’s CoreCrypto for the iOS part of your Kotlin app,
    the [iosMain](./shared/src/iosMain/kotlin) folder would be the right place for such calls.
    Similarly, if you want to edit the Desktop (JVM) specific part, the [jvmMain](./shared/src/jvmMain/kotlin)
    folder is the appropriate location.

### Running the apps

Use the run configurations provided by the run widget in your IDE's toolbar. You can also use these commands and options:

- Android app: `./gradlew :androidApp:assembleDebug`
- ./gradlew :androidApp:installDebug
-  & "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe" shell am start -n com.sonofgillas.tvtimeneverdie/com.example.tvtimeneverdie.MainActivity
- iOS app: open the [/iosApp](./iosApp) directory in Xcode and run it from there.

### Running tests

Use the run button in your IDE's editor gutter, or run tests using Gradle tasks:

- Android tests: `./gradlew :shared:testAndroidHostTest`
- iOS tests: `./gradlew :shared:iosSimulatorArm64Test`

---

## TvTimeNeverDies

App in stile TV Time: cerca serie TV (dati da [TVmaze API](https://www.tvmaze.com/api)), segna gli episodi
visti, scrivi commenti testuali per episodio e tieni traccia di cosa stai guardando. Login e dati utente
sono su Firebase (Authentication + Firestore).

### Setup Firebase richiesto (da fare tu, non automatizzabile)

`androidApp/google-services.json` contiene credenziali del tuo progetto Firebase ed e' nel
**.gitignore** (non va mai committato). Copia [`google-services.json.example`](androidApp/google-services.json.example)
in `androidApp/google-services.json` e scarica quello vero dalla
[Firebase Console](https://console.firebase.google.com) del tuo progetto (Impostazioni progetto → Le tue app).
Prima di usare l'app devi, dalla stessa console:

1. **Authentication** → abilitare i provider **Email/Password** e **Google**.
   - Per Google Sign-In: dopo averlo abilitato, Firebase genera un "Web client ID" (OAuth). Devi
     **ri-scaricare `google-services.json`** e sostituire il file in `androidApp/`, altrimenti l'app
     compila ma il pulsante "Accedi con Google" fallira' con un errore chiaro (`default_web_client_id non
     trovato`). L'accesso con email/password funziona gia' senza questo passaggio.
   - Serve anche l'impronta SHA-1 del keystore di debug registrata sull'app Android in Firebase Console
     perche' Google Sign-In funzioni (`./gradlew signingReport` per ottenerla).
2. **Firestore Database** → crearne uno (modalita' produzione), stessa region del progetto.
3. Pubblicare le regole di sicurezza in [`firestore.rules`](firestore.rules) (Firestore → Regole, oppure
   `firebase deploy --only firestore:rules` se hai la Firebase CLI).

### Setup TMDB richiesto per la sezione Film

Le serie TV usano TVmaze (nessuna chiave richiesta), ma i **film** (Home/Ricerca/Profilo) usano
[TMDB](https://www.themoviedb.org) che richiede una API key gratuita:

1. Crea un account su themoviedb.org, poi vai in Impostazioni → API → richiedi una API key (v3 auth),
   gratuita e istantanea (nessuna carta di credito).
2. Copia [`TmdbConfig.kt.example`](shared/src/commonMain/kotlin/com/example/tvtimeneverdie/config/TmdbConfig.kt.example)
   in `TmdbConfig.kt` (stessa cartella) e sostituisci il placeholder `INSERISCI_LA_TUA_TMDB_API_KEY` con
   la tua chiave.

`TmdbConfig.kt` e' nel **.gitignore** (contiene una chiave API, non va mai committato) — solo il file
`.example` con il placeholder resta nel repository. Finche' `TmdbConfig.kt` non esiste o resta col
placeholder, le chiamate ai film falliscono con un errore visibile in UI (non un crash): le funzionalita'
sulle serie TV restano pienamente utilizzabili nel frattempo.

### Import dei dati da TV Time

Dal Profilo, il bottone "Carica i tuoi dati di TV Time" permette di selezionare lo zip dell'esportazione
GDPR di TV Time (richiedibile dall'app TV Time in Impostazioni → Richiedi i tuoi dati) per popolare
automaticamente le serie/film visti e da vedere. Note importanti:

- Vengono lette **solo** le informazioni di serie/film visti/da vedere: tutto il resto del contenuto
  dello zip (commenti, statistiche, dati account, ecc.) viene ignorato e non lasciato nel dispositivo.
- TV Time usa ID TheTVDB per le serie, non compatibili con gli ID TVmaze usati da questa app: l'abbinamento
  avviene cercando ogni titolo per nome sul catalogo (TVmaze/TMDB) e prendendo il primo risultato. Prima di
  scrivere qualsiasi dato viene mostrata una schermata di conferma con ogni abbinamento proposto, dove puoi
  deselezionare le voci sbagliate o non trovate.
- Per le serie, l'esportazione di TV Time da' solo un **conteggio** di episodi visti (non l'elenco esatto):
  l'import segna come visti i primi N episodi in ordine di stagione/numero, un'approssimazione.
  Il conteggio "N" combacia solo se hai sempre guardato le serie in ordine.
- Per i film, TV Time non esporta un elenco "da vedere": vengono importati solo come "visti" quelli il cui
  nome compare nei tuoi commenti/voti sull'app originale.
- Il file va scelto in formato `.zip`; su iOS questa funzione non e' ancora implementata (serve un
  selettore di documenti nativo, da fare su Mac).

### Limitazioni note

- **iOS**: la UI e la logica sono condivise (modulo `shared`), ma non e' verificabile in questo ambiente
  Windows (serve Xcode su Mac). Manca ancora l'aggiunta del pacchetto Firebase iOS SDK via Swift Package
  Manager e di `GoogleService-Info.plist` al target `iosApp` — vedi il commento in
  [`iosApp/iosApp/iOSApp.swift`](iosApp/iosApp/iOSApp.swift).
- **Google Sign-In su iOS**: non implementato (richiede setup nativo aggiuntivo su Mac); l'accesso
  email/password funziona su entrambe le piattaforme.

Learn more about [Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html)…