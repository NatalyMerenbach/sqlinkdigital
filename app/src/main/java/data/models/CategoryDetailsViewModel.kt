package data.models

import remote.ProductsRepository
import remote.RepoResult

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import remote.Product
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CategoryDetailsViewModel(
    private val category: String,
    private val repo: ProductsRepository = ProductsRepository()
) : ViewModel() {

    private val _state = MutableStateFlow<UiState<List<Product>>>(UiState.Loading)
    val state: StateFlow<UiState<List<Product>>> = _state

    init { load() }

    fun load() {
        _state.value = UiState.Loading
        viewModelScope.launch {
            when (val r = repo.productsByCategory(category)) {
                is RepoResult.Ok  -> _state.value = UiState.Success(r.data)
                is RepoResult.Err -> _state.value = UiState.Error(r.error)
            }
        }
    }
}
