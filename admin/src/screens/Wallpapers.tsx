import type { WallpaperRow } from "../lib/types";
import { useTable } from "../lib/useTable";
import { formatDate } from "../lib/format";
import { ControlGroups } from "../components/ControlGroups";
import { Badge, Empty, ErrorBanner, Loading, SectionTitle, Table } from "../components/ui";

export function Wallpapers() {
  const wallpapers = useTable<WallpaperRow>("wallpapers", { column: "created_at", ascending: false }, 100);

  return (
    <div className="space-y-8">
      <section>
        <SectionTitle title="Catalogue" subtitle="Seven storage buckets back this list; only published rows reach the app" />
        {wallpapers.error ? <ErrorBanner message={wallpapers.error} onRetry={() => void wallpapers.reload()} /> : null}
        {wallpapers.loading ? <Loading label="Reading wallpapers…" /> : null}
        {!wallpapers.loading && wallpapers.rows.length === 0 && !wallpapers.error ? (
          <Empty
            title="No wallpapers yet"
            message="Upload to the wallpapers, html_wallpapers or video_wallpapers bucket, then add the row."
          />
        ) : null}
        {wallpapers.rows.length > 0 ? (
          <Table head={["Title", "Kind", "Premium", "Published", "Created"]}>
            {wallpapers.rows.map((row) => (
              <tr key={row.id}>
                <td className="px-4 py-2">{row.title}</td>
                <td className="px-4 py-2 font-mono text-xs text-rabha-purple">{row.kind}</td>
                <td className="px-4 py-2">{row.is_premium ? <Badge tone="amber">Premium</Badge> : "—"}</td>
                <td className="px-4 py-2">
                  {row.published ? <Badge tone="success">Live</Badge> : <Badge>Draft</Badge>}
                </td>
                <td className="px-4 py-2 text-xs text-rabha-muted">{formatDate(row.created_at)}</td>
              </tr>
            ))}
          </Table>
        ) : null}
      </section>

      <ControlGroups groupIds={["wallpapers"]} />
    </div>
  );
}
