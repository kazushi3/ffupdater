package de.marmaro.krt.ffupdater.network

import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.RequestBody
import kotlin.reflect.KClass


/**
 * Consume a REST-API from the internet.
 */
class ApiConsumer {
    private val gson = Gson()

    suspend fun consume(
        url: String,
        fileDownloader: FileDownloader,
        method: String = "GET",
        requestBody: RequestBody? = null,
    ): String {
        return fileDownloader.downloadSmallFileAsync(url, method, requestBody)
    }

    suspend fun <T : Any> consume(
        url: String,
        fileDownloader: FileDownloader,
        clazz: KClass<T>,
    ): T {
        return withContext(Dispatchers.IO) {
            val stringResponse = consume(url, fileDownloader)
            gson.fromJson(stringResponse, clazz.java) // in IO thread because it could be a lot of work
        }
    }

    companion object {
        val INSTANCE = ApiConsumer()
    }
}