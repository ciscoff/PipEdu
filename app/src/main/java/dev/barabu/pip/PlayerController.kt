package dev.barabu.pip

import android.content.Context
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.offline.FilteringManifestParser
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.source.dash.DashMediaSource
import com.google.android.exoplayer2.source.dash.manifest.DashManifestParser
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.source.hls.playlist.DefaultHlsPlaylistParserFactory
import com.google.android.exoplayer2.source.smoothstreaming.SsMediaSource
import com.google.android.exoplayer2.source.smoothstreaming.manifest.SsManifestParser
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.MimeTypes
import com.google.android.exoplayer2.util.Util

class PlayerController(
    private val playerView: PlayerView,
    private val videoUri: String,
    private val playerListener: Player.Listener
) {
    var player: ExoPlayer? = null
        private set

    private var playWhenReady = true
    private var timeLineWindow = 0
    private var playbackPosition = 0L

    fun initializePlayer(context: Context) {

        if (player == null) {
            val mediaSource = buildMediaSource(videoUri, context)

            // Audio Focus
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(C.USAGE_MEDIA)
                .setContentType(C.CONTENT_TYPE_MOVIE)
                .build()

            player = ExoPlayer.Builder(context)
                .build().also { exoPlayer ->
                    playerView.player = exoPlayer
                    exoPlayer.setMediaSource(mediaSource)
                    exoPlayer.playWhenReady = playWhenReady
                    exoPlayer.seekTo(timeLineWindow, playbackPosition)
                    exoPlayer.addListener(playerListener)
                    exoPlayer.setAudioAttributes(audioAttributes, true)
                    exoPlayer.prepare()
                }
            player?.play()
        }
    }

    fun releasePlayer() {
        player?.run {
            this@PlayerController.playbackPosition = currentPosition
            this@PlayerController.timeLineWindow = this.currentWindowIndex
            this@PlayerController.playWhenReady = this.playWhenReady
            removeListener(playerListener)
            release()
        }
        player = null
    }

    private fun buildMediaSource(uri: String, context: Context): MediaSource {
        @C.ContentType var type = Util.inferContentType(uri)
        if (type == C.TYPE_OTHER) {
            type = inferContentTypeLocal(uri)
        }

        val dataSourceFactory = DefaultDataSourceFactory(
            context, Util.getUserAgent(context, context.packageName)
        )

        return when (type) {
            C.TYPE_DASH -> {
                DashMediaSource
                    .Factory(dataSourceFactory)
                    .setManifestParser(
                        FilteringManifestParser(DashManifestParser(), null)
                    ).createMediaSource(dashMediaItem(uri))
            }
            C.TYPE_SS -> {
                SsMediaSource
                    .Factory(dataSourceFactory)
                    .setManifestParser(
                        FilteringManifestParser(SsManifestParser(), null)
                    ).createMediaSource(smoothStreamingMediaItem(uri))
            }
            C.TYPE_HLS -> {
                HlsMediaSource
                    .Factory(dataSourceFactory)
                    .setPlaylistParserFactory(
                        DefaultHlsPlaylistParserFactory()
                    ).createMediaSource(hlsMediaItem(uri))
            }
            else /*C.TYPE_OTHER*/ -> {
                ProgressiveMediaSource.Factory(dataSourceFactory)
                    .createMediaSource(MediaItem.fromUri(uri))
            }
        }
    }

    /**
     * Попробовать самостоятельно определить тип контента
     */
    private fun inferContentTypeLocal(uri: String): Int {
        val dashPatterns = listOf(PATTERN_DASH)
        val hlsPatterns = listOf(PATTERN_HLS)
        val chunks = uri.split("/")

        return when {
            dashPatterns.all { pt -> chunks.any { ch -> ch.contains(pt) } } -> C.TYPE_DASH
            hlsPatterns.all { pt -> chunks.any { ch -> ch.contains(pt) } } -> C.TYPE_HLS
            else -> C.TYPE_OTHER
        }
    }

    private fun smoothStreamingMediaItem(uri: String) = getMediaItem(uri, MimeTypes.APPLICATION_SS)
    private fun dashMediaItem(uri: String) = getMediaItem(uri, MimeTypes.APPLICATION_MPD)
    private fun hlsMediaItem(uri: String) = getMediaItem(uri, MimeTypes.APPLICATION_M3U8)
    private fun getMediaItem(uri: String, mimeType: String): MediaItem =
        MediaItem.Builder()
            .setUri(uri)
            .setMimeType(mimeType)
            .build()

    companion object {
        private const val PATTERN_DASH = "dash"
        private const val PATTERN_HLS = "m3u8"
    }
}