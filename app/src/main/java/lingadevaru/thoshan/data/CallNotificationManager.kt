package lingadevaru.thoshan.data

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import lingadevaru.thoshan.MainActivity
import lingadevaru.thoshan.model.SpeakerAudioState

/**
 * Dialer-style ongoing notification shown while:
 * • call mode is active, or
 * • an assistant message is being spoken (TTS).
 *
 * Dropdown actions mirror the in-app controls: Mute / End / Speaker,
 * so it feels like a real phone call even outside the app.
 */
object CallNotificationManager {

    const val CHANNEL_ID = "voice_channel"
    const val NOTIFICATION_ID = 1001

    // Last state pushed via show() — used by refresh() from the action receiver.
    private var lastTitle: String = "Thoshan's Assistant"
    private var lastText: String = "Voice active"
    private var lastMuted: Boolean = false
    private var lastSpeakerOn: Boolean = true
    private var lastElapsedSeconds: Int = 0
    private var lastVisible = false

    fun show(
        context: Context,
        title: String,
        text: String,
        isMuted: Boolean,
        isSpeakerOn: Boolean,
        elapsedSeconds: Int
    ) {
        lastTitle = title
        lastText = text
        lastMuted = isMuted
        lastSpeakerOn = isSpeakerOn
        lastElapsedSeconds = elapsedSeconds
        lastVisible = true
        ensureChannel(context)

        val contentIntent = PendingIntent.getActivity(
            context, 0,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val muteIntent = PendingIntent.getBroadcast(
            context, 1,
            Intent(context, CallActionReceiver::class.java).setAction(CallActionReceiver.ACTION_MUTE),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val speakerIntent = PendingIntent.getBroadcast(
            context, 2,
            Intent(context, CallActionReceiver::class.java).setAction(CallActionReceiver.ACTION_SPEAKER),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val endIntent = PendingIntent.getBroadcast(
            context, 3,
            Intent(context, CallActionReceiver::class.java).setAction(CallActionReceiver.ACTION_END),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_phone_call)
            .setContentTitle(title)
            .setContentText(text)
            .setSubText(if (isMuted) "Muted" else text)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setContentIntent(contentIntent)
            .setUsesChronometer(true)
            .setWhen(System.currentTimeMillis() - elapsedSeconds * 1000L)
            .addAction(
                android.R.drawable.ic_btn_speak_now,
                if (isMuted) "Unmute" else "Mute",
                muteIntent
            )
            .addAction(
                android.R.drawable.ic_lock_power_off,
                "End",
                endIntent
            )
            .addAction(
                android.R.drawable.ic_lock_silent_mode_off,
                if (isSpeakerOn) "Speaker off" else "Speaker on",
                speakerIntent
            )
            .build()

        runCatching {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
        }
    }

    fun refresh(context: Context) {
        if (lastVisible) {
            show(context, lastTitle, lastText, lastMuted, lastSpeakerOn, lastElapsedSeconds)
        }
    }

    fun hide(context: Context) {
        lastVisible = false
        runCatching {
            NotificationManagerCompat.from(context).cancel(NOTIFICATION_ID)
        }
    }

    fun stateText(callActive: Boolean, audioState: SpeakerAudioState): String {
        return when {
            callActive -> "Voice call active"
            audioState == SpeakerAudioState.SPEAKING -> "Speaking…"
            audioState == SpeakerAudioState.PAUSED -> "Paused"
            audioState == SpeakerAudioState.LOADING -> "Preparing audio…"
            else -> "Voice active"
        }
    }

    private fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Voice calls",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Ongoing voice call and speech playback controls"
            }
            val manager = context.getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }
}
