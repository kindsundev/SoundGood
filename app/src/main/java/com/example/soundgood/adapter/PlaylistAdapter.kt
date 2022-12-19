package com.example.soundgood.adapter

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.soundgood.R
import com.example.soundgood.activity.PlaylistActivity
import com.example.soundgood.activity.PlaylistDetailActivity
import com.example.soundgood.databinding.PlaylistViewBinding
import com.example.soundgood.model.Playlist
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class PlaylistAdapter(
    private val context: Context,
    private var playlist: ArrayList<Playlist>
) : RecyclerView.Adapter<PlaylistAdapter.PlaylistViewHolder>() {

    class PlaylistViewHolder(binding: PlaylistViewBinding) : RecyclerView.ViewHolder(binding.root) {
        val image = binding.playlistImg
        val name = binding.playlistName
        val delete = binding.playlistDeleteBtn
        val root = binding.root
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistViewHolder {
        val view = PlaylistViewBinding.inflate(LayoutInflater.from(context), parent, false)
        val viewHolder = PlaylistViewHolder(view)

        viewHolder.root.setOnClickListener {
            val position = viewHolder.adapterPosition
            onCLickPlaylistSong(pos = position, ref = "PlaylistAdapter")
        }

        viewHolder.delete.setOnClickListener {
            val position = viewHolder.adapterPosition
            onClickDelete(pos = position)
        }

        return viewHolder
    }

    private fun onCLickPlaylistSong(pos: Int, ref: String) {
        val intent = Intent(context, PlaylistDetailActivity::class.java)
        intent.putExtra("index", pos)
        intent.putExtra("class", ref)
        ContextCompat.startActivity(context, intent, null)
    }

    private fun onClickDelete(pos : Int) {
        val builder = MaterialAlertDialogBuilder(context)
        builder.setTitle(playlist[pos].name)
            .setMessage("Do you want to delete playlist?")
            .setNegativeButton("No") {dialog, _ ->
                dialog.dismiss()
            }
            .setPositiveButton("Yes"){dialog, _ ->
                PlaylistActivity.musicPlaylist.ref.removeAt(pos)
                refreshPlaylist()
                dialog.dismiss()
            }
        val customDialog = builder.create()
        customDialog.show()
        customDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.RED)
        customDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.RED)
    }

    override fun onBindViewHolder(holder: PlaylistViewHolder, position: Int) {
        holder.name.text = playlist[position].name
        holder.name.isSelected = true
        if (PlaylistActivity.musicPlaylist.ref[position].playlist.size > 0) {
            try {
                Glide.with(context)
                    .load(PlaylistActivity.musicPlaylist.ref[position].playlist[0].artUri)
                    .apply(
                        RequestOptions().placeholder(R.drawable.sound_good_icon_slash_screen)
                            .centerCrop()
                    )
                    .into(holder.image)
            } catch (e: Exception) {
                return
            }
        }

    }

    override fun getItemCount(): Int = playlist.size

    fun refreshPlaylist() {
        playlist = ArrayList()
        playlist.addAll(PlaylistActivity.musicPlaylist.ref)
        notifyDataSetChanged()
    }

}