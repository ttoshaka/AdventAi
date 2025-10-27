package ru.toshaka.advent.data.agent

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow

class MessageBus {

    val flow = MutableSharedFlow<Pair<AiResponse, DebugInfo>>(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    fun send(response: AiResponse, debugInfo: DebugInfo) {
        flow.tryEmit(response to debugInfo)
    }
}