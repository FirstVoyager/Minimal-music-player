package limitless.android.minimalmusic.ui.home

import android.content.ContentResolver
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import limitless.android.minimalmusic.db.MainRepository
import limitless.android.minimalmusic.db.model.Song
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(private val mainRepository: MainRepository) : ViewModel(){

    private val _homeEventChannel = Channel<UIState>()
    val homeEventChannel = _homeEventChannel.receiveAsFlow()

    private val _songs = MutableStateFlow<MutableList<Song>>(mutableListOf())
    val songs = _songs.asStateFlow()

    private val _search = MutableStateFlow<List<Song>>(emptyList())
    val search = _search.asStateFlow()

    fun getSongs(contentResolver: ContentResolver){
        viewModelScope.launch {
            mainRepository.getSongs(contentResolver).collectLatest {
                _songs.emit(it)
            }
        }
    }

    fun search(query: String){
        val lowerCaseQuery = query.lowercase()
        viewModelScope.launch {
            songs.collectLatest {
                val list = it.filter {song->
                    song.title.lowercase().contains(lowerCaseQuery) ||
                    song.artist.lowercase().contains(lowerCaseQuery) ||
                    song.album.lowercase().contains(lowerCaseQuery)
                }
                _search.emit(list)
            }
        }
    }

    fun playSong(song: Song) {
        viewModelScope.launch {
            _homeEventChannel.send(UIState.PlaySong(song))
        }
    }

    fun nextButtonClicked(song: Song) {
        viewModelScope.launch {
            _homeEventChannel.send(UIState.NextSong(song))
        }
    }

    fun previousButtonClicked(song: Song){
        viewModelScope.launch {
            _homeEventChannel.send(UIState.PreviousSound(song))
        }
    }

    fun pauseButtonClicked() {
        viewModelScope.launch {
            _homeEventChannel.send(UIState.PauseSong)
        }
    }

    sealed class UIState {
        data class PlaySong(val song: Song) : UIState()
        data class NextSong(val song: Song) : UIState()
        data class PreviousSound(val song: Song) : UIState()
        data object PauseSong : UIState()
    }

}