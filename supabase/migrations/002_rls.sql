-- RABHA Privacy Monitor — Row Level Security
-- Every table has RLS on. A user reads only their own rows.
-- Public catalogue rows (wallpapers, announcements, config, active campaigns) are readable by anyone.

alter table public.profiles enable row level security;
alter table public.devices enable row level security;
alter table public.user_consents enable row level security;
alter table public.device_settings enable row level security;
alter table public.monitoring_summaries enable row level security;
alter table public.app_usage_summaries enable row level security;
alter table public.wallpapers enable row level security;
alter table public.wallpaper_categories enable row level security;
alter table public.ad_campaigns enable row level security;
alter table public.ad_creatives enable row level security;
alter table public.ad_events enable row level security;
alter table public.notification_templates enable row level security;
alter table public.announcements enable row level security;
alter table public.feature_flags enable row level security;
alter table public.app_config enable row level security;
alter table public.support_tickets enable row level security;
alter table public.admin_roles enable row level security;
alter table public.audit_logs enable row level security;

-- ---------------------------------------------------------------------------
-- admin helper
-- ---------------------------------------------------------------------------
create or replace function public.has_admin_role(roles text[])
returns boolean
language sql
stable
security definer
set search_path = public
as $$
    select exists (
        select 1 from public.admin_roles ar
        where ar.user_id = auth.uid() and ar.role = any(roles)
    );
$$;

revoke all on function public.has_admin_role(text[]) from public;
grant execute on function public.has_admin_role(text[]) to authenticated;

-- ---------------------------------------------------------------------------
-- profiles
-- ---------------------------------------------------------------------------
drop policy if exists profiles_select_own on public.profiles;
create policy profiles_select_own on public.profiles
    for select using (id = auth.uid() or public.has_admin_role(array['super_admin','support_admin','analyst']));

drop policy if exists profiles_update_own on public.profiles;
create policy profiles_update_own on public.profiles
    for update using (id = auth.uid()) with check (id = auth.uid());

drop policy if exists profiles_insert_own on public.profiles;
create policy profiles_insert_own on public.profiles
    for insert with check (id = auth.uid());

-- ---------------------------------------------------------------------------
-- owner-scoped tables
-- ---------------------------------------------------------------------------
drop policy if exists devices_own on public.devices;
create policy devices_own on public.devices
    for all using (user_id = auth.uid()) with check (user_id = auth.uid());

drop policy if exists consents_own on public.user_consents;
create policy consents_own on public.user_consents
    for all using (user_id = auth.uid()) with check (user_id = auth.uid());

drop policy if exists device_settings_own on public.device_settings;
create policy device_settings_own on public.device_settings
    for all using (user_id = auth.uid()) with check (user_id = auth.uid());

drop policy if exists monitoring_own on public.monitoring_summaries;
create policy monitoring_own on public.monitoring_summaries
    for all using (user_id = auth.uid()) with check (user_id = auth.uid());

drop policy if exists usage_own on public.app_usage_summaries;
create policy usage_own on public.app_usage_summaries
    for all using (user_id = auth.uid()) with check (user_id = auth.uid());

drop policy if exists tickets_own on public.support_tickets;
create policy tickets_own on public.support_tickets
    for all using (user_id = auth.uid()) with check (user_id = auth.uid());

drop policy if exists tickets_support_read on public.support_tickets;
create policy tickets_support_read on public.support_tickets
    for select using (public.has_admin_role(array['super_admin','support_admin']));

-- ---------------------------------------------------------------------------
-- ad events: a user may write and read only their own; admins read all
-- ---------------------------------------------------------------------------
drop policy if exists ad_events_own_insert on public.ad_events;
create policy ad_events_own_insert on public.ad_events
    for insert with check (user_id = auth.uid() or user_id is null);

drop policy if exists ad_events_own_select on public.ad_events;
create policy ad_events_own_select on public.ad_events
    for select using (user_id = auth.uid() or public.has_admin_role(array['super_admin','ad_admin','analyst']));

-- ---------------------------------------------------------------------------
-- public catalogue: readable, never writable by a normal user
-- ---------------------------------------------------------------------------
drop policy if exists wallpapers_public_read on public.wallpapers;
create policy wallpapers_public_read on public.wallpapers
    for select using (published or public.has_admin_role(array['super_admin','content_admin']));

drop policy if exists wallpaper_categories_public_read on public.wallpaper_categories;
create policy wallpaper_categories_public_read on public.wallpaper_categories
    for select using (true);

drop policy if exists campaigns_public_read on public.ad_campaigns;
create policy campaigns_public_read on public.ad_campaigns
    for select using (enabled or public.has_admin_role(array['super_admin','ad_admin','analyst']));

drop policy if exists creatives_public_read on public.ad_creatives;
create policy creatives_public_read on public.ad_creatives
    for select using (true);

drop policy if exists announcements_public_read on public.announcements;
create policy announcements_public_read on public.announcements
    for select using (active or public.has_admin_role(array['super_admin','content_admin']));

drop policy if exists feature_flags_public_read on public.feature_flags;
create policy feature_flags_public_read on public.feature_flags
    for select using (true);

drop policy if exists app_config_public_read on public.app_config;
create policy app_config_public_read on public.app_config
    for select using (true);

drop policy if exists templates_admin_read on public.notification_templates;
create policy templates_admin_read on public.notification_templates
    for select using (public.has_admin_role(array['super_admin','content_admin']));

-- ---------------------------------------------------------------------------
-- admin-only writes
-- ---------------------------------------------------------------------------
drop policy if exists wallpapers_admin_write on public.wallpapers;
create policy wallpapers_admin_write on public.wallpapers
    for all using (public.has_admin_role(array['super_admin','content_admin']))
    with check (public.has_admin_role(array['super_admin','content_admin']));

drop policy if exists categories_admin_write on public.wallpaper_categories;
create policy categories_admin_write on public.wallpaper_categories
    for all using (public.has_admin_role(array['super_admin','content_admin']))
    with check (public.has_admin_role(array['super_admin','content_admin']));

drop policy if exists campaigns_admin_write on public.ad_campaigns;
create policy campaigns_admin_write on public.ad_campaigns
    for all using (public.has_admin_role(array['super_admin','ad_admin']))
    with check (public.has_admin_role(array['super_admin','ad_admin']));

drop policy if exists creatives_admin_write on public.ad_creatives;
create policy creatives_admin_write on public.ad_creatives
    for all using (public.has_admin_role(array['super_admin','ad_admin']))
    with check (public.has_admin_role(array['super_admin','ad_admin']));

drop policy if exists announcements_admin_write on public.announcements;
create policy announcements_admin_write on public.announcements
    for all using (public.has_admin_role(array['super_admin','content_admin']))
    with check (public.has_admin_role(array['super_admin','content_admin']));

drop policy if exists flags_admin_write on public.feature_flags;
create policy flags_admin_write on public.feature_flags
    for all using (public.has_admin_role(array['super_admin']))
    with check (public.has_admin_role(array['super_admin']));

drop policy if exists config_admin_write on public.app_config;
create policy config_admin_write on public.app_config
    for all using (public.has_admin_role(array['super_admin']))
    with check (public.has_admin_role(array['super_admin']));

drop policy if exists templates_admin_write on public.notification_templates;
create policy templates_admin_write on public.notification_templates
    for all using (public.has_admin_role(array['super_admin','content_admin']))
    with check (public.has_admin_role(array['super_admin','content_admin']));

drop policy if exists roles_admin_read on public.admin_roles;
create policy roles_admin_read on public.admin_roles
    for select using (user_id = auth.uid() or public.has_admin_role(array['super_admin']));

drop policy if exists roles_super_write on public.admin_roles;
create policy roles_super_write on public.admin_roles
    for all using (public.has_admin_role(array['super_admin']))
    with check (public.has_admin_role(array['super_admin']));

drop policy if exists audit_admin_read on public.audit_logs;
create policy audit_admin_read on public.audit_logs
    for select using (public.has_admin_role(array['super_admin','support_admin','analyst']));

drop policy if exists audit_insert_any on public.audit_logs;
create policy audit_insert_any on public.audit_logs
    for insert with check (true);

-- ---------------------------------------------------------------------------
-- admin dashboard aggregates (read-only views, security invoker so RLS still applies)
-- ---------------------------------------------------------------------------
create or replace view public.admin_daily_usage
with (security_invoker = true) as
select
    day,
    count(distinct user_id) as active_users,
    sum(rx_bytes) as rx_bytes,
    sum(tx_bytes) as tx_bytes,
    sum(screen_millis) as screen_millis
from public.monitoring_summaries
group by day;

create or replace view public.admin_ad_performance
with (security_invoker = true) as
select
    c.id as campaign_id,
    c.name,
    c.placement,
    count(*) filter (where e.type = 'impression') as impressions,
    count(*) filter (where e.type = 'click') as clicks
from public.ad_campaigns c
left join public.ad_events e on e.campaign_id = c.id
group by c.id, c.name, c.placement;
