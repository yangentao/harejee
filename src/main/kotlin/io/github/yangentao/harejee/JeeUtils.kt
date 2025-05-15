package io.github.yangentao.harejee

import io.github.yangentao.hare.utils.encodedURL
import io.github.yangentao.httpbasic.HttpHeader
import io.github.yangentao.httpbasic.HttpMethod
import io.github.yangentao.xlog.logd
import jakarta.servlet.ServletContext
import jakarta.servlet.ServletRequest
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import jakarta.websocket.Endpoint
import jakarta.websocket.server.ServerContainer
import jakarta.websocket.server.ServerEndpointConfig
import kotlin.reflect.KClass

//fun ServletContext.addFilter(name: String, cls: KClass<out Filter>, patternUrl: String) {
//    val r: FilterRegistration.Dynamic = this.addFilter(name, cls.java)
//    r.addMappingForUrlPatterns(EnumSet.of(DispatcherType.REQUEST), false, patternUrl)
//    r.setAsyncSupported(true)
//}

val ServletRequest.isMultipart: Boolean get() = (this.contentType ?: "").contains("multipart/", ignoreCase = true)
internal fun HttpServletResponse.setHeaderContentDisposition(filename: String) {
    this.setHeader(HttpHeader.CONTENT_DISPOSITION, "attachment;filename=${filename.encodedURL()}")
}

internal fun trace(request: HttpServletRequest, response: HttpServletResponse) {
    val CRLF = "\r\n"
    val buffer = StringBuilder("TRACE ").append(request.requestURI).append(" ").append(request.protocol)
    val reqHeaderEnum = request.headerNames
    while (reqHeaderEnum.hasMoreElements()) {
        val headerName = reqHeaderEnum.nextElement()
        buffer.append(CRLF).append(headerName).append(": ").append(request.getHeader(headerName))
    }
    buffer.append(CRLF)
    val data = buffer.toString().toByteArray()
    response.contentType = "message/http"
    response.setContentLength(data.size)
    response.outputStream.write(data)
    response.flushBuffer()
}

internal fun options(request: HttpServletRequest, response: HttpServletResponse, options: Set<String>) {
    logd("options.")
    val ls = LinkedHashSet<String>()
    ls.addAll(options)
    if (HttpMethod.GET in options) {
        ls += HttpMethod.HEAD
    }
    ls += HttpMethod.OPTIONS
//        ls += HttpMethod.TRACE
    val methods = ls.joinToString(",")
    response.setHeader("Allow", methods)
    val origin = request.getHeader("Origin")
    if (origin != null) {
        response.setHeader("Access-Control-Allow-Origin", origin)
        response.setHeader("Access-Control-Allow-Credentials", "true")
        response.setHeader("Access-Control-Allow-Methods", methods)
        response.setHeader(
            "Access-Control-Allow-Headers",
            "Origin,Accept,Content-Type,Content-Length,X-Requested-With,Key,Token,Authorization"
        )
    }
    response.flushBuffer()
}

fun ServletContext.addWebsocket(endpoint: KClass<out Endpoint>, path: String) {
    val sec = ServerEndpointConfig.Builder.create(endpoint.java, path).build()
    addWebsocket(sec)
}

fun ServletContext.addWebsocket(config: ServerEndpointConfig) {
    serverContainer.addEndpoint(config)
}

val ServletContext.serverContainer: ServerContainer get() = this.getAttribute(ServerContainer::class.qualifiedName) as ServerContainer
