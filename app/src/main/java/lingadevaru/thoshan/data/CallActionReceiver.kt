package lingadevaru.thoshan.data

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class CallActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        // MainActivity observes uiState and re-pushes the notification
        // with fresh mute/speaker labels, so no refresh here.
        when (intent.action) {
            ACTION_MUTE -> CallActionBus.onToggleMute()
            ACTION_SPEAKER -> CallActionBus.onToggleSpeaker()
            ACTION_END -> {
                CallActionBus.onEndCall()
                CallNotificationManager.hide(context)
            }
        }
    }

    companion object {
        const val ACTION_MUTE = "in.lingadevaru.thoshan.CALL_MUTE"
        const val ACTION_SPEAKER = "in.lingadevaru.thoshan.CALL_SPEAKER"
        const val ACTION_END = "in.lingadevaru.thoshan.CALL_END"
    }
}
