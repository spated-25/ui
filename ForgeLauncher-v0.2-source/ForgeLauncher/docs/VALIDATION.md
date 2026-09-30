# Validation report

Validated on 2026-09-30 with:

- Android Gradle Plugin 9.4.0
- Gradle 9.6.0
- JDK 17
- Compile SDK 37.2; target SDK 36; minimum SDK 26
- Compose BOM 2026.06.01
- Haze Blur 2.0.0
- Preferences DataStore 1.2.1

## Command passed

```bash
./gradlew clean testDebugUnitTest lintDebug assembleDebug
```

Results:

- Kotlin and Java compilation: passed
- Unit tests: 6 passed, 0 failed, 0 skipped
- Debug APK assembly: passed
- Android Lint: 0 errors, 1 intentional warning
- APK signature verification: passed using APK Signature Scheme v2
- Packaged manifest: confirmed `HOME`, `DEFAULT`, and ordinary `LAUNCHER` intent categories
- Packaged manifest: minimum API 26, target API 36, compiled API 37
- `QUERY_ALL_PACKAGES`: not present
- No runtime permission was added for glass or DataStore

## Debug APK

- File: `dist/ForgeLauncher-v0.2-debug.apk`
- Size: 13,176,250 bytes (about 12.6 MiB)
- SHA-256: `046501ca9107a0191f4feb00c47043f4e3f7e35b8850ea7c54ada7d96d5fb9cd`

The APK is signed with the standard Android debug certificate. It is suitable for development testing only. A future release build must use a private release keystore that is never committed to source control.

## Intentional lint notice

Lint reports one `OldTargetApi` warning. Haze 2.0 requires API 37.2 to compile, but Forge deliberately keeps `targetSdk = 36` until Android 17 behavior changes have been exercised on physical launchers. Compile SDK and target SDK are separate settings; this warning does not prevent the build or change Android 8.0+ installation support.

## Device testing still required

Automated build validation cannot reproduce every vendor launcher or graphics stack. Before treating version 0.2 as device-certified, test:

- Home-role approval and recovery to the original launcher
- App and work-profile visibility and launching
- Still and live wallpaper behavior
- Android 13+ Haze blur, scrolling, and tap animation performance
- Android 12-or-older translucent fallback
- Glass settings after closing and reopening the app
- Back and swipe gestures
