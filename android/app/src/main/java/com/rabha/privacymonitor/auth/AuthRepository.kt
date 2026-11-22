package com.rabha.privacymonitor.auth

import com.rabha.privacymonitor.core.RabhaResult
import com.rabha.privacymonitor.core.UnavailableReason
import com.rabha.privacymonitor.core.runCatchingResult
import com.rabha.privacymonitor.supabase.SupabaseClientProvider
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.Email
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

data class SessionState(
    val signedIn: Boolean = false,
    val email: String? = null,
    val userId: String? = null
)

@Singleton
class AuthRepository @Inject constructor(
    private val provider: SupabaseClientProvider
) {

    private val _session = MutableStateFlow(SessionState())
    val session: StateFlow<SessionState> = _session.asStateFlow()

    fun refreshSession() {
        val client = provider.client
        if (client == null) {
            _session.value = SessionState()
            return
        }
        val user = runCatching { client.auth.currentUserOrNull() }.getOrNull()
        _session.value = SessionState(
            signedIn = user != null,
            email = user?.email,
            userId = user?.id
        )
    }

    suspend fun signIn(email: String, password: String): RabhaResult<Unit> {
        val client = provider.client ?: return RabhaResult.Unavailable(
            UnavailableReason.FEATURE_DISABLED,
            "Supabase is not configured for this build. The app works offline; sign-in needs SUPABASE_URL and SUPABASE_ANON_KEY."
        )
        val result = runCatchingResult {
            client.auth.signInWith(Email) {
                this.email = email
                this.password = password
            }
            Unit
        }
        if (result is RabhaResult.Available) refreshSession()
        return result
    }

    suspend fun signUp(email: String, password: String): RabhaResult<Unit> {
        val client = provider.client ?: return RabhaResult.Unavailable(
            UnavailableReason.FEATURE_DISABLED,
            "Supabase is not configured for this build."
        )
        val result = runCatchingResult {
            client.auth.signUpWith(Email) {
                this.email = email
                this.password = password
            }
            Unit
        }
        if (result is RabhaResult.Available) refreshSession()
        return result
    }

    suspend fun signOut(): RabhaResult<Unit> {
        val client = provider.client ?: return RabhaResult.Available(Unit)
        val result = runCatchingResult { client.auth.signOut(); Unit }
        refreshSession()
        return result
    }
}
