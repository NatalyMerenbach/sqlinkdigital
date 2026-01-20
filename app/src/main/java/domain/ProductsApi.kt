package domain


import retrofit2.http.GET
import retrofit2.http.Query

interface ProductsApi {
    @GET("products")
    suspend fun getProducts(@Query("limit") limit: Int = 100): ProductsResponse
}

