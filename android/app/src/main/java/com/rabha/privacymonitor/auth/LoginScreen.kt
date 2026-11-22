package com.rabha.privacymonitor.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rabha.privacymonitor.core.GlassCard
import com.rabha.privacymonitor.core.RabhaColors

@Composable
internal fun LoginScreen(
    onBack: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier.fillMaxWidth().padding(PaddingValues(horizontal = 4.dp, vertical = 8.dp)),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        GlassCard(accent = RabhaColors.Amber) {
            Text("Sign in", style = MaterialTheme.typography.headlineMedium, color = RabhaColors.TextPrimary)
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Signing in syncs your consent record and lets you delete your account from the server. Every monitoring feature works without an account.",
                style = MaterialTheme.typography.bodyMedium,
                color = RabhaColors.TextSecondary
            )
        }

        OutlinedTextField(
            value = state.email,
            onValueChange = viewModel::onEmailChange,
            label = { Text("Email") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = state.password,
            onValueChange = viewModel::onPasswordChange,
            label = { Text("Password") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth()
        )

        if (state.error != null) {
            Text(state.error!!, style = MaterialTheme.typography.bodyMedium, color = RabhaColors.Danger)
        }
        if (state.notice != null) {
            Text(state.notice!!, style = MaterialTheme.typography.bodyMedium, color = RabhaColors.Success)
        }
        if (state.signedIn) {
            Text(
                text = "Signed in as ${state.signedInEmail}",
                style = MaterialTheme.typography.bodyLarge,
                color = RabhaColors.Success
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = viewModel::signIn,
                enabled = !state.busy,
                colors = ButtonDefaults.buttonColors(containerColor = RabhaColors.Amber)
            ) {
                Text("Sign in", color = RabhaColors.Black)
            }
            Button(
                onClick = viewModel::signUp,
                enabled = !state.busy,
                colors = ButtonDefaults.buttonColors(containerColor = RabhaColors.SurfaceHigh)
            ) {
                Text("Create account", color = RabhaColors.TextPrimary)
            }
        }

        TextButton(onClick = onBack) { Text("Back", color = RabhaColors.TextSecondary) }
    }
}
