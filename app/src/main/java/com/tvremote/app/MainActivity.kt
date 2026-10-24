package com.tvremote.app

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val tvStatus = findViewById<TextView>(R.id.tvStatus)
        val tvUrl = findViewById<TextView>(R.id.tvUrl)
        val btnEnable = findViewById<TextView>(R.id.btnEnable)
        val btnStart = findViewById<TextView>(R.id.btnStart)

        tvUrl.text = "http://127.0.0.1:8080"

        if (isAccessibilityEnabled()) {
            tvStatus.text = "Status: READY"
            tvStatus.setTextColor(getColor(android.R.color.holo_green_dark))
            btnStart.isEnabled = true
        } else {
            tvStatus.text = "Status: DISABLED"
            tvStatus.setTextColor(getColor(android.R.color.holo_red_dark))
            btnStart.isEnabled = false
        }

        btnEnable.setOnClickListener {
            try {
                startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            } catch (e: Exception) {
                Toast.makeText(this, "Buka Settings manual: Accessibility", Toast.LENGTH_SHORT).show()
            }
        }

        btnStart.setOnClickListener {
            startForegroundService(Intent(this, RemoteForegroundService::class.java))
            Toast.makeText(this, "Server dimulai", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        val tvStatus = findViewById<TextView>(R.id.tvStatus)
        if (isAccessibilityEnabled()) {
            tvStatus.text = "Status: READY"
            tvStatus.setTextColor(getColor(android.R.color.holo_green_dark))
            findViewById<TextView>(R.id.btnStart).isEnabled = true
        }
    }

    private fun isAccessibilityEnabled(): Boolean {
        val componentName = ComponentName(this, RemoteAccessibilityService::class.java)
        val enabled = try {
            Settings.Secure.getInt(contentResolver, Settings.Secure.ACCESSIBILITY_ENABLED) == 1
        } catch (e: Exception) {
            false
        }

        if (!enabled) return false

        val enabledServices = Settings.Secure.getString(contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES)
        return enabledServices?.contains(componentName.flattenToString()) == true
    }
}
