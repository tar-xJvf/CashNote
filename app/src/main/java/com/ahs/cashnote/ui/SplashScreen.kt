package com.ahs.cashnote.ui

import android.annotation.SuppressLint
import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.ahs.cashnote.R
import com.ahs.cashnote.databinding.ActivitySplashScreenBinding
import com.ahs.cashnote.ui.theme.ThemePreference
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

@SuppressLint("CustomSplashScreen")
class SplashScreen : AppCompatActivity() {
    private lateinit var bindingSplash: ActivitySplashScreenBinding
    override fun onCreate(savedInstanceState: Bundle?) {

        runBlocking {
            ThemePreference.getTheme(this@SplashScreen).first().let { theme ->
                when (theme) {
                    "light" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                    "dark" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                    else -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                }
            }
        }

        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        bindingSplash = ActivitySplashScreenBinding.inflate(layoutInflater)
        val view = bindingSplash.root

        setContentView(view)
        ViewCompat.setOnApplyWindowInsetsListener(bindingSplash.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        lifecycleScope.launch {
            delay(4000)
            val intent=Intent(this@SplashScreen, MainActivity::class.java)
            val options = ActivityOptions.makeCustomAnimation(
                this@SplashScreen,
                R.anim.fade_in,
                R.anim.fade_out
            )
            startActivity(intent, options.toBundle())
            finish()
        }


    }
}