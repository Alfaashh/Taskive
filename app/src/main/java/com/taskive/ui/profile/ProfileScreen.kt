package com.taskive.ui.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.taskive.R
import com.taskive.ui.theme.MediumPurpleDark
import com.taskive.ui.theme.MediumPurpleLight
import com.taskive.ui.theme.Nunito
import com.taskive.model.Pet
import com.taskive.ui.viewmodel.UserViewModel

@Composable
fun StatItem(
    count: String,
    label: String,
    icon: @Composable (() -> Unit)? = null
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                icon()
                Spacer(modifier = Modifier.width(4.dp))
            }
            Text(
                count,
                fontFamily = Nunito,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Text(
            label,
            fontFamily = Nunito,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PetCard(pet: Pet) {
    var showDetailsDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .width(160.dp)
            .height(180.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        onClick = { showDetailsDialog = true }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(pet.getCurrentImage())
                    .crossfade(true)
                    .build(),
                contentDescription = pet.name,
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(MediumPurpleLight.copy(alpha = 0.2f)),
                contentScale = ContentScale.Fit
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                pet.name,
                fontFamily = Nunito,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            val statusColor = when (pet.status) {
                "Healthy" -> Color(0xFF4CAF50)
                "Sick" -> Color(0xFFFF9800)
                else -> Color(0xFFF44336)
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(statusColor.copy(alpha = 0.1f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    pet.status,
                    fontFamily = Nunito,
                    fontSize = 12.sp,
                    color = statusColor
                )
            }
        }
    }

    if (showDetailsDialog) {
        AlertDialog(
            onDismissRequest = { showDetailsDialog = false },
            title = {
                Text(
                    "${pet.name}'s Details",
                    fontFamily = Nunito,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(pet.getCurrentImage())
                            .crossfade(true)
                            .build(),
                        contentDescription = pet.name,
                        modifier = Modifier
                            .size(160.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(MediumPurpleLight.copy(alpha = 0.2f)),
                        contentScale = ContentScale.Fit
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        "Health Points: ${pet.healthPoints}/${pet.maxHealthPoints}",
                        fontFamily = Nunito
                    )
                    Text(
                        "Status: ${pet.status}",
                        fontFamily = Nunito,
                        color = when (pet.status) {
                            "Healthy" -> Color(0xFF4CAF50)
                            "Sick" -> Color(0xFFFF9800)
                            else -> Color(0xFFF44336)
                        }
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showDetailsDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(userViewModel: UserViewModel) {
    var showEditDialog by remember { mutableStateOf(false) }
    var editingUsername by remember { mutableStateOf(userViewModel.username) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))  // Light gray background to match other screens
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        // Profile Picture Section
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(MediumPurpleLight),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Profile Picture",
                tint = Color.White,
                modifier = Modifier.size(50.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Username with Edit Button
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = userViewModel.username,
                fontFamily = Nunito,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            IconButton(onClick = { showEditDialog = true }) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit Username",
                    tint = MediumPurpleDark
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Level Progress Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Level ${userViewModel.currentLevel}",
                        fontFamily = Nunito,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "${userViewModel.currentXP}/${userViewModel.currentLevel * 100} XP",
                        fontFamily = Nunito,
                        fontSize = 16.sp
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // XP Progress Bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(MediumPurpleLight.copy(alpha = 0.2f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(userViewModel.currentXP.toFloat() / (userViewModel.currentLevel * 100))
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(6.dp))
                            .background(MediumPurpleDark)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Stats Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatItem(
                        count = "${userViewModel.coins}",
                        label = "Coins",
                        icon = {
                            Icon(
                                imageVector = Icons.Default.MonetizationOn,
                                contentDescription = "Coins",
                                tint = Color(0xFFFFD700),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    )
                    StatItem(
                        count = "${userViewModel.completedTasks}",
                        label = "Tasks Done",
                        icon = {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Completed Tasks",
                                tint = Color(0xFF4CAF50),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    )
                    StatItem(
                        count = "${userViewModel.pets.size}",
                        label = "Pets"
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Collections Section
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                "COLLECTIONS",
                fontFamily = Nunito,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black.copy(alpha = 0.7f),
                modifier = Modifier.padding(bottom = 16.dp, start = 8.dp)
            )

            if (userViewModel.pets.isEmpty()) {
                // Empty state
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "No pets yet",
                            fontFamily = Nunito,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Go to the Store to buy Pet",
                            fontFamily = Nunito,
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }
                }
            } else {
                // Show pets in horizontal scrollable list
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    userViewModel.pets.forEach { pet ->
                        PetCard(pet = pet)
                    }
                }
            }
        }

        // Username Edit Dialog
        if (showEditDialog) {
            AlertDialog(
                onDismissRequest = { showEditDialog = false },
                containerColor = MaterialTheme.colorScheme.surface,
                title = {
                    Text(
                        "Edit Username",
                        fontFamily = Nunito,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                text = {
                    OutlinedTextField(
                        value = editingUsername,
                        onValueChange = { editingUsername = it },
                        label = { Text("Username") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MediumPurpleDark,
                            focusedLabelColor = MediumPurpleDark
                        )
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            if (editingUsername.isNotBlank()) {
                                userViewModel.updateUsername(editingUsername)
                            }
                            showEditDialog = false
                        }
                    ) {
                        Text("Save", color = MediumPurpleDark)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showEditDialog = false }) {
                        Text("Cancel", color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            )
        }
    }
}
