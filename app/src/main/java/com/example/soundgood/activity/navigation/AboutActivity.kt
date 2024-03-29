package com.example.soundgood.activity.navigation

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.soundgood.MainActivity
import com.example.soundgood.databinding.ActivityAboutBinding

class AboutActivity : AppCompatActivity() {

    lateinit var binding: ActivityAboutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initializedLayout()
        initializedListeners()
    }

    private fun initializedLayout() {
        setTheme(MainActivity.currentTheme[MainActivity.themeIndex])
        binding = ActivityAboutBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.title = "About"
        binding.aboutText.text = aboutText()
    }

    private fun aboutText(): String {
        return """Developed By: Duong Tien 
            
        |If you want to provide feedback, I will love to hear that. 
        """.trimMargin()
    }

    private fun initializedListeners() {
        binding.backBtnAA.setOnClickListener { finish() }
    }
}