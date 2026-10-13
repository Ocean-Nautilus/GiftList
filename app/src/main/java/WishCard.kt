package com.example.wishlist228

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

@Composable
fun WishCard(
    wish: WishItem,
    onReserveClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFFF0F0F0))
            ) {
                if (!wish.imageUrl.isNullOrEmpty()) {
                    AsyncImage(
                        model = wish.imageUrl,
                        contentDescription = wish.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Text(
                        text = "🎁",
                        fontSize = 48.sp,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White.copy(alpha = 0.9f),
                    modifier = Modifier
                        .padding(8.dp)
                        .align(Alignment.TopStart)
                ) {
                    Text(
                        text = "${wish.priority.emoji} ${wish.priority.label}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = wish.title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            wish.price?.let {
                Text(
                    text = it,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = OhMyPink,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onReserveClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (wish.isReserved) ReservedColor.copy(alpha = 0.15f) else OhMyPink.copy(alpha = 0.1f),
                        contentColor = if (wish.isReserved) ReservedColor else OhMyPink
                    ),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = if (wish.isReserved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (wish.isReserved) "Забронировано" else "Забронировать",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}