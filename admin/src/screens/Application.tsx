import { ControlGroups } from "../components/ControlGroups";
import { SectionTitle } from "../components/ui";

export function Application() {
  return (
    <div className="space-y-8">
      <SectionTitle
        title="Application"
        subtitle="App configuration, the four notification channels, and the Supabase wiring in one place"
      />
      <ControlGroups groupIds={["application", "notifications", "supabase"]} />
    </div>
  );
}
