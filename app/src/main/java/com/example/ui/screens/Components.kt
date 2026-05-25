package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

// Theme color constants for matching the organic GreenMart branding
val ForestGreen = Color(0xFF065F46)
val LimeGreen = Color(0xFF10B981)
val OrganicAmber = Color(0xFFFACC15)
val SoftCream = Color(0xFFF0FDF4)
val DeepText = Color(0xFF022C22)
val SoftGrayBackground = Color(0xFFFDFCF5)

// Premium UI Gradients
val GreenGradientPrimary = Brush.linearGradient(
    colors = listOf(Color(0xFF065F46), Color(0xFF059669))
)
val GoldGradientAccent = Brush.horizontalGradient(
    colors = listOf(Color(0xFFFACC15), Color(0xFFFDE047))
)
val FreshSipGradient = Brush.verticalGradient(
    colors = listOf(Color(0xFFF0FDF4), Color(0xFFD1FAE5))
)

@Composable
fun GreenMartImage(
    url: String,
    contentDescription: String,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        AsyncImage(
            model = url,
            contentDescription = contentDescription,
            modifier = Modifier.fillMaxSize(),
            contentScale = androidx.compose.ui.layout.ContentScale.Crop
        )
    }
}

@Composable
fun LoyaltyTierChip(tier: String) {
    val (color, text) = when (tier) {
        "Vàng" -> Color(0xFFFFD700) to "Hạng Vàng ⭐"
        "Kim Cương" -> Color(0xFF00E5FF) to "Hạng Kim Cương 💎"
        else -> Color(0xFFCD7F32) to "Hạng Bạc ☘️"
    }
    
    Box(
        modifier = Modifier
            .background(color.copy(alpha = 0.25f), shape = CircleShape)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = text,
            color = DeepText,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun SectionHeader(title: String, subtitle: String? = null) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = DeepText,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp
        )
        if (subtitle != null) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                fontSize = 13.sp
            )
        }
    }
}

@Composable
fun InfoDialog(
    show: Boolean,
    title: String,
    message: String,
    onDismiss: () -> Unit
) {
    if (show) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    color = ForestGreen
                )
            },
            text = {
                Text(text = message, fontSize = 15.sp, color = Color.DarkGray)
            },
            confirmButton = {
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = ForestGreen)
                ) {
                    Text("Đóng", color = Color.White)
                }
            },
            containerColor = SoftCream,
            shape = MaterialTheme.shapes.large
        )
    }
}
