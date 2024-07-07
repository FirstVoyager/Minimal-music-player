package limitless.android.minimalmusic.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever

object Utils {

    fun toMinutesAndSeconds(v: Long): String {
        val totalSeconds = v / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return "%02d:%02d".format(minutes, seconds)
    }

    fun loadSongAlbumArt(path: String?) : Bitmap? {
        try {
            if (path == null)
                return null
            val mmr = MediaMetadataRetriever()
            mmr.setDataSource(path)
            val data = mmr.embeddedPicture
            return if (data != null) BitmapFactory.decodeByteArray(data, 0, data.size) else null
        }catch (e: Exception){
            return null
        }
    }

}