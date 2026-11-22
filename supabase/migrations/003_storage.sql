-- RABHA Privacy Monitor — storage buckets
-- 7 buckets: wallpapers, html_wallpapers, video_wallpapers,
--            advertisement_images, advertisement_videos, advertisement_html, app_assets

insert into storage.buckets (id, name, public, file_size_limit, allowed_mime_types)
values
    ('wallpapers', 'wallpapers', true, 10485760, array['image/jpeg','image/png','image/webp']),
    ('html_wallpapers', 'html_wallpapers', true, 5242880, array['text/html','text/css','application/javascript','application/zip']),
    ('video_wallpapers', 'video_wallpapers', true, 52428800, array['video/mp4','video/webm']),
    ('advertisement_images', 'advertisement_images', true, 5242880, array['image/jpeg','image/png','image/webp']),
    ('advertisement_videos', 'advertisement_videos', true, 52428800, array['video/mp4']),
    ('advertisement_html', 'advertisement_html', true, 2097152, array['text/html']),
    ('app_assets', 'app_assets', true, 5242880, array['image/png','image/svg+xml'])
on conflict (id) do nothing;

-- Public read for published assets.
drop policy if exists storage_public_read on storage.objects;
create policy storage_public_read on storage.objects
    for select using (
        bucket_id in (
            'wallpapers','html_wallpapers','video_wallpapers',
            'advertisement_images','advertisement_videos','advertisement_html','app_assets'
        )
    );

-- Only content admins write wallpaper buckets.
drop policy if exists storage_wallpaper_admin_write on storage.objects;
create policy storage_wallpaper_admin_write on storage.objects
    for insert with check (
        bucket_id in ('wallpapers','html_wallpapers','video_wallpapers')
        and public.has_admin_role(array['super_admin','content_admin'])
    );

drop policy if exists storage_wallpaper_admin_update on storage.objects;
create policy storage_wallpaper_admin_update on storage.objects
    for update using (
        bucket_id in ('wallpapers','html_wallpapers','video_wallpapers')
        and public.has_admin_role(array['super_admin','content_admin'])
    );

drop policy if exists storage_wallpaper_admin_delete on storage.objects;
create policy storage_wallpaper_admin_delete on storage.objects
    for delete using (
        bucket_id in ('wallpapers','html_wallpapers','video_wallpapers')
        and public.has_admin_role(array['super_admin','content_admin'])
    );

-- Only ad admins write ad buckets.
drop policy if exists storage_ad_admin_write on storage.objects;
create policy storage_ad_admin_write on storage.objects
    for insert with check (
        bucket_id in ('advertisement_images','advertisement_videos','advertisement_html')
        and public.has_admin_role(array['super_admin','ad_admin'])
    );

drop policy if exists storage_ad_admin_delete on storage.objects;
create policy storage_ad_admin_delete on storage.objects
    for delete using (
        bucket_id in ('advertisement_images','advertisement_videos','advertisement_html')
        and public.has_admin_role(array['super_admin','ad_admin'])
    );

-- App assets: super admin only.
drop policy if exists storage_assets_super_write on storage.objects;
create policy storage_assets_super_write on storage.objects
    for insert with check (
        bucket_id = 'app_assets' and public.has_admin_role(array['super_admin'])
    );
