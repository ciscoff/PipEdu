package dev.barabu.pip

import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Player

/**
 * Stub listener
 */
class PlayerListener(
    private val onStateBuffering: () -> Unit,
    private val onStateReady: () -> Unit,
    private val onStateEnded: () -> Unit
) : Player.Listener {

    override fun onPlaybackStateChanged(playbackState: Int) {

        when (playbackState) {
            ExoPlayer.STATE_IDLE -> {
            }
            ExoPlayer.STATE_BUFFERING -> {
                onStateBuffering()
            }
            ExoPlayer.STATE_READY -> {
                onStateReady()
            }
            ExoPlayer.STATE_ENDED -> {
                onStateEnded()
            }
        }
    }
}