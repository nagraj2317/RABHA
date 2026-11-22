// Supabase Edge Function: ad-events
// Accepts batched impression/click events from the app and writes them under the
// caller's own user id. No monitoring data is accepted here, by design.
import { createClient } from "https://esm.sh/@supabase/supabase-js@2";

type IncomingEvent = {
  campaign_id: string;
  creative_id?: string | null;
  type: "impression" | "click" | "close";
  occurred_at?: string;
};

Deno.serve(async (req: Request) => {
  if (req.method !== "POST") {
    return new Response(JSON.stringify({ error: "POST only" }), { status: 405 });
  }

  const authHeader = req.headers.get("Authorization") ?? "";
  const supabase = createClient(
    Deno.env.get("SUPABASE_URL")!,
    Deno.env.get("SUPABASE_ANON_KEY")!,
    { global: { headers: { Authorization: authHeader } } },
  );

  const { data: userData } = await supabase.auth.getUser();
  const userId = userData?.user?.id ?? null;

  let payload: { events?: IncomingEvent[] };
  try {
    payload = await req.json();
  } catch {
    return new Response(JSON.stringify({ error: "invalid json" }), { status: 400 });
  }

  const events = (payload.events ?? []).slice(0, 200).map((event) => ({
    user_id: userId,
    campaign_id: event.campaign_id,
    creative_id: event.creative_id ?? null,
    type: event.type,
    occurred_at: event.occurred_at ?? new Date().toISOString(),
  }));

  if (events.length === 0) {
    return new Response(JSON.stringify({ inserted: 0 }), { status: 200 });
  }

  const { error } = await supabase.from("ad_events").insert(events);
  if (error) {
    return new Response(JSON.stringify({ error: error.message }), { status: 400 });
  }

  return new Response(JSON.stringify({ inserted: events.length }), {
    status: 200,
    headers: { "Content-Type": "application/json" },
  });
});
