package data.models

import remote.CategoryInfo
import remote.ProductsRepository
import remote.RepoResult

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CategoriesViewModel(
    private val repo: ProductsRepository = ProductsRepository()
) : ViewModel() {

    private val _state = MutableStateFlow<UiState<List<CategoryInfo>>>(UiState.Loading)
    val state: StateFlow<UiState<List<CategoryInfo>>> = _state

    init {
        refresh()
    }

    fun refresh() {
        _state.value = UiState.Loading
        viewModelScope.launch {
            when (val result = repo.fetchCategories()) {
                is RepoResult.Ok  -> _state.value = UiState.Success(result.data)
                is RepoResult.Err -> _state.value = UiState.Error(result.error)
            }
        }
    }
}
