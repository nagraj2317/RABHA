# Build instructions

## 1. Prerequisites

- A GitHub account (the APK is built on GitHub's runners — no Android Studio required).
- Optional, for the online features: a Supabase project.

## 2. Push the repository

```bash
git init
git add .
git commit -m "RABHA Privacy Monitor"
git branch -M main
git remote add origin https://github.com/<you>/<repo>.git
git push -u origin main
```

## 3. Add the two secrets

Repository → **Settings → Secrets and variables → Actions → New repository secret**:

| Name | Value |
|---|---|
| `SUPABASE_URL` | `https://<project-ref>.supabase.co` |
| `SUPABASE_ANON_KEY` | the project's **anon / publishable** key |

Never add the `service_role` key. It must not exist in the APK or in the admin bundle.

Both secrets are optional for a build: without them the APK compiles with empty values, the app runs
fully offline, and the screens that need the backend show "Supabase is not configured for this build"
instead of fake data.

## 4. Run the workflow

**Actions → Build Android APK → Run workflow.** Two jobs run:

- `build` — Gradle 8.11.1, JDK 17, Android SDK 35, `./gradlew assembleDebug`. Artifact: `rabha-monitor-debug-apk`.
- `admin` — `npm ci` then `tsc -b && vite build`. Artifact: `rabha-admin-dist`.

The debug APK lands in the artifact zip as `app-debug.apk`. On the phone, allow "install from unknown
sources" for your file manager or browser.

## 5. Local build (optional)

With Android Studio or a command-line SDK:

```bash
cd android
echo "SUPABASE_URL=https://<ref>.supabase.co" >> local.properties
echo "SUPABASE_ANON_KEY=<anon-key>" >> local.properties
./gradlew assembleDebug
```

`local.properties` is git-ignored on purpose.

## 6. Database

In the Supabase SQL editor, run in order:

1. `supabase/migrations/001_schema.sql`
2. `supabase/migrations/002_rls.sql`
3. `supabase/migrations/003_storage.sql`

All three are idempotent (`IF NOT EXISTS`, `drop policy if exists` before every `create policy`).

After your first sign-in, grant yourself an admin role:

```sql
insert into public.admin_roles (user_id, role)
select id, 'super_admin' from auth.users where email = 'you@example.com';
```

## 7. Edge functions (optional)

```bash
supabase functions deploy delete-account
supabase functions deploy ad-events
```

## 8. Admin panel

```bash
cd admin
cp .env.example .env.local     # fill in the URL and the anon key
npm install
npm run dev                    # http://localhost:5173
npm run build                  # production bundle in dist/
```
