package dev.zieger.file_listing

import dev.zieger.utils.time.timeSerializerModule
import io.ktor.application.*
import io.ktor.response.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class JsonFilePresenter : IFilePresenter {

    private val json = Json {
        serializersModule = timeSerializerModule
        prettyPrint = true
    }

    override suspend fun present(listing: Listing, call: ApplicationCall, path: String, host: String) {
        call.respondText(json.encodeToString(listing))
    }
}