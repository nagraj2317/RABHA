import type { AdPerformanceRow, DailyUsageRow } from "../lib/types";
import { useTable } from "../lib/useTable";
import { ctr, formatBytes, formatDuration, toCsv } from "../lib/format";
import { ControlGroups } from "../components/ControlGroups";
import { Empty, ErrorBanner, Loading, SectionTitle, Table } from "../components/ui";

function download(name: string, content: string) {
  const blob = new Blob([content], { type: "text/csv;charset=utf-8" });
  const url = URL.createObjectURL(blob);
  const anchor = document.createElement("a");
  anchor.href = url;
  anchor.download = name;
  anchor.click();
  URL.revokeObjectURL(url);
}

export function Analytics() {
  const usage = useTable<DailyUsageRow>("admin_daily_usage", { column: "day", ascending: false }, 90);
  const ads = useTable<AdPerformanceRow>("admin_ad_performance", { column: "impressions", ascending: false }, 50);

  return (
    <div className="space-y-8">
      <section>
        <SectionTitle title="Usage aggregates" subtitle="admin_daily_usage — one row per day, no per-user detail" />
        {usage.error ? <ErrorBanner message={usage.error} onRetry={() => void usage.reload()} /> : null}
        {usage.loading ? <Loading label="Reading aggregates…" /> : null}
        {!usage.loading && usage.rows.length === 0 && !usage.error ? (
          <Empty title="No aggregate rows" message="Summaries appear when a signed-in device uploads with cloud consent." />
        ) : null}
        {usage.rows.length > 0 ? (
          <>
            <button
              type="button"
              onClick={() => download("rabha-usage.csv", toCsv(usage.rows as unknown as Record<string, unknown>[]))}
              className="mb-3 rounded-lg border border-white/15 px-3 py-1.5 text-xs text-rabha-muted hover:border-rabha-amber/40 hover:text-rabha-amber"
            >
              Export CSV
            </button>
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
          </>
        ) : null}
      </section>

      <section>
        <SectionTitle title="Ad performance" subtitle="admin_ad_performance — impressions and clicks per campaign" />
        {ads.error ? <ErrorBanner message={ads.error} onRetry={() => void ads.reload()} /> : null}
        {ads.loading ? <Loading label="Reading ad events…" /> : null}
        {!ads.loading && ads.rows.length === 0 && !ads.error ? (
          <Empty title="No ad events" message="Events are written by the ad-events edge function." />
        ) : null}
        {ads.rows.length > 0 ? (
          <Table head={["Campaign", "Placement", "Impressions", "Clicks", "CTR"]}>
            {ads.rows.map((row) => (
              <tr key={row.campaign_id}>
                <td className="px-4 py-2">{row.name}</td>
                <td className="px-4 py-2 font-mono text-xs text-rabha-blue">{row.placement}</td>
                <td className="px-4 py-2">{row.impressions}</td>
                <td className="px-4 py-2">{row.clicks}</td>
                <td className="px-4 py-2">{ctr(Number(row.impressions), Number(row.clicks))}</td>
              </tr>
            ))}
          </Table>
        ) : null}
      </section>

      <ControlGroups groupIds={["analytics", "monetization"]} />
    </div>
  );
}
