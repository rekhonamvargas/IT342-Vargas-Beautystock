package com.beautystock.network

import android.os.Build

object BackendUrlProvider {

    private const val LOCAL_BACKEND_URL = "http://localhost:8080/api/"

    fun defaultBaseUrl(): String = LOCAL_BACKEND_URL
}