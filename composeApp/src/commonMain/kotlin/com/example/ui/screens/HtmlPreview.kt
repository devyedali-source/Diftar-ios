package com.example.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/** معاينة HTML ثابتة غير قابلة للتمرير (WKWebView على الآيفون). */
@Composable
expect fun HtmlPreview(html: String, modifier: Modifier = Modifier)
