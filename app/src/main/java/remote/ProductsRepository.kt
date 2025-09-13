package remote

import remote.errors.AppError
import remote.errors.apiCall
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


sealed interface RepoResult<out T> {
    data class Ok<T>(val data: T) : RepoResult<T>
    data class Err(val error: AppError) : RepoResult<Nothing>
}

class ProductsRepository {
    private val api = NetworkModule.api
    private val parser = NetworkModule.errorParser

    private suspend fun <T> io(block: suspend () -> T) =
        withContext(Dispatchers.IO) { block() }

    suspend fun fetchAllProducts(): RepoResult<List<Product>> = io {
        val res = apiCall({ api.getProducts().products }, parser) // returns Result<T>
        res.fold(
            onSuccess = { RepoResult.Ok(it) },
            onFailure = { err ->
                val appErr = err as? AppError ?: AppError.Unknown(err.message)
                RepoResult.Err(appErr)
            }
        )
    }


    suspend fun categories(): RepoResult<List<CategoryInfo>> = when (val result = fetchAllProducts()) {
        is RepoResult.Ok -> {
            val all = result.data
            val categories = all.groupBy { it.category }
                .map { (category, items) ->
                    CategoryInfo(
                        name = category,
                        firstThumbnail = items.firstOrNull()?.thumbnail.orEmpty(),
                        productCount = items.distinctBy { it.id }.size,
                        totalStock = items.sumOf { it.stock }
                    )
                }.sortedBy { it.name }
            RepoResult.Ok(categories)
        }
        is RepoResult.Err -> result
    }

    suspend fun productsByCategory(category: String): RepoResult<List<Product>> = when (val result = fetchAllProducts()) {
        is RepoResult.Ok -> RepoResult.Ok(result.data.filter { it.category == category })
        is RepoResult.Err -> result
    }
}
