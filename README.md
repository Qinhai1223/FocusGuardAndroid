# FocusGuard Android — strict-focus starter project

A minimal Kotlin Android project demonstrating:
- A configurable focus timer
- A package-name blocklist
- An AccessibilityService that detects foreground apps and opens a blocking screen
- A foreground service notification for the running timer

## Important limitations
This is a starter implementation, not a tamper-proof kiosk. A normal app cannot guarantee that users cannot force-stop it, revoke permissions, reboot around it, or use system UI. The accessibility service is used only for the user-configured focus blocklist; explain this clearly to users and do not hide the service.
The app does not read message content or transmit data. Settings are stored locally in SharedPreferences.

## Build
1. Install Android Studio (current stable) and Android SDK Platform 35.
2. Open this folder as a project.
3. Let Gradle sync. Use JDK 17.
4. Build > Build Bundle(s) / APK(s) > Build APK(s).
5. The debug APK is usually at `app/build/outputs/apk/debug/app-debug.apk`.

Alternatively, from a terminal with Android SDK and Gradle available:
`gradle assembleDebug`

## Install and use
1. Install the APK on your Android phone.
2. Open FocusGuard and enter app package IDs separated by commas. Example: `com.example.game, com.example.social`
3. Tap **Save blocklist**.
4. Tap **Open Accessibility Settings** and enable FocusGuard's accessibility service.
5. Set minutes and tap **Start strict focus**.
6. During a session, opening a listed app displays a block screen. The session ends automatically when the timer expires.

To find a package name, use Android Studio / ADB (`adb shell pm list packages`) or an app-info utility. Avoid blocking system UI, Settings, Phone, or the launcher during initial testing.

## What this prototype intentionally does not do
- No Device Owner / kiosk lockdown
- No notification-listener access
- No overlay permission
- No cloud sync, analytics, ads, or account system
- No claim that it can prevent force-stop or permission revocation

## Test checklist
- Start a 1-minute session and confirm the notification countdown.
- Add a harmless test app to the blocklist and open it.
- Verify the block screen appears.
- Let the timer expire and verify the blocked app can be opened.
- Disable the accessibility service and verify the app explains why blocking no longer works.


## Build APK automatically with GitHub Actions (phone-friendly)

This project includes `.github/workflows/build-apk.yml`.

1. Create a GitHub repository, for example `FocusGuardAndroid`.
2. On GitHub, choose **Add file → Upload files** and upload the *contents* of this project folder, including the hidden `.github` directory. If the mobile website hides dot-folders or makes uploads awkward, use GitHub's browser editor or upload the ZIP on a computer.
3. Commit the files to the `main` branch.
4. Open the repository's **Actions** tab and select **Build FocusGuard APK**.
5. If the workflow has not run automatically, choose **Run workflow**.
6. Wait for the run to finish successfully. Open that run, scroll to **Artifacts**, and download `FocusGuard-debug-APK`.
7. Extract the artifact ZIP; inside is `app-debug.apk`. Transfer/install it on your Android phone. Android may ask you to allow installation from that browser/file manager.

The workflow builds a **debug APK**, suitable for personal testing, not a signed release for Play Store publishing. The first build can take several minutes. If the build fails, open the workflow run and copy the first relevant error from the build log.

No GitHub token or secret is needed for this workflow. Do not add passwords, signing keys, or personal data to the repository.
