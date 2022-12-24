package com.example.soundgood.activity

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.soundgood.MainActivity
import com.example.soundgood.adapter.MusicAdapter
import com.example.soundgood.databinding.ActivitySelectionBinding

class SelectionActivity : AppCompatActivity() {

    lateinit var binding: ActivitySelectionBinding
    lateinit var adapter: MusicAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initializedLayout()
        initializedListeners()
    }

    private fun initializedLayout() {
        setTheme(MainActivity.currentTheme[MainActivity.themeIndex])
        binding = ActivitySelectionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initSelectionAdapter()
    }

    private fun initSelectionAdapter() {
        Toast.makeText(this, "Select items to add to playlist. " +
                "If already exists, select again to delete", Toast.LENGTH_LONG).show()
        binding.selectionRV.setItemViewCacheSize(10)
        binding.selectionRV.setHasFixedSize(true)
        binding.selectionRV.layoutManager = LinearLayoutManager(this)
        adapter = MusicAdapter(this, MainActivity.musicListMA, selectionActivity = true)
        binding.selectionRV.adapter = adapter
    }

    private fun initializedListeners() {
        binding.backBtnSA.setOnClickListener { finish() }

        binding.searchViewSA.setOnQueryTextListener(object: SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean =  true

            override fun onQueryTextChange(newText: String?): Boolean {
                MainActivity.musicListSearch = ArrayList()
                if (newText != null) {
                    val userInput = newText.lowercase()
                    for (song in MainActivity.musicListMA) {
                        if (song.title.lowercase().contains(userInput)) {
                            MainActivity.musicListSearch.add(song)
                        }
                    }
                    MainActivity.search = true
                    adapter.updateMusicList(MainActivity.musicListSearch)
                }
                return true
            }
        })
    }
}