package dev.barabu.pip

import android.app.PictureInPictureParams
import android.content.res.Configuration
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.util.Util
import dev.barabu.pip.databinding.ActivityMainBinding

/**
 * Materials: https://developer.android.com/guide/topics/ui/picture-in-picture
 */
class MainActivity : AppCompatActivity() {

    private val viewBinding by lazy(LazyThreadSafetyMode.NONE) {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private val pipParams: PictureInPictureParams by lazy {
        PictureInPictureParams.Builder().build()
    }

    private val playerListener = PlayerListener(::stubOp, ::stubOp, ::stubOp)

    private lateinit var playerController: PlayerController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)

        playerController = PlayerController(
            viewBinding.playerView,
            this@MainActivity.getString(R.string.media_url_hls),
            playerListener
        )

        viewBinding.apply {

            pipToggle.setOnClickListener {
                this@MainActivity.enterPictureInPictureMode(pipParams)
            }

            playerView.addOnLayoutChangeListener { _, left, top, right, bottom,
                                                   oldLeft, oldTop, oldRight, oldBottom ->
                if (left != oldLeft || right != oldRight || top != oldTop || bottom != oldBottom) {
                    val sourceRectHint = Rect()
                    playerView.getGlobalVisibleRect(sourceRectHint)
                    setPictureInPictureParams(
                        PictureInPictureParams.Builder()
                            .setSourceRectHint(sourceRectHint)
                            .build()
                    )
                }
            }
        }
    }

    public override fun onStart() {
        super.onStart()
        playerController.initializePlayer(applicationContext)
    }

    override fun onStop() {
        super.onStop()
        playerController.releasePlayer()
    }

    override fun onPictureInPictureModeChanged(
        isInPictureInPictureMode: Boolean,
        newConfig: Configuration?
    ) {
        viewBinding.pipToggle.visibility = if (isInPictureInPictureMode) {
            View.GONE
        } else {
            View.VISIBLE
        }
    }

    private fun stubOp() {
        // todo nothing
    }
}