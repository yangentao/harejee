package io.github.yangentao.harejee.ws

import io.github.yangentao.types.createInstanceX
import io.github.yangentao.xlog.TagLog
import jakarta.websocket.*
import kotlin.reflect.KClass

open class HareEndpoint : Endpoint() {
    val wsLog = TagLog("websocket")

    open fun onTextMessage(session: Session, message: String) {
        session.hareSession?.onText(message)
    }

    open fun onBinaryMessage(session: Session, message: ByteArray) {
        session.hareSession?.onBinary(message)
    }

    open fun onPongMessage(session: Session, message: PongMessage) {
        session.hareSession?.onPong(message)
    }

    override fun onError(session: Session, ex: Throwable) {
        session.hareSession?.onError(ex)
    }

    override fun onClose(session: Session, reason: CloseReason) {
        session.hareSession?.onClose(reason)
        session.hareSession = null
    }

    override fun onOpen(session: Session, config: EndpointConfig) {
        wsLog.d("Open websocket: ", session.id, session.requestURI)
        wsLog.d("path arameters: ", session.pathParameters)

        session.addMessageHandler(object : MessageHandler.Whole<String> {
            override fun onMessage(message: String) {
                onTextMessage(session, message)
            }
        })
        session.addMessageHandler(object : MessageHandler.Whole<ByteArray> {
            override fun onMessage(message: ByteArray) {
                onBinaryMessage(session, message)
            }
        })
        session.addMessageHandler(object : MessageHandler.Whole<PongMessage> {
            override fun onMessage(message: PongMessage) {
                onPongMessage(session, message)
            }
        })
        session.hareSession = sessionClass?.createInstanceX(session)
        session.hareSession?.onOpen(config)
    }

    private var Session.hareSession: HareSession?
        get() = this.userParam("HareSession") as? HareSession
        set(value) = this.setUserParam("HareSession", value)

    companion object {
        var sessionClass: KClass<out HareSession>? = null
    }
}

