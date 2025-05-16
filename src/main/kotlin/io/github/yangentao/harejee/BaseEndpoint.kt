package io.github.yangentao.harejee

import io.github.yangentao.xlog.TagLog
import jakarta.websocket.*

open class BaseEndpoint : Endpoint() {
    val pathParams: HashMap<String, String> = HashMap()
    val wsLog = TagLog("websocket")

    open fun onTextMessage(session: Session, message: String) {

    }

    open fun onBinaryMessage(session: Session, message: ByteArray) {
    }

    open fun onPongMessage(session: Session, message: PongMessage) {
    }

    override fun onOpen(session: Session, config: EndpointConfig) {
        wsLog.d("open websocket: ", session.id, session.requestURI)
        wsLog.d("pathParameters: ", session.pathParameters)
        pathParams.putAll(session.pathParameters)
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
    }

    override fun onError(session: Session, thr: Throwable) {
        wsLog.d(session.id, thr)
    }

    override fun onClose(session: Session, closeReason: CloseReason) {
        wsLog.d("websocket closed: ", session.id, closeReason)
    }

}