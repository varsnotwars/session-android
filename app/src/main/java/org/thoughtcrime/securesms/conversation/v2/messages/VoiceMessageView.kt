package org.thoughtcrime.securesms.conversation.v2.messages

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.util.Log
import android.view.*
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.core.view.isVisible
import kotlinx.android.synthetic.main.view_voice_message.view.*
import network.loki.messenger.R
import org.thoughtcrime.securesms.audio.AudioSlidePlayer
import org.thoughtcrime.securesms.components.CornerMask
import org.thoughtcrime.securesms.conversation.v2.utilities.MessageBubbleUtilities
import org.thoughtcrime.securesms.database.model.MmsMessageRecord
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt
import kotlin.math.roundToLong

class VoiceMessageView : LinearLayout, AudioSlidePlayer.Listener {
    private val cornerMask by lazy { CornerMask(this) }
    private var isPlaying = false
    private var progress = 0.0
    private var duration = 0L
    private var player: AudioSlidePlayer? = null
    private var isPreparing = false

    // region Lifecycle
    constructor(context: Context) : super(context) { initialize() }
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) { initialize() }
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) { initialize() }

    private fun initialize() {
        LayoutInflater.from(context).inflate(R.layout.view_voice_message, this)
        voiceMessageViewDurationTextView.text = String.format("%01d:%02d",
            TimeUnit.MILLISECONDS.toMinutes(0),
            TimeUnit.MILLISECONDS.toSeconds(0))
    }
    // endregion

    // region Updating
    fun bind(message: MmsMessageRecord, isStartOfMessageCluster: Boolean, isEndOfMessageCluster: Boolean) {
        val audio = message.slideDeck.audioSlide!!
        val player = AudioSlidePlayer.createFor(context, audio, this)
        this.player = player
        isPreparing = true
        if (!audio.isPendingDownload && !audio.isInProgress) {
            player.play(0.0)
        }
        voiceMessageViewLoader.isVisible = audio.isPendingDownload
        val cornerRadii = MessageBubbleUtilities.calculateRadii(context, isStartOfMessageCluster, isEndOfMessageCluster, message.isOutgoing)
        cornerMask.setTopLeftRadius(cornerRadii[0])
        cornerMask.setTopRightRadius(cornerRadii[1])
        cornerMask.setBottomRightRadius(cornerRadii[2])
        cornerMask.setBottomLeftRadius(cornerRadii[3])
    }

    override fun onPlayerStart(player: AudioSlidePlayer) {
        if (!isPreparing) { return }
        isPreparing = false
        duration = player.duration
        voiceMessageViewDurationTextView.text = String.format("%01d:%02d",
            TimeUnit.MILLISECONDS.toMinutes(duration),
            TimeUnit.MILLISECONDS.toSeconds(duration))
        player.stop()
    }

    override fun onPlayerProgress(player: AudioSlidePlayer, progress: Double, unused: Long) {
        if (progress == 1.0) {
            togglePlayback()
            handleProgressChanged(0.0)
        } else {
            handleProgressChanged(progress)
        }
    }

    private fun handleProgressChanged(progress: Double) {
        this.progress = progress
        voiceMessageViewDurationTextView.text = String.format("%01d:%02d",
            TimeUnit.MILLISECONDS.toMinutes(duration - (progress * duration.toDouble()).roundToLong()),
            TimeUnit.MILLISECONDS.toSeconds(duration - (progress * duration.toDouble()).roundToLong()))
        val layoutParams = progressView.layoutParams as RelativeLayout.LayoutParams
        layoutParams.width = (width.toFloat() * progress.toFloat()).roundToInt()
        progressView.layoutParams = layoutParams
    }

    override fun onPlayerStop(player: AudioSlidePlayer) { }

    override fun dispatchDraw(canvas: Canvas) {
        super.dispatchDraw(canvas)
        cornerMask.mask(canvas)
    }
    // endregion

    // region Interaction
    fun togglePlayback() {
        val player = this.player ?: return
        isPlaying = !isPlaying
        val iconID = if (isPlaying) R.drawable.exo_icon_pause else R.drawable.exo_icon_play
        voiceMessagePlaybackImageView.setImageResource(iconID)
        if (isPlaying) {
            player.play(progress)
        } else {
            player.stop()
        }
    }

    fun handleDoubleTap() {
        val player = this.player ?: return
        player.playbackSpeed = if (player.playbackSpeed == 1.0f) 1.5f else 1.0f
    }
    // endregion
}