package com.example.soundgood.service

import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.*
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.core.app.NotificationCompat
import com.example.soundgood.R
import com.example.soundgood.activity.PlayerActivity
import com.example.soundgood.activity.PlayerActivity.Companion.isPlaying
import com.example.soundgood.activity.fragment.NowPlaying
import com.example.soundgood.common.MyApplication
import com.example.soundgood.model.formatDuration
import com.example.soundgood.model.getImgArt

class MusicService : Service(), AudioManager.OnAudioFocusChangeListener {

    private val myBinder = MyBinder()
    var mediaPlayer: MediaPlayer? = null
    private lateinit var mediaSession : MediaSessionCompat
    private lateinit var runnable : Runnable
    lateinit var audioManager: AudioManager

    override fun onBind(intent: Intent?): IBinder {
        mediaSession = MediaSessionCompat(baseContext, "My Music")
        return myBinder
    }

    inner class MyBinder : Binder() {
        fun currentService(): MusicService {
            return this@MusicService
        }
    }

    fun showNotification(playPauseBtn: Int, playBackSpeed: Float) {
        val intent = Intent(baseContext, PlayerActivity::class.java)
        intent.putExtra("index", PlayerActivity.songPosition)
        intent.putExtra("class", "NowPlaying")
        val contextIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val imgArt = getImgArt(PlayerActivity.musicListPA[PlayerActivity.songPosition].path)
        val image = if (imgArt != null) {
            BitmapFactory.decodeByteArray(imgArt, 0, imgArt.size)
        } else {
            BitmapFactory.decodeResource(resources, R.drawable.splash_screen)
        }

        val notification = NotificationCompat.Builder(baseContext, MyApplication.CHANNEL_ID)
            .setContentIntent(contextIntent)
            .setContentTitle(PlayerActivity.musicListPA[PlayerActivity.songPosition].title)
            .setContentText(PlayerActivity.musicListPA[PlayerActivity.songPosition].artist)
            .setSmallIcon(R.drawable.music_icon)
            .setLargeIcon(image)
            .setStyle(androidx.media.app.NotificationCompat.MediaStyle().setMediaSession(mediaSession.sessionToken))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOnlyAlertOnce(true)
            .addAction(MyApplication.PREVIOUS.initNotificationAction(0))
            .addAction(MyApplication.PLAY.initNotificationAction(playPauseBtn))
            .addAction(MyApplication.NEXT.initNotificationAction(0))
            .addAction(MyApplication.EXIT.initNotificationAction(0))
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            mediaSession.setMetadata(MediaMetadataCompat.Builder()
                .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, mediaPlayer!!.duration.toLong())
                .build())
            mediaSession.setPlaybackState(PlaybackStateCompat.Builder()
                .setState(PlaybackStateCompat.STATE_PLAYING, mediaPlayer!!.currentPosition.toLong(), playBackSpeed)
                .setActions(PlaybackStateCompat.ACTION_SEEK_TO)
                .build())
        }

        startForeground(MyApplication.NOTIFICATION_ID, notification)
    }

    private fun String.initNotificationAction(playPauseBtn: Int): NotificationCompat.Action? {
        var notificationAction : NotificationCompat.Action? = null
        when (this) {
            MyApplication.PREVIOUS -> {
                val prevIntent = Intent(baseContext, NotificationReceiver::class.java).setAction(MyApplication.PREVIOUS)
                val prevPendingIntent = PendingIntent.getBroadcast(baseContext, 0, prevIntent, PendingIntent.FLAG_IMMUTABLE)
                notificationAction = NotificationCompat.Action.Builder(R.drawable.previous_icon, "Previous", prevPendingIntent).build()
            }
            MyApplication.PLAY -> {
                val playIntent = Intent(baseContext, NotificationReceiver::class.java).setAction(MyApplication.PLAY)
                val playPendingIntent = PendingIntent.getBroadcast(baseContext, 0, playIntent, PendingIntent.FLAG_IMMUTABLE)
                notificationAction = NotificationCompat.Action.Builder(playPauseBtn, "Play", playPendingIntent).build()
            }
            MyApplication.NEXT -> {
                val nextIntent = Intent(baseContext, NotificationReceiver::class.java).setAction(MyApplication.NEXT)
                val nextPendingIntent = PendingIntent.getBroadcast(baseContext, 0, nextIntent, PendingIntent.FLAG_IMMUTABLE)
                notificationAction = NotificationCompat.Action.Builder(R.drawable.next_icon, "Next", nextPendingIntent).build()
            }
            MyApplication.EXIT -> {
                val exitIntent = Intent(baseContext, NotificationReceiver::class.java).setAction(MyApplication.EXIT)
                val exitPendingIntent = PendingIntent.getBroadcast(baseContext, 0, exitIntent, PendingIntent.FLAG_IMMUTABLE)
                notificationAction = NotificationCompat.Action.Builder(R.drawable.exit_icon, "Exit", exitPendingIntent).build()
            }
        }
        return notificationAction
    }

    fun createMediaPlayer() {
        if(PlayerActivity.musicService == null) PlayerActivity.musicService = MusicService()
        try {
            if (PlayerActivity.musicService!!.mediaPlayer == null) PlayerActivity.musicService!!.mediaPlayer = MediaPlayer()
            PlayerActivity.musicService!!.mediaPlayer!!.reset()
            PlayerActivity.musicService!!.mediaPlayer!!.setDataSource(PlayerActivity.musicListPA[PlayerActivity.songPosition].path)
            PlayerActivity.musicService!!.mediaPlayer!!.prepare()
            PlayerActivity.binding.playPauseBtnPA.setIconResource(R.drawable.pause_icon)
            PlayerActivity.musicService!!.showNotification(R.drawable.pause_icon, 0F)
            PlayerActivity.binding.tvSeekBarStart.text = formatDuration(mediaPlayer!!.currentPosition.toLong())
            PlayerActivity.binding.tvSeekBarEnd.text = formatDuration(mediaPlayer!!.duration.toLong())
            PlayerActivity.binding.seekBarPA.progress = 0
            PlayerActivity.binding.seekBarPA.max = mediaPlayer!!.duration
            PlayerActivity.nowPlayingId = PlayerActivity.musicListPA[PlayerActivity.songPosition].id
        } catch (e: Exception) {
            return
        }
    }

    fun seekBarSetUp(){
        runnable = Runnable {
            PlayerActivity.binding.tvSeekBarStart.text = formatDuration(mediaPlayer!!.currentPosition.toLong())
            PlayerActivity.binding.seekBarPA.progress = mediaPlayer!!.currentPosition
            Handler(Looper.getMainLooper()).postDelayed(runnable, 200)
        }
        Handler(Looper.getMainLooper()).postDelayed(runnable, 0)
    }

    // pause - resume when incoming call
    override fun onAudioFocusChange(focusChange: Int) {
        if (focusChange <= 0) {
            PlayerActivity.binding.playPauseBtnPA.setIconResource(R.drawable.play_icon)
            NowPlaying.binding.playPauseBtnNP.setIconResource(R.drawable.play_icon)
            showNotification(R.drawable.play_icon, 0F)
            isPlaying = false
            mediaPlayer!!.pause()
        } else {
            PlayerActivity.binding.playPauseBtnPA.setIconResource(R.drawable.pause_icon)
            NowPlaying.binding.playPauseBtnNP.setIconResource(R.drawable.pause_icon)
            showNotification(R.drawable.pause_icon, 1F)
            isPlaying = true
            mediaPlayer!!.start()
        }
    }

}