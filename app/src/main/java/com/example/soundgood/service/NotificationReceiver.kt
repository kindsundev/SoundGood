package com.example.soundgood.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.soundgood.R
import com.example.soundgood.activity.PlayerActivity
import com.example.soundgood.activity.fragment.NowPlaying
import com.example.soundgood.common.MyApplication
import com.example.soundgood.model.exitApplication
import com.example.soundgood.model.favoriteChecker
import com.example.soundgood.model.setSongPosition

class NotificationReceiver: BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        when(intent?.action) {
            MyApplication.PREVIOUS -> prevNextSong(increment = false, context = context!!)
            MyApplication.PLAY -> if (PlayerActivity.isPlaying) pauseMusic() else playMusic()
            MyApplication.NEXT -> prevNextSong(increment = true, context = context!!)
            MyApplication.EXIT -> exitApplication()
        }
    }

    private fun playMusic() {
        PlayerActivity.isPlaying = true
        PlayerActivity.musicService!!.mediaPlayer!!.start()
        PlayerActivity.musicService!!.showNotification(R.drawable.pause_icon)
        PlayerActivity.binding.playPauseBtnPA.setIconResource(R.drawable.pause_icon)
        NowPlaying.binding.playPauseBtnNP.setIconResource(R.drawable.pause_icon)
    }

    private fun pauseMusic() {
        PlayerActivity.isPlaying = false
        PlayerActivity.musicService!!.mediaPlayer!!.pause()
        PlayerActivity.musicService!!.showNotification(R.drawable.play_icon)
        PlayerActivity.binding.playPauseBtnPA.setIconResource(R.drawable.play_icon)
        NowPlaying.binding.playPauseBtnNP.setIconResource(R.drawable.play_icon)
    }

    private fun prevNextSong(increment: Boolean, context: Context) {
        setSongPosition(increment = increment)
        PlayerActivity.musicService!!.createMediaPlayer()

        Glide.with(context)
            .load(PlayerActivity.musicListPA[PlayerActivity.songPosition].artUri)
            .apply(RequestOptions().placeholder(R.drawable.sound_good_icon_slash_screen).centerCrop())
            .into(PlayerActivity.binding.songImgPA)
        PlayerActivity.binding.songNamePA.text = PlayerActivity.musicListPA[PlayerActivity.songPosition].title

        Glide.with(context)
            .load(PlayerActivity.musicListPA[PlayerActivity.songPosition].artUri)
            .apply(RequestOptions().placeholder(R.drawable.sound_good_icon_slash_screen).centerCrop())
            .into(NowPlaying.binding.songImgNP)
        NowPlaying.binding.songNameNP.text = PlayerActivity.musicListPA[PlayerActivity.songPosition].title

        playMusic()
        PlayerActivity.fIndex = favoriteChecker(PlayerActivity.musicListPA[PlayerActivity.songPosition].id)
        if (PlayerActivity.isFavorite) PlayerActivity.binding.favoriteBtnPA.setImageResource(R.drawable.favorite_icon)
        else PlayerActivity.binding.favoriteBtnPA.setImageResource(R.drawable.favorite_empty_icon)
    }
}