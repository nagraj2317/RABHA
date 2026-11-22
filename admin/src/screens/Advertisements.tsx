import type { AdCampaign } from "../lib/types";
import { useTable } from "../lib/useTable";
import { formatDate } from "../lib/format";
import { ControlGroups } from "../components/ControlGroups";
import { Badge, Empty, ErrorBanner, Loading, SectionTitle, Table } from "../components/ui";

export function Advertisements() {
  const campaigns = useTable<AdCampaign>("ad_campaigns", { column: "created_at", ascending: false }, 100);

  return (
    <div className="space-y-8">
      <section>
        <SectionTitle title="Campaigns" subtitle="Only enabled, in-date campaigns with cap left are served to a device" />
        {campaigns.error ? <ErrorBanner message={campaigns.error} onRetry={() => void campaigns.reload()} /> : null}
        {campaigns.loading ? <Loading label="Reading campaigns…" /> : null}
        {!campaigns.loading && campaigns.rows.length === 0 && !campaigns.error ? (
          <Empty
            title="No campaigns yet"
            message="Insert a row in ad_campaigns and one in ad_creatives. The app will pick it up on its next load."
          />
        ) : null}
        {campaigns.rows.length > 0 ? (
          <Table head={["Name", "Placement", "Priority", "Interval", "Cap", "Window", "State"]}>
            {campaigns.rows.map((row) => (
              <tr key={row.id}>
                <td className="px-4 py-2">{row.name}</td>
                <td className="px-4 py-2 font-mono text-xs text-rabha-blue">{row.placement}</td>
                <td className="px-4 py-2">{row.priority}</td>
                <td className="px-4 py-2">{row.interval_minutes}m</td>
                <td className="px-4 py-2">{row.frequency_cap}/day</td>
                <td className="px-4 py-2 text-xs text-rabha-muted">
                  {formatDate(row.start_at)} → {formatDate(row.end_at)}
                </td>
                <td className="px-4 py-2">
                  {row.enabled ? <Badge tone="success">Enabled</Badge> : <Badge>Paused</Badge>}
                </td>
              </tr>
            ))}
          </Table>
        ) : null}
      </section>

      <ControlGroups groupIds={["advertisements"]} />
    </div>
  );
}
