import { useTable } from "../lib/useTable";
import { formatBytes, formatDuration } from "../lib/format";
import type { AdPerformanceRow, DailyUsageRow } from "../lib/types";
import { ControlGroups } from "../components/ControlGroups";
import { ErrorBanner, Loading, SectionTitle, Stat, Table } from "../components/ui";

export function Dashboard() {
  const usage = useTable<DailyUsageRow>("admin_daily_usage", { column: "day", ascending: false }, 30);
  const ads = useTable<AdPerformanceRow>("admin_ad_performance", { column: "impressions", ascending: false }, 20);

  const today = usage.rows[0];
  const totalImpressions = ads.rows.reduce((sum, row) => sum + Number(row.impressions ?? 0), 0);
  const totalClicks = ads.rows.reduce((sum, row) => sum + Number(row.clicks ?? 0), 0);

  return (
    <div className="space-y-8">
      <div>
        <SectionTitle title="Dashboard" subtitle="Aggregated data only — no URLs, no packet contents, no app contents" />
        {usage.error ? <ErrorBanner message={usage.error} onRetry={() => void usage.reload()} /> : null}
        {usage.loading ? <Loading label="Reading aggregates…" /> : null}
        <div className="grid gap-3 sm:grid-cols-2 lg:grid-cols-4">
          <Stat label="Active users" value={today ? String(today.active_users) : "—"} hint={today ? today.day : "no rows"} />
          <Stat
            label="Data today"
            value={today ? formatBytes(Number(today.rx_bytes) + Number(today.tx_bytes)) : "—"}
            hint={today ? `down ${formatBytes(Number(today.rx_bytes))}` : "no rows"}
            accent="blue"
          />
          <Stat
            label="Screen time today"
            value={today ? formatDuration(Number(today.screen_millis)) : "—"}
            hint="sum across reporting devices"
            accent="purple"
          />
          <Stat
            label="Ad impressions"
            value={String(totalImpressions)}
            hint={`${totalClicks} clicks`}
            accent="success"
          />
        </div>
      </div>

      <section>
        <SectionTitle title="Last 30 days" subtitle="admin_daily_usage view" />
        {usage.rows.length === 0 && !usage.loading ? (
          <p className="rounded-xl border border-white/10 bg-white/[0.02] p-4 text-xs text-rabha-muted">
            No aggregates yet. Rows appear once a signed-in device uploads a summary with cloud consent on.
          </p>
        ) : (
          <Table head={["Day", "Active users", "Download", "Upload", "Screen time"]}>
            {usage.rows.map((row) => (
              <tr key={row.day}>
                <td className="px-4 py-2 font-mono text-xs">{row.day}</td>
                <td className="px-4 py-2">{row.active_users}</td>
                <td className="px-4 py-2">{formatBytes(Number(row.rx_bytes))}</td>
                <td className="px-4 py-2">{formatBytes(Number(row.tx_bytes))}</td>
                <td className="px-4 py-2">{formatDuration(Number(row.screen_millis))}</td>
              </tr>
            ))}
          </Table>
        )}
      </section>

      <ControlGroups groupIds={["dashboard"]} />
    </div>
  );
}
