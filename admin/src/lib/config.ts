import type { ControlGroup, ScreenDef } from "./types";

const SUPER = ["super_admin"] as const;
const AD = ["super_admin", "ad_admin"] as const;
const CONTENT = ["super_admin", "content_admin"] as const;
const SUPPORT = ["super_admin", "support_admin"] as const;
const ANALYTICS = ["super_admin", "ad_admin", "analyst"] as const;

/**
 * The 60 admin controls, in 11 groups.
 * Every control maps to a real row: app_config(key) or feature_flags(key).
 * Nothing here can reach a user's microphone, camera or private files —
 * there is no such control in this product, by design.
 */
export const GROUPS: ControlGroup[] = [
  {
    id: "dashboard",
    title: "Dashboard",
    subtitle: "Operating picture of the install base",
    controls: [
      { id: "dash_active_users", label: "Show active users card", help: "Daily active user count from aggregated summaries.", kind: "toggle", table: "feature_flags", key: "dash_card_active_users", fallback: "true", roles: [...ANALYTICS] },
      { id: "dash_data_card", label: "Show data volume card", help: "Total rx/tx per day.", kind: "toggle", table: "feature_flags", key: "dash_card_data_volume", fallback: "true", roles: [...ANALYTICS] },
      { id: "dash_ads_card", label: "Show ad performance card", help: "Impressions and clicks per campaign.", kind: "toggle", table: "feature_flags", key: "dash_card_ad_performance", fallback: "true", roles: [...ANALYTICS] },
      { id: "dash_refresh", label: "Dashboard refresh interval", help: "Seconds between automatic reloads.", kind: "number", table: "app_config", key: "dash_refresh_seconds", fallback: "60", unit: "s", min: 15, max: 900, roles: [...ANALYTICS] },
      { id: "dash_range", label: "Default range", help: "Days shown by default on every chart.", kind: "number", table: "app_config", key: "dash_default_range_days", fallback: "30", unit: "days", min: 1, max: 365, roles: [...ANALYTICS] },
      { id: "dash_min_version", label: "Minimum app version", help: "Older clients are told to update.", kind: "text", table: "app_config", key: "min_app_version", fallback: "1.0.0", roles: [...SUPER] },
    ],
  },
  {
    id: "advertisements",
    title: "Advertisements",
    subtitle: "Campaign engine, caps and placements",
    controls: [
      { id: "ads_enabled", label: "Ad engine enabled", help: "Master switch. Off means no ad request is served.", kind: "toggle", table: "feature_flags", key: "ads_enabled", fallback: "true", roles: [...AD] },
      { id: "ads_interval", label: "Default interval", help: "Minutes between ads when a campaign sets no override.", kind: "number", table: "app_config", key: "ad_interval_minutes", fallback: "15", unit: "min", min: 1, max: 1440, roles: [...AD] },
      { id: "ads_session_cap", label: "Max impressions per session", help: "Hard cap enforced on the device.", kind: "number", table: "app_config", key: "ad_max_impressions_per_session", fallback: "6", unit: "ads", min: 1, max: 50, roles: [...AD] },
      { id: "ads_background", label: "Allow ads in background", help: "Locked off. The product rule forbids it, so the switch cannot be turned on.", kind: "toggle", table: "feature_flags", key: "ads_allow_background", fallback: "false", roles: [...SUPER] },
      { id: "ads_notification_bar", label: "Allow ads in notification bar", help: "Locked off. Ads never enter the notification shade.", kind: "toggle", table: "feature_flags", key: "ads_allow_notification_bar", fallback: "false", roles: [...SUPER] },
      { id: "ads_targeting", label: "Allow targeting with monitoring data", help: "Locked off. Monitoring data is never used for targeting.", kind: "toggle", table: "feature_flags", key: "ads_allow_monitoring_targeting", fallback: "false", roles: [...SUPER] },
      { id: "ads_label", label: "Sponsored label text", help: "Shown on every ad unit.", kind: "text", table: "app_config", key: "ad_label_text", fallback: "Sponsored", roles: [...AD] },
      { id: "ads_rewarded", label: "Rewarded ads", help: "Optional opt-in placement.", kind: "toggle", table: "feature_flags", key: "ads_rewarded_enabled", fallback: "false", roles: [...AD] },
    ],
  },
  {
    id: "wallpapers",
    title: "Wallpapers",
    subtitle: "Catalogue and the three engines",
    controls: [
      { id: "wp_html", label: "HTML engine", help: "Uploaded HTML pages, sandboxed to app-private storage.", kind: "toggle", table: "feature_flags", key: "wallpaper_engine_html", fallback: "true", roles: [...CONTENT] },
      { id: "wp_video", label: "Video engine", help: "Media3 ExoPlayer, muted, looping.", kind: "toggle", table: "feature_flags", key: "wallpaper_engine_video", fallback: "true", roles: [...CONTENT] },
      { id: "wp_animated", label: "Animated engine", help: "Canvas particles, clock and battery-aware FPS.", kind: "toggle", table: "feature_flags", key: "wallpaper_engine_animated", fallback: "true", roles: [...CONTENT] },
      { id: "wp_max_video", label: "Max video size", help: "Rejected above this size at upload.", kind: "number", table: "app_config", key: "wallpaper_max_video_mb", fallback: "50", unit: "MB", min: 1, max: 500, roles: [...CONTENT] },
      { id: "wp_max_html", label: "Max HTML package size", help: "Zip of an HTML wallpaper.", kind: "number", table: "app_config", key: "wallpaper_max_html_mb", fallback: "5", unit: "MB", min: 1, max: 50, roles: [...CONTENT] },
      { id: "wp_premium", label: "Premium wallpapers", help: "Marks a subset as premium in the gallery.", kind: "toggle", table: "feature_flags", key: "wallpaper_premium_enabled", fallback: "false", roles: [...CONTENT] },
      { id: "wp_moderation", label: "Require moderation", help: "New uploads stay unpublished until reviewed.", kind: "toggle", table: "feature_flags", key: "wallpaper_require_moderation", fallback: "true", roles: [...CONTENT] },
    ],
  },
  {
    id: "users",
    title: "Users",
    subtitle: "Accounts and support",
    controls: [
      { id: "users_signup", label: "Allow sign-ups", help: "Off means existing accounts only.", kind: "toggle", table: "feature_flags", key: "allow_signups", fallback: "true", roles: [...SUPPORT] },
      { id: "users_email_confirm", label: "Require email confirmation", help: "New accounts must confirm before sign-in.", kind: "toggle", table: "feature_flags", key: "require_email_confirmation", fallback: "true", roles: [...SUPER] },
      { id: "users_max_devices", label: "Max devices per account", help: "Further devices are refused at sign-in.", kind: "number", table: "app_config", key: "max_devices_per_user", fallback: "3", unit: "devices", min: 1, max: 20, roles: [...SUPPORT] },
      { id: "users_retention", label: "Default retention", help: "Days of aggregated history kept server-side.", kind: "number", table: "app_config", key: "default_retention_days", fallback: "30", unit: "days", min: 1, max: 365, roles: [...SUPPORT] },
      { id: "users_support_email", label: "Support email", help: "Shown in the app's About section.", kind: "text", table: "app_config", key: "support_email", fallback: "support@rabha.example.com", roles: [...SUPPORT] },
    ],
  },
  {
    id: "application",
    title: "Application",
    subtitle: "App-level configuration",
    controls: [
      { id: "app_force_update", label: "Force update below minimum", help: "Blocking prompt instead of a dismissible one.", kind: "toggle", table: "feature_flags", key: "force_update", fallback: "false", roles: [...SUPER] },
      { id: "app_maintenance", label: "Maintenance mode", help: "Shows a maintenance screen; monitoring keeps running locally.", kind: "toggle", table: "feature_flags", key: "maintenance_mode", fallback: "false", roles: [...SUPER] },
      { id: "app_cloud_sync", label: "Cloud sync", help: "Aggregated summaries upload. Consent is still required per user.", kind: "toggle", table: "feature_flags", key: "cloud_sync", fallback: "false", roles: [...SUPER] },
      { id: "app_privacy_url", label: "Privacy policy URL", help: "Linked from Settings.", kind: "text", table: "app_config", key: "privacy_policy_url", fallback: "https://rabha.example.com/privacy", roles: [...SUPER] },
      { id: "app_terms_url", label: "Terms URL", help: "Linked from Settings.", kind: "text", table: "app_config", key: "terms_url", fallback: "https://rabha.example.com/terms", roles: [...SUPER] },
    ],
  },
  {
    id: "notifications",
    title: "Notifications",
    subtitle: "Four channels, no ad path",
    controls: [
      { id: "notif_alerts", label: "Alerts channel", help: "Data limit and privacy alerts.", kind: "toggle", table: "feature_flags", key: "notifications_alerts", fallback: "true", roles: [...CONTENT] },
      { id: "notif_reports", label: "Reports channel", help: "Daily and weekly summaries.", kind: "toggle", table: "feature_flags", key: "notifications_reports", fallback: "true", roles: [...CONTENT] },
      { id: "notif_announcements", label: "Announcements channel", help: "Product news.", kind: "toggle", table: "feature_flags", key: "notifications_announcements", fallback: "true", roles: [...CONTENT] },
      { id: "notif_quiet", label: "Quiet hours start", help: "Hour (0-23) after which non-critical notifications are held.", kind: "number", table: "app_config", key: "quiet_hours_start", fallback: "22", unit: "h", min: 0, max: 23, roles: [...CONTENT] },
    ],
  },
  {
    id: "analytics",
    title: "Analytics",
    subtitle: "Aggregated only — no URLs, no contents",
    controls: [
      { id: "an_usage_view", label: "Usage aggregates", help: "Daily rx/tx and screen time per day.", kind: "toggle", table: "feature_flags", key: "analytics_usage", fallback: "true", roles: [...ANALYTICS] },
      { id: "an_ads_view", label: "Ad performance", help: "Impressions and clicks per campaign.", kind: "toggle", table: "feature_flags", key: "analytics_ads", fallback: "true", roles: [...ANALYTICS] },
      { id: "an_retention", label: "Aggregate retention", help: "Days kept in the aggregate views.", kind: "number", table: "app_config", key: "analytics_retention_days", fallback: "90", unit: "days", min: 7, max: 730, roles: [...ANALYTICS] },
      { id: "an_export", label: "Allow CSV export", help: "Analysts can export the aggregate tables.", kind: "toggle", table: "feature_flags", key: "analytics_export", fallback: "true", roles: [...ANALYTICS] },
    ],
  },
  {
    id: "security",
    title: "Security",
    subtitle: "Enforced in code and in RLS",
    controls: [
      { id: "sec_rls", label: "Row Level Security", help: "Cannot be disabled from this panel; shown so the state is visible.", kind: "toggle", table: "feature_flags", key: "rls_enforced", fallback: "true", roles: [...SUPER] },
      { id: "sec_audit", label: "Audit logging", help: "Admin actions are written to audit_logs.", kind: "toggle", table: "feature_flags", key: "audit_logging", fallback: "true", roles: [...SUPER] },
      { id: "sec_session", label: "Admin session length", help: "Minutes before an admin session expires.", kind: "number", table: "app_config", key: "admin_session_minutes", fallback: "60", unit: "min", min: 5, max: 480, roles: [...SUPER] },
    ],
  },
  {
    id: "supabase",
    title: "Supabase",
    subtitle: "Project wiring and storage",
    controls: [
      { id: "sb_project", label: "Project reference", help: "Shown for verification. Keys are never displayed here.", kind: "text", table: "app_config", key: "supabase_project_ref", fallback: "", roles: [...SUPER] },
      { id: "sb_storage_quota", label: "Storage soft quota", help: "Warning threshold for the seven buckets.", kind: "number", table: "app_config", key: "storage_quota_gb", fallback: "50", unit: "GB", min: 1, max: 1000, roles: [...SUPER] },
      { id: "sb_edge_functions", label: "Edge functions enabled", help: "delete-account and ad-events.", kind: "toggle", table: "feature_flags", key: "edge_functions_enabled", fallback: "true", roles: [...SUPER] },
    ],
  },
  {
    id: "monetization",
    title: "Monetization",
    subtitle: "Revenue controls",
    controls: [
      { id: "mon_ads", label: "Ads revenue stream", help: "Primary stream.", kind: "toggle", table: "feature_flags", key: "monetization_ads", fallback: "true", roles: [...AD] },
      { id: "mon_premium", label: "Premium tier", help: "Removes ads for paying users.", kind: "toggle", table: "feature_flags", key: "monetization_premium", fallback: "false", roles: [...AD] },
      { id: "mon_price", label: "Premium price", help: "Monthly price in INR.", kind: "number", table: "app_config", key: "premium_price_inr", fallback: "149", unit: "INR", min: 0, max: 10000, roles: [...AD] },
      { id: "mon_trial", label: "Trial days", help: "Free trial length for premium.", kind: "number", table: "app_config", key: "premium_trial_days", fallback: "7", unit: "days", min: 0, max: 90, roles: [...AD] },
      { id: "mon_sponsored", label: "Sponsored wallpapers", help: "Sell a wallpaper slot.", kind: "toggle", table: "feature_flags", key: "monetization_sponsored_wallpapers", fallback: "false", roles: [...AD] },
    ],
  },
  {
    id: "advanced",
    title: "Advanced",
    subtitle: "Diagnostics, limits and guardrails",
    controls: [
      { id: "adv_log_level", label: "Client log level", help: "What the app writes to logcat.", kind: "text", table: "app_config", key: "client_log_level", fallback: "warn", roles: [...SUPER] },
      { id: "adv_retry", label: "Network retry count", help: "Retries before a sync is abandoned.", kind: "number", table: "app_config", key: "network_retry_count", fallback: "3", unit: "tries", min: 0, max: 10, roles: [...SUPER] },
      { id: "adv_timeout", label: "Request timeout", help: "Seconds before a request is cancelled.", kind: "number", table: "app_config", key: "request_timeout_seconds", fallback: "20", unit: "s", min: 5, max: 120, roles: [...SUPER] },
      { id: "adv_batch", label: "Ad event batch size", help: "Events sent per edge-function call.", kind: "number", table: "app_config", key: "ad_event_batch_size", fallback: "50", unit: "events", min: 1, max: 200, roles: [...SUPER] },
      { id: "adv_vpn", label: "Packet counter", help: "Local packet-size counter. Sizes only, payloads discarded.", kind: "toggle", table: "feature_flags", key: "packet_counter", fallback: "true", roles: [...SUPER] },
      { id: "adv_force_pause", label: "Force-pause monitoring", help: "Emergency stop for every client.", kind: "toggle", table: "feature_flags", key: "force_pause_monitoring", fallback: "false", roles: [...SUPER] },
      { id: "adv_kill_switch", label: "Kill switch", help: "Disables every remote feature except monitoring stop.", kind: "toggle", table: "feature_flags", key: "kill_switch", fallback: "false", roles: [...SUPER] },
      { id: "adv_crash", label: "Crash reporting", help: "Local-only crash log; never uploaded without consent.", kind: "toggle", table: "feature_flags", key: "crash_reporting", fallback: "false", roles: [...SUPER] },
      { id: "adv_export", label: "Admin data export", help: "Enables the CSV export buttons.", kind: "toggle", table: "feature_flags", key: "admin_export", fallback: "true", roles: [...SUPER] },
      { id: "adv_impersonate", label: "Impersonate user", help: "Not available. Reading a user's device is not a feature of this product.", kind: "action", fallback: "disabled", roles: [...SUPER] },
    ],
  },
];

export const SCREENS: ScreenDef[] = [
  { path: "/", title: "Dashboard", groups: ["dashboard"], roles: ["super_admin", "ad_admin", "support_admin", "analyst"] },
  { path: "/ads", title: "Advertisements", groups: ["advertisements"], roles: ["super_admin", "ad_admin"] },
  { path: "/wallpapers", title: "Wallpapers", groups: ["wallpapers"], roles: ["super_admin", "content_admin"] },
  { path: "/users", title: "Users", groups: ["users"], roles: ["super_admin", "support_admin"] },
  { path: "/application", title: "Application", groups: ["application", "notifications", "supabase"], roles: ["super_admin", "content_admin"] },
  { path: "/analytics", title: "Analytics", groups: ["analytics", "monetization"], roles: ["super_admin", "ad_admin", "analyst"] },
  { path: "/advanced", title: "Advanced", groups: ["security", "advanced"], roles: ["super_admin"] },
];

export const TOTAL_CONTROLS = GROUPS.reduce((sum, group) => sum + group.controls.length, 0);

export function groupById(id: string) {
  return GROUPS.find((group) => group.id === id);
}
