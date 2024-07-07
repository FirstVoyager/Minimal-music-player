package limitless.android.minimalmusic.db.local

import android.content.ContentResolver
import kotlinx.coroutines.flow.Flow
import limitless.android.minimalmusic.db.model.Song

interface MusicRepository {

    suspend fun getSongs(contentResolver: ContentResolver) : Flow<MutableList<Song>>

    suspend fun getSong(contentResolver: ContentResolver) : Flow<Song>

}