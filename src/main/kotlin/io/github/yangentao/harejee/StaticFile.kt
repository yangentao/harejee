package io.github.yangentao.harejee

import io.github.yangentao.hare.FileRange
import io.github.yangentao.hare.utils.closeSafe
import io.github.yangentao.hare.utils.uuidString
import io.github.yangentao.httpbasic.HttpFile
import io.github.yangentao.httpbasic.HttpHeader
import io.github.yangentao.httpbasic.HttpStatus
import jakarta.servlet.ServletOutputStream
import jakarta.servlet.http.HttpServletResponse
import java.io.File

// https://developer.mozilla.org/zh-CN/docs/Web/HTTP/Reference/Status/206
internal class StaticFile(
    val context: JeeHttpContext,
    val httpFile: HttpFile,
    private val attach: Boolean = false
) {
    val file: File get() = httpFile.file
    private val filename: String get() = httpFile.filename
    private val mime: String get() = httpFile.mime

    private val fileLength: Long = file.length()
    private val lastModified: Long = file.lastModified() / 1000 * 1000
    private val etag: String = "$fileLength-$lastModified"
    private val isText: Boolean = mime.startsWith("text") || mime.endsWith("xml") || mime.contains("/javascript")

    private val ranges: List<FileRange> = context.headerRanges(etag, lastModified, fileLength)

    fun send() {
        sendFile()
        context.mayCcommit()
    }

    private fun sendFile() {
        if (!file.exists() || !file.isFile) {
            return context.sendError(HttpStatus.NOT_FOUND)
        }
        val resp = context.response
        resp.contentType = this.mime


        if (!context.ifMatchEtag(etag)) {
            if (context.requestHeader(HttpHeader.RANGE) != null) {
                context.sendError(HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE)//416
            } else {
                context.sendError(HttpStatus.PRECONDITION_FAILED)//412
            }
            return
        }
        resp.setDateHeader(HttpHeader.LAST_MODIFIED, lastModified)
        resp.setHeader(HttpHeader.ETAG, "\"$etag\"")
        if (!context.ifModifiedSince(lastModified) || !context.ifNoneMatch(etag)) { // 304
            resp.setContentLengthLong(fileLength)
            resp.status = HttpServletResponse.SC_NOT_MODIFIED
            resp.flushBuffer()
            return
        }

        if (attach) {
            resp.setHeaderContentDisposition(filename)
        }
        if (ranges.isEmpty()) {
            outputFull(resp, file)
        } else if (ranges.size == 1) {
            outputRange(resp, ranges.first())
        } else {
            outputAllRange(resp, mime, ranges)
        }

    }

    private fun outputFull(r: HttpServletResponse, file: File) {
        r.setContentLengthLong(fileLength)
        val os = r.outputStream
        file.inputStream().use {
            it.copyTo(os)
        }
        os.close()
    }

    private fun outputAllRange(r: HttpServletResponse, contentType: String, ranges: List<FileRange>) {
        r.status = 206
        r.contentType = "multipart/byteranges; boundary=$BOUNDARY"
        r.bufferSize = BUF_SIZE
        val os = r.outputStream
        for (range in ranges) {
            os.println()
            os.println("--$BOUNDARY")
            os.println("Content-Type: $contentType")
            os.println("Content-Range: bytes ${range.start}-${range.end}/$fileLength")
            os.println()
            copyRange(file, os, range)
        }
        os.println()
        os.println("--$BOUNDARY--")
        os.close()
    }

    private fun outputRange(r: HttpServletResponse, range: FileRange) {
        r.status = 206
        r.setContentLengthLong(range.size)
        r.setHeader(HttpHeader.CONTENT_RANGE, "bytes ${range.start}-${range.end}/$fileLength")
        val os = r.outputStream
        copyRange(file, os, range)
        os.close()
    }

    companion object {
        private val BOUNDARY = uuidString()
        private const val BUF_SIZE = 4096

        private fun copyRange(file: File, outStream: ServletOutputStream, range: FileRange) {
            if (range.size == 0L) return
            val instream = file.inputStream()
            try {
                if (range.start > 0L) {
                    val skiped = instream.skip(range.start)
                    if (skiped < range.start) error("Range Error: [${range.start},${range.end}]")
                }
                var bytesCopied: Long = 0
                val buffer = ByteArray(BUF_SIZE)
                var bytes = instream.read(buffer)
                while (bytes >= 0) {
                    val left = range.size - bytesCopied
                    if (left >= bytes) {
                        outStream.write(buffer, 0, bytes)
                        bytesCopied += bytes
                    } else if (left > 0) {
                        outStream.write(buffer, 0, left.toInt())
                        bytesCopied += left
                        break
                    } else break
                    bytes = instream.read(buffer)
                }
            } finally {
                instream.closeSafe()
            }
        }
    }
}