# NOCTRA — SPEC (condensed from the 2026-11-11 mega-prompt, 28 sections)

Product: NOCTRA — "Time Beyond Ordinary." Premium Android clock + wallpaper
ecosystem. ONE unified app. Package com.imdadburki.noctra, minSdk 26 (his Y85A,
Android 8.1), targetSdk 36, compileSdk 37. Version 1.0.0. MIT open source intent
(repo when he asks; same pattern as Aurelius/CROPCAST).

## Standing rules (his orders)
- FINAL WORKING APK ONLY. No phase-by-phase installs; find/fix bugs myself;
  deliver the final APK only. (CROPCAST order 2026-11-10 ~19:41.)
- Build ONLY via ~/workspace/noctra/build.sh (proven Aurelius/CROPCAST recipe:
  JDK 17 ~/workspace/jdk, SDK ~/workspace/android-sdk, GRADLE_USER_HOME
  ~/workspace/vyro_studio/.gradle, IPv4 flags, local-maven FIRST).
- gradle.properties CLEAN (no secrets). No INTERNET permission unless a feature
  genuinely needs it — the core app is fully offline.
- No fake functionality: every control must modify real output; never claim an
  alarm/timer/export succeeded when it did not.

## Scope decisions (honest, per prompt §0: closest functional alternative + document)
- BACKEND / CLOUD (§17/18: PostgreSQL, REST, CDN, auth, server moderation):
  NO real server — no paid services, no credentials exist, nothing to host it on.
  Local-first instead: Room DB for users/profiles/wallpapers/favorites/downloads/
  submissions; creator profiles + submissions stored on-device; share/export via
  files. Cloud sync / server-side moderation / 10M-user scaling are documented
  limitations, NOT claimed as done. Backend stays optional per §17.
- DISCOVERY content: built-in collections (all named collections from §5:
  AMOLED, Mafia/Dark Luxury, Minimal, Retro, Cinematic, Futuristic + additional)
  rendered locally as real clock wallpapers — no network catalogue, no downloads
  from a server. "Downloads" = exports saved to the device.
- WEATHER widget (§7): no weather API key exists → weather widget shows a
  graceful "not connected" state, never fake weather.

## Modules (16)
:app, :core:common (models: 30 digital styles, 15 analog styles, wallpaper,
alarm, city), :core:designsystem (Noctra midnight+gold theme), :core:database
(Room), :core:datastore (settings), :core:clock (Canvas rendering engine —
digital + analog + hybrid), :core:wallpaper (export PNG/JPEG/WebP + apply via
WallpaperManager), :feature:home, :feature:clock (main clock + world clock),
:feature:alarm (alarm + timer + stopwatch), :feature:discover, :feature:editor,
:feature:library, :feature:profile (local creator), :feature:settings,
:service:livewallpaper (WallpaperService, battery-conscious).

## Features (all real, on-device)
- Clock engine: shared model; 30 digital styles (Standard, Seven Segment LED,
  Minimal Thin, Bold, Ultra Thin, Oversized, Vertical, Split, Outline, Frame,
  Retro, Amber/Red/Green LED, Matrix, Dot Matrix, LCD, Flip, Word, Typography,
  Monospace, Editorial, Floating, Perspective, Gradient, Glass, Minimal Mono,
  Terminal, Cyber, Experimental); 15 analog styles (Classic, Minimal, Roman,
  Modern Luxury, Vintage, Skeleton, Compass, Minimal Dot, Geometric,
  Monochrome, Gold Accent, Black Edition, Contemporary, Scientific, Abstract);
  hybrid (digital+analog, +date, +battery, +calendar, +weather-stub).
- World clock: city search, multiple zones, DST-aware via java.time, favorites.
- Alarm: Room-backed CRUD, AlarmManager, exact-alarm permission per API level,
  repeat weekdays, label, snooze/dismiss, vibration. Never claim scheduled when
  scheduling failed.
- Timer: countdown, presets, pause/resume/reset, completion notification+sound.
- Stopwatch: elapsed from SystemClock.elapsedRealtime (never a timer loop), laps.
- Editor: background (color/gradient/blur/brightness), clock style/font/size/
  color/position/glow/opacity, date controls, widgets (battery/calendar/text),
  undo/redo/reset/save/duplicate/export/preview/share — all modifying real output.
- Export: PNG/JPEG/WebP, 720p/1080p/1440p/native, correct aspect, background
  processing, progress/cancel/retry, save location.
- Widgets: AppWidgetProvider + RemoteViews (Minimal Digital, Seven Segment,
  Analog, Flip, Vertical, Date, Battery, World, Hybrid, Luxury Analog, Retro,
  Custom) + configuration.
- Wallpaper apply: Home / Lock / Both / Save to Gallery / Share via WallpaperManager.
- Live wallpaper: WallpaperService, clock rendering, pause when not visible.
- Library: Favorites, Downloads, My Designs, Recently Viewed/Downloaded,
  Collections, Exported, Recently Used Clocks — Room-backed, search/sort/delete.
- Settings: Appearance (Dark/AMOLED/Light/System, accent), Clock (12/24h,
  seconds, date, default style), Notifications, Privacy (clear cache/history,
  export data, delete account), General, About (version, licenses, GitHub).
- Design system: buttons, cards, nav, chips, dialogs, bottom sheets, toasts,
  progress, skeletons, empty/error states, color + typography pickers.
- Tests: unit (clock math, geometry, state), export tests, alarm tests;
  JUnit4 (kotlin.test NOT on classpath — proven 2026-11-08).

## Build lessons reused (from AGENTS.md)
- Compose Material3 ships NO XML themes — Activity theme parent
  android:Theme.Material.NoActionBar.
- KSP + built-in Kotlin needs android.disallowKotlinSourceSets=false.
- Room must be `api` in :core:database.
- Gradle journal lock after killed daemon → just re-run.
- cacerts drift → ~/workspace/jdk/rebuild-cacerts.sh.
- Gradle up-to-date quirk: check APK timestamp after annotation-only changes.
- No KVM → no local emulator; device tests via VibeView (minutes return 1 Dec
  2026) or his Y85A.

## Progress log
- 2026-11-11 ~16:52: prompt received, spec saved, scaffold started.
- 2026-11-11 ~17:00 PKT — scaffold complete: 16 modules, all screens/engines/tests written.
- 2026-11-11 ~17:03 PKT — FIRST BUILD FAILED: :core:clock Long→Int literals, min() type mix, missing drawIntoCanvas/nativeCanvas imports.
- 2026-11-11 ~17:05–17:14 PKT — fixes in 4 rounds: (1) hex literals `.toInt()` in AnalogCanvas/DigitalCanvas + drawIntoCanvas/nativeCanvas imports; (2) livewallpaper Engine → inner class, formatTime/formatDate imports, WallpaperExporter smart-cast + Result<Unit>; (3) Room `api` in :core:database, material-icons-extended in all library modules, NoctraButton onClick-last (trailing-lambda fix), builtInDesigns moved to :core:common; (4) formatDateShort import in widget provider.
- 2026-11-11 ~17:14 PKT — ✅ BUILD SUCCESSFUL: APK 20,874,271 B. Tests 27/27 pass (ClockTime 16, Format 7, BuiltInDesigns 4). Lint: 1 NewApi error → fixed (windowLightNavigationBar moved to values-v27/themes.xml); rebuild 17:22 clean, apksigner verified (debug cert), APK 21,351,820 B, md5 a2cc103a95c9590fcb3853418420bb0a.
- 2026-11-11 ~17:26 PKT — ✅ v1.0.0 DEBUG APK DELIVERED (link + attachment; first upload approval expired, retry approved). Honest limits stated: no device test possible (VibeView minutes return 1 Dec 2026) — his Y85A is the check; widget ships Minimal Digital style first (other 11 layouts share the provider+engine, land next pass); weather widget is a stub; no backend/cloud (local-first by design).
- 2026-11-11 ~18:07 PKT — Imdad's v1 feedback: UI kharab/bigda hua; Profile hatao, simple About dalo; clocks sab ek jaisy — 150+ alag clocks chahiye (tall text wale bhi, sab types); un-needed features hatao (clocks + ek aur bas); khudki clocks banane wala section hatao; wallpaper pe lagate hi clock ruk jata hai (live nahi hoti); Pinterest se variations dhoondho.
- 2026-11-11 ~18:07–18:18 PKT — ✅ **v2.0.0 REBUILT**: Pinterest research (flip/neon/word/LED/dot-matrix/terminal/minimal/roman/skeleton/sport); removed modules alarm/editor/library/profile/settings + WorldClock + widgets; 4 tabs only (Home/Clocks/Wallpapers/About); **150-clock catalogue** (120 digital = 6 layouts × 10 treatments × 2 colourways; 30 analog = 10 dials × 3 hands) with new layout×treatment engine + legacy v1 style mapping; UI rebuilt (320dp hero clock, big-preview 2-col grid, search + filters); **live wallpaper fix** (reads selected clock from DataStore, loop restarts on surface change, try/catch per frame, one-tap Set Live Wallpaper intent; static vs live labelled everywhere); About = simple, no profile. BUILD SUCCESSFUL, 26/26 tests pass (catalog 6 + ClockTime 16 + BuiltInDesigns 4), lint clean, APK 20,675,968 B, md5 cdb47a0138e7e47998575433a32879b9, v2.0.0/versionCode 2. Delivered 18:24 PKT. Honest limits: no device test possible — his Y85A is the check; v1 favourites reset once (DB v2 destructive migration); weather stub stays.
- 2026-11-11 ~19:57 PKT — Imdad's v2 verdict: rest is fine, but (a) language must be ENGLISH, (b) clocks layout fully broken — text jumbled/up-down, date text appears OVER the clock. "Fix All the errors."
- 2026-11-11 ~19:57–20:17 PKT — ✅ **v2.0.1 REBUILT** (all errors fixed): (1) **ALL UI strings Roman Urdu → English** (Home/Clocks/Wallpapers/About + status messages; verified by grep — zero Urdu strings left; stale widget_description removed). (2) **Overlap root causes found + fixed**: AM/PM was drawn INSIDE the last digit row (TALL/STACKED) and at the rect bottom under the seconds line (SPLIT); the SPLIT seconds line crossed the HH/MM halves' bottom edge; the exporter drew a SECOND date line over the engine's (digital faces). (3) **Strip-based layout** in drawDigitalClock: rect split up-front into reserved content / AM-PM / date strips (digitalStripRects + isInlineAmPm, pure FloatRect geometry so unit tests can prove it); time, AM/PM and date can never share area in ANY layout; inline AM/PM sits right of the measured time on the same baseline with the time shrunk 12% so it never clips; SPLIT = halves top 72% + seconds row bottom 28%; exporter date line now ANALOG-only. (4) **Grid preview aspect follows layout** (TALL = portrait 0.62 card, STACKED 0.9, others 1.6) so tall digits no longer drown in empty sides. (5) New **ClockStripTest (5 tests)** — strips never overlap across all 6 layouts × 10 treatments × date × AM/PM on grid + hero rects, date strip only STANDARD/WIDE, AM/PM strip only when not inline, layout lines never overlap each other, SPLIT seconds below halves. First run caught a real test-infra trap: android.graphics.RectF methods are NOT mocked in JVM unit tests — geometry moved to pure FloatRect with toRectF() at the Canvas boundary. BUILD SUCCESSFUL, **31/31 tests pass** (catalog 6 + strip 5 + ClockTime 16 + designs 4), lint clean, APK 20,726,313 B, md5 09907d5bb4ce860d5b0d4c08d2e52b29, v2.0.1/versionCode 3, apksigner verified. Delivered ~20:20 PKT. Honest limits unchanged: no device test — his Y85A is the check.
