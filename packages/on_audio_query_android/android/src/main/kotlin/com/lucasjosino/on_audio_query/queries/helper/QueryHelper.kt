package com.lucasjosino.on_audio_query.queries.helper

import android.content.ContentResolver
import android.content.ContentUris
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import java.io.File

class QueryHelper {
    //This method will load some extra information about audio/song
    fun loadSongExtraInfo(
        uri: Uri,
        songData: MutableMap<String, Any?>
    ): MutableMap<String, Any?> {
        val file = File(songData["_data"].toString())

        //Getting displayName without [Extension].
        songData["_display_name_wo_ext"] = file.nameWithoutExtension
        //Adding only the extension
        songData["file_extension"] = file.extension

        //A different type of "data"
        val tempUri = ContentUris.withAppendedId(uri, songData["_id"].toString().toLong())
        songData["_uri"] = tempUri.toString()

        return songData
    }

    //This method will separate [String] from [Int]
    fun loadSongItem(itemProperty: String, cursor: Cursor): Any? {
        val columnIndex = cursor.getColumnIndex(itemProperty)
        if (columnIndex == -1 && itemProperty != "is_alarm" && itemProperty != "is_audiobook" && itemProperty != "is_music" && itemProperty != "is_notification" && itemProperty != "is_podcast" && itemProperty != "is_ringtone") {
            // For boolean types, they are handled differently below, allow columnIndex to be -1 if string is null.
            // For other types, if column doesn't exist, return a sensible default or null.
            return if (itemProperty == "_id" || itemProperty == "album_id" || itemProperty == "artist_id" || itemProperty == "_size" || itemProperty == "bookmark" || itemProperty == "date_added" || itemProperty == "date_modified" || itemProperty == "duration" || itemProperty == "track") 0 else null
        }

        return when (itemProperty) {
            // Int or Long
            "_id",
            "album_id",
            "artist_id" -> {
                if (Build.VERSION.SDK_INT >= 30) {
                    if (columnIndex != -1) cursor.getLong(columnIndex) else 0L
                } else {
                    if (columnIndex != -1) cursor.getInt(columnIndex) else 0
                }
            }
            "_size",
            "bookmark",
            "date_added",
            "date_modified",
            "duration",
            "track" -> if (columnIndex != -1) cursor.getInt(columnIndex) else 0
            // Boolean
            "is_alarm",
            "is_audiobook",
            "is_music",
            "is_notification",
            "is_podcast",
            "is_ringtone" -> {
                // getString can return null if column doesn't exist or value is null
                val value = if (columnIndex != -1) cursor.getString(columnIndex) else null
                value == "1" // More robust boolean check, assuming "1" is true, anything else (null, "0") is false.
            }
            // String
            else -> if (columnIndex != -1) cursor.getString(columnIndex) else null
        }
    }

    //This method will separate [String] from [Int]
    fun loadAlbumItem(itemProperty: String, cursor: Cursor): Any? {
        val columnIndex = cursor.getColumnIndex(itemProperty)
        if (columnIndex == -1) {
            return if (itemProperty == "_id" || itemProperty == "artist_id" || itemProperty == "numsongs") 0 else null
        }

        return when (itemProperty) {
            "_id",
            "artist_id" -> {
                if (Build.VERSION.SDK_INT >= 30) {
                    cursor.getLong(columnIndex)
                } else {
                    cursor.getInt(columnIndex)
                }
            }
            "numsongs" -> cursor.getInt(columnIndex)
            else -> cursor.getString(columnIndex)
        }
    }

    //This method will separate [String] from [Int]
    fun loadPlaylistItem(itemProperty: String, cursor: Cursor): Any? {
        val columnIndex = cursor.getColumnIndex(itemProperty)
        if (columnIndex == -1) {
            return if (itemProperty == "_id" || itemProperty == "date_added" || itemProperty == "date_modified") 0L else null
        }

        return when (itemProperty) {
            "_id",
            "date_added",
            "date_modified" -> cursor.getLong(columnIndex)
            else -> cursor.getString(columnIndex)
        }
    }

    //This method will separate [String] from [Int]
    fun loadArtistItem(itemProperty: String, cursor: Cursor): Any? {
        val columnIndex = cursor.getColumnIndex(itemProperty)
        if (columnIndex == -1) {
            return if (itemProperty == "_id" || itemProperty == "number_of_albums" || itemProperty == "number_of_tracks") 0 else null
        }

        return when (itemProperty) {
            "_id" -> {
                if (Build.VERSION.SDK_INT >= 30) {
                    cursor.getLong(columnIndex)
                } else {
                    cursor.getInt(columnIndex)
                }
            }
            "number_of_albums",
            "number_of_tracks" -> cursor.getInt(columnIndex)
            else -> cursor.getString(columnIndex)
        }
    }

    //This method will separate [String] from [Int]
    fun loadGenreItem(itemProperty: String, cursor: Cursor): Any? {
        val columnIndex = cursor.getColumnIndex(itemProperty)
        if (columnIndex == -1) {
            return if (itemProperty == "_id") 0 else null
        }
        return when (itemProperty) {
            "_id" -> {
                if (Build.VERSION.SDK_INT >= 30) {
                    cursor.getLong(columnIndex)
                } else {
                    cursor.getInt(columnIndex)
                }
            }
            else -> cursor.getString(columnIndex)
        }
    }

    fun getMediaCount(type: Int, arg: String, resolver: ContentResolver): Int {
        val uri: Uri = if (type == 0) {
            MediaStore.Audio.Genres.Members.getContentUri("external", arg.toLong())
        } else {
            MediaStore.Audio.Playlists.Members.getContentUri("external", arg.toLong())
        }
        val cursor = resolver.query(uri, null, null, null, null)
        val count = cursor?.count ?: -1
        cursor?.close()
        return count
    }

    // Ignore the [Data] deprecation because this plugin support older versions.
    @Suppress("DEPRECATION")
    fun loadFirstItem(type: Int, id: Number, resolver: ContentResolver): String? {

        // We use almost the same method to 'query' the first item from Song/Album/Artist and we
        // need to use a different uri when 'querying' from playlist.
        // If [type] is something different, return null.
        val selection: String? = when (type) {
            0 -> MediaStore.Audio.Media._ID + "=?"
            1 -> MediaStore.Audio.Media.ALBUM_ID + "=?"
            2 -> null
            3 -> MediaStore.Audio.Media.ARTIST_ID + "=?"
            4 -> null
            else -> return null
        }

        var dataOrId: String? = null
        var cursor: Cursor? = null
        try {
            // Type 2 or 4 we use a different uri.
            //
            // Type 2 == Playlist
            // Type 4 == Genre
            //
            // And the others we use the normal uri.
            when (true) {
                (type == 2 && selection == null) -> {
                    cursor = resolver.query(
                        MediaStore.Audio.Playlists.Members.getContentUri("external", id.toLong()),
                        arrayOf(
                            MediaStore.Audio.Playlists.Members.DATA,
                            MediaStore.Audio.Playlists.Members.AUDIO_ID
                        ),
                        null,
                        null,
                        null
                    )
                }
                (type == 4 && selection == null) -> {
                    cursor = resolver.query(
                        MediaStore.Audio.Genres.Members.getContentUri("external", id.toLong()),
                        arrayOf(
                            MediaStore.Audio.Genres.Members.DATA,
                            MediaStore.Audio.Genres.Members.AUDIO_ID
                        ),
                        null,
                        null,
                        null
                    )
                }
                else -> {
                    cursor = resolver.query(
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        arrayOf(MediaStore.Audio.Media.DATA, MediaStore.Audio.Media._ID),
                        selection,
                        arrayOf(id.toString()),
                        null
                    )
                }
            }
        } catch (e: Exception) {
//            Log.i("on_audio_error", e.toString())
        }

        //
        if (cursor != null) {
            cursor.moveToFirst()
            // Try / Catch to avoid problems. Everytime someone request the first song from a playlist and
            // this playlist is empty will crash the app, so we just 'print' the error.
            try {
                dataOrId =
                    if (Build.VERSION.SDK_INT >= 29 && (type == 2 || type == 3 || type == 4)) {
                        cursor.getString(1)
                    } else {
                        cursor.getString(0)
                    }
            } catch (e: Exception) {
                Log.i("on_audio_error", e.toString())
            }
        }
        cursor?.close()

        return dataOrId
    }

    fun chooseWithFilterType(uri: Uri, itemProperty: String, cursor: Cursor): Any? {
        return when (uri) {
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI -> loadSongItem(itemProperty, cursor)
            MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI -> loadAlbumItem(itemProperty, cursor)
            MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI -> loadPlaylistItem(
                itemProperty,
                cursor
            )
            MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI -> loadArtistItem(itemProperty, cursor)
            MediaStore.Audio.Genres.EXTERNAL_CONTENT_URI -> loadGenreItem(itemProperty, cursor)
            else -> null
        }
    }
}