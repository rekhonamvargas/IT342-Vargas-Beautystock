package com.beautystock.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.beautystock.data.api.RetrofitClient
import com.beautystock.data.model.ProductDTO
import com.beautystock.ui.components.ExpiryChip
import com.beautystock.ui.components.ExpiryStatus
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(productId: Long, onBack: () -> Unit) {
    var product by remember { mutableStateOf<ProductDTO?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val colorScheme = MaterialTheme.colorScheme

    LaunchedEffect(productId) {
        try {
            val res = RetrofitClient.api.getProduct(productId)
            if (res.isSuccessful) product = res.body()
        } catch (_: Exception) {}
    }

    if (product == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = colorScheme.primary)
        }
        return
    }

    val p = product!!

    Scaffold(containerColor = colorScheme.background) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // Hero image area with overlay buttons
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .background(colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                if (p.imageUrl != null) {
                    AsyncImage(
                        model = p.imageUrl,
                        contentDescription = p.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text("\uD83E\uDDF4", style = MaterialTheme.typography.displayLarge)
                }
                // Back button
                FilledIconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(16.dp),
                    shape = CircleShape,
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = colorScheme.surface.copy(alpha = 0.9f),
                        contentColor = colorScheme.onSurface
                    )
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                // Favorite button
                FilledIconButton(
                    onClick = {
                        scope.launch {
                            try {
                                if (p.isFavorite) RetrofitClient.api.removeFavorite(p.id)
                                else RetrofitClient.api.addFavorite(p.id)
                                val res = RetrofitClient.api.getProduct(productId)
                                if (res.isSuccessful) product = res.body()
                            } catch (_: Exception) {}
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp),
                    shape = CircleShape,
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = colorScheme.surface.copy(alpha = 0.9f),
                        contentColor = if (p.isFavorite) colorScheme.primary else colorScheme.onSurfaceVariant
                    )
                ) {
                    Icon(
                        if (p.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        contentDescription = "Favorite"
                    )
                }
            }

            Column(modifier = Modifier.padding(20.dp)) {
                // Brand and name
                Text(
                    p.brand.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = colorScheme.onSurfaceVariant,
                    letterSpacing = 1.sp
                )
                Text(
                    p.name,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onSurface
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "$${String.format("%.2f", p.price)}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.primary
                )
                Spacer(Modifier.height(12.dp))

                // Status chips
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ExpiryChip(label = p.category, status = ExpiryStatus.VALID)
                    if (p.isExpired) ExpiryChip(label = "Expired", status = ExpiryStatus.EXPIRED)
                    if (p.isExpiringWithin15Days && !p.isExpired) ExpiryChip(label = "Expiring Soon", status = ExpiryStatus.WARNING)
                }
                Spacer(Modifier.height(16.dp))

                // Description
                if (!p.description.isNullOrBlank()) {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                "Description",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = colorScheme.onSurface
                            )
                            Text(
                                p.description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = colorScheme.onSurfaceVariant,
                                lineHeight = 20.sp,
                                modifier = Modifier.padding(top = 6.dp)
                            )
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                }

                // Meta grid
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            "Details",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = colorScheme.onSurface
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            MetaBox("\uD83D\uDCCD", "Location", p.purchaseLocation ?: "\u2014", Modifier.weight(1f))
                            MetaBox("\uD83D\uDCC5", "Expiry", p.expirationDate ?: "\u2014", Modifier.weight(1f))
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            MetaBox("\uD83D\uDCE6", "Opened", p.openedDate ?: "\u2014", Modifier.weight(1f))
                            MetaBox("\uD83C\uDFF7\uFE0F", "Category", p.category, Modifier.weight(1f))
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))

                // Actions
                Button(
                    onClick = {
                        scope.launch {
                            try {
                                if (p.isFavorite) RetrofitClient.api.removeFavorite(p.id)
                                else RetrofitClient.api.addFavorite(p.id)
                                val res = RetrofitClient.api.getProduct(productId)
                                if (res.isSuccessful) product = res.body()
                            } catch (_: Exception) {}
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (p.isFavorite) colorScheme.surfaceVariant else colorScheme.primary
                    )
                ) {
                    Text(
                        if (p.isFavorite) "\u2764\uFE0F Remove from Favorites" else "\uD83E\uDD0D Add to Favorites",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (p.isFavorite) colorScheme.onSurfaceVariant else colorScheme.onPrimary
                    )
                }
                Spacer(Modifier.height(8.dp))
                OutlinedButton(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = colorScheme.error)
                ) {
                    Text(
                        "\uD83D\uDDD1 Delete Product",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(Modifier.height(20.dp))
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = {
                Text(
                    "Delete Product",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    "Are you sure? This action cannot be undone.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch {
                        try { RetrofitClient.api.deleteProduct(p.id) } catch (_: Exception) {}
                        showDeleteDialog = false
                        onBack()
                    }
                }) {
                    Text(
                        "Delete",
                        color = colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun MetaBox(icon: String, label: String, value: String, modifier: Modifier) {
    val colorScheme = MaterialTheme.colorScheme
    Column(
        modifier = modifier
            .background(colorScheme.surfaceVariant, RoundedCornerShape(10.dp))
            .padding(12.dp)
    ) {
        Text(
            "$icon $label",
            style = MaterialTheme.typography.labelSmall,
            color = colorScheme.onSurfaceVariant
        )
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = colorScheme.onSurface,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}
