package ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import data.models.CategoryDetailsViewModel
import remote.Product

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryDetailsScreen(
    category: String,
) {
    val categoryDetailsViewModel = remember(category) { CategoryDetailsViewModel(category) }
    val state by categoryDetailsViewModel.state.collectAsState()

    Scaffold(topBar = { TopAppBar(title = { Text(category) }) }) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when (val s = state) {
                is data.models.UiState.Error ->  ErrorPane(
                    error = s.error,
                    onRetry = categoryDetailsViewModel::load,
                    modifier = Modifier.align(Alignment.Center)
                )
                is data.models.UiState.Loading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                is data.models.UiState.Success -> ProductList(s.data)
            }
        }
    }
}

@Composable
private fun ProductList(products: List<Product>) {
    LazyColumn(
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(products, key = { it.id }) { p ->
            ProductRow(p)
        }
    }
}

@Composable
private fun ProductRow(p: Product) {
    Card {
        Row(Modifier.padding(12.dp)) {
            AsyncImage(
                model = p.thumbnail,
                contentDescription = p.title,
                modifier = Modifier.size(84.dp)
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(p.title, fontWeight = FontWeight.Bold)
                Text("Price: $${"%.2f".format(p.price)}")
                Text("Stock: ${p.stock}")
            }
        }
    }
}
