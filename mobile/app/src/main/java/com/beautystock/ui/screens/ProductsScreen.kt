package com.beautystock.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.beautystock.data.api.RetrofitClient
import com.beautystock.data.model.ProductDTO
import com.beautystock.ui.components.ExpiryChip
import com.beautystock.ui.components.ExpiryStatus
import kotlinx.coroutines.launch

private val categories = listOf("All", "Skincare", "Makeup", "Hair Care", "Body Care", "Fragrance", "Tools", "Other")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductsScreen(onProductClick: (Long) -> Unit, onAddClick: () -> Unit) {
    var products by remember { mutableStateOf<List<ProductDTO>>(emptyList()) }
    var search by remember { mutableStateOf("") }
    var activeCategory by remember { mutableStateOf("All") }
    val scope = rememberCoroutineScope()
    val colorScheme = MaterialTheme.colorScheme

    LaunchedEffect(Unit) {
        try {
            val res = RetrofitClient.api.getProducts()
            if (res.isSuccessful) products = res.body() ?: emptyList()
        } catch (_: Exception) {}
    }

    val filtered = products.filter { p ->
        val matchSearch = search.isBlank() || p.name.contains(search, true) || p.brand.contains(search, true)
        val matchCat = activeCategory == "All" || p.category.equals(activeCategory, true)
        matchSearch && matchCat
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "My Products",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "${products.size} products in your collection",
                            style = MaterialTheme.typography.bodySmall,
                            color = colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = colorScheme.surface)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddClick,
                containerColor = colorScheme.primary
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add", tint = colorScheme.onPrimary)
            }
        },
        containerColor = colorScheme.background
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            // Search
            OutlinedTextField(
                value = search,
                onValueChange = { search = it },
                placeholder = { Text("Search products...") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(12.dp)
            )

            // Category chips
            Row(
                modifier = Modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { cat ->
                    val selected = activeCategory == cat
                    FilterChip(
                        selected = selected,
                        onClick = { activeCategory = cat },
                        label = {
                            Text(
                                cat,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = colorScheme.primary,
                            selectedLabelColor = colorScheme.onPrimary,
                            containerColor = colorScheme.surface,
                            labelColor = colorScheme.onSurfaceVariant
                        ),
                        shape = RoundedCornerShape(50)
                    )
                }
            }

            // Product grid
            if (filtered.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(40.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("\uD83D\uDCE6", style = MaterialTheme.typography.displaySmall)
                    Text(
                        "No products found",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onSurface
                    )
                    Text(
                        "Try adjusting your search",
                        style = MaterialTheme.typography.bodySmall,
                        color = colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filtered) { product ->
                        ProductCard(product = product, onClick = { onProductClick(product.id) }) {
                            scope.launch {
                                try {
                                    if (product.isFavorite) RetrofitClient.api.removeFavorite(product.id)
                                    else RetrofitClient.api.addFavorite(product.id)
                                    // Reload
                                    val res = RetrofitClient.api.getProducts()
                                    if (res.isSuccessful) products = res.body() ?: emptyList()
                                } catch (_: Exception) {}
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProductCard(product: ProductDTO, onClick: () -> Unit, onToggleFavorite: () -> Unit) {
    val colorScheme = MaterialTheme.colorScheme
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
        modifier = Modifier.clickable { onClick() }
    ) {
        Column {
            // Image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .background(colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                if (product.imageUrl != null) {
                    AsyncImage(
                        model = product.imageUrl,
                        contentDescription = product.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text("\uD83E\uDDF4", style = MaterialTheme.typography.displaySmall)
                }
                // Favorite button
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(colorScheme.surface.copy(alpha = 0.85f))
                        .clickable { onToggleFavorite() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        if (product.isFavorite) "\u2764\uFE0F" else "\uD83E\uDD0D",
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
            // Info
            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    product.brand.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = colorScheme.onSurfaceVariant,
                    letterSpacing = 0.5.sp
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
                    modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
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
        }
    }
}
