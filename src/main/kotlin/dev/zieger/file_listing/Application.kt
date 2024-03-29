package dev.zieger.file_listing

import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.util.pipeline.*
import kotlinx.css.CssBuilder
import org.apache.log4j.BasicConfigurator
import java.io.File

fun main(args: Array<String>): Unit {
    BasicConfigurator.configure()

    val port = args.indexOfFirst { it == "--port" }.let { if (it < 0) null else it }
        ?.let { args.getOrNull(it + 1) }
        ?.toIntOrNull()
        ?: 9000
    val path = args.indexOfFirst { it == "--path" }.let { if (it < 0) null else it }
        ?.let { args.getOrNull(it + 1)?.trim() }
        ?: "."
    val host = args.indexOfFirst { it == "--host" }.let { if (it < 0) null else it }
        ?.let { args.getOrNull(it + 1)?.trim() }
        ?: "localhost:$port"
    val hostPath = args.indexOfFirst { it == "--hostPath" }.let { if (it < 0) null else it }
        ?.let { args.getOrNull(it + 1)?.trim() }
        ?: ""

    embeddedServer(Netty, port = port) {
        install(DefaultHeaders)
        install(CallLogging)

        val rootFile = File(File(path).absolutePath)
        val provider = FileProvider(rootFile)
        val jsonPresenter = JsonFilePresenter()
        val htmlPresenter = HtmlFilePresenter()

        routing {
            get("/files/styles.css") {
                call.respondCss {
                    htmlPresenter.buildCss(this)
                }
            }

            route("/") {
                accept(ContentType.Application.Json) {
                    route(path, host, hostPath, rootFile, provider, jsonPresenter)
                }
                accept(ContentType.Text.Html) {
                    route(path, host, hostPath, rootFile, provider, htmlPresenter)
                }
            }
        }
    }.start(wait = true)
}

private fun Route.route(
    path: String,
    host: String,
    hostPath: String,
    rootFile: File,
    provider: FileProvider,
    presenter: IFilePresenter
) {
    forPath { p ->
        println("request for \"$p\"")
        val pFile = File(File(path + File.separatorChar + p.removePrefix(hostPath)).absolutePath)
        if (!pFile.absolutePath.contains(rootFile.absolutePath)) return@forPath

        when {
            pFile.isDirectory -> {
                presenter.present(provider.provide(pFile), call, p, host)
            }
            pFile.mimeType?.isText == true -> call.respondText(pFile.readText())
            else -> {
                call.response.header(
                    HttpHeaders.ContentDisposition,
                    ContentDisposition.Attachment.withParameter(
                        ContentDisposition.Parameters.FileName,
                        pFile.name
                    ).withParameter(
                        ContentDisposition.Parameters.Size,
                        "%d".format(pFile.length())
                    ).toString()
                )
                call.respondFile(pFile)
            }
        }
    }
}

private val String.isText: Boolean
    get() = when {
        startsWith("text")
                || endsWith("x-sh")
                || endsWith("x-bat")
                || endsWith("xml") -> true
        else -> false
    }


private fun Route.forPath(block: suspend PipelineContext<Unit, ApplicationCall>.(String) -> Unit) {
    val pathParameterName = "static-content-path-parameter"
    get("{$pathParameterName...}") {
        val relativePath = call.parameters.getAll(pathParameterName)?.joinToString(File.separator) ?: return@get
        block(relativePath)
    }
}

private suspend inline fun ApplicationCall.respondCss(builder: CssBuilder.() -> Unit) {
    this.respondText(CssBuilder().apply(builder).toString(), ContentType.Text.CSS)
}
