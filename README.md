# RABHA Privacy Monitor

A privacy-first Android app that shows a user what their own phone is doing — network traffic,
per-app data, app usage, permission state, privacy signals and file-transfer metadata — plus three
live-wallpaper engines, a Supabase-funded ad engine, and a web admin panel.

**Privacy posture, in one line:** nothing is hidden, nothing is intercepted, and no data leaves the
device without an explicit consent flag.

## What is in this repository

| Path | What it is |
|---|---|
| `android/` | Native Android app (Kotlin 2.0.21, Compose, Hilt, Room, Media3, supabase-kt 2.6.0) |
| `admin/` | React 18 + TypeScript + Tailwind admin panel (7 screens, 60 controls in 11 groups) |
| `supabase/migrations/` | 3 SQL migrations: schema, RLS, storage |
| `supabase/functions/` | 2 edge functions: `delete-account`, `ad-events` |
| `.github/workflows/build-apk.yml` | Builds the APK and the admin bundle on GitHub runners |
| `docs/` | Build instructions, testing report, limits and known issues |

## Build the APK (no Android Studio needed)

1. Push this repository to GitHub.
2. Settings → Secrets and variables → Actions → add `SUPABASE_URL` and `SUPABASE_ANON_KEY`.
   The build works without them (the app runs offline and says so); they are only needed for sign-in,
   the wallpaper gallery and the ad engine.
3. Actions → **Build Android APK** → **Run workflow**.
4. Download `rabha-monitor-debug-apk` from the run's Artifacts. Install on Android 10+.

Full details: `docs/BUILD_INSTRUCTIONS.md`.

## Database

Run the three migrations in order in the Supabase SQL editor (or `supabase db push`):

```
supabase/migrations/001_schema.sql
supabase/migrations/002_rls.sql
supabase/migrations/003_storage.sql
```

They are safe to re-run: every object uses `IF NOT EXISTS`, every policy is dropped before it is created.

## Admin panel

```
cd admin
cp .env.example .env.local   # VITE_SUPABASE_URL, VITE_SUPABASE_ANON_KEY
npm install
npm run dev
```

Sign in with an account that has a row in `admin_roles`. Without a role the panel shows nothing.

## What this app deliberately does not do

See `docs/LIMITS.md`. Short version: it cannot read another app's microphone or camera state, another
app's granted permissions, or another app's private files, and it does not pretend to.
