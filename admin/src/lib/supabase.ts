import { createClient, type SupabaseClient } from "@supabase/supabase-js";

const url = import.meta.env.VITE_SUPABASE_URL;
const anonKey = import.meta.env.VITE_SUPABASE_ANON_KEY;

/**
 * Only the anon key is ever bundled. There is no service_role key in this app:
 * every privileged action is a policy decision made by Postgres, not by the client.
 */
export const isConfigured = Boolean(url && anonKey);

export const supabase: SupabaseClient | null = isConfigured
  ? createClient(url as string, anonKey as string, {
      auth: { persistSession: true, autoRefreshToken: true },
    })
  : null;

export const NOT_CONFIGURED_MESSAGE =
  "Supabase is not configured for this build. Create .env.local with VITE_SUPABASE_URL and VITE_SUPABASE_ANON_KEY, then reload.";
