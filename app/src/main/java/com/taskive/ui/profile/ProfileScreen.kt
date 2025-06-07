package com.taskive.ui.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.taskive.R
import com.taskive.ui.theme.MediumPurpleDark
import com.taskive.ui.theme.MediumPurpleLight
import com.taskive.ui.theme.Nunito

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8F6FF))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Profile Header
        Text(
            text = "My Profile",
            fontFamily = Nunito,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MediumPurpleDark,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // User Avatar
        Card(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape),
            colors = CardDefaults.cardColors(containerColor = MediumPurpleLight)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Profile Picture",
                    modifier = Modifier.size(64.dp),
                    tint = MediumPurpleDark
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // User Info Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            colors = CardDefaults.cardColors(containerColor = MediumPurpleLight),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "John Doe",
                    fontFamily = Nunito,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                Text(
                    text = "Level 5",
                    fontFamily = Nunito,
                    fontSize = 16.sp,
                    color = Color.Gray
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Pet Section
        Text(
            text = "My Pet",
            fontFamily = Nunito,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MediumPurpleDark,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Pet GIF Display
        Card(
            modifier = Modifier
                .size(200.dp)
                .clip(RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = MediumPurpleLight)
        ) {
            Image(
                painter = rememberAsyncImagePainter(
                    ImageRequest.Builder(LocalContext.current)
                        .data(R.drawable.penguin)
                        .build()
                ),
                contentDescription = "Pet Animation",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Penguin",
            fontFamily = Nunito,
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Black
        )
    }
}
