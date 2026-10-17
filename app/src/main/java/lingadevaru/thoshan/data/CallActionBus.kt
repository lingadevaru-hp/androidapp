package lingadevaru.thoshan.data

/**
 * Bus that forwards notification action taps (Mute / End / Speaker)
 * to the active [lingadevaru.thoshan.viewmodel.ChatViewModel].
 * Wired up in MainActivity; the receiver below only calls these lambdas.
 */
object CallActionBus {
    var onToggleMute: () -> Unit = {}
    var onToggleSpeaker: () -> Unit = {}
    var onEndCall: () -> Unit = {}
}
