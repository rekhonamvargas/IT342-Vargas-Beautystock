package com.beautystock.ui.auth

import android.content.Context
import android.content.Intent
import android.net.Uri

private const val GOOGLE_OAUTH_URL = "http://10.0.2.2:8080/api/oauth2/authorization/google"

fun Context.launchGoogleOAuth() {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(GOOGLE_OAUTH_URL))
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    startActivity(intent)
}