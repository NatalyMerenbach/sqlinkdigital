package remote


data class ProductsResponse(
    val products: List<Product>
)

data class Product(
    val id: Int,
    val title: String,
    val category: String,
    val price: Double,
    val stock: Int,
    val thumbnail: String,
)

data class CategoryInfo(
    val name: String,
    val thumbnail: String,
    val productCount: Int,
    val totalStock: Int
)
