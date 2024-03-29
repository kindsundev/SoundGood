package com.example.soundgood.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.soundgood.MainActivity
import com.example.soundgood.R
import com.example.soundgood.adapter.MusicAdapter
import com.example.soundgood.databinding.ActivityPlaylistDetailBinding
import com.example.soundgood.model.checkPlaylist
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.GsonBuilder

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
        setTheme(MainActivity.currentTheme[MainActivity.themeIndex])
        binding = ActivityPlaylistDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        try {
            PlaylistActivity.musicPlaylist.ref[currentPlaylistPosition].playlist =
                checkPlaylist(playlist = PlaylistActivity.musicPlaylist.ref[currentPlaylistPosition].playlist)
        } catch (e: Exception) {
            return
        }
        initAdapter()
    }

    private fun initAdapter() {
        binding.playlistDetailRV.setItemViewCacheSize(10)
        binding.playlistDetailRV.setHasFixedSize(true)
        binding.playlistDetailRV.layoutManager = LinearLayoutManager(this)
        adapter = MusicAdapter(this, PlaylistActivity.musicPlaylist.ref[currentPlaylistPosition].playlist, true)
        binding.playlistDetailRV.adapter = adapter
    }

    private fun initializedListeners() {
        binding.backBtnPD.setOnClickListener { finish() }
        binding.shuffleBtnPD.setOnClickListener { onClickShuffle() }
        binding.addBtnPD.setOnClickListener { onClickAdd() }
        binding.removeAllBtnPD.setOnClickListener { onClickRemoveAll() }
    }

    private fun onClickShuffle() {
        val intent = Intent(this, PlayerActivity::class.java)
        intent.putExtra("index", 0)
        intent.putExtra("class", "PlaylistDetailShuffle")
        startActivity(intent)
    }

    private fun onClickAdd() {
        val intent = Intent(this, SelectionActivity::class.java)
        startActivity(intent)
    }

    private fun onClickRemoveAll() {
        val builder = MaterialAlertDialogBuilder(this)
        builder.setTitle("Remove")
            .setMessage("Do you want to remove all songs from playlist?")
            .setNegativeButton("No") {dialog, _ ->
                dialog.dismiss()
            }
            .setPositiveButton("Yes"){dialog, _ ->
                PlaylistActivity.musicPlaylist.ref[currentPlaylistPosition].playlist.clear()
                adapter.refreshPlaylist()
                dialog.dismiss()
            }
        val customDialog = builder.create()
        customDialog.show()
        customDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.RED)
        customDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.RED)
    }

    override fun onResume() {
        super.onResume()
        bindLayout()
    }

    @SuppressLint("SetTextI18n", "NotifyDataSetChanged")
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

        adapter.notifyDataSetChanged()
        storingFavoriteData()
    }

    @SuppressLint("CommitPrefEdits")
    fun storingFavoriteData() {
        val editor = getSharedPreferences("FAVORITES", MODE_PRIVATE).edit()
        val jsonStringPlaylist = GsonBuilder().create().toJson(PlaylistActivity.musicPlaylist)
        editor.putString("MusicPlaylist", jsonStringPlaylist)
        editor.apply()
    }
}