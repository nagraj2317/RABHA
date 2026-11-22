-- RABHA Privacy Monitor — schema
-- Safe to re-run: every object uses IF NOT EXISTS.
-- Monitoring data is stored AGGREGATED: no URLs, no packet contents, no app contents.

create extension if not exists "pgcrypto";

-- ---------------------------------------------------------------------------
-- profiles
-- ---------------------------------------------------------------------------
create table if not exists public.profiles (
    id uuid primary key references auth.users(id) on delete cascade,
    display_name text,
    email text,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now()
);

-- ---------------------------------------------------------------------------
-- devices
-- ---------------------------------------------------------------------------
create table if not exists public.devices (
    id uuid primary key default gen_random_uuid(),
    user_id uuid not null references auth.users(id) on delete cascade,
    device_hash text not null,
    model text,
    android_version text,
    app_version text,
    last_seen timestamptz not null default now(),
    created_at timestamptz not null default now(),
    unique (user_id, device_hash)
);

-- ---------------------------------------------------------------------------
-- consent
-- ---------------------------------------------------------------------------
create table if not exists public.user_consents (
    id uuid primary key default gen_random_uuid(),
    user_id uuid not null references auth.users(id) on delete cascade,
    scope text not null check (scope in ('monitoring','cloud_upload','ads','analytics')),
    granted boolean not null default false,
    updated_at timestamptz not null default now(),
    unique (user_id, scope)
);

create table if not exists public.device_settings (
    id uuid primary key default gen_random_uuid(),
    user_id uuid not null references auth.users(id) on delete cascade,
    device_hash text not null,
    alerts_enabled boolean not null default true,
    reports_enabled boolean not null default true,
    announcements_enabled boolean not null default true,
    retention_days int not null default 30 check (retention_days between 1 and 365),
    data_limit_bytes bigint not null default 2147483648,
    updated_at timestamptz not null default now(),
    unique (user_id, device_hash)
);

-- ---------------------------------------------------------------------------
-- aggregated monitoring
-- ---------------------------------------------------------------------------
create table if not exists public.monitoring_summaries (
    id uuid primary key default gen_random_uuid(),
    user_id uuid not null references auth.users(id) on delete cascade,
    device_hash text not null,
    day date not null,
    rx_bytes bigint not null default 0,
    tx_bytes bigint not null default 0,
    screen_millis bigint not null default 0,
    top_package text,
    created_at timestamptz not null default now(),
    unique (user_id, device_hash, day)
);

create table if not exists public.app_usage_summaries (
    id uuid primary key default gen_random_uuid(),
    user_id uuid not null references auth.users(id) on delete cascade,
    device_hash text not null,
    day date not null,
    package_name text not null,
    foreground_millis bigint not null default 0,
    created_at timestamptz not null default now(),
    unique (user_id, device_hash, day, package_name)
);

-- ---------------------------------------------------------------------------
-- wallpapers
-- ---------------------------------------------------------------------------
create table if not exists public.wallpaper_categories (
    id uuid primary key default gen_random_uuid(),
    name text not null,
    slug text unique,
    sort_order int not null default 0,
    created_at timestamptz not null default now()
);

create table if not exists public.wallpapers (
    id uuid primary key default gen_random_uuid(),
    title text not null,
    kind text not null check (kind in ('html','video','animated','image')),
    category_id uuid references public.wallpaper_categories(id) on delete set null,
    preview_url text,
    asset_path text,
    is_premium boolean not null default false,
    published boolean not null default false,
    sort_order int not null default 0,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now()
);

-- ---------------------------------------------------------------------------
-- ads
-- ---------------------------------------------------------------------------
create table if not exists public.ad_campaigns (
    id uuid primary key default gen_random_uuid(),
    name text not null,
    placement text not null check (placement in (
        'home_dashboard','wallpaper_gallery','wallpaper_detail',
        'content_section','tools_section','dedicated_section'
    )),
    priority int not null default 0,
    enabled boolean not null default true,
    start_at timestamptz,
    end_at timestamptz,
    frequency_cap int not null default 3 check (frequency_cap between 1 and 100),
    interval_minutes int not null default 15 check (interval_minutes between 1 and 1440),
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now()
);

create table if not exists public.ad_creatives (
    id uuid primary key default gen_random_uuid(),
    campaign_id uuid not null references public.ad_campaigns(id) on delete cascade,
    type text not null check (type in ('image','video','html','native','banner','sponsored_wallpaper','rewarded')),
    image_url text,
    video_url text,
    html_url text,
    headline text,
    body text,
    click_url text,
    created_at timestamptz not null default now()
);

create table if not exists public.ad_events (
    id uuid primary key default gen_random_uuid(),
    user_id uuid references auth.users(id) on delete cascade,
    device_hash text,
    campaign_id uuid not null references public.ad_campaigns(id) on delete cascade,
    creative_id uuid references public.ad_creatives(id) on delete set null,
    type text not null check (type in ('impression','click','close')),
    occurred_at timestamptz not null default now()
);

-- ---------------------------------------------------------------------------
-- content and configuration
-- ---------------------------------------------------------------------------
create table if not exists public.notification_templates (
    id uuid primary key default gen_random_uuid(),
    title text not null,
    body text not null,
    category text not null check (category in ('monitoring','alerts','reports','announcements')),
    created_at timestamptz not null default now()
);

create table if not exists public.announcements (
    id uuid primary key default gen_random_uuid(),
    title text not null,
    body text not null,
    active boolean not null default true,
    starts_at timestamptz,
    ends_at timestamptz,
    created_at timestamptz not null default now()
);

create table if not exists public.feature_flags (
    key text primary key,
    enabled boolean not null default false,
    description text,
    updated_at timestamptz not null default now()
);

create table if not exists public.app_config (
    key text primary key,
    value text not null,
    updated_at timestamptz not null default now()
);

create table if not exists public.support_tickets (
    id uuid primary key default gen_random_uuid(),
    user_id uuid not null references auth.users(id) on delete cascade,
    subject text not null,
    message text not null,
    status text not null default 'open' check (status in ('open','pending','closed')),
    created_at timestamptz not null default now()
);

-- ---------------------------------------------------------------------------
-- admin
-- ---------------------------------------------------------------------------
create table if not exists public.admin_roles (
    id uuid primary key default gen_random_uuid(),
    user_id uuid not null references auth.users(id) on delete cascade,
    role text not null check (role in ('super_admin','ad_admin','content_admin','support_admin','analyst')),
    created_at timestamptz not null default now(),
    unique (user_id, role)
);

create table if not exists public.audit_logs (
    id uuid primary key default gen_random_uuid(),
    actor_id uuid references auth.users(id) on delete set null,
    action text not null,
    target text,
    detail jsonb,
    created_at timestamptz not null default now()
);

-- ---------------------------------------------------------------------------
-- indexes
-- ---------------------------------------------------------------------------
create index if not exists idx_devices_user on public.devices(user_id);
create index if not exists idx_monitoring_user_day on public.monitoring_summaries(user_id, day desc);
create index if not exists idx_usage_user_day on public.app_usage_summaries(user_id, day desc);
create index if not exists idx_ad_events_campaign on public.ad_events(campaign_id, occurred_at desc);
create index if not exists idx_ad_campaigns_placement on public.ad_campaigns(placement) where enabled;
create index if not exists idx_wallpapers_published on public.wallpapers(published);

-- ---------------------------------------------------------------------------
-- updated_at helper
-- ---------------------------------------------------------------------------
create or replace function public.touch_updated_at()
returns trigger
language plpgsql
as $$
begin
    new.updated_at = now();
    return new;
end;
$$;

drop trigger if exists trg_profiles_touch on public.profiles;
create trigger trg_profiles_touch before update on public.profiles
    for each row execute function public.touch_updated_at();

drop trigger if exists trg_wallpapers_touch on public.wallpapers;
create trigger trg_wallpapers_touch before update on public.wallpapers
    for each row execute function public.touch_updated_at();

drop trigger if exists trg_campaigns_touch on public.ad_campaigns;
create trigger trg_campaigns_touch before update on public.ad_campaigns
    for each row execute function public.touch_updated_at();

-- ---------------------------------------------------------------------------
-- profile on signup
-- ---------------------------------------------------------------------------
create or replace function public.handle_new_user()
returns trigger
language plpgsql
security definer
set search_path = public
as $$
begin
    insert into public.profiles (id, email, display_name)
    values (new.id, new.email, split_part(coalesce(new.email, ''), '@', 1))
    on conflict (id) do nothing;
    return new;
end;
$$;

drop trigger if exists trg_on_auth_user_created on auth.users;
create trigger trg_on_auth_user_created after insert on auth.users
    for each row execute function public.handle_new_user();

-- ---------------------------------------------------------------------------
-- account deletion: SECURITY DEFINER, and it can only ever act on auth.uid()
-- ---------------------------------------------------------------------------
create or replace function public.delete_own_account()
returns void
language plpgsql
security definer
set search_path = public
as $$
declare
    caller uuid := auth.uid();
begin
    if caller is null then
        raise exception 'not authenticated';
    end if;

    insert into public.audit_logs (actor_id, action, target, detail)
    values (caller, 'account.delete.self', caller::text, jsonb_build_object('source', 'app'));

    delete from public.monitoring_summaries where user_id = caller;
    delete from public.app_usage_summaries where user_id = caller;
    delete from public.devices where user_id = caller;
    delete from public.device_settings where user_id = caller;
    delete from public.user_consents where user_id = caller;
    delete from public.support_tickets where user_id = caller;
    delete from public.profiles where id = caller;

    delete from auth.users where id = caller;
end;
$$;

revoke all on function public.delete_own_account() from public;
grant execute on function public.delete_own_account() to authenticated;

-- ---------------------------------------------------------------------------
-- default configuration rows
-- ---------------------------------------------------------------------------
insert into public.app_config (key, value)
values
    ('ad_interval_minutes', '15'),
    ('ad_max_impressions_per_session', '6'),
    ('min_app_version', '1.0.0'),
    ('support_email', 'support@rabha.example.com')
on conflict (key) do nothing;

insert into public.feature_flags (key, enabled, description)
values
    ('wallpaper_engine_html', true, 'HTML live wallpaper engine'),
    ('wallpaper_engine_video', true, 'Media3 video live wallpaper'),
    ('packet_counter', true, 'Local packet-size counter'),
    ('cloud_sync', false, 'Aggregated summaries upload, consent gated')
on conflict (key) do nothing;
