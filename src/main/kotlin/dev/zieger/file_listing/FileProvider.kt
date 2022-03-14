package dev.zieger.file_listing

import dev.zieger.utils.time.ITimeStamp
import dev.zieger.utils.time.toTime
import io.ktor.http.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.apache.tika.Tika
import java.io.File
import java.net.URLConnection
import java.nio.file.Files
import java.nio.file.attribute.BasicFileAttributes
import java.util.concurrent.TimeUnit


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
        isDirectory -> ListItem.Directory(toRelativeString(rootFile).removeSuffix("/"), name, createdAt, lastModifierAt)
        else -> ListItem.File(
            toRelativeString(rootFile).removeSuffix("/"), name, createdAt, lastModifierAt,
            mimeType ?: "-", length()
        )
    }

    private val File.lastModifierAt: ITimeStamp
        get() = Files.readAttributes(toPath(), BasicFileAttributes::class.java)
            .lastModifiedTime().to(TimeUnit.MILLISECONDS).toTime()

    private val File.createdAt: ITimeStamp
        get() = Files.readAttributes(toPath(), BasicFileAttributes::class.java)
            .creationTime().to(TimeUnit.MILLISECONDS).toTime()

    private val File.filesRecursive: List<File>
        get() = if (isDirectory) listFiles()?.flatMap { it.filesRecursive } ?: emptyList() else listOf(this)
}

val File.mimeType: String?
    get() = Tika().detect(this)
        ?: ContentType.fromFilePath(absolutePath).firstOrNull()?.toString()
        ?: Files.probeContentType(toPath())
        ?: URLConnection.guessContentTypeFromName(name)

@Serializable
@SerialName("Listing")
data class Listing(val parent: String?, val items: List<ListItem>)

@Serializable
sealed class ListItem {

    abstract val path: String
    abstract val name: String
    abstract val createdAt: ITimeStamp
    abstract val lastModifiedAt: ITimeStamp

    @Serializable
    @SerialName("File")
    data class File(
        override val path: String,
        override val name: String,
        override val createdAt: ITimeStamp,
        override val lastModifiedAt: ITimeStamp,
        val mimeType: String,
        val size: Long
    ) : ListItem()

    @Serializable
    @SerialName("Directory")
    data class Directory(
        override val path: String,
        override val name: String,
        override val createdAt: ITimeStamp,
        override val lastModifiedAt: ITimeStamp
    ) : ListItem()
}