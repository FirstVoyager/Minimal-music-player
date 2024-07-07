package limitless.android.minimalmusic.db

import android.content.ContentResolver
import kotlinx.coroutines.flow.Flow
import limitless.android.minimalmusic.db.local.MusicRepository
import limitless.android.minimalmusic.db.model.Song
import javax.inject.Inject

class MainRepository @Inject constructor(private val musicRepository: MusicRepository)  {

    suspend fun getSongs(contentResolver: ContentResolver): Flow<MutableList<Song>> {
        return musicRepository.getSongs(contentResolver)
    }

}