package com.pudding233.webtoapp.web

import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import com.pudding233.webtoapp.cache.CacheManager
import java.io.ByteArrayInputStream
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

open class CachingWebViewClient(private val cacheManager: CacheManager) : WebViewClient() {
    
    override fun shouldInterceptRequest(
        view: WebView,
        request: WebResourceRequest
    ): WebResourceResponse? {
        val url = request.url.toString()
        
        // 只缓存静态资源
        if (!cacheManager.isStaticResource(url)) {
            return super.shouldInterceptRequest(view, request)
        }

        // 检查缓存
        cacheManager.getCachedFile(url)?.let { cachedFile ->
            return WebResourceResponse(
                getMimeType(url),
                "UTF-8",
                ByteArrayInputStream(cachedFile.readBytes())
            )
        }

        // 如果没有缓存，下载并缓存
        try {
            val connection = URL(url).openConnection() as HttpURLConnection
            val input = connection.inputStream
            val data = input.readBytes()
            
            // 异步缓存资源
            CoroutineScope(Dispatchers.IO).launch {
                cacheManager.cacheResource(url, data)
            }

            return WebResourceResponse(
                getMimeType(url),
                "UTF-8",
                ByteArrayInputStream(data)
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return super.shouldInterceptRequest(view, request)
    }

    private fun getMimeType(url: String): String {
        return when(url.substringAfterLast('.', "").lowercase()) {
            "jpg", "jpeg" -> "image/jpeg"
            "png" -> "image/png"
            "gif" -> "image/gif"
            "webp" -> "image/webp"
            "css" -> "text/css"
            "js" -> "application/javascript"
            "woff" -> "font/woff"
            "woff2" -> "font/woff2"
            "ttf" -> "font/ttf"
            "svg" -> "image/svg+xml"
            "ico" -> "image/x-icon"
            else -> "application/octet-stream"
        }
    }

    override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
        view.loadUrl(url)
        return true
    }

    open override fun onPageFinished(view: WebView, url: String) {
        super.onPageFinished(view, url)
    }
} 