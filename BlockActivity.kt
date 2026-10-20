package com.example.focusguard

import android.app.Activity
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class BlockActivity : Activity() {
    companion object { const val EXTRA_BLOCKED_PACKAGE = "blocked_package" }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        render()
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        render()
    }

    private fun render() {
        if (!FocusStore.isActive(this)) { finish(); return }
        val blocked = intent.getStringExtra(EXTRA_BLOCKED_PACKAGE) ?: "该应用"
        val left = ((FocusStore.endAt(this) - System.currentTimeMillis()).coerceAtLeast(0) / 1000)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(32, 32, 32, 32)
        }
        root.addView(TextView(this).apply {
            text = "现在是专注时间"
            textSize = 28f
            gravity = Gravity.CENTER
        })
        root.addView(TextView(this).apply {
            text = "已屏蔽：$blocked\n剩余 ${left / 60} 分 ${left % 60} 秒\n\n先完成当前目标，再回来使用这个应用。"
            textSize = 18f
            gravity = Gravity.CENTER
            setPadding(0, 24, 0, 24)
        })
        root.addView(Button(this).apply {
            text = "返回专注"
            setOnClickListener { finish() }
        })
        setContentView(root)
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        // Returning to the blocked app should trigger the accessibility service again.
        super.onBackPressed()
    }
}
