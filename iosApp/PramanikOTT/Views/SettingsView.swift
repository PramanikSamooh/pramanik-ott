import SwiftUI
import FirebaseAuth
import GoogleSignIn

struct SettingsView: View {
    let isHindi: Bool
    let onLanguageChange: (String) -> Void
    @State private var isSignedIn = Auth.auth().currentUser != nil
    @State private var userEmail = Auth.auth().currentUser?.email ?? ""

    var body: some View {
        List {
            // Language
            Section(header: Text(isHindi ? "भाषा" : "Language")) {
                Button(action: { onLanguageChange("hi") }) {
                    HStack {
                        Text("हिंदी")
                        Spacer()
                        if isHindi { Image(systemName: "checkmark").foregroundColor(Color("Saffron")) }
                    }
                }
                Button(action: { onLanguageChange("en") }) {
                    HStack {
                        Text("English")
                        Spacer()
                        if !isHindi { Image(systemName: "checkmark").foregroundColor(Color("Saffron")) }
                    }
                }
            }

            // Account
            Section(header: Text(isHindi ? "खाता" : "Account")) {
                if isSignedIn {
                    HStack {
                        Image(systemName: "person.circle.fill")
                            .foregroundColor(Color("Saffron"))
                        Text(userEmail)
                            .font(.subheadline)
                    }
                    Button(role: .destructive, action: signOut) {
                        Text(isHindi ? "साइन आउट" : "Sign Out")
                    }
                } else {
                    Button(action: signIn) {
                        HStack {
                            Image(systemName: "person.crop.circle.badge.plus")
                            Text(isHindi ? "Google से साइन इन करें" : "Sign in with Google")
                        }
                    }
                }
            }

            // About
            Section(header: Text(isHindi ? "जानकारी" : "About")) {
                HStack {
                    Text(isHindi ? "संस्करण" : "Version")
                    Spacer()
                    Text(Bundle.main.infoDictionary?["CFBundleShortVersionString"] as? String ?? "3.0")
                        .foregroundColor(.secondary)
                }
            }
        }
        .navigationTitle(isHindi ? "सेटिंग्स" : "Settings")
    }

    func signIn() {
        guard let rootVC = UIApplication.shared.connectedScenes
            .compactMap({ $0 as? UIWindowScene })
            .first?.windows.first?.rootViewController else { return }

        GIDSignIn.sharedInstance.signIn(withPresenting: rootVC) { result, error in
            guard let user = result?.user, let idToken = user.idToken?.tokenString else { return }
            let credential = GoogleAuthProvider.credential(withIDToken: idToken, accessToken: user.accessToken.tokenString)
            Auth.auth().signIn(with: credential) { authResult, error in
                if let authResult = authResult {
                    isSignedIn = true
                    userEmail = authResult.user.email ?? ""
                }
            }
        }
    }

    func signOut() {
        try? Auth.auth().signOut()
        GIDSignIn.sharedInstance.signOut()
        isSignedIn = false
        userEmail = ""
    }
}
