import SwiftUI
// Richiede il pacchetto Firebase iOS SDK aggiunto via Swift Package Manager in Xcode
// (https://github.com/firebase/firebase-ios-sdk) e GoogleService-Info.plist nel target iosApp.
// Non ancora installabile/verificabile da questa macchina (serve Xcode su Mac).
import FirebaseCore

@main
struct iOSApp: App {
    init() {
        FirebaseApp.configure()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
