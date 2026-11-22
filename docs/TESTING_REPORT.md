# Testing report

Honest status of every deliverable: what was actually run, what passed, and what still needs a device.

## 1. Verified

| Check | How | Result |
|---|---|---|
| Android Kotlin compiles | JDK 17 + Gradle 8.11.1 + Android SDK 35, `./gradlew assembleDebug` locally | `:app:compileDebugKotlin` and `:app:kspDebugKotlin` passed; only deprecation warnings |
| Android APK builds | GitHub Actions run **44987292439** on `rabha-privacy-monitor` branch `rabha-monitor-v2` | `build` job succeeded in **3m 31s**; artifact `rabha-monitor-debug-apk` |
| APK identity | `aapt2 dump badging app-debug.apk` | package `com.rabha.privacymonitor.debug`, versionName 1.0.0, minSdk 29, targetSdk 35, compileSdk 35, 42 MB |
| Admin panel typecheck | `npx tsc -b` | No errors |
| Admin panel build | `npm run build` (Vite 5) | 129 modules transformed, `dist/` written |
| Admin panel in CI | GitHub Actions `admin` job | `npm ci` + `tsc -b` + `vite build` succeeded in 18 s |
| Control registry | counted entries in `src/lib/config.ts` | 60 controls, 60 unique ids, 11 groups |
| SQL migrations | written and reviewed; not executed (no project touched, as agreed) | — |

## 2. Compile errors found and fixed

The first real Kotlin compile surfaced 13 errors. They were fixed rather than papered over:

1. `AuthRepository` — `signInWith` / `signUpWith` / `signOut` return values leaked into `RabhaResult<Unit>`; each block now ends with `Unit`.
2. `NetworkStatsRepository` — the android-35 stub returns a **`NetworkStats.Bucket`** from `querySummaryForDevice`, not a `NetworkStats`, and iteration is `getNextBucket(Bucket)` (a method, not a property). Device totals and per-app traffic now share one stable path through `querySummary(...)` + `getNextBucket(bucket)`, which behaves the same on API 29–35.
3. `PermissionsRepository` — `PackageInfo.applicationInfo` is nullable; it is null-checked before `getApplicationLabel`.
4. `ConsentManager` — the RPC helper is the extension `io.github.jan.supabase.postgrest.rpc` (in `PostgrestRpcKt`); the import was missing.
5. `AnimatedWallpaperService` — `WallpaperService.Engine` does **not** implement `SurfaceHolder.Callback`; the engine implements it and registers itself in `onCreate`.
6. `material-icons-extended` was removed (it pulls thousands of classes for five icons). Two missing icons (`Monitor`, `Wallpaper`) are now local `ImageVector`s in `core/RabhaIcons.kt`.

## 3. Not verified here — and how to check

| Item | Why not | How to check |
|---|---|---|
| App runtime behaviour | No emulator or device in this sandbox | Install `rabha-monitor-debug.apk` on Android 10+ and walk the list below |
| Supabase migrations | No project was touched, by design | Run the three files in the SQL editor; run them twice to prove idempotency |
| RLS behaviour | Needs a live project with two users | Sign in as A, confirm B's rows are invisible; confirm `delete_own_account()` cannot touch another id |
| Wallpaper engines | Needs a device and the system picker | Apply each engine, lock/unlock, watch battery |
| Ad engine | Needs campaigns in the database | Insert a campaign + creative, open the app three times inside the interval, confirm one impression |
| Admin panel visuals | Rendered only in CI build; no browser session against a live project | `npm run dev` with real `.env.local`, sign in with an `admin_roles` account |

## 4. Manual test plan (Definition of Done)

1. Install on Android 10+; the dashboard opens without crashing.
2. Monitoring is **off** until Start is pressed; then a notification appears that cannot be dismissed.
3. Pause from the notification — it says paused and reads stop. Stop — monitoring ends and consent is withdrawn.
4. Without Usage Access, Network and Usage show a grant prompt with the reason, never zeroes.
5. Grant Usage Access, reopen — real totals and a 7-day chart from real buckets.
6. Files: pick a file; only its metadata is recorded; nothing is uploaded.
7. Privacy: this app's mic/camera state reads; other-app rows say Unavailable with the exact blocking permission.
8. Import an image, a video and an HTML page; apply each; the system picker must appear (silent switching is impossible).
9. Turn Cloud upload on and sign in; only aggregated summaries reach `monitoring_summaries`.
10. Delete my account: local tables cleared, server row gone, `audit_logs` entry written.
11. Airplane mode: local screens work; remote sections show the offline/unavailable state.

## 5. Build fixes from the master prompt, respected

`buildConfigField` inside `defaultConfig` · `signingConfigs` before `buildTypes` · `buildToolsVersion = "35.0.0"` ·
supabase-kt 2.6.0 with Ktor 2.3.12 · `runCatchingResult` is `suspend` · no `clientPackageName` ·
shared composables are `internal` · colours use `Color(0xFF…)` literals · per-app traffic does not use
`queryDetailsForUid` (see `LIMITS.md`) · Room `exportSchema = false`.
