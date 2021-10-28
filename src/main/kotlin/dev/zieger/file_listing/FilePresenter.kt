package dev.zieger.file_listing

import dev.zieger.utils.misc.format

class FilePresenter {

    fun present(listing: Listing): List<TableItem> {
        val (directories, files) = listing.items
            .map { it.toTableItem() }
            .sortedBy { it.name }
            .groupBy { it.isDirectory }
            .run { (get(true) ?: emptyList()) to (get(false) ?: emptyList()) }

        val toParent = listOfNotNull(listing.parent?.let {
            TableItem("..", it, "")
        })
        return toParent + directories + files
    }

    private fun ListItem.toTableItem(): TableItem = when (this) {
        is ListItem.Directory -> TableItem(name, path, lastModifiedAt.formatTime())
        is ListItem.File -> TableItem(
            name, path, createdAt.formatTime(), lastModifiedAt.formatTime(),
            size.formatSize(), type, false
        )
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
