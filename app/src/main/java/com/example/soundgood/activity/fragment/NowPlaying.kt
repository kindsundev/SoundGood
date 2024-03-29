package com.example.soundgood.activity.fragment

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.soundgood.MainActivity
import com.example.soundgood.R
import com.example.soundgood.activity.PlayerActivity
import com.example.soundgood.databinding.FragmentNowPlayingBinding
import com.example.soundgood.model.setSongPosition

class NowPlaying : Fragment() {

    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var binding: FragmentNowPlayingBinding
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        requireContext().theme.applyStyle(MainActivity.currentTheme[MainActivity.themeIndex], true)
        val view = inflater.inflate(R.layout.fragment_now_playing, container, false)
        binding = FragmentNowPlayingBinding.bind(view)
        binding.root.visibility = View.INVISIBLE
        initializedListener()
        return view
    }

    private fun initializedListener() {
        binding.playPauseBtnNP.setOnClickListener {
            if (PlayerActivity.isPlaying) pauseMusic() else playMusic()
        }
        binding.nextBtnNP.setOnClickListener { onClickNext() }
        binding.root.setOnClickListener { onClickNowPlayingBottom() }
    }

    private fun onClickNext() {
        setSongPosition(increment = true)
        PlayerActivity.musicService!!.createMediaPlayer()

        Glide.with(this)
            .load(PlayerActivity.musicListPA[PlayerActivity.songPosition].artUri)
            .apply(RequestOptions().placeholder(R.drawable.sound_good_icon_slash_screen).centerCrop())
            .into(binding.songImgNP)
        binding.songNameNP.text = PlayerActivity.musicListPA[PlayerActivity.songPosition].title

        PlayerActivity.musicService!!.showNotification(R.drawable.pause_icon, 1F)
        playMusic()
    }

    private fun onClickNowPlayingBottom() {
        val intent = Intent(requireContext(), PlayerActivity::class.java)
        intent.putExtra("index", PlayerActivity.songPosition)
        intent.putExtra("class", "NowPlaying")
        ContextCompat.startActivity(requireContext(), intent, null)
    }

    override fun onResume() {
        super.onResume()
        if (PlayerActivity.musicService != null) {
            binding.root.visibility = View.VISIBLE
            binding.songNameNP.isSelected = true
            try {
                Glide.with(this)
                    .load(PlayerActivity.musicListPA[PlayerActivity.songPosition].artUri)
                    .apply(RequestOptions().placeholder(R.drawable.sound_good_icon_slash_screen).centerCrop())
                    .into(binding.songImgNP)
                binding.songNameNP.text = PlayerActivity.musicListPA[PlayerActivity.songPosition].title
                if (PlayerActivity.isPlaying) binding.playPauseBtnNP.setIconResource(R.drawable.pause_icon)
                else binding.playPauseBtnNP.setIconResource(R.drawable.play_icon)
            } catch (e: Exception) {
                return
            }
        }
    }

    private fun playMusic() {
        PlayerActivity.musicService!!.mediaPlayer!!.start()
        binding.playPauseBtnNP.setIconResource(R.drawable.pause_icon)
        PlayerActivity.musicService!!.showNotification(R.drawable.pause_icon, 1F)
        PlayerActivity.binding.playPauseBtnPA.setIconResource(R.drawable.pause_icon)
        PlayerActivity.isPlaying = true
    }

    private fun pauseMusic() {
        PlayerActivity.musicService!!.mediaPlayer!!.pause()
        binding.playPauseBtnNP.setIconResource(R.drawable.play_icon)
        PlayerActivity.musicService!!.showNotification(R.drawable.play_icon, 0F)
        PlayerActivity.binding.playPauseBtnPA.setIconResource(R.drawable.play_icon)
        PlayerActivity.isPlaying = false
    }

}