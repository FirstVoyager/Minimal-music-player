package limitless.android.minimalmusic.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import limitless.android.minimalmusic.R
import limitless.android.minimalmusic.databinding.ItemSongBinding
import limitless.android.minimalmusic.db.model.Song
import limitless.android.minimalmusic.utils.Utils

class SongAdapter(
     val onPlayClick: (Song,Int) -> Unit
) : ListAdapter<Song, SongAdapter.SongViewHolder>(DiffCallback) {

    inner class SongViewHolder(private val binding: ItemSongBinding) : RecyclerView.ViewHolder(binding.root){

        init {

            binding.root.setOnClickListener {
                onPlayClick.invoke(getItem(adapterPosition), adapterPosition)
            }
        }

        fun songClicked(song: Song){

        }

        fun bind(song: Song){
            binding.tvTitle.text = song.title
            binding.tvArtist.text = song.artist
            binding.tvDuration.text = Utils.toMinutesAndSeconds(song.duration)
            if (song.art == null) {
                binding.iv.setImageResource(R.drawable.music)
                CoroutineScope(Dispatchers.Main).launch {
                    val icon = withContext(Dispatchers.IO) {
                        Utils.loadSongAlbumArt(song.path)
                    }
                    if (icon == null)
                        return@launch
                    binding.iv.setImageBitmap(icon)
                    song.art = icon
                }
            }else{
                binding.iv.setImageBitmap(song.art)
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        return SongViewHolder(ItemSongBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    fun getSong(i: Int): Song? {
        return try {
            getItem(i)
        } catch (e: Exception) {
            null
        }
    }

}

object DiffCallback : DiffUtil.ItemCallback<Song>() {
    override fun areItemsTheSame(oldItem: Song, newItem: Song): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Song, newItem: Song): Boolean {
        return oldItem == newItem
    }

}