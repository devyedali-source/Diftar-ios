package com.example

import androidx.compose.ui.window.ComposeUIViewController

fun MainViewController() = ComposeUIViewController { App() }

/** يُستدعى من Swift عند فتح رابط (daftarmeallim://… أو https://…) */
fun handleDeepLink(url: String) = DeepLinkBridge.handle(url)
