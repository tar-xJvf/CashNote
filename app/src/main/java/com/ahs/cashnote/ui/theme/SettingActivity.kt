package com.ahs.cashnote.ui.theme

import android.content.Intent
import android.os.Bundle
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.ahs.cashnote.databinding.ActivitySettingBinding
import com.ahs.cashnote.ui.SplashScreen
import kotlinx.coroutines.launch

class SettingActivity : AppCompatActivity() {

    private lateinit var settingActivity: ActivitySettingBinding
    private var currentTheme: String = "system"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        settingActivity = ActivitySettingBinding.inflate(layoutInflater)
        val view = settingActivity.root
        setContentView(view)
        setupToolbar()
        setupInserts()
        val themeSpinner = settingActivity.themeSpinner
        val options = listOf("System Default", "Light", "Dark")
        themeSpinner.adapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_item, options)

        lifecycleScope.launch {
            ThemePreference.getTheme(this@SettingActivity).collect { theme ->
                currentTheme = theme
                themeSpinner.setSelection(
                    when (theme) {
                        "light" -> 1
                        "dark" -> 2
                        else -> 0
                    },
                    false
                )
            }
        }
        themeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: android.view.View?,
                position: Int,
                id: Long
            ) {
                val theme = when (position) {
                    1 -> "light"
                    2 -> "dark"
                    else -> "system"
                }

                if (theme != currentTheme) {
                    lifecycleScope.launch {
                        ThemePreference.savedTheme(this@SettingActivity, theme)
                        restartApps()
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                TODO("Not yet implemented")
            }

            private fun restartApps() {
                val intent = Intent(this@SettingActivity, SplashScreen::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                finish()

            }
        }
    }

    private fun setupToolbar() {

            settingActivity.toolbar.title = "Settings"
            setSupportActionBar(settingActivity.toolbar)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.setDisplayShowHomeEnabled(true)

    }

    private fun setupInserts() {
        ViewCompat.setOnApplyWindowInsetsListener(settingActivity.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}