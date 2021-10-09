package dev.zieger.file_listing

import dev.zieger.utils.time.ITimeStamp
import dev.zieger.utils.time.TimeStamp
import dev.zieger.utils.time.toTime
import io.ktor.http.*
import org.apache.tika.Tika
import java.io.File
import java.net.URLConnection
import java.nio.file.Files

class FileProvider(private val rootFile: File) {

    fun provide(file: File): Listing {
        if (!file.exists())
            throw IllegalArgumentException("$file does not exist")
        if (!file.isDirectory)
            throw IllegalArgumentException("$file is not a directory")

        val isRoot = file.absoluteFile == rootFile.absoluteFile
        val parent = if (isRoot) null else File(file.parent).toRelativeString(rootFile)
        return Listing(parent, file.listFiles()?.map { it.toListItem() } ?: emptyList())
    }

    private fun File.toListItem(): ListItem = when {
        isDirectory -> ListItem.Directory(toRelativeString(rootFile), name, TimeStamp())
        else -> ListItem.File(
            toRelativeString(rootFile), name, lastModified().toTime(),
            mimeType ?: "-",
            length()
        )
    }
}

val File.mimeType: String?
    get() = Tika().detect(this)
        ?: ContentType.fromFilePath(absolutePath).firstOrNull()?.toString()
        ?: Files.probeContentType(toPath())
        ?: URLConnection.guessContentTypeFromName(name)

data class Listing(val parent: String?, val items: List<ListItem>)

sealed class ListItem {

    abstract val path: String
    abstract val name: String
    abstract val lastChange: ITimeStamp

    data class File(
        override val path: String,
        override val name: String,
        override val lastChange: ITimeStamp,
        val type: String,
        val size: Long
    ) : ListItem()

    data class Directory(
        override val path: String,
        override val name: String,
        override val lastChange: ITimeStamp
    ) : ListItem()
}