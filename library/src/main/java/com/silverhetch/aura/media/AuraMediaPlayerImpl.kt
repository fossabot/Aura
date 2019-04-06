package com.silverhetch.aura.media

import android.content.Context
import android.graphics.Point
import android.media.MediaPlayer
import android.net.Uri
import android.os.Handler
import android.util.Size
import android.view.SurfaceHolder
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import java.util.concurrent.TimeUnit.SECONDS

/**
 * Implementation of [AuraMediaPlayer]
 */
class AuraMediaPlayerImpl(private val context: Context) : AuraMediaPlayer {
    private val duration = MutableLiveData<Int>().apply { value = 0 }
    private val progress = MutableLiveData<Int>().apply { value = 0 }
    private val buffered = MutableLiveData<Int>().apply { value = 0 }
    private val videoSize = MutableLiveData<Point>().apply { value = Point(0, 0) }
    private val handler = Handler()
    private var mediaPlayer: MediaPlayer = MediaPlayer()
    private var attemptPlay = false
    private val progressRunnable = object : Runnable {
        override fun run() {
            if (mediaPlayer.duration != 0) {
                progress.value = mediaPlayer.currentPosition
            }
            handler.postDelayed(this, SECONDS.toMillis(1))
        }
    }

    override fun load(uri: String) {
        mediaPlayer.reset()
        mediaPlayer.setDisplay(null)
        mediaPlayer.setDataSource(context, Uri.parse(uri))
        mediaPlayer.setOnVideoSizeChangedListener { mp, width, height ->
            videoSize.value = Point(width, height)
        }
        mediaPlayer.setOnBufferingUpdateListener { mp, percent ->
            buffered.value = percent
        }
        mediaPlayer.setOnPreparedListener {
            duration.value = mediaPlayer.duration
            if (attemptPlay){
                mediaPlayer.start()
            }
        }
        mediaPlayer.prepareAsync()
        handler.postDelayed(progressRunnable, SECONDS.toMillis(1))
    }

    override fun play() {
        attemptPlay = true
        mediaPlayer.start()
    }

    override fun pause() {
        attemptPlay = false
        mediaPlayer.pause()
    }

    override fun seekTo(position: Int) {
        mediaPlayer.seekTo(position)
    }

    override fun progress(): LiveData<Int> {
        return progress
    }

    override fun duration(): LiveData<Int> {
        return duration
    }

    override fun isPlaying(): Boolean {
        return mediaPlayer.isPlaying
    }

    override fun buffered(): LiveData<Int> {
        return buffered
    }

    override fun videoSize(): LiveData<Point> {
        return videoSize
    }

    override fun attachDisplay(surfaceHolder: SurfaceHolder) {
        mediaPlayer.setDisplay(surfaceHolder)
    }

    override fun detachDisplay() {
        mediaPlayer.setDisplay(null)
    }

    override fun release() {
        attemptPlay = false
        handler.removeCallbacks(progressRunnable)
        mediaPlayer.release()
    }

}