import { useAuth } from "../auth/AuthContext";
import { groupById } from "../lib/config";
import { useConfig } from "../lib/ConfigContext";
import { Control } from "./Control";
import { ErrorBanner, Loading, SectionTitle } from "./ui";

export function ControlGroups({ groupIds }: { groupIds: string[] }) {
  const { roles, canSee } = useAuth();
  const { status, error, reload } = useConfig();

  return (
    <div className="space-y-8">
      {error ? <ErrorBanner message={error} onRetry={() => void reload()} /> : null}
      {status === "unconfigured" ? (
        <ErrorBanner message="Supabase is not configured for this build. Create .env.local with VITE_SUPABASE_URL and VITE_SUPABASE_ANON_KEY to load and save controls." />
      ) : null}
      {status === "loading" ? <Loading label="Reading app_config and feature_flags…" /> : null}

      {groupIds.map((groupId) => {
        const group = groupById(groupId);
        if (!group) return null;
        const visible = group.controls.filter((control) =>
          control.roles.some((role) => roles.includes(role)),
        );
        const hiddenCount = group.controls.length - visible.length;

        return (
          <section key={group.id}>
            <SectionTitle
              title={group.title}
              subtitle={`${group.subtitle} · ${group.controls.length} controls${
                hiddenCount > 0 ? ` · ${hiddenCount} not visible for your role` : ""
              }`}
            />
            <div className="space-y-2">
              {visible.length === 0 ? (
                <p className="rounded-xl border border-white/10 bg-white/[0.02] p-4 text-xs text-rabha-muted">
                  No control in this group is available to your role.
                </p>
              ) : null}
              {visible.map((control) => (
                <Control
                  key={control.id}
                  control={control}
                  disabled={status !== "ready" || !canSee(control.roles)}
                />
              ))}
            </div>
          </section>
        );
      })}
    </div>
  );
}
