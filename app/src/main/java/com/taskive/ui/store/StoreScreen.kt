package com.taskive.ui.store

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.taskive.DarkPurple
import com.taskive.R
import com.taskive.ui.dashboard.TextColorDarkGlobal
import com.taskive.ui.theme.MediumPurpleLight
import com.taskive.ui.theme.MediumPurpleDark
import com.taskive.ui.theme.Nunito

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoreScreen(navController: NavController? = null) {
    var selectedCategory by remember { mutableStateOf(StoreCategory.PET) }
    var selectedItem by remember { mutableStateOf<StoreItem?>(null) }
    var userCoins by remember { mutableStateOf(1452) }

    val gradientBrush = Brush.verticalGradient(
        colors = listOf(MediumPurpleLight, MediumPurpleDark)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        TopAppBar(
            title = {
                Text(
                    "Store",
                    fontFamily = Nunito,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            },
            navigationIcon = {
                IconButton(onClick = { navController?.navigateUp() }) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = DarkPurple
                    )
                }
            },
            actions = {
                CoinBalance(userCoins)
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color(0xFFF5F5F5), // samakan dengan warna background layar
                titleContentColor = DarkPurple
            )
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CategoryTab(
                title = "Pet",
                selected = selectedCategory == StoreCategory.PET,
                onClick = { selectedCategory = StoreCategory.PET },
                modifier = Modifier.weight(1f),
                brush = gradientBrush
            )
            CategoryTab(
                title = "Food",
                selected = selectedCategory == StoreCategory.FOOD,
                onClick = { selectedCategory = StoreCategory.FOOD },
                modifier = Modifier.weight(1f),
                brush = gradientBrush
            )
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.weight(1f)
        ) {
            val items = getItemsForCategory(selectedCategory)
            items(items, key = { it.name }) { item ->
                StoreItemCard(item = item, coins = item.price) {
                    selectedItem = item
                }
            }
        }
    }

    if (selectedItem != null) {
        Dialog(
            onDismissRequest = { selectedItem = null },
            properties = DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = true,
                usePlatformDefaultWidth = false
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable(onClick = { selectedItem = null }),
                contentAlignment = Alignment.Center
            ) {
                selectedItem?.let { item ->
                    ItemDetailDialog(
                        item = item,
                        userCoins = userCoins,
                        coins = item.price,
                        onPurchase = {
                            if (userCoins >= item.price) {
                                userCoins -= item.price
                                selectedItem = null
                            }
                        },
                        brush = gradientBrush
                    )
                }
            }
        }
    }
}

@Composable
fun CoinBalance(coins: Int) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .padding(end = 16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .background(Color(0xFFFFC107), shape = CircleShape)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = coins.toString(),
            fontFamily = Nunito,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = TextColorDarkGlobal
        )
    }
}

@Composable
fun CategoryTab(
    title: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    brush: Brush
) {
    val backgroundBrush = if (selected) brush else Brush.linearGradient(listOf(Color.White, Color.White))

    Box(
        modifier = modifier
            .background(brush = backgroundBrush, shape = RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            color = if (selected) Color.White else Color.Black,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun StoreItemCard(item: StoreItem, coins: Int, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.9f)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            val painter = rememberAsyncImagePainter(
                ImageRequest.Builder(LocalContext.current)
                    .data(item.imageRes)
                    .build()
            )
            Image(
                painter = painter,
                contentDescription = item.name,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .size(100.dp)
                    .clip(RoundedCornerShape(12.dp))
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = item.name,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .background(Color(0xFFFFC107), shape = CircleShape)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = coins.toString(),
                    fontFamily = Nunito,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = TextColorDarkGlobal
                )
            }
        }
    }
}

@Composable
fun ItemDetailDialog(item: StoreItem, userCoins: Int, coins: Int, onPurchase: () -> Unit, brush: Brush) {
    Card(
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .padding(16.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            val painter = rememberAsyncImagePainter(
                ImageRequest.Builder(LocalContext.current)
                    .data(item.imageRes)
                    .build()
            )
            Image(
                painter = painter,
                contentDescription = item.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(120.dp)
                    .clip(RoundedCornerShape(16.dp))
            )

            Spacer(modifier = Modifier.height(16.dp))
            Text(text = item.name, fontWeight = FontWeight.Bold, fontSize = 20.sp)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .background(Color(0xFFFFC107), shape = CircleShape)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = coins.toString(),
                    fontFamily = Nunito,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = TextColorDarkGlobal
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Tombol beli
            Button(
                onClick = onPurchase,
                modifier = Modifier.fillMaxWidth(),
                enabled = userCoins >= item.price,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
            ) {
                Box(
                    modifier = Modifier
                        .background(brush = brush, shape = RoundedCornerShape(12.dp))
                        .padding(horizontal = 32.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Buy", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                }
            }

            // Notifikasi jika koin tidak cukup
            if (userCoins < item.price) {
                Spacer(modifier = Modifier.height(8.dp))
                Text("Not enough coins", color = Color.Red, fontSize = 14.sp)
            }
        }
    }
}

enum class StoreCategory(val label: String) {
    PET("Pet"),
    FOOD("Food")
}

data class StoreItem(val name: String, val price: Int, val imageRes: Int)

fun getItemsForCategory(category: StoreCategory): List<StoreItem> {
    return when (category) {
        StoreCategory.PET -> listOf(
            StoreItem("Cat", 240, R.drawable.cat),
            StoreItem("Penguin", 257, R.drawable.penguin),
            StoreItem("Hamster", 200, R.drawable.cat),
            StoreItem("Rabbit", 220, R.drawable.penguin)
        )
        StoreCategory.FOOD -> listOf(
            StoreItem("Sushi", 257, R.drawable.sushi),
            StoreItem("Tomato", 257, R.drawable.tomato),
            StoreItem("Banana", 180, R.drawable.sushi),
            StoreItem("Milk", 200, R.drawable.tomato)
        )
    }
}
