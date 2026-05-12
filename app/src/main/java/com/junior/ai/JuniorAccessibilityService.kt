package com.junior.ai

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent

class JuniorAccessibilityService : AccessibilityService() {
    companion object {
        var instance: JuniorAccessibilityService? = null
    }

    override fun onServiceConnected() {
        instance = this
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}

    override fun onInterrupt() {}

    override fun onDestroy() {
        instance = null
        super.onDestroy()
    }

    // Screenshot lena
    fun takeScreenshot() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
            performGlobalAction(GLOBAL_ACTION_TAKE_SCREENSHOT)
        }
    }

    // Back button
    fun pressBack() {
        performGlobalAction(GLOBAL_ACTION_BACK)
    }

    // Home button
    fun pressHome() {
        performGlobalAction(GLOBAL_ACTION_HOME)
    }

    // Recent apps
    fun showRecents() {
        performGlobalAction(GLOBAL_ACTION_RECENTS)
    }
}
