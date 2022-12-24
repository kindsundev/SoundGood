package com.example.soundgood.activity

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.Color
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.audiofx.AudioEffect
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.soundgood.MainActivity
import com.example.soundgood.R
import com.example.soundgood.databinding.ActivityPlayerBinding
import com.example.soundgood.model.*
import com.example.soundgood.service.MusicService
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class PlayerActivity : AppCompatActivity(), ServiceConnection, MediaPlayer.OnCompletionListener {

    private val TAG = "PlayerActivity"
    private var mEqualizerActivity: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult())
    {
        return@registerForActivityResult
    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var binding: ActivityPlayerBinding
        lateinit var musicListPA: ArrayList<Music>
        var songPosition: Int = 0
        var isPlaying: Boolean = false
        var musicService: MusicService? = null
        var repeat: Boolean = false
        var min15: Boolean = false
        var min30: Boolean = false
        var min60: Boolean = false
        var nowPlayingId: String = ""
        var isFavorite: Boolean = false
        var fIndex: Int = -1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(MainActivity.currentTheme[MainActivity.themeIndex])
        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initializedLayout()
        initializedListener()
    }

    private fun initializedLayout() {
        songPosition = intent.getIntExtra("index", 0)
        when (intent.getStringExtra("class")) {
            "MusicAdapter" -> { initMusicAdapter() }
            "FavoriteAdapter" -> { initFavoriteAdapter() }
            "MainActivity" -> { initMainActivity() }
            "MusicAdapterSearch" -> { initMusicAdapterSearch() }
            "NowPlaying" -> { initNowPlaying() }
            "FavoriteShuffle" -> { initFavoriteShuffle() }
            "PlaylistDetailAdapter" -> { initPlaylistDetailAdapter() }
            "PlaylistDetailShuffle" -> { initPlaylistDetailShuffle() }
        }
    }

    private fun initMusicAdapter() {
        initializedService()
        musicListPA = ArrayList()
        musicListPA.addAll(MainActivity.musicListMA)
        setLayout()
    }

    private fun initFavoriteAdapter() {
        initializedService()
        musicListPA = ArrayList()
        musicListPA.addAll(FavoriteActivity.favoriteSongs)
        setLayout()
    }

    private fun initMainActivity() {
        initializedService()
        musicListPA = ArrayList()
        musicListPA.addAll(MainActivity.musicListMA)
        musicListPA.shuffle()
        setLayout()
    }

    private fun initMusicAdapterSearch() {
        initializedService()
        musicListPA = ArrayList()
        musicListPA.addAll(MainActivity.musicListSearch)
        setLayout()
    }

    private fun initNowPlaying() {
        setLayout()
        binding.tvSeekBarStart.text = formatDuration(musicService!!.mediaPlayer!!.currentPosition.toLong())
        binding.tvSeekBarEnd.text = formatDuration(musicService!!.mediaPlayer!!.duration.toLong())
        binding.seekBarPA.progress = musicService!!.mediaPlayer!!.currentPosition
        binding.seekBarPA.max = musicService!!.mediaPlayer!!.duration
        if (isPlaying) binding.playPauseBtnPA.setIconResource(R.drawable.pause_icon)
        else binding.playPauseBtnPA.setIconResource(R.drawable.play_icon)
    }

    private fun initFavoriteShuffle() {
        initializedService()
        musicListPA = ArrayList()
        musicListPA.addAll(FavoriteActivity.favoriteSongs)
        musicListPA.shuffle()
        setLayout()
    }

    private fun initPlaylistDetailAdapter() {
        initializedService()
        musicListPA = ArrayList()
        musicListPA.addAll(PlaylistActivity.musicPlaylist.ref[PlaylistDetailActivity.currentPlaylistPosition].playlist)
        setLayout()
    }

    private fun initPlaylistDetailShuffle() {
        initializedService()
        musicListPA = ArrayList()
        musicListPA.addAll(PlaylistActivity.musicPlaylist.ref[PlaylistDetailActivity.currentPlaylistPosition].playlist)
        musicListPA.shuffle()
        setLayout()
    }

    private fun initializedService() {
        val intent = Intent(this@PlayerActivity, MusicService::class.java)
        bindService(intent, this, BIND_AUTO_CREATE)
        startService(intent)
    }

    private fun setLayout() {
        try {
            fIndex = favoriteChecker(musicListPA[songPosition].id)
            Glide.with(this)
                .load(musicListPA[songPosition].artUri)
                .apply(RequestOptions().placeholder(R.drawable.sound_good_icon_slash_screen).centerCrop())
                .into(binding.songImgPA)
        } catch (e: Exception) {
            return
        }
        binding.songNamePA.text = musicListPA[songPosition].title
        if (repeat) {
            binding.repeatBtnPA.setColorFilter(ContextCompat.getColor(this, R.color.purple_500))
        }
        if (min15 || min30 || min60) {
            binding.timerBtnPA.setColorFilter(ContextCompat.getColor(this, R.color.purple_500))
        }
        if (isFavorite) {
            binding.favoriteBtnPA.setImageResource(R.drawable.favorite_icon)
        } else {
            binding.favoriteBtnPA.setImageResource(R.drawable.favorite_empty_icon)
        }
    }

    private fun initializedListener() {
        binding.backBtnPA.setOnClickListener { finish() }

        binding.playPauseBtnPA.setOnClickListener {
            if (isPlaying) pauseMusic()
            else playMusic()
        }
        binding.previousBtnPA.setOnClickListener { prevNextSong(increment = false) }
        binding.nextBtnPA.setOnClickListener { prevNextSong(increment = true) }

        binding.seekBarPA.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) musicService!!.mediaPlayer!!.seekTo(progress)
            }

            override fun onStartTrackingTouch(p0: SeekBar?) = Unit
            override fun onStopTrackingTouch(p0: SeekBar?) = Unit
        })

        binding.repeatBtnPA.setOnClickListener { onClickRepeat() }
        binding.equalizerBtnPA.setOnClickListener { onClickEqualizer() }
        binding.timerBtnPA.setOnClickListener { onClickTimer() }
        binding.shareBtnPA.setOnClickListener { onClickShare() }
        binding.favoriteBtnPA.setOnClickListener { onClickFavorite() }
    }

    private fun pauseMusic() {
        binding.playPauseBtnPA.setIconResource(R.drawable.play_icon)
        musicService!!.showNotification(R.drawable.play_icon)
        isPlaying = false
        musicService!!.mediaPlayer!!.pause()
    }

    private fun playMusic() {
        binding.playPauseBtnPA.setIconResource(R.drawable.pause_icon)
        musicService!!.showNotification(R.drawable.pause_icon)
        isPlaying = true
        musicService!!.mediaPlayer!!.start()
    }

    private fun prevNextSong(increment: Boolean) {
        if (increment) {
            setSongPosition(increment = true)
            if (!repeat) ++songPosition
            setLayout()
            createMediaPlayer()
        } else {
            setSongPosition(increment = false)
            if (!repeat) --songPosition
            setLayout()
            createMediaPlayer()
        }
    }

    private fun onClickRepeat() {
        if (!repeat) {
            repeat = true
            binding.repeatBtnPA.setColorFilter(ContextCompat.getColor(this, R.color.purple_500))
        } else {
            repeat = false
            binding.repeatBtnPA.setColorFilter(ContextCompat.getColor(this, R.color.cool_pink))
        }
    }

    private fun onClickEqualizer() {
        try {
            val eqIntent = Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL)
            eqIntent.putExtra(AudioEffect.EXTRA_AUDIO_SESSION, musicService!!.mediaPlayer!!.audioSessionId)
            eqIntent.putExtra(AudioEffect.EXTRA_PACKAGE_NAME, baseContext.packageName)
            eqIntent.putExtra(AudioEffect.EXTRA_CONTENT_TYPE, AudioEffect.CONTENT_TYPE_MUSIC)
            mEqualizerActivity.launch(eqIntent)
        } catch (e: Exception) {
            Toast.makeText(this, "Equalizer Feature not Supported!!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun onClickTimer() {
        val timer = min15 || min30 || min60
        if (!timer) showBottomDialog()
        else showAlertDialog()
    }

    private fun showBottomDialog() {
        val dialog = BottomSheetDialog(this)
        dialog.setContentView(R.layout.bottom_sheet_dialog)
        dialog.show()
        dialog.findViewById<LinearLayout>(R.id.min_15)?.setOnClickListener {
            scheduleMusicOff(dialog, "15 minutes", R.id.min_15)
        }
        dialog.findViewById<LinearLayout>(R.id.min_30)?.setOnClickListener {
            scheduleMusicOff(dialog, "30 minutes", R.id.min_30)
        }
        dialog.findViewById<LinearLayout>(R.id.min_60)?.setOnClickListener {
            scheduleMusicOff(dialog, "60 minutes", R.id.min_60)
        }
    }

    private fun scheduleMusicOff(dialog: BottomSheetDialog, time: String, id: Int) {
        Toast.makeText(baseContext, "Music will stop after $time!!", Toast.LENGTH_SHORT).show()
        binding.timerBtnPA.setColorFilter(ContextCompat.getColor(this, R.color.purple_500))
        when (id) {
            R.id.min_15 -> {
                min15 = true
                Thread {
                    Thread.sleep((15 * 60000).toLong())
                    if (min15) exitApplication()
                }.start()
            }
            R.id.min_30 -> {
                min30 = true
                Thread {
                    Thread.sleep((30 * 60000).toLong())
                    if (min30) exitApplication()
                }.start()
            }
            R.id.min_60 -> {
                min60 = true
                Thread {
                    Thread.sleep((60 * 60000).toLong())
                    if (min60) exitApplication()
                }.start()
            }
        }
        dialog.dismiss()
    }

    private fun showAlertDialog() {
        val builder = MaterialAlertDialogBuilder(this)
        builder.setTitle("Stop Timer")
            .setMessage("Do you want to stop timer?")
            .setNegativeButton("No") {dialog, _ -> dialog.dismiss()}
            .setPositiveButton("Yes"){_, _ ->
                min15 = false
                min30 = false
                min60 = false
                binding.timerBtnPA.setColorFilter(ContextCompat.getColor(this, R.color.cool_pink))
            }
        val customDialog = builder.create()
        customDialog.show()
        customDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.RED)
        customDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.RED)
    }

    private fun onClickShare() {
        val shareIntent = Intent()
        shareIntent.action = Intent.ACTION_SEND
        shareIntent.type = "audio/*"
        shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(musicListPA[songPosition].path))
        startActivity(Intent.createChooser(shareIntent, "Sharing music file!!!"))
    }

    private fun onClickFavorite() {
        if (isFavorite) {
            isFavorite = false
            binding.favoriteBtnPA.setImageResource(R.drawable.favorite_empty_icon)
            FavoriteActivity.favoriteSongs.removeAt(fIndex)
            fIndex--
        } else {
            fIndex++
            isFavorite = true
            binding.favoriteBtnPA.setImageResource(R.drawable.favorite_icon)
            FavoriteActivity.favoriteSongs.add(musicListPA[songPosition])
        }
    }

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        val binder = service as MusicService.MyBinder
        musicService = binder.currentService()
        createMediaPlayer()
        musicService!!.seekBarSetUp()
        musicService!!.audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        musicService!!.audioManager.requestAudioFocus(musicService, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN)
    }

    private fun createMediaPlayer() {
        if(musicService == null) musicService = MusicService()
        try {
            if (musicService!!.mediaPlayer == null) musicService!!.mediaPlayer = MediaPlayer()
            musicService!!.mediaPlayer!!.reset()
            musicService!!.mediaPlayer!!.setDataSource(musicListPA[songPosition].path)
            musicService!!.mediaPlayer!!.prepare()
            musicService!!.mediaPlayer!!.start()
            isPlaying = true
            binding.playPauseBtnPA.setIconResource(R.drawable.pause_icon)
            musicService!!.showNotification(R.drawable.pause_icon)
            binding.tvSeekBarStart.text = formatDuration(musicService!!.mediaPlayer!!.currentPosition.toLong())
            binding.tvSeekBarEnd.text = formatDuration(musicService!!.mediaPlayer!!.duration.toLong())
            binding.seekBarPA.progress = 0
            binding.seekBarPA.max = musicService!!.mediaPlayer!!.duration
            musicService!!.mediaPlayer!!.setOnCompletionListener(this)
            nowPlayingId = musicListPA[songPosition].id
        } catch (e: Exception) {
            return
        }
    }

    override fun onServiceDisconnected(name: ComponentName?) {
        musicService = null
    }

    override fun onCompletion(mp: MediaPlayer?) {
        setSongPosition(increment = true)
        createMediaPlayer()
        try {
            setLayout()
        } catch (e: Exception) {
            return
        }
    }
}