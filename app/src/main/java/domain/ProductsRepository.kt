package domain

import data.errors.AppError


sealed interface RepoResult<out T> {
    data class Ok<T>(val data: T) : RepoResult<T>
    data class Err(val error: AppError) : RepoResult<Nothing>
}

interface ProductsRepository {

    /**
     * Fetches all products from the API and converts the response into RepoResult.
     */
    suspend fun fetchAllProducts(): RepoResult<List<Product>>


    /**
     * Fetches all products and transforms them into a list of CategoryInfo objects.
     */
    suspend fun fetchCategories(): RepoResult<List<CategoryInfo>>


    suspend fun productsByCategory(category: String): RepoResult<List<Product>>
}

