/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.domain.model.control

import android.media.MediaPlayer.*
import android.os.Build
import android.widget.VideoView

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
class VideoViewControl(
    private val videoView: VideoView
) : MediaControl {
    override val currentPosition: Int
        get() = try {
            videoView.currentPosition
        } catch (ignored: IllegalStateException) {
            0
        }
    override val duration: Int
        get() = try {
            videoView.duration
        } catch (ignored: IllegalStateException) {
            0
        }
    override val isPlaying: Boolean
        get() = try {
            videoView.isPlaying
        } catch (ignored: IllegalStateException) {
            false
        }

    override fun play() {
        if (!videoView.isPlaying) {
            videoView.start()
        }
    }

    override fun pause() {
        if (videoView.isPlaying) {
            videoView.pause()
        }
    }

    override fun seekTo(position: Int) {
        videoView.seekTo(position)
        if (!videoView.isPlaying) {
            videoView.start()
        }
    }

    override fun stop() {
        videoView.stopPlayback()
    }

    override fun setOnPreparedListener(listener: OnPreparedListener?) {
        videoView.setOnPreparedListener(listener)
    }

    override fun setOnErrorListener(listener: OnErrorListener?) {
        videoView.setOnErrorListener { mediaPlayer, what, extra ->
            listener == null || listener.onError(
                mediaPlayer,
                what,
                extra
            )
        }
    }

    override fun setOnInfoListener(listener: OnInfoListener?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            videoView.setOnInfoListener(listener)
        }
    }

    override fun setOnCompletionListener(listener: OnCompletionListener?) {
        videoView.setOnCompletionListener(listener)
    }
}
