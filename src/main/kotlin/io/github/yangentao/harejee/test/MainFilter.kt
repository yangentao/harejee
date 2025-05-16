package io.github.yangentao.harejee.test
//
//import io.github.yangentao.harejee.BaseEndpoint
//import io.github.yangentao.harejee.HareFilter
//import io.github.yangentao.xlog.logd
//import jakarta.servlet.annotation.MultipartConfig
//import jakarta.servlet.annotation.WebFilter
//import jakarta.websocket.EndpointConfig
//import jakarta.websocket.Session
//
//@MultipartConfig
//@WebFilter(urlPatterns = ["/*"], asyncSupported = true)
//class MainFilter : HareFilter() {
//    override fun onCreate() {
//        websocket("/echo/{ident}", EchoEndpoint::class)
//    }
//
//    override fun onDestory() {
//    }
//}
//
//class EchoEndpoint : BaseEndpoint() {
//    val ident: String by pathParams
//
//    override fun onTextMessage(session: Session, message: String) {
//        logd("recv text message: ", message)
//        session.basicRemote.sendText("Echo: $message")
//    }
//
//    override fun onOpen(session: Session, config: EndpointConfig) {
//        super.onOpen(session, config)
//        wsLog.d("ident: ", ident)
//    }
//}