package com.rabha.privacymonitor.core

object Constants {
    const val AD_DEFAULT_INTERVAL_MINUTES = 15
    const val AD_MAX_IMPRESSIONS_PER_SESSION = 6
    const val DATA_LIMIT_DEFAULT_BYTES = 2L * 1024 * 1024 * 1024
    const val MONITOR_CHANNEL_ID = "monitoring"
    const val ALERTS_CHANNEL_ID = "alerts"
    const val REPORTS_CHANNEL_ID = "reports"
    const val ANNOUNCEMENTS_CHANNEL_ID = "announcements"
    const val MONITOR_NOTIFICATION_ID = 1001
    const val FOREGROUND_SERVICE_ACTION_START = "com.rabha.privacymonitor.action.START"
    const val FOREGROUND_SERVICE_ACTION_PAUSE = "com.rabha.privacymonitor.action.PAUSE"
    const val FOREGROUND_SERVICE_ACTION_STOP = "com.rabha.privacymonitor.action.STOP"
    const val MONITOR_WORK_NAME = "rabha-monitor-refresh"
    const val WALLPAPER_DIR = "wallpapers"
    const val WALLPAPER_SELECTION = "rabha.wallpaper.selection"
    const val WALLPAPER_HTML_ENTRY = "rabha.wallpaper.html.entry"
    const val WALLPAPER_VIDEO_PATH = "rabha.wallpaper.video.path"
    const val WALLPAPER_ANIMATED_STYLE = "rabha.wallpaper.animated.style"
    const val PRIVACY_POLICY_URL = "https://rabha.example.com/privacy"
}
