package limitless.android.minimalmusic.ui.home

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.widget.SeekBar
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import limitless.android.minimalmusic.R
import limitless.android.minimalmusic.databinding.ActivityMainBinding
import limitless.android.minimalmusic.db.model.Song

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: SongAdapter
    private val viewModel: HomeViewModel by viewModels()
    private var exoPlayer: ExoPlayer? = null
    private lateinit var handler: Handler
    private var adapterPosition: Int = 0
    private val updateProgressTask = object : Runnable {
        override fun run() {
            if (exoPlayer?.isPlaying == true) {
                binding.sb.progress = exoPlayer?.currentPosition?.toInt()!!
                handler.postDelayed(this, 100)
            }
        }
    }
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                getSongs()
            } else {
                // permission denied
            }
        }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handler = Handler(mainLooper)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        adapter = SongAdapter(onPlayClick = { songs: Song, adapterPosition: Int ->
            viewModel.playSong(songs)
            this.adapterPosition = adapterPosition
        })
        binding.ibtnNext.setOnClickListener {
            val song = adapter.getSong(adapterPosition + 1)
            if (song != null) {
                viewModel.nextButtonClicked(song)
                adapterPosition += 1
            }
        }
        binding.ibtnPrevious.setOnClickListener {
            val song = adapter.getSong(adapterPosition - 1)
            if (song != null){
                viewModel.previousButtonClicked(song)
                adapterPosition -= 1
            }
        }
        binding.ibtnPlay.setOnClickListener {
            viewModel.pauseButtonClicked()
        }
        binding.rv.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        binding.rv.adapter = adapter
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.search(s.toString().trim())
                if (s.toString().trim().isEmpty()) {
                    binding.etSearch.clearFocus()
                }
            }

            override fun afterTextChanged(s: Editable?) {
            }

        })
        lifecycleScope.launch {
            viewModel.songs.collectLatest {
                adapter.submitList(it)
            }
        }
        lifecycleScope.launch {
            viewModel.search.collectLatest {
                adapter.submitList(it)
            }
        }
        lifecycleScope.launch {
            viewModel.homeEventChannel.collectLatest { event ->
                when (event) {
                    HomeViewModel.UIState.PauseSong -> {
                        if (exoPlayer != null) {
                            if (exoPlayer?.isPlaying == true) {
                                exoPlayer?.pause()
                                binding.ibtnPlay.setImageResource(R.drawable.play_arrow_24px)
                            } else {
                                binding.ibtnPlay.setImageResource(R.drawable.pause_24px)
                                exoPlayer?.play()
                            }
                        }
                    }

                    is HomeViewModel.UIState.PlaySong -> {
                        playSound(event.song)
                    }

                    is HomeViewModel.UIState.NextSong -> {
                        viewModel.playSong(event.song)
                    }

                    is HomeViewModel.UIState.PreviousSound -> {
                        viewModel.playSong(event.song)
                    }
                }
            }
        }
        binding.sb.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser)
                    exoPlayer?.seekTo(progress.toLong())
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {

            }

        })
        checkAndRequestPermission()
    }

    private fun playSound(song: Song) {
        if (exoPlayer != null && exoPlayer?.isPlaying == true) {
            exoPlayer?.pause()
        }
        exoPlayer = ExoPlayer.Builder(this).build()
        exoPlayer?.playWhenReady = true
        exoPlayer?.setMediaItem(MediaItem.fromUri(song.uri))
        exoPlayer?.prepare()
        exoPlayer?.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                super.onPlaybackStateChanged(playbackState)
                when (playbackState) {
                    Player.STATE_READY -> {
//                        binding.pb.max = exoPlayer?.duration?.toInt()!!
                        binding.ibtnPlay.setImageResource(R.drawable.pause_24px)

                    }
                }
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                super.onIsPlayingChanged(isPlaying)
                if (isPlaying) {
                    binding.sb.max = exoPlayer?.duration?.toInt()!!
                    handler.postDelayed(updateProgressTask, 100)
                } else {
                    handler.removeCallbacks(updateProgressTask)
                }
            }
        })
    }

    fun getSongs() {
        viewModel.getSongs(contentResolver)
    }

    fun checkAndRequestPermission() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED -> {
                getSongs()
            }

            shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE) -> {
                0
            }

            else -> {
                requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }
    }

}