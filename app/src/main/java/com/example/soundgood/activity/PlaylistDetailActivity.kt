package com.example.soundgood.activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.soundgood.MainActivity
import com.example.soundgood.R
import com.example.soundgood.adapter.MusicAdapter
import com.example.soundgood.databinding.ActivityPlaylistDetailBinding

class PlaylistDetailActivity : AppCompatActivity() {

    lateinit var binding: ActivityPlaylistDetailBinding
    lateinit var adapter: MusicAdapter

    companion object {
        var currentPlaylistPosition: Int = -1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        currentPlaylistPosition = intent?.extras?.get("index") as Int
        initializedLayout()
        initializedListeners()
    }

    private fun initializedLayout() {
        setTheme(R.style.coolPink)
        binding = ActivityPlaylistDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initAdapter()
    }

    private fun initAdapter() {
        binding.playlistDetailRV.setItemViewCacheSize(10)
        binding.playlistDetailRV.setHasFixedSize(true)
        binding.playlistDetailRV.layoutManager = LinearLayoutManager(this)
        PlaylistActivity.musicPlaylist.ref[currentPlaylistPosition].playlist.addAll(MainActivity.musicListMA)
        PlaylistActivity.musicPlaylist.ref[currentPlaylistPosition].playlist.shuffle()
        adapter = MusicAdapter(this, PlaylistActivity.musicPlaylist.ref[currentPlaylistPosition].playlist, true)
        binding.playlistDetailRV.adapter = adapter
    }

    private fun initializedListeners() {
        binding.backBtnPD.setOnClickListener { finish() }
    }

    override fun onResume() {
        super.onResume()
        bindLayout()
    }

    @SuppressLint("SetTextI18n")
    private fun bindLayout() {
        binding.playlistNamePD.text = PlaylistActivity.musicPlaylist.ref[currentPlaylistPosition].name
        binding.totalSongPD.text = "Total songs: ${adapter.itemCount}"
        binding.createOnPD.text = "Create on: ${PlaylistActivity.musicPlaylist.ref[currentPlaylistPosition].createOn}"
        binding.byPD.text = "By: ${PlaylistActivity.musicPlaylist.ref[currentPlaylistPosition].createBy}"

        if (adapter.itemCount > 0) {
            try {
                Glide.with(this)
                    .load(PlaylistActivity.musicPlaylist.ref[currentPlaylistPosition].playlist[0].artUri)
                    .apply(RequestOptions().placeholder(R.drawable.sound_good_icon_slash_screen).centerCrop())
                    .into(binding.playlistImgPD)
                binding.shuffleBtnPD.visibility = View.VISIBLE
            } catch (e: Exception) {
                return
            }
        }
    }
}