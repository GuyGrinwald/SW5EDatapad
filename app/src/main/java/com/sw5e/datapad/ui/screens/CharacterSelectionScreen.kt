package com.sw5e.datapad.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sw5e.datapad.ui.theme.*

data class Character(
    val id: String,
    val name: String,
    val level: Int,
    val species: String,
    val className: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CharacterSelectionScreen(
    characters: List<Character>,
    onCreateNewCharacter: () -> Unit,
    onOpenCharacter: (Character) -> Unit,
    onDeleteCharacter: (Character) -> Unit
) {
    Scaffold(
        containerColor = SpaceBlack,
        topBar = {
            TopAppBar(
                title = { Text("Character Roster", color = NeonAmber) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SpaceBlack)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreateNewCharacter,
                containerColor = NeonAmber,
                contentColor = SpaceBlack
            ) {
                Icon(Icons.Default.Add, contentDescription = "Import Character")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(12.dp)
        ) {
            if (characters.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "No characters found. Tap '+' to import one.",
                        color = CardBorder
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(characters, key = { it.id }) { character ->
                        CharacterCard(
                            character = character,
                            onOpen = { onOpenCharacter(character) },
                            onDelete = { onDeleteCharacter(character) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CharacterCard(
    character: Character,
    onOpen: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, CardBorder, RoundedCornerShape(8.dp)),
        colors = CardDefaults.cardColors(containerColor = SpaceBlack)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = character.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = NeonAmber
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Level ${character.level} • ${character.species} ${character.className}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(onClick = onOpen) {
                    Icon(Icons.Default.FolderOpen, contentDescription = "Open", tint = NeonAmber)
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = SithRed)
                }
            }
        }
    }
}