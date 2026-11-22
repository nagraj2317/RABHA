import type { ProfileRow } from "../lib/types";
import { useTable } from "../lib/useTable";
import { formatDate } from "../lib/format";
import { ControlGroups } from "../components/ControlGroups";
import { Empty, ErrorBanner, Loading, SectionTitle, Table } from "../components/ui";

export function Users() {
  const profiles = useTable<ProfileRow>("profiles", { column: "created_at", ascending: false }, 200);

  return (
    <div className="space-y-8">
      <section>
        <SectionTitle
          title="Accounts"
          subtitle="RLS shows support and super admins every profile. There is no per-user device view, because the product does not collect one."
        />
        {profiles.error ? <ErrorBanner message={profiles.error} onRetry={() => void profiles.reload()} /> : null}
        {profiles.loading ? <Loading label="Reading profiles…" /> : null}
        {!profiles.loading && profiles.rows.length === 0 && !profiles.error ? (
          <Empty title="No accounts yet" message="A profile row is created automatically on sign-up." />
        ) : null}
        {profiles.rows.length > 0 ? (
          <Table head={["Name", "Email", "Joined", "User id"]}>
            {profiles.rows.map((row) => (
              <tr key={row.id}>
                <td className="px-4 py-2">{row.display_name ?? "—"}</td>
                <td className="px-4 py-2 text-rabha-muted">{row.email ?? "—"}</td>
                <td className="px-4 py-2 text-xs text-rabha-muted">{formatDate(row.created_at)}</td>
                <td className="px-4 py-2 font-mono text-[10px] text-rabha-muted/70">{row.id}</td>
              </tr>
            ))}
          </Table>
        ) : null}
      </section>

      <ControlGroups groupIds={["users"]} />
    </div>
  );
}
