package com.example.soundgood.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.soundgood.R
import com.example.soundgood.activity.PlayerActivity
import com.example.soundgood.databinding.FavoriteViewBinding
import com.example.soundgood.model.Music

class FavoriteAdapter(
    private val context: Context,
    private var musicList: ArrayList<Music>
) : RecyclerView.Adapter<FavoriteAdapter.FavoriteViewHolder>() {

    class FavoriteViewHolder(binding: FavoriteViewBinding) : RecyclerView.ViewHolder(binding.root) {
        val image = binding.songImgFV
        val name = binding.songNameFV
        val root = binding.root
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteViewHolder {
        val view = FavoriteViewBinding.inflate(LayoutInflater.from(context), parent, false)
        val viewHolder = FavoriteViewHolder(view)

        viewHolder.root.setOnClickListener {
            val position = viewHolder.adapterPosition
            onClickFavoriteSong(pos = position, ref = "FavoriteAdapter")
        }

        return viewHolder
    }

    private fun onClickFavoriteSong(pos: Int, ref: String) {
        val intent = Intent(context, PlayerActivity::class.java)
        intent.putExtra("index", pos)
        intent.putExtra("class", ref)
        ContextCompat.startActivity(context, intent, null)
    }

    override fun onBindViewHolder(holder: FavoriteViewHolder, position: Int) {
        holder.name.text = musicList[position].title
        try {
            Glide.with(context)
                .load(musicList[position].artUri)
                .apply(
                    RequestOptions().placeholder(R.drawable.sound_good_icon_slash_screen)
                        .centerCrop()
                )
                .into(holder.image)
        } catch (e: Exception) {
            return
        }
    }

    override fun getItemCount(): Int = musicList.size
}