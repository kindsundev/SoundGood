package com.example.soundgood.activity.navigation

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.soundgood.MainActivity
import com.example.soundgood.databinding.ActivityFeedbackBinding

class FeedbackActivity : AppCompatActivity() {

    lateinit var binding: ActivityFeedbackBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initializedLayout()
        initializedListeners()
    }

    private fun initializedLayout() {
        setTheme(MainActivity.currentTheme[MainActivity.themeIndex])
        binding = ActivityFeedbackBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.title = "Feedback"
    }

    private fun initializedListeners() {
        binding.sendFA.setOnClickListener {
            Toast.makeText(this, "Thanks for the feedback!!", Toast.LENGTH_SHORT).show()
            finish()
        }

        binding.backBtnFA.setOnClickListener { finish() }
    }
}