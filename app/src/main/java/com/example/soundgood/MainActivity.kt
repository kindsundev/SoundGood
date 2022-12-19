package com.example.soundgood

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.soundgood.activity.FavoriteActivity
import com.example.soundgood.activity.PlayerActivity
import com.example.soundgood.activity.PlaylistActivity
import com.example.soundgood.activity.navigation.AboutActivity
import com.example.soundgood.activity.navigation.FeedbackActivity
import com.example.soundgood.activity.navigation.SettingActivity
import com.example.soundgood.adapter.MusicAdapter
import com.example.soundgood.databinding.ActivityMainBinding
import com.example.soundgood.model.Music
import com.example.soundgood.model.MusicPlaylist
import com.example.soundgood.model.exitApplication
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import java.io.File

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var musicAdapter: MusicAdapter

    companion object {
        const val WRITE_EXTERNAL_STORAGE_CODE = 13
        lateinit var musicListMA: ArrayList<Music>
        lateinit var musicListSearch: ArrayList<Music>
        var search: Boolean = false
        var themeIndex: Int = 0
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initializedLayout()
        if (requestPermissions()) {
            initializedMusicAdapter()
            retrievingFavoriteData()
        }
        initializedListeners()
    }

    private fun initializedLayout() {
        val themeEditor = getSharedPreferences("THEMES", MODE_PRIVATE)
        themeIndex = themeEditor.getInt("themeIndex", 0)
        search = false
        setTheme(R.style.coolPinkNav)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initializedNavDrawer()
    }

    private fun initializedNavDrawer() {
        toggle = ActionBarDrawerToggle(this, binding.root, R.string.open, R.string.close)
        binding.root.addDrawerListener(toggle)
        toggle.syncState()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun initializedMusicAdapter() {
        binding.musicRV.setHasFixedSize(true)
        binding.musicRV.setItemViewCacheSize(13)
        binding.musicRV.layoutManager = LinearLayoutManager(this@MainActivity)
        musicListMA = getAllAudio()
        musicAdapter = MusicAdapter(this@MainActivity, musicListMA)
        "Total song: ${musicAdapter.itemCount}".also { binding.totalSongTV.text = it }
        binding.musicRV.adapter = musicAdapter
    }

    private fun getAllAudio(): ArrayList<Music> {
        val musicList = ArrayList<Music>()
        val cursor = queryDataFromDevice()
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    renderDataFromDevice(cursor, musicList)
                } while (cursor.moveToNext())
                cursor.close()
            }
        }
        return musicList
    }

    @SuppressLint("Recycle")
    private fun queryDataFromDevice(): Cursor? {
        val selection = MediaStore.Audio.Media.IS_MUSIC + " != 0"
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATE_ADDED,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.ALBUM_ID
        )
        return contentResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, selection,
            null, MediaStore.Audio.Media.DATE_ADDED + " DESC", null)
    }

    @SuppressLint("Range")
    private fun renderDataFromDevice(cursor: Cursor, musicList: ArrayList<Music>) {
        val titleC = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE))
        val idC = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media._ID))
        val albumC = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM))
        val artistC = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST))
        val pathC = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA))
        val durationC = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION))
        val albumId = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)).toString()
        val uri = Uri.parse("content://media/external/audio/albumart")
        val artUriC = Uri.withAppendedPath(uri, albumId).toString()

        val music = Music(id = idC, title = titleC, album = albumC, artist = artistC,
            path = pathC, duration = durationC, artUri = artUriC)
        val file = File(music.path)
        if (file.exists()) {
            musicList.add(music)
        }
    }

    private fun initializedListeners() {
        binding.shuffleBtn.setOnClickListener { onCLickShuffle() }
        binding.favoriteBtn.setOnClickListener { onCLickFavorite() }
        binding.playlistBtn.setOnClickListener { onCLickPlaylist() }
        binding.navView.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.navFeedback -> startActivity(Intent(this@MainActivity, FeedbackActivity::class.java))
                R.id.navSettings -> startActivity(Intent(this@MainActivity, SettingActivity::class.java))
                R.id.navAbout -> startActivity(Intent(this@MainActivity, AboutActivity::class.java))
                R.id.navExit -> { onClickNavExit() }
            }
            true
        }
    }

    private fun onCLickShuffle() {
        val intent = Intent(this@MainActivity, PlayerActivity::class.java)
        intent.putExtra("index", 0)
        intent.putExtra("class", "MainActivity")
        startActivity(intent)
    }

    private fun onCLickFavorite() {
        startActivity(Intent(this@MainActivity, FavoriteActivity::class.java))
    }

    private fun onCLickPlaylist() {
        startActivity(Intent(this@MainActivity, PlaylistActivity::class.java))
    }

    private fun onClickNavExit() {
        val builder = MaterialAlertDialogBuilder(this)
        builder.setTitle("Exit")
            .setMessage("Do you want to close app?")
            .setNegativeButton("No") {dialog, _ -> dialog.dismiss()}
            .setPositiveButton("Yes"){_, _ -> exitApplication() }
        val customDialog = builder.create()
        customDialog.show()
        customDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.RED)
        customDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.RED)
    }

    private fun requestPermissions() : Boolean {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                WRITE_EXTERNAL_STORAGE_CODE)
            return false
        }
        return true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == WRITE_EXTERNAL_STORAGE_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show()
                initializedMusicAdapter()
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), WRITE_EXTERNAL_STORAGE_CODE
                )
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toggle.onOptionsItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.search_view_menu, menu)
        val searchView = menu?.findItem(R.id.searchView)?.actionView as SearchView
        onClickSearch(searchView)
        return super.onCreateOptionsMenu(menu)
    }

    private fun onClickSearch(searchView: SearchView) {
        searchView.setOnQueryTextListener(object: SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean =  true

            override fun onQueryTextChange(newText: String?): Boolean {
                musicListSearch = ArrayList()
                if (newText != null) {
                    val userInput = newText.lowercase()
                    for (song in musicListMA) {
                        if (song.title.lowercase().contains(userInput)) {
                            musicListSearch.add(song)
                        }
                    }
                    search = true
                    musicAdapter.updateMusicList(musicListSearch)
                }
                return true
            }
        })
    }

    override fun onResume() {
        super.onResume()
        storingFavoriteData()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (!PlayerActivity.isPlaying || PlayerActivity.musicService != null) {
            exitApplication()
        }
    }

    private fun retrievingFavoriteData() {
        val editor = getSharedPreferences("FAVORITES", MODE_PRIVATE)

        FavoriteActivity.favoriteSongs = ArrayList()
        val jsonStringFavorite = editor.getString("FavoriteSongs", null)
        val typeTokenFavorite = object : TypeToken<ArrayList<Music>>(){}.type
        if (jsonStringFavorite != null) {
            val dataFavorite : ArrayList<Music> = GsonBuilder().create().fromJson(jsonStringFavorite, typeTokenFavorite)
            FavoriteActivity.favoriteSongs.addAll(dataFavorite)
        }

        PlaylistActivity.musicPlaylist = MusicPlaylist()
        val jsonStringPlaylist = editor.getString("MusicPlaylist", null)
        if (jsonStringPlaylist != null) {
            val dataPlaylist : MusicPlaylist = GsonBuilder().create().fromJson(jsonStringPlaylist, MusicPlaylist::class.java)
            PlaylistActivity.musicPlaylist = dataPlaylist
        }
    }

    @SuppressLint("CommitPrefEdits")
    fun storingFavoriteData() {
        val editor = getSharedPreferences("FAVORITES", MODE_PRIVATE).edit()
        val jsonStringFavorite = GsonBuilder().create().toJson(FavoriteActivity.favoriteSongs)
        editor.putString("FavoriteSongs", jsonStringFavorite)
        val jsonStringPlaylist = GsonBuilder().create().toJson(PlaylistActivity.musicPlaylist)
        editor.putString("MusicPlaylist", jsonStringPlaylist)
        editor.apply()
    }
}