package io.github.yangentao.harejee.ws

import io.github.yangentao.types.DateTime
import io.github.yangentao.xlog.logd
import jakarta.websocket.CloseReason
import jakarta.websocket.EndpointConfig
import jakarta.websocket.OnClose
import jakarta.websocket.OnError
import jakarta.websocket.OnMessage
import jakarta.websocket.OnOpen
import jakarta.websocket.Session
import jakarta.websocket.server.PathParam
import jakarta.websocket.server.ServerEndpoint

@ServerEndpoint("/echotest/{ident}")
class WSEcho {

    @OnOpen
    fun onOpen(session: Session, config: EndpointConfig, @PathParam("ident") ident: String) {
        val id = session.pathParameters["ident"]

        logd("WSHello.onOpen", ident, " id=$id")
    }

    @OnMessage
    fun onMessage(session: Session, message: String, @PathParam("ident") ident: String) {
        logd("onMessage: ", message, ident)
        session.basicRemote.sendText("ECHO: " + message + " " + DateTime().formatTime())
    }

    @OnError
    fun onError(session: Session, t: Throwable, @PathParam("ident") ident: String) {
        logd("onError: ", t.localizedMessage)
        t.printStackTrace()
    }

    @OnClose
    fun onClose(session: Session, reason: CloseReason, @PathParam("ident") ident: String) {
        logd("onClose: ", reason.toString())
    }

}