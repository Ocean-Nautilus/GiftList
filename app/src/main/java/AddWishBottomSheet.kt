package com.example.wishlist228

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddWishBottomSheet(
    onDismiss: () -> Unit,
    onAddWish: (title: String, price: String?, link: String?, priority: WishPriority) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var link by remember { mutableStateOf("") }
    var selectedPriority by remember { mutableStateOf(WishPriority.MEDIUM) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        containerColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth()
        ) {
            Text("Новое желание ✨", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Что вам подарить?") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = price,
                onValueChange = { price = it },
                label = { Text("Примерная цена (₽)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = link,
                onValueChange = { link = it },
                label = { Text("Ссылка на магазин") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text("Желанность:", fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                WishPriority.entries.forEach { priority ->
                    FilterChip(
                        selected = selectedPriority == priority,
                        onClick = { selectedPriority = priority },
                        label = { Text("${priority.emoji} ${priority.label}") },
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onAddWish(title, price, link, selectedPriority)
                        onDismiss()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = OhMyPink),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("Сохранить желание", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}