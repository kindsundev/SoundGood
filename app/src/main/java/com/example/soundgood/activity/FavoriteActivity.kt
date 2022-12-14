package com.example.soundgood.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.example.soundgood.R
import com.example.soundgood.adapter.FavoriteAdapter
import com.example.soundgood.databinding.ActivityFavoriteBinding
import com.example.soundgood.model.Music

class FavoriteActivity : AppCompatActivity() {
    private val TAG = "FavoriteActivity"
    private lateinit var binding: ActivityFavoriteBinding
    private lateinit var adapter: FavoriteAdapter

    companion object {
        var favoriteSongs: ArrayList<Music> = ArrayList()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initializedLayout()
        initializedListeners()
    }

    private fun initializedLayout() {
        setTheme(R.style.coolPink)
        binding = ActivityFavoriteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.favoriteRV.setHasFixedSize(true)
        binding.favoriteRV.setItemViewCacheSize(13)
        binding.favoriteRV.layoutManager = GridLayoutManager(this, 4)
        adapter = FavoriteAdapter(this, favoriteSongs)
        binding.favoriteRV.adapter = adapter
    }

    private fun initializedListeners() {
        binding.backBtnFA.setOnClickListener { finish() }
    }
}