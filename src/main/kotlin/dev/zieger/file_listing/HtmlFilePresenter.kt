package dev.zieger.file_listing

import dev.zieger.utils.misc.format
import io.ktor.application.*
import io.ktor.html.*
import kotlinx.html.*

interface IFilePresenter {
    suspend fun present(listing: Listing, call: ApplicationCall, path: String, host: String)
}

class HtmlFilePresenter : IFilePresenter {

    override suspend fun present(listing: Listing, call: ApplicationCall, path: String, host: String) {
        val (directories, files) = listing.items
            .map { it.toTableItem() }
            .sortedBy { it.name }
            .groupBy { it.isDirectory }
            .run { (get(true) ?: emptyList()) to (get(false) ?: emptyList()) }

        val toParent = listOfNotNull(listing.parent?.let {
            TableItem("..", it, "", "")
        })
        val content = toParent + directories + files
        call.respondHtml {
            body {
                h1 {
                    +"Index of $path/"
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

    private fun ListItem.toTableItem(): TableItem = when (this) {
        is ListItem.Directory -> TableItem(name, path, createdAt.formatTime(), lastModifiedAt.formatTime())
        is ListItem.File -> TableItem(
            name, path, createdAt.formatTime(), lastModifiedAt.formatTime(),
            size.formatSize(), mimeType, false
        )
    }

    private fun TABLE.head() {
        thead {
            tr {
                for (column in listOf("Name", "Last Modified At", "Size", "MimeType")) {
                    th {
                        style = "width: ${
                            when (column) {
                                "Size" -> 15
                                "Name" -> 35
                                else -> (100 - 15 - 35) / 2
                            }.toInt()
                        }%; text-align: left;"
                        +column
                    }
                }
            }
        }
    }

    private fun TABLE.items(host: String, content: List<TableItem>) {
        tbody {
            for (info in content) {
                tr {
                    td {
                        a("https://$host/${info.link}".removeSuffix("/")) { +info.name }
                    }
                    td {
                        +info.lastModifiedAt
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
}

private fun Long.formatSize(): String = when {
    this / 1024 / 1024 / 1024 / 1024 > 1 -> "${(this / 1024 / 1024 / 1024 / 1024).format(1)} TB"
    this / 1024 / 1024 / 1024 > 1 -> "${(this / 1024 / 1024 / 1024).format(1)} GB"
    this / 1024 / 1024 > 1 -> "${(this / 1024 / 1024).format(1)} MB"
    this / 1024 > 1 -> "${(this / 1024).format(1)} KB"
    else -> "$this B"
}

data class TableItem(
    val name: String,
    val link: String,
    val createdAt: String,
    val lastModifiedAt: String,
    val size: String = "",
    val mimeType: String = "",
    val isDirectory: Boolean = true
)
