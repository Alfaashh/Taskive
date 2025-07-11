package com.taskive.ui.store

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.taskive.DarkPurple
import com.taskive.model.StoreItem
import com.taskive.model.Pet
import com.taskive.ui.theme.MediumPurpleLight
import com.taskive.ui.theme.MediumPurpleDark
import com.taskive.ui.theme.Nunito
import com.taskive.ui.viewmodel.StoreViewModel
import com.taskive.ui.viewmodel.UserViewModel

enum class StoreCategory {
    PET, FOOD
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoreScreen(
    navController: NavHostController,
    storeViewModel: StoreViewModel,
    userViewModel: UserViewModel
) {
    var selectedCategory by remember { mutableStateOf(StoreCategory.PET) }
    var selectedItem: StoreItem? by remember { mutableStateOf(null) }
    var showPetSelection by remember { mutableStateOf(false) }
    var selectedFood: StoreItem? by remember { mutableStateOf(null) }

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
            actions = {
                CoinBalance(userViewModel.coins)
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color(0xFFF5F5F5),
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
            val items = when (selectedCategory) {
                StoreCategory.PET -> storeViewModel.availablePets
                StoreCategory.FOOD -> storeViewModel.availableFoods
            }
            items(items, key = { it.id }) { item ->
                StoreItemCard(
                    item = item,
                    userViewModel = userViewModel,
                    coins = item.price,
                    isPurchased = selectedCategory == StoreCategory.PET &&
                                storeViewModel.purchasedPetIds.value.contains(item.id)
                ) {
                    selectedItem = item
                }
            }
        }
    }

    selectedItem?.let { item ->
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
                ItemDetailDialog(
                    item = item,
                    userCoins = userViewModel.coins,
                    isPurchased = selectedCategory == StoreCategory.PET &&
                                storeViewModel.purchasedPetIds.value.contains(item.id),
                    onPurchase = {
                        if (selectedCategory == StoreCategory.PET) {
                            storeViewModel.buyPet(item.id, userViewModel)
                            selectedItem = null
                        } else {
                            // For food items, show pet selection dialog
                            selectedFood = item
                            showPetSelection = true
                            selectedItem = null
                        }
                    },
                    brush = gradientBrush
                )
            }
        }
    }

    if (showPetSelection && selectedFood != null) {
        Dialog(
            onDismissRequest = {
                showPetSelection = false
                selectedFood = null
            }
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        "Select Pet to Feed",
                        fontFamily = Nunito,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    if (userViewModel.pets.isEmpty()) {
                        Text(
                            "You don't have any pets yet!",
                            fontFamily = Nunito,
                            fontSize = 16.sp,
                            color = Color.Gray
                        )
                    } else {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            userViewModel.pets.forEach { pet ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            storeViewModel.purchaseAndUseFoodItem(selectedFood!!, pet.id, userViewModel)
                                            showPetSelection = false
                                            selectedFood = null
                                        },
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        AsyncImage(
                                            model = ImageRequest.Builder(LocalContext.current)
                                                .data(pet.getCurrentImage())
                                                .crossfade(true)
                                                .build(),
                                            contentDescription = pet.name,
                                            modifier = Modifier
                                                .size(40.dp)
                                                .clip(CircleShape),
                                            contentScale = ContentScale.Fit
                                        )

                                        Spacer(modifier = Modifier.width(8.dp))

                                        Column {
                                            Text(
                                                pet.name,
                                                fontFamily = Nunito,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                "HP: ${pet.healthPoints}/${pet.maxHealthPoints}",
                                                fontFamily = Nunito,
                                                fontSize = 12.sp,
                                                color = Color.Gray
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    TextButton(
                        onClick = {
                            showPetSelection = false
                            selectedFood = null
                        },
                        modifier = Modifier
                            .align(Alignment.End)
                            .padding(top = 16.dp)
                    ) {
                        Text("Cancel")
                    }
                }
            }
        }
    }

    if (storeViewModel.showHealDialog.value) {
        storeViewModel.selectedFood.value?.let { food ->
            HealDialog(
                userViewModel = userViewModel,
                storeViewModel = storeViewModel,
                selectedFood = food
            )
        }
    }
}

@Composable
fun CoinBalance(coins: Int) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(end = 16.dp)
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
            color = Color.Black
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
private fun StoreItemCard(
    item: StoreItem,
    userViewModel: UserViewModel,
    coins: Int,
    isPurchased: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)  // Fixed height for consistency
            .padding(8.dp)
            .clickable(
                enabled = !isPurchased &&
                        (item.id != 1 || userViewModel.currentLevel >= 2) &&
                        userViewModel.coins >= item.price,
                onClick = onClick
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(85.dp)  // Increased size
                    .clip(CircleShape)
                    .background(Color.White)  // Added white background
                    .padding(4.dp)
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(item.imageRes)
                        .crossfade(true)
                        .build(),
                    contentDescription = item.name,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = item.name,
                    fontFamily = Nunito,
                    fontWeight = FontWeight.Bold
                )

                if (item.id == 1 && !isPurchased && userViewModel.currentLevel < 2) {
                    Text(
                        text = "Requires Level 2",
                        color = Color.Red,
                        fontSize = 12.sp,
                        fontFamily = Nunito
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    if (!isPurchased) {
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .background(Color(0xFFFFC107), CircleShape)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = coins.toString(),
                            color = if (userViewModel.coins >= item.price) Color.Black else Color.Red,
                            fontFamily = Nunito
                        )
                    } else {
                        Text(
                            text = "Purchased",
                            color = Color.Gray,
                            fontFamily = Nunito
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ItemDetailDialog(
    item: StoreItem,
    userCoins: Int,
    isPurchased: Boolean,
    onPurchase: () -> Unit,
    brush: Brush
) {
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
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(item.imageRes)
                    .crossfade(true)
                    .build(),
                contentDescription = item.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(120.dp)
                    .clip(RoundedCornerShape(16.dp))
            )

            Spacer(modifier = Modifier.height(16.dp))
            Text(text = item.name, fontWeight = FontWeight.Bold, fontSize = 20.sp)

            if (!isPurchased) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .background(Color(0xFFFFC107), shape = CircleShape)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = item.price.toString(),
                        fontFamily = Nunito,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color.Black
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (isPurchased) {
                Text(
                    text = "Already owned",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.Gray
                )
            } else {
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

                if (userCoins < item.price) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Not enough coins", color = Color.Red, fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
fun HealDialog(
    userViewModel: UserViewModel,
    storeViewModel: StoreViewModel,
    selectedFood: StoreItem
) {
    AlertDialog(
        onDismissRequest = { storeViewModel.dismissHealDialog() },
        title = {
            Text(
                "Choose Pet to Heal",
                fontFamily = Nunito,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    "Using ${selectedFood.name} (+${selectedFood.healingPoints} HP)",
                    fontFamily = Nunito,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    userViewModel.pets.forEach { pet ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { storeViewModel.healPet(pet.id, userViewModel) },
                            colors = CardDefaults.cardColors(
                                containerColor = if (pet.healthPoints < pet.maxHealthPoints)
                                    MaterialTheme.colorScheme.errorContainer
                                else MaterialTheme.colorScheme.secondaryContainer
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    AsyncImage(
                                        model = ImageRequest.Builder(LocalContext.current)
                                            .data(pet.getCurrentImage())
                                            .crossfade(true)
                                            .build(),
                                        contentDescription = pet.name,
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape),
                                        contentScale = ContentScale.Fit
                                    )
                                    Text(
                                        pet.name,
                                        fontFamily = Nunito,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                                Text(
                                    "${pet.healthPoints}/${pet.maxHealthPoints} HP",
                                    fontFamily = Nunito,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { storeViewModel.dismissHealDialog() }) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun PetCard(
    pet: Pet,
    coins: Int,
    onBuyPet: (Pet) -> Unit,
    userViewModel: UserViewModel
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(MediumPurpleLight, MediumPurpleDark)
                    )
                )
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    pet.name,
                    fontFamily = Nunito,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    "Coins",  // Note: Pet model doesn't have price, you might need to add it
                    fontFamily = Nunito,
                    fontSize = 16.sp,
                    color = Color.White.copy(alpha = 0.8f)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = { onBuyPet(pet) },
                    enabled = coins >= 0 && !userViewModel.pets.any { it.id == pet.id },  // Adjusted to use direct id property
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (coins >= 0 && !userViewModel.pets.any { it.id == pet.id })
                            Color(0xFF4CAF50) else Color.Gray
                    )
                ) {
                    Text(
                        if (userViewModel.pets.any { it.id == pet.id }) "Owned"
                        else if (coins >= 0) "Buy"
                        else "Not enough coins",
                        fontFamily = Nunito,
                        color = Color.White
                    )
                }
            }

            Box(
                modifier = Modifier
                    .size(65.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .padding(4.dp)
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(pet.getCurrentImage())  // This method exists in Pet model
                        .crossfade(true)
                        .build(),
                    contentDescription = pet.name,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
