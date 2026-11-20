package com.example.ui.components

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.model.DailyStudyRecord
import com.example.model.ModuleProgress
import com.example.model.Subject
import com.example.ui.theme.NavyDark
import com.example.ui.theme.NavyPrimary
import com.example.util.ModuleProgressEngine

/**
 * Recharts-powered interactive WebView dashboard visualizing student progress across
 * Physics and Chemistry modules, showing completion rates and quiz performance trends.
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun RechartsDashboardView(
    modules: List<ModuleProgress>,
    dailyRecords: List<DailyStudyRecord>,
    selectedSubject: Subject? = null,
    modifier: Modifier = Modifier,
    height: Dp = 560.dp
) {
    val htmlContent = remember(modules, dailyRecords, selectedSubject) {
        ModuleProgressEngine.generateRechartsDashboardHtml(modules, dailyRecords, selectedSubject)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(16.dp))
            .background(NavyDark)
            .border(1.dp, androidx.compose.ui.graphics.Color.White.copy(alpha = 0.12f), RoundedCornerShape(16.dp))
            .testTag("recharts_dashboard_webview_container")
    ) {
        AndroidView(
            modifier = Modifier.testTag("recharts_webview"),
            factory = { context ->
                WebView(context).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    setBackgroundColor(Color.parseColor("#0A1628"))
                    settings.apply {
                        javaScriptEnabled = true
                        domStorageEnabled = true
                        loadWithOverviewMode = true
                        useWideViewPort = true
                        setSupportZoom(false)
                        cacheMode = WebSettings.LOAD_NO_CACHE
                    }
                    webChromeClient = WebChromeClient()
                    webViewClient = WebViewClient()
                    loadDataWithBaseURL("https://recharts.local", htmlContent, "text/html", "UTF-8", null)
                }
            },
            update = { webView ->
                webView.loadDataWithBaseURL("https://recharts.local", htmlContent, "text/html", "UTF-8", null)
            }
        )
    }
}
