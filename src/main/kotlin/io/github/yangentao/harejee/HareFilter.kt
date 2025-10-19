package io.github.yangentao.harejee

import io.github.yangentao.hare.HttpApp
import io.github.yangentao.hare.TargetRouterAction
import io.github.yangentao.hare.utils.ensureDirs
import io.github.yangentao.hare.utils.istart
import io.github.yangentao.harejee.ws.HareEndpoint
import io.github.yangentao.harejee.ws.HareSession
import io.github.yangentao.httpbasic.HttpMethod
import io.github.yangentao.types.printX
import io.github.yangentao.xlog.loge
import jakarta.servlet.*
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import jakarta.websocket.Endpoint
import java.io.File
import kotlin.reflect.KClass

// tomcatXX/conf/context.xml
// <Context allowCasualMultipartParsing="true">...</Context>
// https://docs.oracle.com/javaee/7/tutorial/servlets011.htm

//@MultipartConfig
//@WebFilter(urlPatterns = ["/*"], asyncSupported = true )
abstract class HareFilter : Filter {
    var timeoutSeconds: Long = 15
    lateinit var filterConfig: FilterConfig
    val servletContext: ServletContext get() = filterConfig.servletContext
    val contextPath: String get() = servletContext.contextPath
    val dirApp: File by lazy { File(servletContext.getRealPath("/")) }
    open val appName: String get() = filterConfig.getInitParameter("appname") ?: "APP"
    lateinit var app: HttpApp

    override fun init(filterConfig: FilterConfig) {
        this.filterConfig = filterConfig
        filterConfig.dump()
        app = HttpApp(contextPath, appName, work = filterConfig.dirWork(), dirWeb = dirApp)

        onCreate()
    }

    // 跟Filter的urlPattern没有父子关系,  只跟contextPath有关系. =>  /contextPath/echo/{ident}
    // websocket( "/echo/{ident}", EchoEndpoint::class)
    fun websocket(uri: String, endpoint: KClass<out Endpoint>) {
        servletContext.addWebsocket(endpoint, uri)
    }

    fun websocketSession(uri: String, session: KClass<out HareSession>) {
        HareEndpoint.sessionClass = session
        servletContext.addWebsocket(HareEndpoint::class, uri)
    }

    abstract fun onCreate()

    abstract fun onDestory()

    override fun destroy() {
        app.destroy()
        onDestory()
    }

    override fun doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
        if (request !is HttpServletRequest || response !is HttpServletResponse || !request.scheme.startsWith("http", ignoreCase = true)) {
            chain.doFilter(request, response)
            return
        }
        val method = request.method.uppercase()
        if (method == HttpMethod.TRACE) {
            trace(request, response)
            return
        }

        if (!request.requestURI.istart(contextPath)) {
            chain.doFilter(request, response)
            return
        }
        val p: TargetRouterAction? = app.findRouter(request.requestURI)
        if (p == null) {
            chain.doFilter(request, response)
            return
        }
        if (method == HttpMethod.OPTIONS) {
            options(request, response, p.action.methods)
            return
        }
        if (!p.checkMethods(method)) {
            response.sendError(403)// forbidden
            return
        }

        request.characterEncoding = "UTF-8"
        val context = JeeHttpContext(app, request, response, p.routePath)
        context.cors()
        if (!request.isAsyncSupported) {
            p.process(context)
            return
        }
        val ctx: AsyncContext = request.startAsync()
        ctx.timeout = timeoutSeconds * 1000
        ctx.addListener(JeeAsyncListener(context))
        ctx.start {
            p.process(context)
            ctx.complete()
        }
    }

}

private fun FilterConfig.dump() {
    printX("params:")
    for (name in this.initParameterNames) {
        printX(name, " : ", this.getInitParameter(name))
    }
    printX("attrs:")
    for (name in this.servletContext.attributeNames) {
        printX(name, " : ", this.servletContext.getAttribute(name))
    }
}

private class JeeAsyncListener(val context: JeeHttpContext) : AsyncListener {
    override fun onComplete(event: AsyncEvent) {

    }

    override fun onTimeout(event: AsyncEvent) {
        loge("请求超时, request timeout: ", context.requestUri)
    }

    override fun onError(event: AsyncEvent) {
        context.app.error(context, event.throwable)
    }

    override fun onStartAsync(event: AsyncEvent) {
    }
}

fun FilterConfig.dirApp(): File {
    return File(this.servletContext.getRealPath("/"))
}

fun FilterConfig.dirWork(): File {
    return File(dirApp().parentFile, servletContext.contextPath.trim('/') + "_work").ensureDirs()
}