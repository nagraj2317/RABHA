import { NavLink, Outlet, useNavigate } from "react-router-dom";
import { useAuth } from "../auth/AuthContext";
import { SCREENS, TOTAL_CONTROLS } from "../lib/config";
import { ROLE_LABEL } from "../lib/types";
import { Badge } from "./ui";

export function Layout() {
  const { session, roles, signOut, canSee } = useAuth();
  const navigate = useNavigate();

  const visible = SCREENS.filter((screen) => canSee(screen.roles));
  const buildStamp = "v1.0.0 · build 2026-11-22";

  return (
    <div className="flex min-h-screen bg-rabha-black text-rabha-text">
      <aside className="hidden w-64 shrink-0 border-r border-white/10 bg-rabha-surface p-5 lg:block">
        <div className="mb-8">
          <p className="text-[11px] uppercase tracking-[0.2em] text-rabha-muted">RABHA</p>
          <p className="text-xl font-semibold">Admin</p>
          <p className="mt-2 text-[11px] text-rabha-muted">{TOTAL_CONTROLS} controls · 11 groups</p>
        </div>

        <nav className="space-y-1">
          {visible.map((screen) => (
            <NavLink
              key={screen.path}
              to={screen.path}
              end={screen.path === "/"}
              className={({ isActive }) =>
                `block rounded-lg px-3 py-2 text-sm transition ${
                  isActive ? "bg-rabha-amber/15 text-rabha-amber" : "text-rabha-muted hover:bg-white/5"
                }`
              }
            >
              {screen.title}
            </NavLink>
          ))}
        </nav>

        <div className="mt-8 rounded-xl border border-white/10 p-3">
          <p className="text-[11px] uppercase tracking-widest text-rabha-muted">Your roles</p>
          <div className="mt-2 flex flex-wrap gap-1">
            {roles.length === 0 ? <Badge tone="danger">No role</Badge> : null}
            {roles.map((role) => (
              <Badge key={role} tone="amber">
                {ROLE_LABEL[role]}
              </Badge>
            ))}
          </div>
        </div>

        <p className="mt-8 font-mono text-[10px] text-rabha-muted/60">{buildStamp}</p>
      </aside>

      <div className="flex min-w-0 flex-1 flex-col">
        <header className="flex items-center justify-between gap-4 border-b border-white/10 bg-rabha-surface/60 px-5 py-4 backdrop-blur">
          <div>
            <p className="text-sm font-medium">{session?.user.email ?? "Signed in"}</p>
            <p className="text-[11px] text-rabha-muted">
              Admin panel · the panel never has access to a user's microphone, camera or private files
            </p>
          </div>
          <button
            type="button"
            onClick={async () => {
              await signOut();
              navigate("/");
            }}
            className="rounded-lg border border-white/15 px-3 py-1.5 text-xs text-rabha-muted hover:border-rabha-danger/40 hover:text-rabha-danger"
          >
            Sign out
          </button>
        </header>

        <main className="flex-1 overflow-y-auto p-5">
          <div className="mx-auto max-w-5xl">
            <Outlet />
          </div>
        </main>
      </div>
    </div>
  );
}
