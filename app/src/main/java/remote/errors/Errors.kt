package remote.errors

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import okhttp3.ResponseBody
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException

data class ApiError(val message: String? = null)

sealed class AppError(
    override val message: String? = null,
    override val cause: Throwable? = null
) : Exception(message, cause) {

    data class Http(
        val code: Int,
        val reason: String?,         // raw HttpException message
        val apiMessage: String?      // parsed body message
    ) : AppError("HTTP $code: ${apiMessage ?: reason}")

    data object Network : AppError("Network error")
    data object Timeout : AppError("Request timed out")
    data class Serialization(val details: String) : AppError("Serialization error: $details")
    data class Unknown(val details: String?) : AppError(details)
}

class ApiErrorParser(private val gson: Gson) {
    fun parse(body: ResponseBody?): ApiError? = try {
        body?.charStream()?.use { gson.fromJson(it, ApiError::class.java) }
    } catch (_: Exception) { null }
}

suspend inline fun <T> apiCall(
    crossinline block: suspend () -> T,
    parser: ApiErrorParser
): Result<T> = try {
    Result.success(block())
} catch (e: HttpException) {
    val apiMsg = parser.parse(e.response()?.errorBody())?.message
    Result.failure(AppError.Http(e.code(), e.message(), apiMsg))
} catch (e: SocketTimeoutException) {
    Result.failure(AppError.Timeout)
} catch (e: IOException) {
    Result.failure(AppError.Network)
} catch (e: JsonSyntaxException) {
    Result.failure(AppError.Serialization(e.localizedMessage ?: "JSON error"))
} catch (e: Throwable) {
    Result.failure(AppError.Unknown(e.localizedMessage))
}
