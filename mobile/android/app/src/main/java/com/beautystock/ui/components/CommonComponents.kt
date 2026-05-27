package com.beautystock.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import android.widget.Toast
import android.util.Log
import com.beautystock.BuildConfig
import com.beautystock.R
import com.beautystock.utils.BeautyColors
import com.beautystock.utils.BeautyRadius
import com.beautystock.utils.BeautySpacing
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException

@Composable
fun BeautyTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
    isPassword: Boolean = false,
    isError: Boolean = false,
    errorMessage: String? = null,
    enabled: Boolean = true
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
        isError = isError,
        enabled = enabled,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = BeautyColors.Surface,
            unfocusedContainerColor = BeautyColors.Surface,
            focusedTextColor = BeautyColors.TextPrimary,
            unfocusedTextColor = BeautyColors.TextPrimary,
            focusedIndicatorColor = BeautyColors.Primary,
            unfocusedIndicatorColor = BeautyColors.Divider,
            errorIndicatorColor = BeautyColors.Error,
            disabledContainerColor = BeautyColors.Background,
            disabledTextColor = BeautyColors.TextSecondary,
            focusedLabelColor = BeautyColors.Primary,
            unfocusedLabelColor = BeautyColors.TextSecondary
        ),
        shape = RoundedCornerShape(BeautyRadius.xl)
    )
    if (isError && errorMessage != null) {
        Text(
            text = errorMessage,
            color = BeautyColors.Error,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun BeautyButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp),
        enabled = enabled && !isLoading,
        colors = ButtonDefaults.buttonColors(
            containerColor = BeautyColors.Primary,
            contentColor = BeautyColors.Surface,
            disabledContainerColor = BeautyColors.TextHint,
            disabledContentColor = BeautyColors.TextSecondary
        ),
        shape = RoundedCornerShape(BeautyRadius.xl)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.height(24.dp),
                color = BeautyColors.Surface,
                strokeWidth = 2.dp
            )
        } else {
            Text(text, style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
fun BeautyOutlinedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp),
        enabled = enabled,
        border = BorderStroke(1.dp, BeautyColors.Divider),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = BeautyColors.Primary,
            containerColor = BeautyColors.Surface,
            disabledContentColor = BeautyColors.TextHint
        ),
        shape = RoundedCornerShape(BeautyRadius.xl)
    ) {
        Text(text, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
fun GoogleSignInButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp),
        enabled = enabled && !isLoading,
        border = BorderStroke(1.dp, BeautyColors.Divider),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = BeautyColors.Surface,
            contentColor = BeautyColors.TextPrimary,
            disabledContentColor = BeautyColors.TextHint
        ),
        shape = RoundedCornerShape(BeautyRadius.xl)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.height(20.dp),
                color = BeautyColors.Primary,
                strokeWidth = 2.dp
            )
        } else {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = R.drawable.ic_google_logo),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(text, style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

@Composable
fun GoogleAuthButton(
    text: String,
    isLoading: Boolean,
    onGoogleLogin: (idToken: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val googleSignInClient = remember {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestIdToken(BuildConfig.GOOGLE_WEB_CLIENT_ID)
            .build()

        GoogleSignIn.getClient(context, gso)
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)

        try {
            val account = task.getResult(ApiException::class.java)
            val idToken = account.idToken

            if (idToken.isNullOrBlank()) {
                Toast.makeText(context, "Could not retrieve Google ID token", Toast.LENGTH_LONG).show()
                return@rememberLauncherForActivityResult
            }

            Log.d("GOOGLE_AUTH", "Google sign-in succeeded")
            onGoogleLogin(idToken)
        } catch (e: Exception) {
            Log.e("GOOGLE_AUTH", e.message ?: "Google Login Failed")
            Toast.makeText(context, e.message ?: "Google Login Failed", Toast.LENGTH_LONG).show()
        }
    }

    GoogleSignInButton(
        text = text,
        onClick = {
            launcher.launch(googleSignInClient.signInIntent)
        },
        isLoading = isLoading,
        enabled = !isLoading,
        modifier = modifier
    )
}

@Composable
fun LoadingDialog() {
    AlertDialog(
        onDismissRequest = { },
        confirmButton = { },
        text = {
            CircularProgressIndicator(
                color = BeautyColors.Primary
            )
        }
    )
}

@Composable
fun ErrorDialog(
    message: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("OK")
            }
        },
        title = { Text("Error") },
        text = { Text(message) }
    )
}
