package com.example.composesample.presentation.legacy.sub

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import com.example.data.db.ItemDTO
import org.koin.androidx.compose.koinViewModel
import java.util.UUID

@Composable
fun MainContent() {
    val viewModelStoreOwner = checkNotNull(LocalViewModelStoreOwner.current)
    val model =
        koinViewModel<SubActivityViewModel>(owner = viewModelStoreOwner)
    val textState = remember { mutableStateOf("") }
    val list = model.search(textState.value).collectAsState(initial = emptyList())

    Column(
        modifier = Modifier.padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(onClick = {
                for (i in 1..100) {
                    model.insert(
                        ItemDTO(
                            null, UUID.randomUUID().toString()
                        )
                    )
                }
            }) { Text(text = "Add 100 items") }

            Button(onClick = { model.clear() }) {
                Text(text = "Clear")
            }
        }

        Text(
            text = if (textState.value.trim().isEmpty()) {
                "ItemDTO"
            } else {
                "ItemDTO start with (${textState.value.trim()})"
            } + " ${list.value.size}",
            fontWeight = FontWeight.Bold
        )

        Row {
            TextField(
                value = textState.value,
                onValueChange = { textState.value = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(text = "Search ItemDTO") }
            )
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            itemsIndexed(list.value) { _, item ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = Color(0xFFDCE2C9)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF8DB600)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${item.id}",
                                style = MaterialTheme.typography.h6
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = item.uniqueId.take(12),
                            style = MaterialTheme.typography.h6
                        )

                        Spacer(modifier = Modifier.weight(1F))
                        IconButton(onClick = {
                            val updatedItem = ItemDTO(
                                item.id, UUID.randomUUID().toString()
                            )
                            model.update(updatedItem)
                        }) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = ""
                            )
                        }

                        Spacer(modifier = Modifier.weight(1F))
                        IconButton(onClick = {
                            model.delete(item)
                        }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "",
                                tint = Color(0xFFD3212D)
                            )
                        }
                    }
                }
            }
        }
    }
}