package com.example.soundgood.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.soundgood.MainActivity
import com.example.soundgood.R
import com.example.soundgood.activity.PlayerActivity
import com.example.soundgood.databinding.MusicViewBinding
import com.example.soundgood.model.Music
import com.example.soundgood.model.formatDuration

class MusicAdapter(
    private val context: Context,
    private var musicList: ArrayList<Music>
) : RecyclerView.Adapter<MusicAdapter.MusicViewHolder>() {

    class MusicViewHolder(binding: MusicViewBinding) : RecyclerView.ViewHolder(binding.root) {
        val title = binding.songNameMV
        val album = binding.songAlbumMV
        val image = binding.imageMV
        val duration = binding.songDuration
        val root = binding.root
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MusicViewHolder {
        val view = MusicViewBinding.inflate(LayoutInflater.from(context), parent, false)
        val viewHolder = MusicViewHolder(view)

        viewHolder.root.setOnClickListener {
            val position = viewHolder.adapterPosition
            when {
                MainActivity.search -> sendIntent(ref = "MusicAdapterSearch", pos = position)
                musicList[position].id == PlayerActivity.nowPlayingId ->
                    sendIntent(ref = "NowPlaying", pos = PlayerActivity.songPosition)
                else -> sendIntent(ref = "MusicAdapter", pos = position)
            }
        }

        return viewHolder
    }

    private fun sendIntent(ref: String, pos: Int) {
        val intent = Intent(context, PlayerActivity::class.java)
        intent.putExtra("index", pos)
        intent.putExtra("class", ref)
        ContextCompat.startActivity(context, intent, null)
    }

    @SuppressLint("CheckResult")
    override fun onBindViewHolder(holder: MusicViewHolder, position: Int) {
        holder.title.text = musicList[position].title
        holder.album.text = musicList[position].album
        holder.duration.text = formatDuration(musicList[position].duration)
        Glide.with(context)
            .load(musicList[position].artUri)
            .apply(RequestOptions().placeholder(R.drawable.sound_good_icon_slash_screen).centerCrop())
            .into(holder.image)
    }

    override fun getItemCount(): Int = musicList.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateMusicList(searchList: ArrayList<Music>) {
        musicList = ArrayList()
        musicList.addAll(searchList)
        notifyDataSetChanged()
    }
}