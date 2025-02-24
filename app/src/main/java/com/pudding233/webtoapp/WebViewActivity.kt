package com.pudding233.webtoapp

import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.view.View
import android.graphics.Color
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.pudding233.webtoapp.cache.CacheManager
import com.pudding233.webtoapp.web.CachingWebViewClient

class WebViewActivity : AppCompatActivity() {
    private lateinit var webView: WebView
    private lateinit var cacheManager: CacheManager
    
    private val paddingScript = """
        function adjustPadding() {
            const mainContent = document.querySelector('.h-screen.max-h-\\[100dvh\\].transition-width.duration-200.ease-in-out.w-full.max-w-full.flex.flex-col');
            const sidebar = document.querySelector('.py-2.my-auto.flex.flex-col.justify-between.h-screen.max-h-\\[100dvh\\].w-\\[260px\\].overflow-x-hidden.z-50');
            
            if (mainContent) mainContent.style.paddingTop = "50px";
            if (sidebar) sidebar.style.paddingTop = "50px";
        }
        
        // 执行一次
        adjustPadding();
        
        // 监听DOM变化，确保在动态加载的内容上也能生效
        const observer = new MutationObserver(function(mutations) {
            adjustPadding();
        });
        
        observer.observe(document.body, {
            childList: true,
            subtree: true
        });
    """.trimIndent()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 初始化缓存管理器
        cacheManager = CacheManager(this)
        
        // 设置状态栏
        window.apply {
            // 设置状态栏黑色字符
            addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or  // 设置状态栏黑色字符
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            )
            
            // 设置状态栏和导航栏颜色
            statusBarColor = Color.TRANSPARENT
            navigationBarColor = Color.TRANSPARENT
        }
        
        // 使用布局文件
        setContentView(R.layout.activity_webview)
        
        // 初始化WebView
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
            
            // 启用应用缓存
            cacheMode = WebSettings.LOAD_DEFAULT
            databaseEnabled = true
            domStorageEnabled = true
        }

        // 使用自定义的WebViewClient
        webView.webViewClient = object : CachingWebViewClient(cacheManager) {
            override fun onPageFinished(view: WebView, url: String) {
                super.onPageFinished(view, url)
                // 页面加载完成后执行JavaScript代码
                webView.evaluateJavascript(paddingScript, null)
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