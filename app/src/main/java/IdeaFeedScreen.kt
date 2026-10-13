package com.example.wishlist228

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

@Composable
fun IdeaFeedScreen(collections: List<IdeaCollection>) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Text("Идеи подарков 💡", fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text("Вдохновляйтесь подборками редакции", fontSize = 13.sp, color = Color.Gray)
        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            items(collections) { collection ->
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        AsyncImage(
                            model = collection.imageUrl,
                            contentDescription = collection.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.35f))
                        )
                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(16.dp)
                        ) {
                            Text(text = collection.subtitle, color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                            Text(text = collection.title, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}