package com.beautystock.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.beautystock.data.api.RetrofitClient
import com.beautystock.data.model.RegisterRequest
import kotlinx.coroutines.launch
import android.util.Patterns
import android.widget.Toast
import org.json.JSONObject

@Composable
fun RegisterScreen(onNavigateToLogin: () -> Unit) {
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var ageRange by remember { mutableStateOf("YOUTH") }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val colorScheme = MaterialTheme.colorScheme

    Scaffold(containerColor = colorScheme.background) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "\u2726 BeautyStock",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = colorScheme.primary
            )
            Spacer(Modifier.height(12.dp))
            Text(
                "Create Account",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = colorScheme.onBackground
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "Join and start tracking your beauty collection",
                style = MaterialTheme.typography.bodyMedium,
                color = colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(20.dp))

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
                border = CardDefaults.outlinedCardBorder(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Names row
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = firstName,
                            onValueChange = { firstName = it },
                            label = { Text("First Name") },
                            leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null) },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        OutlinedTextField(
                            value = lastName,
                            onValueChange = { lastName = it },
                            label = { Text("Last Name") },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    // Email
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email") },
                        placeholder = { Text("your@email.com") },
                        leadingIcon = { Icon(Icons.Filled.Email, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Password
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        placeholder = { Text("Min 8 characters") },
                        leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null) },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Confirm
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        label = { Text("Confirm Password") },
                        placeholder = { Text("\u2022\u2022\u2022\u2022\u2022\u2022\u2022\u2022") },
                        leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null) },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Age group
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            "Age Group",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = colorScheme.onSurface
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            listOf(
                                "YOUTH" to "\uD83C\uDF31 Youth\nAge 13\u201324",
                                "ADULT" to "\uD83C\uDF38 Adult\nAge 25\u201344"
                            ).forEach { (value, label) ->
                                AgeGroupTile(
                                    label = label,
                                    selected = ageRange == value,
                                    onClick = { ageRange = value },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    error?.let {
                        Text(
                            it,
                            color = colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    Button(
                        onClick = {
                            scope.launch {
                                val fullName = listOf(firstName.trim(), lastName.trim())
                                    .filter { it.isNotBlank() }
                                    .joinToString(" ")

                                if (fullName.isBlank() || email.isBlank() || password.isBlank() || confirmPassword.isBlank()) {
                                    error = "Please complete all fields"
                                    return@launch
                                }
                                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                                    error = "Invalid email format"
                                    return@launch
                                }
                                if (!Regex("^(?=.*[A-Z])(?=.*\\d).{8,}$").matches(password)) {
                                    error = "Password needs 8+ chars, 1 uppercase, 1 digit"
                                    return@launch
                                }
                                if (password != confirmPassword) {
                                    error = "Passwords do not match"
                                    return@launch
                                }

                                loading = true; error = null
                                try {
                                    val res = RetrofitClient.api.register(
                                        RegisterRequest(fullName, email, password, confirmPassword, ageRange)
                                    )
                                    if (res.isSuccessful && res.body() != null) {
                                        Toast.makeText(context, "Account created. Please login.", Toast.LENGTH_SHORT).show()
                                        onNavigateToLogin()
                                    } else {
                                        val rawError = res.errorBody()?.string()
                                        error = parseApiError(rawError) ?: "Registration failed"
                                    }
                                } catch (e: Exception) {
                                    error = e.message ?: "Registration failed"
                                } finally { loading = false }
                            }
                        },
                        enabled = !loading,
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(containerColor = colorScheme.primary)
                    ) {
                        if (loading) CircularProgressIndicator(
                            color = colorScheme.onPrimary,
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                        else Text(
                            "Create Account",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))
            Row(horizontalArrangement = Arrangement.Center) {
                Text(
                    "Already have an account? ",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colorScheme.onSurfaceVariant
                )
                Text(
                    "Sign in",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.primary,
                    modifier = Modifier.clickable { onNavigateToLogin() }
                )
            }
        }
    }
}

private fun parseApiError(rawError: String?): String? {
    if (rawError.isNullOrBlank()) return null

    return try {
        val json = JSONObject(rawError)
        when {
            json.has("message") -> json.optString("message")
            json.has("error") -> {
                val errorValue = json.opt("error")
                when (errorValue) {
                    is JSONObject -> errorValue.optString("message").takeIf { it.isNotBlank() }
                    is String -> errorValue
                    else -> null
                }
            }
            else -> null
        }
    } catch (_: Exception) {
        rawError.take(120)
    }
}

@Composable
private fun AgeGroupTile(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    Box(
        modifier = modifier
            .border(
                width = 2.dp,
                color = if (selected) colorScheme.primary else colorScheme.outlineVariant,
                shape = RoundedCornerShape(12.dp)
            )
            .background(
                if (selected) colorScheme.primaryContainer else colorScheme.surface,
                RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = if (selected) colorScheme.onPrimaryContainer else colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
    }
}
