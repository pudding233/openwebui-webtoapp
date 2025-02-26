package com.pudding233.webtoapp

import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.webkit.WebView
import android.webkit.WebViewClient
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import androidx.appcompat.app.AppCompatActivity

class WebViewActivity : AppCompatActivity() {
    private lateinit var webView: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 设置状态栏
        window.apply {
            // 设置状态栏绘制
            addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            // 清除半透明状态栏
            clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            // 设置导航栏半透明
            addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
            // 设置状态栏黑色字体
            decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }
        
        // 使用布局文件
        setContentView(R.layout.activity_webview)
        
        // 获取WebView实例
        webView = findViewById(R.id.webView)

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

        // 设置WebViewClient以在WebView内处理导航
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                view.loadUrl(url)
                return true
            }
        }

        // 加载网页
        webView.loadUrl("https://chat.furryowo.top/")
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