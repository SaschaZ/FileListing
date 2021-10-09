package dev.zieger.file_listing

import io.ktor.application.*
import io.ktor.features.*
import io.ktor.html.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.util.pipeline.*
import kotlinx.html.*
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
        val presenter = FilePresenter()

        routing {
            route("/") {
                forPath { p ->
                    println("request for \"$p\"")
                    val pFile = File(File(path + File.separatorChar + p.removePrefix(hostPath)).absolutePath)
                    if (!pFile.absolutePath.contains(rootFile.absolutePath)) return@forPath

                    when {
                        pFile.isDirectory -> {
                            val content = presenter.present(provider.provide(pFile))
                            call.respondHtml {
                                body {
                                    h1 {
                                        +"Index of $p/"
                                    }
                                    hr {}
                                    table {
                                        style = "width: 100%;"
                                        head()
                                        items(host, content)
                                    }
                                    hr {}
                                }
                            }
                        }
                        pFile.mimeType?.isText == true -> call.respondText(pFile.readText())
                        else -> call.respondFile(pFile)
                    }
                }
            }
        }
    }.start(wait = true)
}

private val String.isText: Boolean
    get() = when {
        startsWith("text")
                || endsWith("x-sh")
                || endsWith("x-bat")
                || endsWith("xml") -> true
        else -> false
    }

private fun TABLE.items(host: String, content: List<TableItem>) {
    tbody {
        for (info in content) {
            tr {
                td {
                    a("http://$host/${info.link}".removeSuffix("/")) { +info.name }
                }
                td {
                    +info.lastModified
                }
                td {
                    +info.size
                }
                td {
                    +info.mimeType
                }
            }
        }
    }
}

private fun TABLE.head() {
    thead {
        tr {
            for (column in listOf("Name", "Last Modified", "Size", "MimeType")) {
                th {
                    style = "width: ${
                        when (column) {
                            "Size" -> 15
                            "Name" -> 45
                            else -> (100 - 15 - 45) / 2
                        }.toInt()
                    }%; text-align: left;"
                    +column
                }
            }
        }
    }
}

private fun Route.forPath(block: suspend PipelineContext<Unit, ApplicationCall>.(String) -> Unit) {
    val pathParameterName = "static-content-path-parameter"
    get("{$pathParameterName...}") {
        val relativePath = call.parameters.getAll(pathParameterName)?.joinToString(File.separator) ?: return@get
        block(relativePath)
    }
}
