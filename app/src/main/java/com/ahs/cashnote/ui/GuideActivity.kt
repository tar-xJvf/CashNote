package com.ahs.cashnote.ui

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.ahs.cashnote.databinding.ActivityGuideBinding

class GuideActivity : AppCompatActivity() {

    private lateinit var guideActivity: ActivityGuideBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        guideActivity = ActivityGuideBinding.inflate(layoutInflater)
        val view = guideActivity.root
        setContentView(view)
        setupToolbar()
        setupInserts()

    }

    private fun setupToolbar() {

        guideActivity.toolbar.title = "Guide CashNote"
        setSupportActionBar(guideActivity.toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
    }

    private fun setupInserts() {

        ViewCompat.setOnApplyWindowInsetsListener(guideActivity.root) { v, insets ->
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