package com.example.soundgood.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.soundgood.R
import com.example.soundgood.databinding.ActivityPlaylistBinding

class PlaylistActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPlaylistBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initializedLayout()
        initializedListeners()
    }

    private fun initializedLayout() {
        setTheme(R.style.coolPink)
        binding = ActivityPlaylistBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    private fun initializedListeners() {
        binding.backBtnPLA.setOnClickListener { finish() }
    }
}