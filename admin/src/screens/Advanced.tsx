import { useTable } from "../lib/useTable";
import { formatDate } from "../lib/format";
import { ControlGroups } from "../components/ControlGroups";
import { Empty, ErrorBanner, Loading, SectionTitle, Table } from "../components/ui";

interface AuditRow {
  id: string;
  actor_id: string | null;
  action: string;
  target: string | null;
  created_at: string;
}

export function Advanced() {
  const audit = useTable<AuditRow>("audit_logs", { column: "created_at", ascending: false }, 100);

  return (
    <div className="space-y-8">
      <section>
        <SectionTitle title="Audit log" subtitle="Admin and account actions, newest first" />
        {audit.error ? <ErrorBanner message={audit.error} onRetry={() => void audit.reload()} /> : null}
        {audit.loading ? <Loading label="Reading audit_logs…" /> : null}
        {!audit.loading && audit.rows.length === 0 && !audit.error ? (
          <Empty title="Nothing logged yet" message="Account deletions and admin writes appear here." />
        ) : null}
        {audit.rows.length > 0 ? (
          <Table head={["Action", "Target", "Actor", "When"]}>
            {audit.rows.map((row) => (
              <tr key={row.id}>
                <td className="px-4 py-2 font-mono text-xs text-rabha-amber">{row.action}</td>
                <td className="px-4 py-2 font-mono text-[10px] text-rabha-muted">{row.target ?? "—"}</td>
                <td className="px-4 py-2 font-mono text-[10px] text-rabha-muted/70">{row.actor_id ?? "system"}</td>
                <td className="px-4 py-2 text-xs text-rabha-muted">{formatDate(row.created_at)}</td>
              </tr>
            ))}
          </Table>
        ) : null}
      </section>

      <ControlGroups groupIds={["security", "advanced"]} />
    </div>
  );
}
