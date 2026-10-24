package com.tvremote.app

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

class RemoteAccessibilityService : AccessibilityService() {
    companion object {
        var instance: RemoteAccessibilityService? = null
            private set
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        Log.d("TVRemote", "AccessibilityService connected")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // No-op: we only use this for injecting input
    }

    override fun onInterrupt() {
        // No-op
    }

    override fun onDestroy() {
        super.onDestroy()
        instance = null
    }

    fun sendKey(keyCode: Int): Boolean {
        return try {
            val event = KeyEvent(KeyEvent.ACTION_DOWN, keyCode)
            val event2 = KeyEvent(KeyEvent.ACTION_UP, keyCode)
            sendKeyEvent(event)
            sendKeyEvent(event2)
            Log.d("TVRemote", "Sent keycode: $keyCode")
            true
        } catch (e: Exception) {
            Log.e("TVRemote", "Failed to send keycode: $keyCode", e)
            false
        }
    }

    fun sendText(text: String): Boolean {
        return try {
            val args = Bundle().apply {
                putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text)
            }
            val root = rootInActiveWindow ?: return false
            val focused = root.findFocus(AccessibilityNodeInfo.FOCUS_INPUT)
                ?: root
            focused.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args)
            Log.d("TVRemote", "Sent text: $text")
            true
        } catch (e: Exception) {
            Log.e("TVRemote", "Failed to send text", e)
            false
        }
    }

    fun launchApp(packageName: String): Boolean {
        return try {
            val intent = packageManager.getLaunchIntentForPackage(packageName) ?: return false
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            Log.d("TVRemote", "Launched: $packageName")
            true
        } catch (e: Exception) {
            Log.e("TVRemote", "Failed to launch: $packageName", e)
            false
        }
    }
}
