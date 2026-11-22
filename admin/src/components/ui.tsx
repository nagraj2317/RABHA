import type { ReactNode } from "react";

export function Card({ children, className = "" }: { children: ReactNode; className?: string }) {
  return (
    <div
      className={`rounded-2xl border border-white/10 bg-white/[0.04] p-5 backdrop-blur-sm ${className}`}
    >
      {children}
    </div>
  );
}

export function SectionTitle({ title, subtitle }: { title: string; subtitle?: string }) {
  return (
    <div className="mb-3">
      <h2 className="text-lg font-semibold text-rabha-text">{title}</h2>
      {subtitle ? <p className="text-sm text-rabha-muted">{subtitle}</p> : null}
    </div>
  );
}

export function Stat({
  label,
  value,
  hint,
  accent = "amber",
}: {
  label: string;
  value: string;
  hint?: string;
  accent?: "amber" | "blue" | "purple" | "success" | "danger";
}) {
  const accents: Record<string, string> = {
    amber: "text-rabha-amber",
    blue: "text-rabha-blue",
    purple: "text-rabha-purple",
    success: "text-rabha-success",
    danger: "text-rabha-danger",
  };
  return (
    <Card>
      <p className="text-[11px] uppercase tracking-widest text-rabha-muted">{label}</p>
      <p className={`mt-2 text-3xl font-semibold ${accents[accent]}`}>{value}</p>
      {hint ? <p className="mt-1 text-xs text-rabha-muted">{hint}</p> : null}
    </Card>
  );
}

export function Badge({ children, tone = "muted" }: { children: ReactNode; tone?: "muted" | "amber" | "success" | "danger" }) {
  const tones: Record<string, string> = {
    muted: "bg-white/10 text-rabha-muted",
    amber: "bg-rabha-amber/15 text-rabha-amber",
    success: "bg-rabha-success/15 text-rabha-success",
    danger: "bg-rabha-danger/15 text-rabha-danger",
  };
  return (
    <span className={`rounded-md px-2 py-0.5 text-[11px] font-medium uppercase tracking-wider ${tones[tone]}`}>
      {children}
    </span>
  );
}

export function Empty({ title, message }: { title: string; message: string }) {
  return (
    <Card className="text-center">
      <p className="text-sm font-medium text-rabha-text">{title}</p>
      <p className="mt-1 text-xs text-rabha-muted">{message}</p>
    </Card>
  );
}

export function ErrorBanner({ message, onRetry }: { message: string; onRetry?: () => void }) {
  return (
    <div className="mb-4 flex items-start justify-between gap-4 rounded-xl border border-rabha-danger/40 bg-rabha-danger/10 p-4">
      <p className="text-sm text-rabha-danger">{message}</p>
      {onRetry ? (
        <button
          type="button"
          onClick={onRetry}
          className="shrink-0 rounded-lg border border-rabha-danger/40 px-3 py-1 text-xs text-rabha-danger hover:bg-rabha-danger/10"
        >
          Retry
        </button>
      ) : null}
    </div>
  );
}

export function Loading({ label = "Loading…" }: { label?: string }) {
  return (
    <div className="flex items-center gap-3 rounded-xl border border-white/10 bg-white/[0.03] p-4">
      <span className="h-3 w-3 animate-pulse rounded-full bg-rabha-amber" />
      <p className="text-sm text-rabha-muted">{label}</p>
    </div>
  );
}

export function Table({ head, children }: { head: string[]; children: ReactNode }) {
  return (
    <div className="overflow-x-auto rounded-xl border border-white/10">
      <table className="w-full text-left text-sm">
        <thead className="bg-white/[0.04] text-[11px] uppercase tracking-widest text-rabha-muted">
          <tr>
            {head.map((column) => (
              <th key={column} className="px-4 py-3 font-medium">
                {column}
              </th>
            ))}
          </tr>
        </thead>
        <tbody className="divide-y divide-white/5">{children}</tbody>
      </table>
    </div>
  );
}
