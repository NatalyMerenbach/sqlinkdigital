package ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import domain.Product

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryDetailsScreen(
    category: String,
    onBack: () -> Unit
) {
    val categoryDetailsViewModel = remember(category) { CategoryDetailsViewModel(category) }
    val state by categoryDetailsViewModel.state.collectAsState()

    Scaffold(topBar = {
        TopAppBar(title = { Text(category) }, navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back"
                )
            }
        })
    }) { padding ->
        Box(Modifier
            .fillMaxSize()
            .padding(padding)) {
            when (val s = state) {
                is UiState.Error -> ErrorPane(
                    error = s.error,
                    onRetry = categoryDetailsViewModel::load,
                    modifier = Modifier.align(Alignment.Center)
                )

                is UiState.Loading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                is UiState.Success -> ProductList(s.data)
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
