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


    /**
     * Fetches all products and transforms them into a list of CategoryInfo objects.
     */
    suspend fun fetchCategories(): RepoResult<List<CategoryInfo>> {
        // Call another function that fetches all products
        return when (val result = fetchAllProducts()) {

            is RepoResult.Ok -> {
                val products = result.data

                // Group products by their category
                val categories = products
                    .groupBy { it.category }
                    .map { (categoryName, categoryItems) ->

                        // Create CategoryInfo for each category
                        CategoryInfo(
                            name = categoryName,
                            thumbnail = categoryItems.firstOrNull()?.thumbnail.orEmpty(),
                            productCount = categoryItems.distinctBy { it.id }.size,
                            totalStock = categoryItems.sumOf { it.stock }
                        )
                    }
                    // Sort categories alphabetically by name
                    .sortedBy { it.name }

                RepoResult.Ok(categories)
            }

            //Error case – just pass the error forward
            is RepoResult.Err -> result
        }
    }


    suspend fun productsByCategory(category: String): RepoResult<List<Product>> = when (val result = fetchAllProducts()) {
        is RepoResult.Ok -> RepoResult.Ok(result.data.filter { it.category == category })
        is RepoResult.Err -> result
    }
}
