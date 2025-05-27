package com.taskive.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.taskive.ui.theme.Nunito // Impor Nunito dari theme Anda
import com.taskive.ui.theme.MediumPurpleDark // Impor warna dari theme Anda
import com.taskive.ui.theme.MediumPurpleLight // Impor warna dari theme Anda

// Warna lokal yang mungkin belum ada di Color.kt Anda (bisa dipindahkan ke Color.kt)
val LightPurpleBackground = Color(0xFFF8F6FF)
val TextColorDarkGlobal = Color(0xFF333333) // Mengganti nama agar tidak konflik
val TextColorLightGlobal = Color.Gray      // Mengganti nama agar tidak konflik

@Composable
fun DashboardScreen() {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(LightPurpleBackground)
            .padding(horizontal = 20.dp)
    ) {
        item { TopSection() }
        item { Spacer(modifier = Modifier.height(30.dp)) }
        item { GreetingSection() }
        item { Spacer(modifier = Modifier.height(20.dp)) }
        item { SummarySection() }
        item { Spacer(modifier = Modifier.height(30.dp)) }
        item { RecentTaskSection() }
        item { TaskCard("Pre Test 1", "09.30 am, 5 June", "6 days left", Color.Gray) }
        item { Spacer(modifier = Modifier.height(16.dp)) }
        item { TaskCard("Client Meeting", "09.30 am - 13.10 pm", "5 days left", Color.Gray) }
        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}

@Composable
private fun TopSection() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 24.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        val taskiveText = buildAnnotatedString {
            withStyle(
                style = SpanStyle(
                    color = MediumPurpleDark // Menggunakan warna dari theme Anda

                )
            ) {
                append("TASK")
            }
            withStyle(
                style = SpanStyle(
                    color = MediumPurpleLight, // Menggunakan warna dari theme Anda
                    // FontFamily dan FontWeight sudah dari displayLarge
                )
            ) {
                append("IVE")
            }
        }

        Text(
            text = taskiveText,
            style = MaterialTheme.typography.displayLarge // Menggunakan style dari Type.kt
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(top = 8.dp)
                .background(Color.White, RoundedCornerShape(16.dp))
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(Color(0xFFFFC107), CircleShape)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "240",
                // Jika titleMedium belum ada di Type.kt, bisa definisikan atau set manual
                fontFamily = Nunito,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = TextColorDarkGlobal
            )
        }
    }
}

@Composable
private fun GreetingSection() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        Text(
            text = "Hi, Hitam",
            fontFamily = Nunito, // Langsung pakai Nunito
            fontWeight = FontWeight.Bold,
            fontSize = 30.sp, // Anda bisa membuat style khusus di Type.kt untuk ini
            color = TextColorDarkGlobal
        )
        Text(
            text = "Monday, 30 May 2025",
            style = MaterialTheme.typography.bodyLarge, // Menggunakan style dari Type.kt
            color = TextColorLightGlobal,
            modifier = Modifier.padding(bottom = 4.dp)
        )
    }
}

@Composable
private fun SummarySection() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        SummaryCard(count = "16", label = "Upcoming")
        SummaryCard(count = "20", label = "Today")
        SummaryCard(count = "150", label = "Completed")
    }
}

@Composable
private fun SummaryCard(count: String, label: String) {
    val gradientBrush = Brush.verticalGradient(
        colors = listOf(MediumPurpleLight, MediumPurpleDark) // Warna gradasi dari theme
    )

    Card(
        modifier = Modifier.size(width = 110.dp, height = 120.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradientBrush)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.Start
            ) {
                Text(count, fontSize = 32.sp, fontFamily = Nunito, fontWeight = FontWeight.Bold, color = Color.White)
                Text("tasks", fontSize = 14.sp, fontFamily = Nunito, color = Color.White.copy(alpha = 0.8f))
                Text(label, fontSize = 16.sp, fontFamily = Nunito, fontWeight = FontWeight.Normal, color = Color.White)
            }
        }
    }
}

@Composable
private fun RecentTaskSection() {
    Row(
        Modifier.fillMaxWidth().padding(bottom = 16.dp),
        Arrangement.SpaceBetween, Alignment.CenterVertically
    ) {
        Text(
            "Recent Task",
            style = MaterialTheme.typography.titleLarge, // Menggunakan style dari Type.kt
            color = TextColorDarkGlobal
        )
        Text(
            "See all",
            fontFamily = Nunito,
            fontSize = 14.sp,
            color = Color.Gray, // Menggunakan warna dari theme
            fontWeight = FontWeight.SemiBold // Nunito tidak punya SemiBold default di R.font
            // Pastikan Anda punya nunito_semibold.ttf atau gunakan Bold/Normal
        )
    }
}

@Composable
private fun TaskCard(title: String, time: String, daysLeft: String, avatarColor: Color) {
    val gradientBrush = Brush.verticalGradient(
        colors = listOf(MediumPurpleLight, MediumPurpleDark) // Warna gradasi dari theme
    )

    Card(
        Modifier.fillMaxWidth(),
        RoundedCornerShape(20.dp),
        CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(gradientBrush)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text(title, fontSize = 18.sp, fontFamily = Nunito, fontWeight = FontWeight.Bold, color = Color.White)
                    Spacer(Modifier.height(4.dp))
                    Text(time, fontSize = 12.sp, fontFamily = Nunito, fontWeight = FontWeight.Normal, color = Color.White.copy(alpha = 0.7f))
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "loremipsumdolorsitametloremipsumdolorsitametloremipsumdolorsitamet",
                        fontSize = 13.sp, fontFamily = Nunito, fontWeight = FontWeight.Normal, color = Color.White.copy(alpha = 0.9f),
                        maxLines = 3, overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(Modifier.width(16.dp))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(Modifier.size(50.dp).background(avatarColor, CircleShape))
                    Spacer(Modifier.height(8.dp))
                    Text(daysLeft, fontSize = 12.sp, fontFamily = Nunito, fontWeight = FontWeight.SemiBold, color = Color.White) // Sama seperti "See all", perhatikan fontWeight
                }
            }
        }
    }
}