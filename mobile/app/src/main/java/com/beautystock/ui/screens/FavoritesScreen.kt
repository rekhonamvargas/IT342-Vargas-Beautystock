package com.beautystock.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.beautystock.data.api.RetrofitClient
import com.beautystock.data.model.ProductDTO
import com.beautystock.ui.components.ExpiryChip
import com.beautystock.ui.components.ExpiryStatus
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(onProductClick: (Long) -> Unit) {
    var favorites by remember { mutableStateOf<List<ProductDTO>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()
    val colorScheme = MaterialTheme.colorScheme

    LaunchedEffect(Unit) {
        try {
            val res = RetrofitClient.api.getProducts()
            if (res.isSuccessful) favorites = (res.body() ?: emptyList()).filter { it.isFavorite }
        } catch (_: Exception) {}
        loading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "My Favorites",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "${favorites.size} favorite products",
                            style = MaterialTheme.typography.bodySmall,
                            color = colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = colorScheme.surface)
            )
        },
        containerColor = colorScheme.background
    ) { padding ->
        if (loading) {
            Box(
                Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = colorScheme.primary)
            }
        } else if (favorites.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize().padding(padding),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("\u2764\uFE0F", style = MaterialTheme.typography.displayMedium)
                Spacer(Modifier.height(10.dp))
                Text(
                    "No favorites yet",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onSurface
                )
                Text(
                    "Products you love will appear here",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(favorites) { product ->
                    FavoriteListItem(
                        product = product,
                        onClick = { onProductClick(product.id) },
                        onRemove = {
                            scope.launch {
                                try {
                                    RetrofitClient.api.removeFavorite(product.id)
                                    favorites = favorites.filter { it.id != product.id }
                                } catch (_: Exception) {}
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun FavoriteListItem(product: ProductDTO, onClick: () -> Unit, onRemove: () -> Unit) {
    val colorScheme = MaterialTheme.colorScheme
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
        modifier = Modifier.fillMaxWidth().clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Emoji avatar
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = colorScheme.primaryContainer,
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("\uD83E\uDDF4", style = MaterialTheme.typography.titleMedium)
                }
            }
            // Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    product.brand.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = colorScheme.onSurfaceVariant
                )
                Text(
                    product.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "$${String.format("%.2f", product.price)}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.primary
                    )
                    if (product.isExpired) {
                        ExpiryChip(label = "Expired", status = ExpiryStatus.EXPIRED)
                    } else if (product.isExpiringWithin15Days) {
                        ExpiryChip(label = "Expiring", status = ExpiryStatus.WARNING)
                    }
                }
            }
            // Remove favorite
            IconButton(onClick = onRemove) {
                Icon(
                    Icons.Filled.Favorite,
                    contentDescription = "Remove",
                    tint = colorScheme.primary
                )
            }
        }
    }
}
