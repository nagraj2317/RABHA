export type Role =
  | "super_admin"
  | "ad_admin"
  | "content_admin"
  | "support_admin"
  | "analyst";

export const ALL_ROLES: Role[] = [
  "super_admin",
  "ad_admin",
  "content_admin",
  "support_admin",
  "analyst",
];

export const ROLE_LABEL: Record<Role, string> = {
  super_admin: "Super admin",
  ad_admin: "Ad admin",
  content_admin: "Content admin",
  support_admin: "Support admin",
  analyst: "Analyst",
};

export type ControlKind = "toggle" | "number" | "text" | "action";

export type StoreTable = "app_config" | "feature_flags";

export interface ControlDef {
  id: string;
  label: string;
  help: string;
  kind: ControlKind;
  table?: StoreTable;
  key?: string;
  fallback: string;
  unit?: string;
  min?: number;
  max?: number;
  roles: Role[];
}

export interface ControlGroup {
  id: string;
  title: string;
  subtitle: string;
  controls: ControlDef[];
}

export interface ScreenDef {
  path: string;
  title: string;
  groups: string[];
  roles: Role[];
}

export interface AdCampaign {
  id: string;
  name: string;
  placement: string;
  priority: number;
  enabled: boolean;
  start_at: string | null;
  end_at: string | null;
  frequency_cap: number;
  interval_minutes: number;
}

export interface WallpaperRow {
  id: string;
  title: string;
  kind: string;
  preview_url: string | null;
  is_premium: boolean;
  published: boolean;
  created_at: string;
}

export interface ProfileRow {
  id: string;
  display_name: string | null;
  email: string | null;
  created_at: string;
}

export interface AdPerformanceRow {
  campaign_id: string;
  name: string;
  placement: string;
  impressions: number;
  clicks: number;
}

export interface DailyUsageRow {
  day: string;
  active_users: number;
  rx_bytes: number;
  tx_bytes: number;
  screen_millis: number;
}
