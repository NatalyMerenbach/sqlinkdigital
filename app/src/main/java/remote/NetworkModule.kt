package remote

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import remote.errors.ApiErrorParser
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object NetworkModule {
    private const val BASE_URL = "https://dummyjson.com/"

    private val client = OkHttpClient.Builder()
//        .addInterceptor(logging)
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()

    private val gson: Gson = GsonBuilder()
        .serializeNulls()
        .create()

    val api: ProductsApi = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()
        .create(ProductsApi::class.java)

    val errorParser = ApiErrorParser(gson)
}
