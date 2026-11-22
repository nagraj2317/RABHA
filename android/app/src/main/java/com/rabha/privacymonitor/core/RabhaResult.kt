package com.rabha.privacymonitor.core

/**
 * Every data source returns this type. A module never shows a wrong status:
 * if Android does not allow the read, the UI shows [Unavailable] with the real reason.
 */
sealed interface RabhaResult<out T> {
    data class Available<T>(val value: T) : RabhaResult<T>
    data class Unavailable(
        val reason: UnavailableReason,
        val detail: String
    ) : RabhaResult<Nothing>

    data class Failed(
        val message: String,
        val cause: String? = null
    ) : RabhaResult<Nothing>
}

enum class UnavailableReason {
    MISSING_USAGE_ACCESS,
    MISSING_PHONE_STATE,
    MISSING_PERMISSION,
    SIGNATURE_PERMISSION_REQUIRED,
    NOT_SUPPORTED_ON_THIS_ANDROID,
    REQUIRES_ROOT,
    NO_DATA,
    CONSENT_REQUIRED,
    FEATURE_DISABLED,
    NOT_SIGNED_IN
}

fun <T> RabhaResult<T>.valueOrNull(): T? = (this as? RabhaResult.Available)?.value

fun <T> RabhaResult<T>.unavailableText(): String? = when (this) {
    is RabhaResult.Unavailable -> "${reason.name}: $detail"
    is RabhaResult.Failed -> message
    is RabhaResult.Available -> null
}

/** Wraps a suspend call, turning platform refusals into an explicit Unavailable state. */
suspend fun <T> runCatchingResult(block: suspend () -> T): RabhaResult<T> =
    try {
        RabhaResult.Available(block())
    } catch (se: SecurityException) {
        RabhaResult.Unavailable(
            UnavailableReason.MISSING_PERMISSION,
            se.message ?: "The system refused this read"
        )
    } catch (t: Throwable) {
        RabhaResult.Failed(
            message = t.message ?: "Unknown error",
            cause = t::class.java.simpleName
        )
    }
