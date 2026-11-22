# Testing report

Honest status of every deliverable, and what was actually run versus what could not be run here.

## Verified in this environment

| Check | Command | Result |
|---|---|---|
| Admin panel typecheck | `cd admin && npx tsc -b` | Passed, no errors |
| Admin panel production build | `cd admin && npm run build` | Passed — 129 modules, `dist/` written |
| Control registry completeness | counted `id:` entries in `src/lib/config.ts` | 60 controls, 60 unique ids, 11 groups |
| SQL syntax review | read-through of the three migrations | No unbalanced statements; all `create` guarded |

## Not verified here (and how to verify)

| Item | Why it could not run here | How to check |
|---|---|---|
| Android compile | No JDK, no Gradle, no Android SDK in this sandbox | Push and run the **Build Android APK** workflow; it compiles on GitHub's runner and uploads `app-debug.apk` |
| App runtime behaviour | Android apps cannot run in a Linux container without an emulator | Install the APK on an Android 10+ device and walk the Definition of Done |
| Supabase migrations | No project was touched (by design) | Run the three files in the SQL editor; the second run must also succeed |
| RLS behaviour | Needs a live project with two users | Sign in as user A, confirm user B's rows are invisible; confirm `delete_own_account()` cannot touch another id |
| Wallpaper engines | Needs a device and a wallpaper picker | Apply each engine, lock and unlock the screen, check battery use |
| Ad engine | Needs campaigns in the database | Insert a campaign and a creative, open the app three times inside the interval, confirm no second impression |

## Manual test plan (Definition of Done)

1. Install on Android 10+; the app opens on the dashboard without crashing.
2. Monitoring is **off** until you press Start; a notification appears and cannot be dismissed.
3. Press Pause in the notification — the notification says paused and the reads stop.
4. Press Stop — monitoring ends and consent is withdrawn.
5. Without Usage Access, the Network and Usage tabs show a grant prompt with the reason, not zeroes.
6. Grant Usage Access, reopen — real totals appear, plus a 7-day chart from real buckets.
7. Pick a file in Files — only its metadata is recorded; nothing is uploaded.
8. Privacy screen: own-app mic/camera state reads; other-app rows say Unavailable with the reason.
9. Import an image, a video and an HTML page; apply each; confirm the system picker appears.
10. Turn Cloud upload on, sign in, and confirm only aggregated summaries reach `monitoring_summaries`.
11. Delete my account: local tables are cleared and the server row is gone.
12. Airplane mode: every local screen still works, remote sections show the offline/unavailable state.

## Build fixes already applied

The ten fixes listed in section 10 of the master prompt are respected:

1. `buildConfigField` is inside `defaultConfig`.
2. `signingConfigs` is declared before `buildTypes`.
3. `buildToolsVersion = "35.0.0"` is set explicitly.
4. supabase-kt **2.6.0** with Ktor **2.3.12** (no `gotrue-kt:3.1.4`).
5. `runCatchingResult` is a `suspend` function.
6. `AudioRecordingConfiguration.clientPackageName` is not used anywhere.
7. Composables shared across files are `internal`.
8. Colours are declared as `Color(0xFFFFB020)` (`Long` literal inside `Color(...)`), so no `Int` cast is needed.
9. Per-app traffic uses `querySummary` rather than `queryDetailsForUid` — see `docs/LIMITS.md` item 3.
10. Room uses `exportSchema = false`.
