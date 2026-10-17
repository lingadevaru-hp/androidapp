package com.example.data

/**
 * Bus that forwards notification action taps (Mute / End / Speaker)
 * to the active [com.example.viewmodel.ChatViewModel].
 * Wired up in MainActivity; the receiver below only calls these lambdas.
 */
object CallActionBus {
    var onToggleMute: () -> Unit = {}
    var onToggleSpeaker: () -> Unit = {}
    var onEndCall: () -> Unit = {}
}
