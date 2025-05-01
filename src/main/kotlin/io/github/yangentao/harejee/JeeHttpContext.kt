package io.github.yangentao.harejee


import io.github.yangentao.hare.HttpApp
import io.github.yangentao.hare.HttpContext
import io.github.yangentao.hare.HttpResult
import io.github.yangentao.hare.utils.UriPath
import io.github.yangentao.httpbasic.*
import io.github.yangentao.types.appendAll
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import java.io.File
import java.nio.file.Files
import kotlin.collections.iterator
import kotlin.io.path.pathString

class JeeHttpContext(override val app: HttpApp, val request: HttpServletRequest, val response: HttpServletResponse, override val routePath: UriPath) : HttpContext() {

    override val method: String = request.method.uppercase()
    override val requestUri: String = request.requestURI
    override val queryString: String? = request.queryString
    override val removeAddress: String = request.remoteAddr
    override val commited: Boolean get() = response.isCommitted

    init {
        parseParams()
    }

    fun parseParams() {
        for ((k, v) in request.parameterMap) {
            paramMap.appendAll(k.substringBefore('['), v.toList())
        }
        if (request.isMultipart) {
            for (p in request.parts) {
                val fileName = p.submittedFileName ?: continue
                val path = Files.createTempFile(null, null)
                val file = File(path.pathString)
                p.write(file.canonicalPath)
                val item = HttpFileParam(p.name, HttpFile(file, fileName, p.contentType ?: Mimes.ofFile(fileName)))
                this.fileUploads.add(item)
            }
        }
    }

    override val requestContent: ByteArray? by lazy { request.inputStream.readAllBytes() }
    override fun requestHeader(name: String): String? {
        return request.getHeader(name)
    }

    override fun responseHeader(name: String, value: Any) {
        response.setHeader(name, value.toString())
    }

    fun mayCcommit() {
        if (!this.response.isCommitted) {
            this.response.flushBuffer()
        }
    }

    override fun send(result: HttpResult) {
        response.status = result.status.code
        for ((k, v) in result.headers) {
            response.setHeader(k, v)
        }
        if (!result.isEmptyContent) {
            if (!result.containsHeader(HttpHeader.CONTENT_LENGTH)) {
                response.setContentLength(result.contentLength)
            }
            response.outputStream.write(result.content!!)
        }
        response.flushBuffer()
    }

    override fun sendError(status: HttpStatus) {
        response.sendError(status.code, status.reason)
    }

    override fun sendFile(httpFile: HttpFile, attachment: Boolean) {
        StaticFile(this, httpFile, attach = attachment).send()
    }

}