import { useState } from "react";
import { useAuth } from "../auth/AuthContext";
import { isConfigured } from "../lib/supabase";
import { Card, ErrorBanner } from "../components/ui";

export function Login() {
  const { signIn, error, loading } = useAuth();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [busy, setBusy] = useState(false);

  return (
    <div className="flex min-h-screen items-center justify-center bg-rabha-black p-6">
      <div className="w-full max-w-md space-y-4">
        <div>
          <p className="text-[11px] uppercase tracking-[0.25em] text-rabha-muted">RABHA</p>
          <h1 className="text-2xl font-semibold text-rabha-text">Admin sign-in</h1>
          <p className="mt-1 text-xs text-rabha-muted">
            Only accounts with a row in admin_roles can see any screen.
          </p>
        </div>

        {!isConfigured ? (
          <ErrorBanner message="Supabase is not configured. Create .env.local with VITE_SUPABASE_URL and VITE_SUPABASE_ANON_KEY." />
        ) : null}
        {error ? <ErrorBanner message={error} /> : null}

        <Card>
          <form
            className="space-y-3"
            onSubmit={async (event) => {
              event.preventDefault();
              setBusy(true);
              await signIn(email.trim(), password);
              setBusy(false);
            }}
          >
            <label className="block text-xs uppercase tracking-widest text-rabha-muted">
              Email
              <input
                type="email"
                required
                value={email}
                onChange={(event) => setEmail(event.target.value)}
                className="mt-1 w-full rounded-lg border border-white/15 bg-black/40 px-3 py-2 text-sm text-rabha-text outline-none focus:border-rabha-amber"
              />
            </label>
            <label className="block text-xs uppercase tracking-widest text-rabha-muted">
              Password
              <input
                type="password"
                required
                value={password}
                onChange={(event) => setPassword(event.target.value)}
                className="mt-1 w-full rounded-lg border border-white/15 bg-black/40 px-3 py-2 text-sm text-rabha-text outline-none focus:border-rabha-amber"
              />
            </label>
            <button
              type="submit"
              disabled={busy || loading || !isConfigured}
              className="w-full rounded-lg bg-rabha-amber px-3 py-2 text-sm font-semibold text-black disabled:opacity-50"
            >
              {busy ? "Signing in…" : "Sign in"}
            </button>
          </form>
        </Card>

        <p className="text-center font-mono text-[10px] text-rabha-muted/60">v1.0.0 · build 2026-11-22</p>
      </div>
    </div>
  );
}
