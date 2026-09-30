# Run Forge Launcher on your Android phone

## 1. Install Android Studio

Download and install the latest stable Android Studio. During setup, keep these selected:

- Android SDK
- Android SDK Platform
- Android Virtual Device (optional if you will use only your phone)

The project uses JDK 17. Android Studio normally supplies the correct JDK automatically.

## 2. Open the project

1. Extract `ForgeLauncher-v0.2-source.zip`.
2. Open Android Studio.
3. Select **Open**.
4. Select the extracted `ForgeLauncher` folder—the folder containing `settings.gradle.kts`.
5. Choose **Trust Project** if Android Studio asks.
6. Wait for **Gradle Sync** to finish.

If Android Studio says SDK 37.2 is missing:

1. Open **Tools → SDK Manager**.
2. Under **SDK Platforms**, enable **Show Package Details**.
3. Expand **Android 17 / API 37** and select **Android SDK Platform 37.2**.
4. Under **SDK Tools**, keep the latest Android SDK Build-Tools, Platform-Tools, and Command-line Tools selected.
5. Press **Apply**, accept the SDK license, then sync again.

API 37.2 is needed only to compile Haze 2.0. Forge still installs on Android 8.0 and newer because `minSdk` remains 26, and its runtime behavior target remains API 36.

### Optional quick APK test

The extracted project includes `dist/ForgeLauncher-v0.2-debug.apk`. You can copy it to the phone and open it for a quick test, allowing installation from that file app when Android asks. This is only a debug build.

Before later pressing **Run** from Android Studio, uninstall that supplied APK if Android shows `App not installed`, `incompatible update`, or a signature error. Android Studio creates a different local debug key, so its build cannot update an APK signed on another computer.

## 3. Enable developer mode on the phone

1. Open **Settings → About phone**.
2. Tap **Build number** seven times.
3. Enter your phone PIN if asked.
4. Return to Settings and open **Developer options**.
5. Turn on **USB debugging**.

The exact menu wording can differ on Samsung, Oppo, Redmi/Poco, Vivo, and other brands. Searching Settings for `Build number` and `USB debugging` is usually fastest.

## 4. Connect and run

1. Connect the phone to the laptop with a data-capable USB cable.
2. Choose **File transfer** if the phone asks for a USB mode.
3. Approve the **Allow USB debugging** prompt on the phone.
4. In Android Studio's top device selector, choose your phone.
5. Press the green **Run app** triangle.
6. Wait for installation; Forge Launcher opens automatically.

## 5. Make it the Home app

Tap **Set** in the banner and approve Forge Launcher. If the chooser does not appear, use:

**Phone Settings → Apps → Default apps → Home app → Forge Launcher**

Press the phone's Home gesture/button to test it.

## 6. Try the glass controls

1. Tap the settings icon on the Forge home screen.
2. Use the live preview at the top while changing **Light**, **Dark**, or **Auto**.
3. Choose a tint, then drag the transparency, blur, and corner sliders.
4. Close settings. Values are saved automatically with DataStore.

On Android 13 and newer, Haze blurs registered Forge content behind glass surfaces. On Android 12 and older, Forge automatically uses the lighter translucent tint, border, highlight, and shadow fallback. No permission prompt is needed for either path.

## 7. Rebuild after making changes

1. Save your files in Android Studio.
2. Choose **File → Sync Project with Gradle Files** after changing a dependency.
3. Press the green **Run app** triangle again.

For a clean command-line check, open Android Studio's Terminal tab and run:

```bash
./gradlew clean testDebugUnitTest lintDebug assembleDebug
```

The rebuilt APK is `app/build/outputs/apk/debug/app-debug.apk`.

## 8. Return to the original launcher

Open:

**Phone Settings → Apps → Default apps → Home app**

Select the launcher's original name, such as One UI Home, System Launcher, ColorOS Launcher, Pixel Launcher, or Funtouch OS Launcher.

Keep the original launcher installed during development.

## Common beginner fixes

### Phone is not listed in Android Studio

- Unlock the phone.
- Confirm the cable supports data, not charging only.
- Reconnect and approve the RSA debugging prompt.
- On Windows, install the phone maker's USB driver if required.
- In Android Studio, open **Tools → Troubleshoot Device Connections**.

### Gradle sync uses too much memory

This project already limits Gradle to 1.5 GB and two workers for a roughly 6 GB RAM laptop. Close browsers, emulators, and other heavy apps while building. Testing on a physical phone uses less RAM than running an emulator.

### Android says no default apps are available

Confirm `MainActivity` still has the `HOME` and `DEFAULT` categories in `AndroidManifest.xml`, then uninstall the development build and run it again.

### Blank or plain background

Choose a normal system wallpaper from Forge settings. Some vendor live wallpapers restrict third-party launcher previews; test with a still wallpaper first.

### Glass has no live blur

- Android 12 and older intentionally use the fallback.
- On Android 13+, make sure **Blur strength** is above `0 dp`.
- A system wallpaper is drawn in a separate Android window. Haze blurs Forge's registered in-window content, while the translucent tint still lets the real wallpaper show through.
