import SwiftUI

struct ContentView: View {
    @State private var selectedTab = 0
    @AppStorage("language") private var language = "hi"

    var isHindi: Bool { language == "hi" }

    var body: some View {
        TabView(selection: $selectedTab) {
            NavigationStack {
                HomeView(isHindi: isHindi)
            }
            .tabItem {
                Image("ic_home_custom")
                    .renderingMode(.template)
                Text(isHindi ? "होम" : "Home")
            }
            .tag(0)

            NavigationStack {
                PathshalaView(isHindi: isHindi)
            }
            .tabItem {
                Image("ic_pathshala")
                    .renderingMode(.template)
                Text(isHindi ? "पाठशाला" : "Pathshala")
            }
            .tag(1)

            NavigationStack {
                PoojanView(isHindi: isHindi)
            }
            .tabItem {
                Image("ic_puja")
                    .renderingMode(.template)
                Text(isHindi ? "पूजन" : "Poojan")
            }
            .tag(2)

            NavigationStack {
                SettingsView(isHindi: isHindi, onLanguageChange: { lang in
                    language = lang
                })
            }
            .tabItem {
                Image(systemName: "gearshape")
                Text(isHindi ? "सेटिंग्स" : "Settings")
            }
            .tag(3)
        }
        .tint(Color("Saffron"))
    }
}
