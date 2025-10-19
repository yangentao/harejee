@file:Suppress("unused")

package io.github.yangentao.harejee.ws

import jakarta.websocket.CloseReason
import jakarta.websocket.EndpointConfig
import jakarta.websocket.PongMessage
import jakarta.websocket.Session
import java.nio.ByteBuffer
import java.util.concurrent.Future

open class HareSession(val session: Session) {

    open fun onText(message: String) {

    }

    open fun onBinary(data: ByteArray) {

    }

    open fun onPong(pong: PongMessage) {

    }

    open fun onError(ex: Throwable) {

    }

    open fun onClose(reason: CloseReason) {

    }

    open fun onOpen(config: EndpointConfig) {

    }

    fun sendText(text: String) {
        session.basicRemote.sendText(text)
    }

    fun sendTextAsync(text: String): Future<Void> {
        return session.asyncRemote.sendText(text)
    }

    fun sendBinary(data: ByteArray) {
        session.basicRemote.sendBinary(ByteBuffer.wrap(data))
    }

    fun sendBinaryAsync(data: ByteArray): Future<Void> {
        return session.asyncRemote.sendBinary(ByteBuffer.wrap(data))
    }

}