package data.errors

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import okhttp3.ResponseBody
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException

data class ApiError(val message: String? = null)


class ApiErrorParser(private val gson: Gson) {
    fun parse(body: ResponseBody?): ApiError? = try {
        body?.charStream()?.use { gson.fromJson(it, ApiError::class.java) }
    } catch (_: Exception) { null }
}

sealed class AppError(
    override val message: String? = null,
    override val cause: Throwable? = null
) : Exception(message, cause) {

    data class HttpReasonError(
        val code: Int,
        val reason: String?,         // raw HttpException message
        val apiMessage: String?      // parsed body message
    ) : AppError("HTTP $code: ${apiMessage ?: reason}")

    data object Network : AppError("Network error")
    data object Timeout : AppError("Request timed out")
    data class Serialization(val details: String) : AppError("Serialization error: $details")
    data class Unknown(val details: String?) : AppError(details)
}
/**
 * Safely executes a suspend API call and wraps the result in a Result<T>.
 *
 * Handles common network and serialization errors and maps them to AppError.
 */
suspend inline fun <T> apiCall(
    crossinline request: suspend () -> T,
    errorParser: ApiErrorParser
): Result<T> {
    return try {
        // Run the API call and wrap the result as success
        val response = request()
        Result.success(response)

    } catch (httpError: HttpException) {
        // Handle HTTP errors (e.g. 400, 500, etc.)
        val errorBody = httpError.response()?.errorBody()
        val parsedMessage = errorParser.parse(errorBody)?.message

        Result.failure(
            AppError.HttpReasonError(
                code = httpError.code(),
                reason = httpError.message(),
                apiMessage = parsedMessage
            )
        )

    } catch (timeout: SocketTimeoutException) {
        // Request timed out
        Result.failure(AppError.Timeout)

    } catch (networkError: IOException) {
        // No internet, DNS fail, etc.
        Result.failure(AppError.Network)

    } catch (jsonError: JsonSyntaxException) {
        // Response was malformed or mismatched
        Result.failure(AppError.Serialization(jsonError.localizedMessage ?: "Invalid JSON"))

    } catch (unknown: Throwable) {
        // Any other unknown error
        Result.failure(AppError.Unknown(unknown.localizedMessage))
    }
}

