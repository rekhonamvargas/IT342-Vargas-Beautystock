package com.beautystock.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.navigation.NavHostController
import com.beautystock.model.ProductDTO
import com.beautystock.network.RetrofitClient
import com.beautystock.repository.BeautyStockRepository
import com.beautystock.ui.components.BeautyButton
import com.beautystock.ui.components.BeautyTextField
import com.beautystock.utils.BeautyColors
import com.beautystock.utils.BeautySpacing
import com.beautystock.utils.DateFormatter
import com.beautystock.utils.PriceFormatter
import com.beautystock.viewmodel.ProductViewModel
import com.beautystock.viewmodel.ProductDetailUiState
import com.beautystock.viewmodel.ProductsUiState
import com.beautystock.viewmodel.ProductViewModelFactory
import kotlinx.coroutines.delay

// ─── Products List ───────────────────────────────────────────────────────────

@Composable
fun ProductsScreen(navController: NavHostController? = null) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val repository = BeautyStockRepository(RetrofitClient.getApiService(), context)
    val productViewModel: ProductViewModel = viewModel(factory = ProductViewModelFactory(repository))

    val productsUiState by productViewModel.productsUiState.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                productViewModel.getProducts()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            BeautyTextField(
                value = searchQuery,
                onValueChange = {
                    searchQuery = it
                    if (it.isBlank()) {
                        productViewModel.getProducts()
                    }
                },
                label = "Search products...",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = BeautySpacing.xl, vertical = BeautySpacing.lg)
            )

            LaunchedEffect(searchQuery) {
                if (searchQuery.isNotBlank()) {
                    delay(350)
                    productViewModel.searchProducts(searchQuery.trim())
                }
            }

            when (productsUiState) {
                is ProductsUiState.Loading -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator(color = BeautyColors.Primary) }

                is ProductsUiState.Success -> {
                    val products = (productsUiState as ProductsUiState.Success).products
                    if (products.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Filled.ShoppingCart,
                                    contentDescription = null,
                                    modifier = Modifier.size(64.dp),
                                    tint = BeautyColors.TextHint
                                )
                                Spacer(Modifier.height(BeautySpacing.md))
                                Text("No products found", color = BeautyColors.TextSecondary)
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(
                                horizontal = BeautySpacing.xl,
                                vertical = BeautySpacing.sm
                            )
                        ) {
                            items(products, key = { it.id }) { product ->
                                ProductCard(
                                    product = product,
                                    onClick = { navController?.navigate("product_detail/${product.id}") },
                                    modifier = Modifier.padding(bottom = BeautySpacing.lg)
                                )
                            }
                        }
                    }
                }

                is ProductsUiState.Error -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = (productsUiState as ProductsUiState.Error).message,
                            color = BeautyColors.Error
                        )
                        Spacer(Modifier.height(BeautySpacing.md))
                        BeautyButton(
                            text = "Retry",
                            onClick = { productViewModel.getProducts() },
                            modifier = Modifier.width(160.dp)
                        )
                    }
                }

                is ProductsUiState.Idle -> {}
            }
        }

        FloatingActionButton(
            onClick = { navController?.navigate("add_product") },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(BeautySpacing.xl),
            containerColor = BeautyColors.Primary,
            contentColor = Color.White
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Add Product")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProductCard(
    product: ProductDTO,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val statusColor = when {
        product.isExpired == true -> BeautyColors.Expired
        product.isExpiringWithin15Days == true -> BeautyColors.ExpiringSoon
        else -> BeautyColors.Available
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick,
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = BeautyColors.Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(BeautySpacing.lg),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Color indicator strip
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(64.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(statusColor)
            )

            Spacer(Modifier.width(BeautySpacing.md))

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
                Spacer(Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(BeautySpacing.sm),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CategoryChip(product.category)
                    if (product.expirationDate != null) {
                        Text(
                            text = "Exp: ${DateFormatter.formatDateForDisplay(product.expirationDate)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = statusColor
                        )
                    }
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = PriceFormatter.formatPrice(product.price),
                    style = MaterialTheme.typography.bodyMedium,
                    color = BeautyColors.Primary,
                    fontWeight = FontWeight.Bold
                )
                if (product.isFavorite == true) {
                    Icon(
                        Icons.Filled.Favorite,
                        contentDescription = "Favorited",
                        tint = BeautyColors.Error,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun CategoryChip(category: String) {
    Surface(
        color = BeautyColors.PrimaryLight.copy(alpha = 0.3f),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            text = category,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall,
            color = BeautyColors.Primary
        )
    }
}

// ─── Add Product ─────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProductScreen(navController: NavHostController? = null) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val repository = BeautyStockRepository(RetrofitClient.getApiService(), context)
    val productViewModel: ProductViewModel = viewModel(factory = ProductViewModelFactory(repository))

    val detailUiState by productViewModel.productDetailUiState.collectAsState()

    var name by remember { mutableStateOf("") }
    var brand by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("SKINCARE") }
    var description by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var purchaseLocation by remember { mutableStateOf("") }
    var expirationDate by remember { mutableStateOf("") }
    var openedDate by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    val categories = listOf("SKINCARE", "MAKEUP", "FRAGRANCE", "HAIRCARE", "BODYCARE", "OTHER")
    var categoryExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(detailUiState) {
        when (detailUiState) {
            is ProductDetailUiState.Success -> {
                successMessage = "Product added successfully!"
            }
            is ProductDetailUiState.Error -> {
                errorMessage = (detailUiState as ProductDetailUiState.Error).message
            }
            else -> {}
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(BeautySpacing.xl)
    ) {
        Text(
            text = "Add Product",
            style = MaterialTheme.typography.headlineSmall,
            color = BeautyColors.TextPrimary,
            modifier = Modifier.padding(bottom = BeautySpacing.xl)
        )

        BeautyTextField(
            value = name,
            onValueChange = { name = it },
            label = "Product Name *",
            modifier = Modifier.padding(bottom = BeautySpacing.lg)
        )

        BeautyTextField(
            value = brand,
            onValueChange = { brand = it },
            label = "Brand *",
            modifier = Modifier.padding(bottom = BeautySpacing.lg)
        )

        // Category dropdown
        Text(
            text = "Category *",
            style = MaterialTheme.typography.labelMedium,
            color = BeautyColors.TextSecondary,
            modifier = Modifier.padding(bottom = BeautySpacing.xs)
        )
        ExposedDropdownMenuBox(
            expanded = categoryExpanded,
            onExpandedChange = { categoryExpanded = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = BeautySpacing.lg)
        ) {
            OutlinedTextField(
                value = category,
                onValueChange = {},
                readOnly = true,
                label = { Text("Category") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BeautyColors.Primary,
                    unfocusedBorderColor = BeautyColors.Divider,
                    focusedLabelColor = BeautyColors.Primary
                )
            )
            ExposedDropdownMenu(
                expanded = categoryExpanded,
                onDismissRequest = { categoryExpanded = false }
            ) {
                categories.forEach { cat ->
                    DropdownMenuItem(
                        text = { Text(cat) },
                        onClick = {
                            category = cat
                            categoryExpanded = false
                        }
                    )
                }
            }
        }

        BeautyTextField(
            value = description,
            onValueChange = { description = it },
            label = "Description",
            modifier = Modifier.padding(bottom = BeautySpacing.lg)
        )

        BeautyTextField(
            value = price,
            onValueChange = { price = it },
            label = "Price (₱)",
            keyboardType = KeyboardType.Decimal,
            modifier = Modifier.padding(bottom = BeautySpacing.lg)
        )

        BeautyTextField(
            value = purchaseLocation,
            onValueChange = { purchaseLocation = it },
            label = "Purchase Location",
            modifier = Modifier.padding(bottom = BeautySpacing.lg)
        )

        BeautyTextField(
            value = expirationDate,
            onValueChange = { expirationDate = it },
            label = "Expiration Date (YYYY-MM-DD)",
            modifier = Modifier.padding(bottom = BeautySpacing.lg)
        )

        BeautyTextField(
            value = openedDate,
            onValueChange = { openedDate = it },
            label = "Date Opened (YYYY-MM-DD, optional)",
            modifier = Modifier.padding(bottom = BeautySpacing.xl)
        )

        BeautyButton(
            text = "Add Product",
            onClick = {
                errorMessage = null
                when {
                    name.isBlank() -> errorMessage = "Product name is required"
                    brand.isBlank() -> errorMessage = "Brand is required"
                    price.isNotBlank() && price.toDoubleOrNull() == null ->
                        errorMessage = "Price must be a valid number"
                    expirationDate.isNotBlank() && !expirationDate.matches(Regex("\\d{4}-\\d{2}-\\d{2}")) ->
                        errorMessage = "Expiration date must be in YYYY-MM-DD format"
                    else -> productViewModel.createProduct(
                        name = name.trim(),
                        brand = brand.trim(),
                        category = category,
                        description = description.trim(),
                        price = price.toDoubleOrNull(),
                        purchaseLocation = purchaseLocation.trim(),
                        expirationDate = expirationDate.trim().ifBlank { null },
                        openedDate = openedDate.trim().ifBlank { null }
                    )
                }
            },
            isLoading = detailUiState is ProductDetailUiState.Loading,
            enabled = detailUiState !is ProductDetailUiState.Loading,
            modifier = Modifier.padding(bottom = BeautySpacing.lg)
        )
    }

    if (errorMessage != null) {
        AlertDialog(
            onDismissRequest = { errorMessage = null },
            title = { Text("Error") },
            text = { Text(errorMessage!!) },
            confirmButton = {
                Button(onClick = { errorMessage = null }) { Text("OK") }
            }
        )
    }

    if (successMessage != null) {
        AlertDialog(
            onDismissRequest = {
                successMessage = null
                navController?.popBackStack()
            },
            title = { Text("Success") },
            text = { Text(successMessage!!) },
            confirmButton = {
                Button(onClick = {
                    successMessage = null
                    navController?.popBackStack()
                }) { Text("OK") }
            }
        )
    }
}

// ─── Product Detail ──────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(productId: Long, navController: NavHostController? = null) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val repository = BeautyStockRepository(RetrofitClient.getApiService(), context)
    val productViewModel: ProductViewModel = viewModel(factory = ProductViewModelFactory(repository))

    val detailUiState by productViewModel.productDetailUiState.collectAsState()
    val isFavorite by productViewModel.isFavorite.collectAsState()

    var isEditing by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var deletionInProgress by remember { mutableStateOf(false) }

    // Edit form state
    var editName by remember { mutableStateOf("") }
    var editBrand by remember { mutableStateOf("") }
    var editCategory by remember { mutableStateOf("SKINCARE") }
    var editDescription by remember { mutableStateOf("") }
    var editPrice by remember { mutableStateOf("") }
    var editLocation by remember { mutableStateOf("") }
    var editExpirationDate by remember { mutableStateOf("") }
    var editOpenedDate by remember { mutableStateOf("") }
    var categoryExpanded by remember { mutableStateOf(false) }

    val categories = listOf("SKINCARE", "MAKEUP", "FRAGRANCE", "HAIRCARE", "BODYCARE", "OTHER")

    LaunchedEffect(productId) { productViewModel.getProductById(productId) }

    // When a product loads, seed the edit fields
    LaunchedEffect(detailUiState) {
        when (val state = detailUiState) {
            is ProductDetailUiState.Success -> {
                val p = state.product
                editName = p.name
                editBrand = p.brand
                editCategory = p.category
                editDescription = p.description ?: ""
                editPrice = p.price?.toString() ?: ""
                editLocation = p.purchaseLocation ?: ""
                editExpirationDate = p.expirationDate ?: ""
                editOpenedDate = p.openedDate ?: ""
            }
            is ProductDetailUiState.Error -> errorMessage = state.message
            is ProductDetailUiState.Idle -> {
                if (deletionInProgress) {
                    navController?.popBackStack()
                }
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditing) "Edit Product" else "Product Detail") },
                navigationIcon = {
                    IconButton(onClick = {
                        if (isEditing) isEditing = false
                        else navController?.popBackStack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (!isEditing) {
                        IconButton(onClick = {
                            val product = (detailUiState as? ProductDetailUiState.Success)?.product
                            if (product != null) {
                                if (isFavorite) productViewModel.removeFavoriteFromDetail(productId)
                                else productViewModel.addFavoriteToDetail(productId)
                            }
                        }) {
                            Icon(
                                if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                                contentDescription = "Toggle Favorite",
                                tint = if (isFavorite) BeautyColors.Error else BeautyColors.TextSecondary
                            )
                        }
                        IconButton(onClick = { isEditing = true }) {
                            Icon(Icons.Filled.Edit, contentDescription = "Edit")
                        }
                        IconButton(onClick = { showDeleteConfirm = true }) {
                            Icon(
                                Icons.Filled.Delete,
                                contentDescription = "Delete",
                                tint = BeautyColors.Error
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BeautyColors.Surface
                )
            )
        }
    ) { paddingValues ->
        when (detailUiState) {
            is ProductDetailUiState.Loading -> Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator(color = BeautyColors.Primary) }

            is ProductDetailUiState.Success -> {
                val product = (detailUiState as ProductDetailUiState.Success).product
                if (isEditing) {
                    EditProductForm(
                        paddingValues = paddingValues,
                        name = editName, onNameChange = { editName = it },
                        brand = editBrand, onBrandChange = { editBrand = it },
                        category = editCategory, onCategoryChange = { editCategory = it },
                        description = editDescription, onDescriptionChange = { editDescription = it },
                        price = editPrice, onPriceChange = { editPrice = it },
                        location = editLocation, onLocationChange = { editLocation = it },
                        expirationDate = editExpirationDate, onExpirationDateChange = { editExpirationDate = it },
                        openedDate = editOpenedDate, onOpenedDateChange = { editOpenedDate = it },
                        categories = categories,
                        categoryExpanded = categoryExpanded,
                        onCategoryExpandedChange = { categoryExpanded = it },
                        isLoading = false,
                        onSave = {
                            val parsedPrice = editPrice.toDoubleOrNull()
                            when {
                                editName.isBlank() -> errorMessage = "Product name is required"
                                editBrand.isBlank() -> errorMessage = "Brand is required"
                                editPrice.isNotBlank() && parsedPrice == null ->
                                    errorMessage = "Price must be a valid number"
                                else -> {
                                    productViewModel.updateProduct(
                                        id = productId,
                                        name = editName.trim(),
                                        brand = editBrand.trim(),
                                        category = editCategory,
                                        description = editDescription.trim(),
                                        price = parsedPrice,
                                        purchaseLocation = editLocation.trim(),
                                        expirationDate = editExpirationDate.trim(),
                                        openedDate = editOpenedDate.trim().ifBlank { null }
                                    )
                                    isEditing = false
                                }
                            }
                        },
                        onCancel = { isEditing = false }
                    )
                } else {
                    ProductDetailContent(
                        product = product,
                        isFavorite = isFavorite,
                        paddingValues = paddingValues
                    )
                }
            }

            is ProductDetailUiState.Error -> Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = (detailUiState as ProductDetailUiState.Error).message,
                    color = BeautyColors.Error
                )
            }

            is ProductDetailUiState.Idle -> {}
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Product") },
            text = { Text("Are you sure you want to delete this product? This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirm = false
                        deletionInProgress = true
                        productViewModel.deleteProduct(productId)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BeautyColors.Error)
                ) { Text("Delete") }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") }
            }
        )
    }

    if (errorMessage != null) {
        AlertDialog(
            onDismissRequest = { errorMessage = null },
            title = { Text("Error") },
            text = { Text(errorMessage!!) },
            confirmButton = { Button(onClick = { errorMessage = null }) { Text("OK") } }
        )
    }
}

@Composable
private fun ProductDetailContent(
    product: ProductDTO,
    isFavorite: Boolean,
    paddingValues: PaddingValues
) {
    val statusColor = when {
        product.isExpired == true -> BeautyColors.Expired
        product.isExpiringWithin15Days == true -> BeautyColors.ExpiringSoon
        else -> BeautyColors.Available
    }
    val statusLabel = when {
        product.isExpired == true -> "Expired"
        product.isExpiringWithin15Days == true -> "Expiring Soon"
        else -> product.status ?: "Available"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .verticalScroll(rememberScrollState())
            .padding(BeautySpacing.xl)
    ) {
        // Status badge
        Surface(
            color = statusColor.copy(alpha = 0.15f),
            shape = RoundedCornerShape(20.dp)
        ) {
            Text(
                text = statusLabel,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                color = statusColor,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold
            )
        }

        if (isFavorite) {
            Spacer(Modifier.height(BeautySpacing.sm))
            Surface(
                color = BeautyColors.Error.copy(alpha = 0.12f),
                shape = RoundedCornerShape(20.dp)
            ) {
                Text(
                    text = "In Favorites",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    color = BeautyColors.Error,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(Modifier.height(BeautySpacing.lg))

        Text(
            text = product.name,
            style = MaterialTheme.typography.headlineSmall,
            color = BeautyColors.TextPrimary,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "by ${product.brand}",
            style = MaterialTheme.typography.bodyLarge,
            color = BeautyColors.TextSecondary,
            modifier = Modifier.padding(top = BeautySpacing.xs, bottom = BeautySpacing.xl)
        )

        DetailInfoCard {
            DetailRow("Category", product.category)
            DetailRow("Price", PriceFormatter.formatPrice(product.price))
            if (product.purchaseLocation != null) DetailRow("Purchase Location", product.purchaseLocation)
            if (product.expirationDate != null)
                DetailRow("Expires", DateFormatter.formatDateForDisplay(product.expirationDate), valueColor = statusColor)
            if (product.openedDate != null)
                DetailRow("Opened", DateFormatter.formatDateForDisplay(product.openedDate))
            if (product.description != null) DetailRow("Description", product.description)
        }
    }
}

@Composable
private fun DetailInfoCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = BeautyColors.Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(BeautySpacing.xl),
            content = content
        )
    }
}

@Composable
private fun DetailRow(label: String, value: String, valueColor: Color = BeautyColors.TextPrimary) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = BeautySpacing.sm),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = BeautyColors.TextSecondary,
            modifier = Modifier.weight(0.4f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = valueColor,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(0.6f)
        )
    }
    Divider(color = BeautyColors.Divider.copy(alpha = 0.5f))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditProductForm(
    paddingValues: PaddingValues,
    name: String, onNameChange: (String) -> Unit,
    brand: String, onBrandChange: (String) -> Unit,
    category: String, onCategoryChange: (String) -> Unit,
    description: String, onDescriptionChange: (String) -> Unit,
    price: String, onPriceChange: (String) -> Unit,
    location: String, onLocationChange: (String) -> Unit,
    expirationDate: String, onExpirationDateChange: (String) -> Unit,
    openedDate: String, onOpenedDateChange: (String) -> Unit,
    categories: List<String>,
    categoryExpanded: Boolean,
    onCategoryExpandedChange: (Boolean) -> Unit,
    isLoading: Boolean,
    onSave: () -> Unit,
    onCancel: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .verticalScroll(rememberScrollState())
            .padding(BeautySpacing.xl)
    ) {
        BeautyTextField(
            value = name, onValueChange = onNameChange,
            label = "Product Name *",
            modifier = Modifier.padding(bottom = BeautySpacing.lg)
        )
        BeautyTextField(
            value = brand, onValueChange = onBrandChange,
            label = "Brand *",
            modifier = Modifier.padding(bottom = BeautySpacing.lg)
        )

        ExposedDropdownMenuBox(
            expanded = categoryExpanded,
            onExpandedChange = onCategoryExpandedChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = BeautySpacing.lg)
        ) {
            OutlinedTextField(
                value = category, onValueChange = {},
                readOnly = true,
                label = { Text("Category") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BeautyColors.Primary,
                    unfocusedBorderColor = BeautyColors.Divider
                )
            )
            ExposedDropdownMenu(
                expanded = categoryExpanded,
                onDismissRequest = { onCategoryExpandedChange(false) }
            ) {
                categories.forEach { cat ->
                    DropdownMenuItem(
                        text = { Text(cat) },
                        onClick = { onCategoryChange(cat); onCategoryExpandedChange(false) }
                    )
                }
            }
        }

        BeautyTextField(
            value = description, onValueChange = onDescriptionChange,
            label = "Description",
            modifier = Modifier.padding(bottom = BeautySpacing.lg)
        )
        BeautyTextField(
            value = price, onValueChange = onPriceChange,
            label = "Price (₱)",
            keyboardType = KeyboardType.Decimal,
            modifier = Modifier.padding(bottom = BeautySpacing.lg)
        )
        BeautyTextField(
            value = location, onValueChange = onLocationChange,
            label = "Purchase Location",
            modifier = Modifier.padding(bottom = BeautySpacing.lg)
        )
        BeautyTextField(
            value = expirationDate, onValueChange = onExpirationDateChange,
            label = "Expiration Date (YYYY-MM-DD)",
            modifier = Modifier.padding(bottom = BeautySpacing.lg)
        )
        BeautyTextField(
            value = openedDate, onValueChange = onOpenedDateChange,
            label = "Date Opened (YYYY-MM-DD, optional)",
            modifier = Modifier.padding(bottom = BeautySpacing.xl)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(BeautySpacing.md)
        ) {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.weight(1f)
            ) { Text("Cancel") }

            Button(
                onClick = onSave,
                modifier = Modifier.weight(1f),
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = BeautyColors.Primary)
            ) {
                if (isLoading) CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
                else Text("Save Changes")
            }
        }
    }
}
