@file:Suppress("unused")

package io.github.yangentao.harejee.ws

import jakarta.websocket.Session
import java.nio.ByteBuffer
import java.util.concurrent.Future

fun Session.pathParam(name: String): String? {
    return this.pathParameters[name]
}

fun Session.userParam(name: String): Any? {
    return this.userProperties[name]
}

fun Session.setUserParam(name: String, value: Any?) {
    if (value == null) {
        this.userProperties.remove(name)
    } else {
        this.userProperties[name] = value
    }
}

fun Session.sendText(text: String) {
    this.basicRemote.sendText(text)
}

fun Session.sendTextAsync(text: String): Future<Void> {
    return this.asyncRemote.sendText(text)
}

fun Session.sendBinary(data: ByteArray) {
    this.basicRemote.sendBinary(ByteBuffer.wrap(data))
}

fun Session.sendBinaryAsync(data: ByteArray): Future<Void> {
    return this.asyncRemote.sendBinary(ByteBuffer.wrap(data))
}