# Architecture

## Android

One Gradle module (`:app`), 15 packages, 62 Kotlin files. Each area owns its repository, its ViewModel
and — where a long-running job exists — its service.

```
com.rabha.privacymonitor
├── core          Result type, formatters, theme, shared Compose components, connectivity
├── auth          Supabase session, sign-in / sign-up
├── dashboard     Aggregated home screen
├── network       NetworkStatsManager, per-app traffic, foreground service, packet counter, worker
├── usage         UsageStatsManager
├── permissions   Own permissions + declared-only view of other apps
├── privacy       AppOps reads for this app, explicit Unavailable for everything else
├── files         Storage Access Framework metadata only
├── notifications Four channels, history in Room
├── wallpaper     Three engines (HTML / Media3 video / Canvas) + installer
├── ads           Campaign selection, caps, event log, one composable slot
├── settings      Consent, retention, limits, deletion
├── local         Room database, DAOs, DataStore preferences
├── security      Consent gate, hashed device id, account deletion
├── supabase      Client provider, DTOs, PostgREST access
└── navigation    Nav host and bottom bar
```

### Data flow

`Repository → RabhaResult → ViewModel StateFlow → Compose screen`

`RabhaResult` has three states and no fourth: `Available(value)`, `Unavailable(reason, detail)` and
`Failed(message)`. A screen must handle all three. This is what makes "never show a wrong status"
structural rather than a review rule.

### Services

- `MonitorForegroundService` — `dataSync` type, always-visible notification with Pause and Stop,
  refreshes every 30 s, raises the data-limit alert once per day.
- `RabhaVpnService` — reads the tun descriptor, counts bytes, discards payloads.
- `HtmlWallpaperService` / `VideoWallpaperService` / `AnimatedWallpaperService` — one engine each.
- `MonitorWorker` — periodic aggregate snapshot into Room (Hilt worker).

## Supabase

18 tables, 7 storage buckets, RLS on every table, two security-definer helpers
(`has_admin_role`, `delete_own_account`) and two read-only views for the admin panel
(`admin_daily_usage`, `admin_ad_performance`).

Aggregated storage only: `monitoring_summaries` keeps daily rx/tx/screen-time per device,
`app_usage_summaries` keeps per-day per-package durations. No URLs, no packet contents, no app contents.

## Admin panel

React 18 + TypeScript + Tailwind + Vite. Seven screens, 60 controls in 11 groups, all declared in one
registry (`src/lib/config.ts`) so the count and the mapping to `app_config` / `feature_flags` are
checkable. Role gating is applied per screen and per control. Three controls are deliberately locked off
in code because the product forbids them (ads in background, ads in the notification bar, ad targeting
from monitoring data).
