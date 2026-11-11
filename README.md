# NOCTRA — Time Beyond Ordinary.

NOCTRA is a premium Android clock + wallpaper app. **150 different clocks**
and 70 built-in wallpapers — everything on-device, offline, no accounts,
no ads, no tracking.

- **150 clocks** — 120 digital (6 layouts × 10 treatments × 2 colourways:
  standard, tall vertical text, stacked rows, split, compact, wide × solid,
  seven-segment LED, dot-matrix, flip, word, neon, gradient, outline, glass,
  terminal) and 30 analog (10 dials × 3 hand styles: classic, gold accent,
  vintage, skeleton, compass, scientific, abstract, minimal, roman, sport).
- **Live wallpaper that actually ticks** — pick any clock, set it as your
  live wallpaper, and it keeps ticking on the home screen. A static wallpaper
  is just a picture; NOCTRA labels the two clearly so you always know which
  one you're setting.
- **70 built-in wallpapers** in 7 collections, each with its own clock face.
- **Native Android** — Kotlin + Jetpack Compose, Modular Clean Architecture +
  MVVM, Room + DataStore, minSdk 26 (Android 8.1), targetSdk 36.
- **Privacy first** — no INTERNET permission, no API keys, no accounts, no
  analytics. Your favourites live in a local Room database.

## Status — v2.0.1 (2026-11-11)

150-clock catalogue, English UI throughout, and a strip-based clock layout
engine: time, AM/PM and date each get their own reserved strip, so text can
never overlap in any layout (regression-tested across every layout ×
treatment). Live wallpaper reads your selected clock and restarts its loop
on surface changes.

## Build

```bash
./build.sh :app:testDebugUnitTest :app:assembleDebug
```

Requirements: JDK 17, Android SDK with compileSdk 37, Gradle 9.6.0
(provided via the wrapper). The APK lands at
`app/build/outputs/apk/debug/app-debug.apk`.

Release signing is configured only with owner-provided credentials — never
commit a keystore or passwords.

## License

MIT — see [LICENSE](LICENSE).
