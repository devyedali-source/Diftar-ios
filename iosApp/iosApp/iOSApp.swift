import SwiftUI
import ComposeApp

struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: UIViewControllerRepresentableContext<ComposeView>) -> UIViewController {
        MainViewControllerKt.MainViewController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: UIViewControllerRepresentableContext<ComposeView>) {}
}

@main
struct iOSApp: App {
    @Environment(\.scenePhase) private var scenePhase

    /// مهلة إضافية عند مغادرة التطبيق حتى يكتمل النسخ الاحتياطي إلى Google Drive
    private func requestBackgroundTime() {
        var taskId: UIBackgroundTaskIdentifier = .invalid
        taskId = UIApplication.shared.beginBackgroundTask(withName: "drive_backup") {
            UIApplication.shared.endBackgroundTask(taskId)
            taskId = .invalid
        }
        DispatchQueue.main.asyncAfter(deadline: .now() + 25) {
            if taskId != .invalid {
                UIApplication.shared.endBackgroundTask(taskId)
                taskId = .invalid
            }
        }
    }

    var body: some Scene {
        WindowGroup {
            ComposeView()
                .ignoresSafeArea(.all)
                .onOpenURL { url in
                    MainViewControllerKt.handleDeepLink(url: url.absoluteString)
                }
                .onChange(of: scenePhase) { phase in
                    if phase == .background { requestBackgroundTime() }
                }
                .onContinueUserActivity(NSUserActivityTypeBrowsingWeb) { activity in
                    if let url = activity.webpageURL {
                        MainViewControllerKt.handleDeepLink(url: url.absoluteString)
                    }
                }
        }
    }
}
