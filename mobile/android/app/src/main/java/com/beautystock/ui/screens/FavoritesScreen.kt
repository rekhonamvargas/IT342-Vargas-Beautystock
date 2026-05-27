package com.beautystock.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.navigation.NavHostController
import com.beautystock.model.ProductDTO
import com.beautystock.network.RetrofitClient
import com.beautystock.repository.BeautyStockRepository
import com.beautystock.utils.BeautyColors
import com.beautystock.utils.BeautySpacing
import com.beautystock.utils.PriceFormatter
import com.beautystock.viewmodel.FavoriteViewModel
import com.beautystock.viewmodel.FavoriteViewModelFactory
import com.beautystock.viewmodel.FavoritesUiState

@Composable
fun FavoritesScreen(navController: NavHostController? = null) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val repository = BeautyStockRepository(RetrofitClient.getApiService(), context)
    val favoriteViewModel: FavoriteViewModel = viewModel(factory = FavoriteViewModelFactory(repository))

    val favoritesUiState by favoriteViewModel.favoritesUiState.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                favoriteViewModel.loadFavorites()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(BeautySpacing.xl)
    ) {
        Text(
            text = "Favorites",
            style = MaterialTheme.typography.headlineMedium,
            color = BeautyColors.TextPrimary
        )
        Text(
            text = "Your saved beauty products.",
            style = MaterialTheme.typography.bodyMedium,
            color = BeautyColors.TextSecondary,
            modifier = Modifier.padding(top = BeautySpacing.xs, bottom = BeautySpacing.lg)
        )

        when (favoritesUiState) {
            is FavoritesUiState.Loading -> Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator(color = BeautyColors.Primary) }

            is FavoritesUiState.Success -> {
                val products = (favoritesUiState as FavoritesUiState.Success).products
                if (products.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = BeautySpacing.xxxl),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Filled.Favorite,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = BeautyColors.TextHint
                            )
                            Spacer(Modifier.height(BeautySpacing.md))
                            Text("No favorites yet", color = BeautyColors.TextSecondary)
                            Text(
                                "Open a product and tap the heart to save it here.",
                                style = MaterialTheme.typography.bodySmall,
                                color = BeautyColors.TextHint
                            )
                        }
                    }
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(products, key = { it.id }) { product ->
                            FavoriteCard(
                                product = product,
                                onRemove = { favoriteViewModel.removeFavorite(product.id) },
                                onClick = { navController?.navigate("product_detail/${product.id}") },
                                modifier = Modifier.padding(bottom = BeautySpacing.lg)
                            )
                        }
                    }
                }
            }

            is FavoritesUiState.Error -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = (favoritesUiState as FavoritesUiState.Error).message,
                        color = BeautyColors.Error
                    )
                    Spacer(Modifier.height(BeautySpacing.md))
                    Button(onClick = { favoriteViewModel.loadFavorites() }) {
                        Text("Retry")
                    }
                }
            }

            is FavoritesUiState.Idle -> {}
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FavoriteCard(
    product: ProductDTO,
    onRemove: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick,
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = BeautyColors.Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(BeautySpacing.lg),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = BeautyColors.TextPrimary,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = product.brand,
                    style = MaterialTheme.typography.bodySmall,
                    color = BeautyColors.TextSecondary
                )
                Text(
                    text = PriceFormatter.formatPrice(product.price),
                    style = MaterialTheme.typography.bodyMedium,
                    color = BeautyColors.Primary,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            FilledTonalIconButton(
                onClick = onRemove,
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = BeautyColors.Error.copy(alpha = 0.1f)
                )
            ) {
                Icon(
                    imageVector = Icons.Filled.Favorite,
                    contentDescription = "Remove from favorites",
                    tint = BeautyColors.Error
                )
            }
        }
    }
}
