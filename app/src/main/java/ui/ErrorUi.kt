package ui

import data.errors.AppError

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

fun errorMessageOf(error: AppError): String = when (error) {
    is AppError.HttpReasonError -> {
        val body = error.apiMessage?.takeIf { it.isNotBlank() }
        "Server error (${error.code})" +
                (body?.let { ": $it" } ?: error.reason?.let { ": $it" }.orEmpty())
    }
    AppError.Network           -> "No internet connection. Check your network and try again."
    AppError.Timeout           -> "The request timed out. Please retry."
    is AppError.Serialization  -> "Data error: ${error.details}"
    is AppError.Unknown        -> "Unexpected error: ${error.details ?: "unknown"}"
}


@Composable
fun ErrorPane(
    error: AppError,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    val msg = errorMessageOf(error)
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Something went wrong", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text(msg)
        Spacer(Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = onRetry) { Text("Retry") }
            //OutlinedButton(onClick = { /* you can navigate to a help screen if you want */ }) { Text("Help") }
        }
    }
}

/** Optional: one-line snackbar trigger you can call when switching to Error state */
@Composable
fun ErrorSnackbarHost(message: String): SnackbarHostState {
    val host = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    LaunchedEffect(message) {
        if (message.isNotBlank()) scope.launch { host.showSnackbar(message) }
    }
    return host
}
