package com.example.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.interop.UIKitView
import kotlinx.cinterop.ExperimentalForeignApi
import platform.CoreGraphics.CGRectMake
import platform.Foundation.NSURL
import platform.UIKit.UIColor
import platform.WebKit.WKWebView
import platform.WebKit.WKWebViewConfiguration

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun HtmlPreview(html: String, modifier: Modifier) {
    UIKitView(
        factory = {
            val web = WKWebView(frame = CGRectMake(0.0, 0.0, 0.0, 0.0), configuration = WKWebViewConfiguration())
            web.opaque = false
            web.backgroundColor = UIColor.whiteColor
            web.scrollView.scrollEnabled = false
            web.userInteractionEnabled = false
            web.loadHTMLString(html, baseURL = null as NSURL?)
            web
        },
        modifier = modifier,
        update = { it.loadHTMLString(html, baseURL = null as NSURL?) }
    )
}
