package com.example.soundgood.activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.example.soundgood.R
import com.example.soundgood.adapter.PlaylistAdapter
import com.example.soundgood.databinding.ActivityPlaylistBinding
import com.example.soundgood.databinding.AddPlaylistDialogBinding
import com.example.soundgood.model.MusicPlaylist
import com.example.soundgood.model.Playlist
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.text.SimpleDateFormat
import java.util.*

class PlaylistActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPlaylistBinding
    private lateinit var adapter: PlaylistAdapter

    companion object {
        var musicPlaylist: MusicPlaylist = MusicPlaylist()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initializedLayout()
        initializedListeners()
    }

    private fun initializedLayout() {
        setTheme(R.style.coolPink)
        binding = ActivityPlaylistBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initPlaylistAdapter()
    }

    private fun initPlaylistAdapter() {
        binding.playlistRV.setHasFixedSize(true)
        binding.playlistRV.setItemViewCacheSize(13)
        binding.playlistRV.layoutManager = GridLayoutManager(this@PlaylistActivity, 2)
        adapter = PlaylistAdapter(this, playlist = musicPlaylist.ref)
        binding.playlistRV.adapter = adapter
    }

    private fun initializedListeners() {
        binding.backBtnPLA.setOnClickListener { finish() }
        binding.addPlaylistBtn.setOnClickListener { customAlertDialog()}
    }

    private fun customAlertDialog() {
        val customDialog = LayoutInflater.from(this@PlaylistActivity)
            .inflate(R.layout.add_playlist_dialog, binding.root, false)
        val binder = AddPlaylistDialogBinding.bind(customDialog)

        val builder = MaterialAlertDialogBuilder(this)
        builder.setView(customDialog)
            .setTitle("Playlist Details")
            .setPositiveButton("ADD") { dialog, _ ->
                val playlistName = binder.playlistName.text
                val createBy = binder.yourName.text
                if (playlistName != null && createBy != null) {
                    if (playlistName.isNotEmpty() && createBy.isNotEmpty()) {
                        addPlaylist(playlistName.toString(), createBy.toString())
                    }
                }
                dialog.dismiss()
            }
            .show()
    }

    private fun addPlaylist(name: String, author: String) {
        var playlistExist = false
        for (i in musicPlaylist.ref) {
            if (name.equals(i.name)) {
                playlistExist = true
                break
            }
        }
        if (playlistExist) {
            Toast.makeText(this, "Playlist exist!!!", Toast.LENGTH_SHORT).show()
        } else {
            val newPlaylist = Playlist()
            val calendar = Calendar.getInstance().time
            val sdf = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH)

            newPlaylist.name = name
            newPlaylist.playlist = ArrayList()
            newPlaylist.createBy = author
            newPlaylist.createOn = sdf.format(calendar)
            musicPlaylist.ref.add(newPlaylist)
            adapter.refreshPlaylist()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onResume() {
        super.onResume()
        adapter.notifyDataSetChanged()
    }
}