package com.pudding233.webtoapp

import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.webkit.*
import androidx.appcompat.app.AppCompatActivity
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import android.util.Log

class WebViewActivity : AppCompatActivity() {
    private lateinit var webView: WebView
    private lateinit var cacheDir: File
    private val staticExtensions = setOf(".js", ".css", ".jpg", ".jpeg", ".png", ".gif", ".ico", ".woff", ".woff2")
    private val CACHE_EXPIRATION_TIME = 3 * 24 * 60 * 60 * 1000L // 3天的毫秒数

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 设置沉浸式状态栏和导航栏，以及状态栏文字颜色
        window.apply {
            addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)      // 设置沉浸式状态栏
            addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)  // 设置沉浸式虚拟键
            // 设置状态栏黑色字体
            decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }
        
        // 使用布局文件
        setContentView(R.layout.activity_webview)
        
        // 获取WebView实例
        webView = findViewById(R.id.webView)

        // 初始化缓存目录
        cacheDir = File(applicationContext.cacheDir, "web_cache")
        if (!cacheDir.exists()) {
            cacheDir.mkdirs()
        }
        cleanExpiredCache() // 清理过期缓存

        // 配置WebView设置
        webView.settings.apply {
            javaScriptEnabled = true  // 启用JavaScript
            domStorageEnabled = true  // 启用DOM存储
            useWideViewPort = true    // 使用宽视图
            loadWithOverviewMode = true // 自适应屏幕
            setSupportZoom(true)      // 支持缩放
            builtInZoomControls = true // 显示内置缩放控件
            displayZoomControls = false // 隐藏缩放控件
            
            // 缓存配置
            cacheMode = WebSettings.LOAD_DEFAULT
            databaseEnabled = true
            domStorageEnabled = true // 启用 DOM storage API
        }

        // 设置自定义WebViewClient来处理资源加载
        webView.webViewClient = object : WebViewClient() {
            override fun shouldInterceptRequest(
                view: WebView,
                request: WebResourceRequest
            ): WebResourceResponse? {
                val url = request.url.toString()
                
                // 检查是否是静态资源
                if (isStaticResource(url)) {
                    val cachedResponse = loadFromCache(url)
                    if (cachedResponse != null) {
                        return cachedResponse
                    }
                    
                    // 如果缓存中没有，则从网络加载并缓存
                    try {
                        val connection = java.net.URL(url).openConnection()
                        val input = connection.getInputStream()
                        val mimeType = connection.contentType ?: getMimeType(url)
                        
                        // 保存到缓存
                        val cacheFile = getCacheFile(url)
                        FileOutputStream(cacheFile).use { output ->
                            input.copyTo(output)
                        }
                        
                        // 返回资源
                        return WebResourceResponse(
                            mimeType,
                            "UTF-8",
                            cacheFile.inputStream()
                        )
                    } catch (e: IOException) {
                        Log.e("WebView", "Error loading resource: $url", e)
                    }
                }
                
                return super.shouldInterceptRequest(view, request)
            }

            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                view.loadUrl(url)
                return true
            }
        }

        // 加载网页
        webView.loadUrl("https://chat.furryowo.top/")
    }

    private fun isStaticResource(url: String): Boolean {
        return staticExtensions.any { url.lowercase().endsWith(it) }
    }

    private fun getCacheFile(url: String): File {
        val fileName = url.replace(Regex("[^a-zA-Z0-9.]"), "_")
        return File(cacheDir, fileName)
    }

    private fun loadFromCache(url: String): WebResourceResponse? {
        val cacheFile = getCacheFile(url)
        if (cacheFile.exists()) {
            // 检查文件是否过期
            val lastModified = cacheFile.lastModified()
            val now = System.currentTimeMillis()
            if (now - lastModified > CACHE_EXPIRATION_TIME) {
                // 文件已过期，删除它
                cacheFile.delete()
                return null
            }

            try {
                return WebResourceResponse(
                    getMimeType(url),
                    "UTF-8",
                    cacheFile.inputStream()
                )
            } catch (e: IOException) {
                Log.e("WebView", "Error loading from cache: $url", e)
            }
        }
        return null
    }

    private fun getMimeType(url: String): String {
        return when {
            url.endsWith(".js") -> "application/javascript"
            url.endsWith(".css") -> "text/css"
            url.endsWith(".jpg", true) || url.endsWith(".jpeg", true) -> "image/jpeg"
            url.endsWith(".png") -> "image/png"
            url.endsWith(".gif") -> "image/gif"
            url.endsWith(".ico") -> "image/x-icon"
            url.endsWith(".woff") -> "font/woff"
            url.endsWith(".woff2") -> "font/woff2"
            else -> "application/octet-stream"
        }
    }

    // 添加清理过期缓存的方法
    private fun cleanExpiredCache() {
        try {
            val now = System.currentTimeMillis()
            cacheDir.listFiles()?.forEach { file ->
                if (now - file.lastModified() > CACHE_EXPIRATION_TIME) {
                    file.delete()
                }
            }
        } catch (e: Exception) {
            Log.e("WebView", "Error cleaning expired cache", e)
        }
    }

    // 处理返回按钮
    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }
} 