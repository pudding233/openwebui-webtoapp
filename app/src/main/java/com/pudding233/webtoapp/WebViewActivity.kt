package com.pudding233.webtoapp

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.webkit.*
import android.view.View
import android.graphics.Color
import android.view.WindowManager
import android.webkit.ValueCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.pudding233.webtoapp.cache.CacheManager
import com.pudding233.webtoapp.web.CachingWebViewClient

class WebViewActivity : AppCompatActivity() {
    private lateinit var webView: WebView
    private lateinit var cacheManager: CacheManager
    private var filePathCallback: ValueCallback<Array<Uri>>? = null
    
    // 文件选择器启动器
    private val fileChooserLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            val results = if (data?.clipData != null) { // 处理多个文件
                val count = data.clipData!!.itemCount
                Array(count) { i -> data.clipData!!.getItemAt(i).uri }
            } else if (data?.data != null) { // 处理单个文件
                arrayOf(data.data!!)
            } else {
                null
            }
            filePathCallback?.onReceiveValue(results ?: arrayOf())
        } else {
            filePathCallback?.onReceiveValue(arrayOf())
        }
        filePathCallback = null
    }

    // 权限请求启动器
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.all { it.value }) {
            openFileChooser()
        } else {
            filePathCallback?.onReceiveValue(arrayOf())
            filePathCallback = null
        }
    }

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
            // 添加文件访问支持
            allowFileAccess = true
            allowContentAccess = true
        }

        // 使用自定义的WebViewClient
        webView.webViewClient = object : CachingWebViewClient(cacheManager) {
            override fun onPageFinished(view: WebView, url: String) {
                super.onPageFinished(view, url)
                // 页面加载完成后执行JavaScript代码
                webView.evaluateJavascript(paddingScript, null)
            }
        }

        // 设置WebChromeClient处理文件上传
        webView.webChromeClient = object : WebChromeClient() {
            override fun onShowFileChooser(
                webView: WebView,
                filePathCallback: ValueCallback<Array<Uri>>,
                fileChooserParams: FileChooserParams
            ): Boolean {
                this@WebViewActivity.filePathCallback?.onReceiveValue(arrayOf())
                this@WebViewActivity.filePathCallback = filePathCallback

                checkAndRequestPermissions()
                return true
            }
        }

        // 加载网页
        webView.loadUrl("https://chat.furryowo.top/")
    }

    private fun checkAndRequestPermissions() {
        val permissions = if (android.os.Build.VERSION.SDK_INT >= 33) {
            arrayOf(
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VIDEO,
                Manifest.permission.READ_MEDIA_AUDIO
            )
        } else {
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        val notGrantedPermissions = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }.toTypedArray()

        if (notGrantedPermissions.isEmpty()) {
            openFileChooser()
        } else {
            permissionLauncher.launch(notGrantedPermissions)
        }
    }

    private fun openFileChooser() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        }
        fileChooserLauncher.launch(intent)
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