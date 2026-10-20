package com.example.focusguard

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.accessibility.AccessibilityEvent

class FocusAccessibilityService : AccessibilityService() {
    private var lastBlockedPackage: String? = null
    private var lastLaunchAt = 0L

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null || !FocusStore.isActive(this)) {
            lastBlockedPackage = null
            return
        }
        val packageName = event.packageName?.toString() ?: return
        if (packageName == packageNameOfSelf() || packageName == "com.android.systemui") return
        if (packageName !in FocusStore.blocklist(this)) {
            lastBlockedPackage = null
            return
        }

        val now = System.currentTimeMillis()
        // Avoid launching the block activity repeatedly for the same app/window event burst.
        if (lastBlockedPackage == packageName && now - lastLaunchAt < 1500) return
        lastBlockedPackage = packageName
        lastLaunchAt = now

        startActivity(Intent(this, BlockActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            putExtra(BlockActivity.EXTRA_BLOCKED_PACKAGE, packageName)
        })
    }

    private fun packageNameOfSelf() = applicationContext.packageName
    override fun onInterrupt() {}
}
