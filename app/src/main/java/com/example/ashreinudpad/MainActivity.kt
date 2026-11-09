package com.example.ashreinudpad

import android.app.Activity
import android.os.Bundle
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.setPadding
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import org.json.JSONObject
import java.util.Locale

data class Clip(val id: Int, val name: String, val url: String, val durationMs: Long)

class MainActivity : Activity() {
    private lateinit var player: ExoPlayer
    private lateinit var list: ListView
    private lateinit var status: TextView
    private lateinit var title: TextView
    private lateinit var seek: SeekBar
    private lateinit var clips: List<Clip>
    private var selected = 0
    private var userSeeking = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.decorView.keepScreenOn = true
        clips = loadClips()
        player = ExoPlayer.Builder(this).build()
        buildScreen()
        player.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                updateStatus()
            }
            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                updateStatus()
            }
            override fun onPlaybackStateChanged(playbackState: Int) {
                updateStatus()
            }
        })
        if (clips.isNotEmpty()) list.setSelection(0)
        list.requestFocus()
    }

    private fun loadClips(): List<Clip> {
        val raw = assets.open("playlist.json").bufferedReader(Charsets.UTF_8).use { it.readText() }
        val root = JSONObject(raw)
        val array = root.getJSONArray("clips")
        return (0 until array.length()).map { i ->
            val item = array.getJSONObject(i)
            Clip(
                item.getInt("id"),
                item.getString("name"),
                item.getString("audio_uri"),
                item.optLong("duration", 0L)
            )
        }
    }

    private fun buildScreen() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(16)
            setBackgroundColor(0xFF101820.toInt())
        }
        title = TextView(this).apply {
            text = "אשׁרינו • ${getPlaylistName()}"
            textSize = 21f
            setTextColor(0xFFFFFFFF.toInt())
            gravity = Gravity.CENTER
            setPadding(8)
        }
        status = TextView(this).apply {
            text = "בחר שיעור ולחץ OK להפעלה"
            textSize = 15f
            setTextColor(0xFFB0BEC5.toInt())
            gravity = Gravity.CENTER
            setPadding(8)
        }
        seek = SeekBar(this).apply {
            max = 1000
            isFocusable = false
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(bar: SeekBar?, progress: Int, fromUser: Boolean) {
                    if (fromUser) userSeeking = true
                }
                override fun onStartTrackingTouch(bar: SeekBar?) { userSeeking = true }
                override fun onStopTrackingTouch(bar: SeekBar?) {
                    val duration = player.duration
                    if (duration > 0) player.seekTo(duration * (bar?.progress ?: 0) / 1000)
                    userSeeking = false
                }
            })
        }
        list = ListView(this).apply {
            dividerHeight = 1
            setBackgroundColor(0xFF18232D.toInt())
            choiceMode = ListView.CHOICE_MODE_SINGLE
            adapter = object : ArrayAdapter<String>(
                this@MainActivity, android.R.layout.simple_list_item_activated_1,
                clips.mapIndexed { index, clip -> "${index + 1}. ${clip.name}" }
            ) {
                override fun getView(position: Int, convertView: View?, parent: android.view.ViewGroup): View {
                    return super.getView(position, convertView, parent).apply {
                        setBackgroundColor(if (position == selected) 0xFF29445A.toInt() else 0xFF18232D.toInt())
                        (this as TextView).apply {
                            setTextColor(0xFFFFFFFF.toInt())
                            textSize = 16f
                            setPadding(12, 14, 12, 14)
                        }
                    }
                }
            }
            setOnItemClickListener { _, _, position, _ -> play(position) }
        }

        root.addView(title, LinearLayout.LayoutParams(-1, -2))
        root.addView(status, LinearLayout.LayoutParams(-1, -2))
        root.addView(seek, LinearLayout.LayoutParams(-1, -2))
        root.addView(list, LinearLayout.LayoutParams(-1, 0, 1f))
        setContentView(root)
    }

    private fun getPlaylistName(): String {
        val raw = assets.open("playlist.json").bufferedReader(Charsets.UTF_8).use { it.readText() }
        return JSONObject(raw).optString("name", "Ashreinu")
    }

    private fun play(position: Int) {
        if (position !in clips.indices) return
        selected = position
        val clip = clips[position]
        player.setMediaItem(MediaItem.fromUri(clip.url))
        player.prepare()
        player.play()
        list.invalidateViews()
        updateStatus()
    }

    private fun updateStatus() {
        if (!::status.isInitialized) return
        val current = clips.getOrNull(selected)
        val state = when {
            player.isPlaying -> "▶ מנגן"
            player.playbackState == Player.STATE_BUFFERING -> "טוען..."
            player.playbackState == Player.STATE_READY -> "מושהה"
            else -> "בחר שיעור ולחץ OK"
        }
        status.text = if (current != null && player.currentMediaItem != null)
            "$state  •  ${current.name}" else state
        val duration = player.duration
        if (!userSeeking && duration > 0) {
            seek.progress = (player.currentPosition * 1000 / duration).toInt().coerceIn(0, 1000)
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE, KeyEvent.KEYCODE_HEADSETHOOK -> {
                if (player.isPlaying) player.pause() else if (player.currentMediaItem != null) player.play()
                return true
            }
            KeyEvent.KEYCODE_MEDIA_PLAY -> { player.play(); return true }
            KeyEvent.KEYCODE_MEDIA_PAUSE -> { player.pause(); return true }
            KeyEvent.KEYCODE_MEDIA_NEXT -> { play((selected + 1).coerceAtMost(clips.lastIndex)); return true }
            KeyEvent.KEYCODE_MEDIA_PREVIOUS -> { play((selected - 1).coerceAtLeast(0)); return true }
            KeyEvent.KEYCODE_DPAD_LEFT -> {
                if (player.currentMediaItem != null) player.seekTo((player.currentPosition - 10_000).coerceAtLeast(0))
                return true
            }
            KeyEvent.KEYCODE_DPAD_RIGHT -> {
                if (player.currentMediaItem != null) player.seekTo((player.currentPosition + 10_000).coerceAtMost(player.duration))
                return true
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onDestroy() {
        player.release()
        super.onDestroy()
    }
}
