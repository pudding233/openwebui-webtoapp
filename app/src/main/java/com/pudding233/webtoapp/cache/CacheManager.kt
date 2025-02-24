package com.pudding233.webtoapp.cache

import android.content.Context
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit

class CacheManager(context: Context) {
    private val cacheDir = File(context.cacheDir, "web_cache")
    private val maxAge = TimeUnit.DAYS.toMillis(3) // 3天的过期时间

    init {
        if (!cacheDir.exists()) {
            cacheDir.mkdirs()
        }
        cleanExpiredCache()
    }

    fun getCachedFile(url: String): File? {
        val file = File(cacheDir, url.hashCode().toString())
        return if (file.exists() && !isExpired(file)) {
            file
        } else {
            null
        }
    }

    fun cacheResource(url: String, data: ByteArray) {
        val file = File(cacheDir, url.hashCode().toString())
        FileOutputStream(file).use { 
            it.write(data)
        }
    }

    private fun isExpired(file: File): Boolean {
        val lastModified = file.lastModified()
        val now = System.currentTimeMillis()
        return (now - lastModified) > maxAge
    }

    private fun cleanExpiredCache() {
        cacheDir.listFiles()?.forEach { file ->
            if (isExpired(file)) {
                file.delete()
            }
        }
    }

    fun isStaticResource(url: String): Boolean {
        val extension = url.substringAfterLast('.', "").lowercase()
        return extension in listOf(
            "jpg", "jpeg", "png", "gif", "webp",  // 图片
            "css", "js",                          // 样式和脚本
            "woff", "woff2", "ttf", "eot",        // 字体
            "svg", "ico"                          // 图标
        )
    }
} 