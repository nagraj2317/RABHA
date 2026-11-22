# Hard limits — what Android will not let this app do

Every item below is implemented as an explicit `Unavailable` state that names the real reason.
The app never shows a status it cannot prove.

| # | Claim | Reality | Where it surfaces |
|---|---|---|---|
| 1 | Other apps' microphone / camera activity | No public API. `AppOpsManager` reads for other apps need `GET_APP_OPS_STATS` (signature level). `AudioManager.getActiveRecordingConfigurations()` needs `MODIFY_AUDIO_ROUTING`, and `AudioRecordingConfiguration` never exposes the client package name. The Android 12+ green dot is the only real source and it is not readable by an app. | `privacy/PrivacySignalsRepository.kt` |
| 2 | Other apps' granted permissions | Only the *declared* permissions are readable from the manifest. The panel and the app say "declares", never "granted". | `permissions/PermissionsRepository.kt` |
| 3 | Other apps' private files | Impossible without root. Only files the user picks through the Storage Access Framework are recorded. | `files/FileMetadataRepository.kt` |
| 4 | Per-app traffic without Usage Access | `NetworkStatsManager` returns nothing. The UI shows a grant prompt, never zeroes that look like data. | `network/NetworkStatsRepository.kt` |
| 5 | Mobile-data totals | Need a subscriber id, which needs `READ_PHONE_STATE`. Without it only Wi-Fi totals are shown, and the reason is stated. | `network/NetworkStatsRepository.kt` |
| 6 | Silent live-wallpaper switching | Android requires confirmation in the system picker. Every Apply button opens that picker. | `wallpaper/WallpaperInstaller.kt` |
| 7 | VPN payload inspection | Not implemented and not offered. The packet counter reads the tun descriptor, adds up byte counts and discards every payload. No TLS interception, no certificates, no payload parsing. | `network/RabhaVpnService.kt` |

## Known limitations of this build

1. **The packet counter interrupts connectivity while it runs.** There is no userspace forwarder, so
   counted packets are not re-injected. It is a short measurement tool, and the UI says so before you
   start it. Adding forwarding (or a SOCKS path) is the next step if this should be usable for long runs.
2. **The HTML wallpaper engine uses a WebView added through `WindowManager`.** This is the standard
   approach for HTML live wallpapers, but the exact behaviour varies by OEM. If a device refuses the
   window, the engine draws a message on the wallpaper surface instead of failing silently.
   `addJavascriptInterface` is never called, `allowFileAccessFromFileURLs` is false and
   `blockNetworkLoads` is true, so an uploaded page cannot read other files or reach the network.
3. **`NetworkStatsManager.queryDetailsForUid` was not used.** Per-app traffic is summed from
   `querySummary(...)` buckets, which has a stable signature across API 29–35. Verify per-app numbers on
   a real device before shipping.
4. **The Android module layout is one Gradle module (`:app`) with 15 packages**, not 15 Gradle modules.
   The architecture (one repository, one ViewModel, one service per area) is intact; the packaging is
   flatter so the build stays reproducible on a CI runner. Splitting into `:core`, `:network`, … is a
   mechanical change if you want it.
5. **Release signing is not configured.** `assembleRelease` produces an unsigned APK unless you provide
   `RELEASE_KEYSTORE_PATH`, `RELEASE_KEYSTORE_PASSWORD`, `RELEASE_KEY_ALIAS` and `RELEASE_KEY_PASSWORD`.
   The debug APK is the artefact to test with.
