import SwiftUI

struct PathshalaView: View {
    let isHindi: Bool
    @StateObject private var viewModel = PathshalaViewModel()

    var body: some View {
        ScrollView {
            VStack(spacing: 16) {
                if viewModel.isLoading {
                    ProgressView()
                        .tint(Color("Saffron"))
                        .frame(maxWidth: .infinity, maxHeight: .infinity)
                } else if viewModel.todaysClasses.isEmpty {
                    VStack(spacing: 8) {
                        Image(systemName: "calendar.badge.exclamationmark")
                            .font(.largeTitle)
                            .foregroundColor(.secondary)
                        Text(isHindi ? "आज कोई कक्षा नहीं है" : "No classes today")
                            .foregroundColor(.secondary)
                    }
                    .padding(.top, 60)
                } else {
                    ForEach(viewModel.todaysClasses, id: \.id) { cls in
                        PathshalaClassCard(cls: cls, isHindi: isHindi)
                    }
                }

                // Schedule
                if !viewModel.classesByDay.isEmpty {
                    Text(isHindi ? "साप्ताहिक कार्यक्रम" : "Weekly Schedule")
                        .font(.headline)
                        .padding(.top, 24)

                    ForEach(Array(viewModel.classesByDay.keys.sorted()), id: \.self) { day in
                        let dayName = ["Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"][day]
                        VStack(alignment: .leading, spacing: 4) {
                            Text(dayName)
                                .font(.subheadline)
                                .fontWeight(.bold)
                                .foregroundColor(Color("Saffron"))

                            ForEach(viewModel.classesByDay[day] ?? [], id: \.id) { cls in
                                Text("• \(cls.getTitle(isHindi: isHindi)) - \(cls.time)")
                                    .font(.caption)
                                    .foregroundColor(.secondary)
                            }
                        }
                        .frame(maxWidth: .infinity, alignment: .leading)
                        .padding(.horizontal, 16)
                    }
                }
            }
            .padding()
        }
        .background(Color("Background"))
        .navigationTitle(isHindi ? "पाठशाला" : "Pathshala")
        .task {
            await viewModel.loadClasses()
        }
    }
}

struct PathshalaClassCard: View {
    let cls: PathshalaClass
    let isHindi: Bool

    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            Text(cls.getTitle(isHindi: isHindi))
                .font(.headline)
                .foregroundColor(.primary)

            Text("\(cls.time) IST")
                .font(.subheadline)
                .fontWeight(.bold)
                .foregroundColor(Color("Saffron"))

            if !cls.youtubeLink.isEmpty {
                Button(action: {
                    if let url = URL(string: cls.youtubeLink) {
                        UIApplication.shared.open(url)
                    }
                }) {
                    Text(isHindi ? "कक्षा में शामिल हों" : "Join Class")
                        .font(.subheadline)
                        .fontWeight(.medium)
                        .foregroundColor(.white)
                        .padding(.horizontal, 16)
                        .padding(.vertical, 8)
                        .background(Color("Saffron"))
                        .cornerRadius(8)
                }
            }
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding()
        .background(Color("CardBackground"))
        .cornerRadius(12)
    }
}
