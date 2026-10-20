package com.example.focusguard

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {
    private lateinit var status: TextView
    private lateinit var blocklist: EditText
    private lateinit var minutes: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= 33) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 20)
        }

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(28, 36, 28, 28)
            gravity = Gravity.CENTER_HORIZONTAL
        }
        root.addView(TextView(this).apply {
            text = "FocusGuard"
            textSize = 30f
        }, match(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
        root.addView(TextView(this).apply {
            text = "严格专注 · 本地运行 · 不上传数据"
            textSize = 16f
            setPadding(0, 8, 0, 24)
        })

        root.addView(TextView(this).apply { text = "屏蔽应用包名（用英文逗号分隔）"; textSize = 16f })
        blocklist = EditText(this).apply {
            hint = "com.example.game, com.example.social"
            minLines = 2
            setText(FocusStore.blocklist(this@MainActivity).joinToString(", "))
        }
        root.addView(blocklist, match(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))

        val save = Button(this).apply {
            text = "保存屏蔽名单"
            setOnClickListener {
                FocusStore.setBlocklist(this@MainActivity, blocklist.text.toString())
                blocklist.setText(FocusStore.blocklist(this@MainActivity).joinToString(", "))
                Toast.makeText(this@MainActivity, "已保存", Toast.LENGTH_SHORT).show()
            }
        }
        root.addView(save, match(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))

        root.addView(TextView(this).apply {
            text = "专注时长（分钟）"
            textSize = 16f
            setPadding(0, 18, 0, 4)
        })
        minutes = EditText(this).apply {
            setText("25")
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
        }
        root.addView(minutes, match(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))

        root.addView(Button(this).apply {
            text = "打开无障碍设置"
            setOnClickListener {
                startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            }
        }, match(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))

        root.addView(Button(this).apply {
            text = "开始严格专注"
            setOnClickListener {
                FocusStore.setBlocklist(this@MainActivity, blocklist.text.toString())
                val mins = minutes.text.toString().toLongOrNull()?.coerceIn(1, 240) ?: 25L
                FocusStore.setEndAt(this@MainActivity, System.currentTimeMillis() + mins * 60_000L)
                val intent = Intent(this@MainActivity, FocusTimerService::class.java)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) startForegroundService(intent) else startService(intent)
                updateStatus()
                Toast.makeText(this@MainActivity, "专注已开始", Toast.LENGTH_SHORT).show()
            }
        }, match(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))

        root.addView(Button(this).apply {
            text = "结束专注"
            setOnClickListener {
                FocusStore.clear(this@MainActivity)
                stopService(Intent(this@MainActivity, FocusTimerService::class.java))
                updateStatus()
            }
        }, match(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))

        status = TextView(this).apply {
            textSize = 16f
            setPadding(0, 24, 0, 0)
        }
        root.addView(status, match(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
        root.addView(TextView(this).apply {
            text = "提示：严格模式需要手动启用无障碍服务。普通应用无法阻止用户强制停止应用或撤销权限。首次测试请使用非关键应用。"
            textSize = 13f
            setPadding(0, 20, 0, 0)
        })
        setContentView(root)
        updateStatus()
    }

    override fun onResume() { super.onResume(); if (::status.isInitialized) updateStatus() }

    private fun updateStatus() {
        if (!::status.isInitialized) return
        val active = FocusStore.isActive(this)
        status.text = if (active) {
            val left = ((FocusStore.endAt(this) - System.currentTimeMillis()).coerceAtLeast(0) / 1000)
            "状态：专注中，剩余 ${left / 60} 分 ${left % 60} 秒"
        } else "状态：未专注"
    }

    private fun match(w: Int, h: Int) = LinearLayout.LayoutParams(w, h).apply { bottomMargin = 8 }
}
