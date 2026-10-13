package com.example.wishlist228

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

@Composable
fun WishlistScreen(
    state: WishlistUiState,
    onCategorySelected: (WishCategory) -> Unit,
    onReserveWish: (String) -> Unit,
    onAddWishClick: () -> Unit
) {
    val filteredWishes = remember(state.selectedCategory, state.wishes) {
        state.wishes.filter { it.category == state.selectedCategory?.name }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddWishClick,
                containerColor = OhMyPink,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Добавить желание", modifier = Modifier.size(28.dp))
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = "https://i.pravatar.cc/150?img=9",
                    contentDescription = "Аватар",
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Мои Желания ✨", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text(text = "@alex_wishes • 🎂 15 Октября", fontSize = 13.sp, color = TextSecondary)
                }
                IconButton(
                    onClick = { },
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color.White)
                ) {
                    Icon(Icons.Default.Share, contentDescription = "Поделиться", tint = OhMyPink)
                }
            }

            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                items(state.categories) { category ->
                    val isSelected = category == state.selectedCategory
                    FilterChip(
                        selected = isSelected,
                        onClick = { onCategorySelected(category) },
                        label = {
                            Text(
                                text = category.name,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        shape = RoundedCornerShape(20.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = OhMyPink,
                            selectedLabelColor = Color.White,
                            containerColor = Color.White,
                            labelColor = MaterialTheme.colorScheme.onBackground
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (filteredWishes.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🎁", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Список пока пуст", fontWeight = FontWeight.Bold, color = TextSecondary)
                        Text("Нажмите '+', чтобы добавить желание", fontSize = 12.sp, color = TextSecondary)
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(filteredWishes) { wish ->
                        WishCard(
                            wish = wish,
                            onReserveClick = { onReserveWish(wish.id) }
                        )
                    }
                }
            }
        }
    }
}