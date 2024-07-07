package limitless.android.minimalmusic.db.model

import android.graphics.Bitmap
import android.net.Uri
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Song(
    val id: Long = 0,
    val title: String,
    val artist: String,
    val album: String,
    val duration: Long,
    val path: String,
    var art: Bitmap?,
    val uri: Uri
) : Parcelable {

    constructor() : this(
        id = 0,
        title = "",
        artist = "",
        album = "",
        duration = 0,
        path = "",
        art = null,
        uri = Uri.EMPTY
    )

}
