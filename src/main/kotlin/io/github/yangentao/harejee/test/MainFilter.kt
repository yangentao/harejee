package io.github.yangentao.harejee.test

import io.github.yangentao.harejee.HareFilter
import io.github.yangentao.harejee.addWebsocket
import io.github.yangentao.xlog.logd
import io.github.yangentao.xlog.loge
import jakarta.servlet.annotation.MultipartConfig
import jakarta.servlet.annotation.WebFilter
import jakarta.websocket.*

//@MultipartConfig
//@WebFilter(urlPatterns = ["/*"], asyncSupported = true)
//class MainFilter : HareFilter() {
//    override fun onCreate() {
//        servletContext.addWebsocket(EchoEndpoint::class, "/echo/{ident}")
//    }
//
//    override fun onDestory() {
//    }
//}

class EchoEndpoint : Endpoint() {
    fun onTextMessage(session: Session, message: String) {
        logd("text message: ", message)
        session.basicRemote.sendText("Echo: $message")
    }

    fun onBinaryMessage(session: Session, message: ByteArray) {
        logd("binary message: ", message.size)
    }

    fun onPongMessage(session: Session, message: PongMessage) {
        logd("pong message: ", message.applicationData.limit())
    }

    override fun onOpen(session: Session, config: EndpointConfig) {
        logd("open: ", session.requestURI)
        logd("pathParameters: ", session.pathParameters)
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

    override fun onClose(session: Session, closeReason: CloseReason) {
        super.onClose(session, closeReason)
        logd("close")
    }

    override fun onError(session: Session, thr: Throwable) {
        super.onError(session, thr)
        loge(session.id, thr)
    }

}