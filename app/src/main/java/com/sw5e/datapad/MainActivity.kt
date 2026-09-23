package com.sw5e.datapad

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.sw5e.datapad.ui.MainViewModel
import com.sw5e.datapad.ui.screens.*
import com.sw5e.datapad.ui.theme.SW5ETheme

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SW5ETheme {
                var selectedTab by remember { mutableIntStateOf(0) }
                val characters by viewModel.characters.collectAsState()
                val selectedCharacter by viewModel.selectedCharacter.collectAsState()

                // Check if a character is actively selected to view
                val isCharacterSelected = selectedCharacter != null
                
                // Only allow viewing Roster(0), Dice(6), and Vault(7) when no character is selected
                val effectiveTab = if (!isCharacterSelected && selectedTab !in listOf(0, 6, 7)) 0 else selectedTab

                LaunchedEffect(isCharacterSelected) {
                    if (!isCharacterSelected && selectedTab !in listOf(0, 6, 7)) {
                        selectedTab = 0
                    }
                }

                val navItems = listOf(
                    NavDestination(0, "Roster", Icons.Default.People),
                    NavDestination(1, "Summary", Icons.Default.Person),
                    NavDestination(2, "Proficiencies", Icons.Default.Psychology),
                    NavDestination(3, "Powers", Icons.Default.Cyclone),
                    NavDestination(4, "Combat", Icons.Default.MilitaryTech),
                    NavDestination(5, "Gear", Icons.Default.Backpack),
                    NavDestination(6, "Dice", Icons.Default.Casino),
                    NavDestination(7, "Vault", Icons.Default.Storage)
                )

                // Dynamically filter tabs based on character selection status
                val visibleNavItems = navItems.filter { 
                    if (isCharacterSelected) true else it.index in listOf(0, 6, 7) 
                }

                Scaffold(
                    bottomBar = {
                        SW5EBottomBar(
                            items = visibleNavItems,
                            selectedTab = effectiveTab,
                            onTabSelected = { selectedTab = it }
                        )
                    }
                ) { padding ->
                    Surface(modifier = Modifier.fillMaxSize().padding(padding)) {
                        when (effectiveTab) {
                            0 -> CharacterSelectionScreen(
                                characters = characters,
                                onImportCharacter = { viewModel.importCharacter("New Recruit", 1, "Human", "Soldier") },
                                onOpenCharacter = { character ->
                                    viewModel.selectCharacter(character)
                                    selectedTab = 1
                                },
                                onDeleteCharacter = { viewModel.deleteCharacter(it) }
                            )
                            1 -> CharacterSummaryScreen(viewModel)
                            2 -> CharacterProficienciesScreen(viewModel)
                            3 -> PowersScreen(viewModel)
                            4 -> CombatStylesScreen(viewModel)
                            5 -> EquipmentScreen(viewModel)
                            6 -> TacticalRollerScreen(viewModel)
                            7 -> DataVaultScreen(viewModel)
                        }
                    }
                }
            }
        }
    }
}

// Extracted helper Composable to keep MainActivity clean and modular
@Composable
private fun SW5EBottomBar(
    items: List<NavDestination>,
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    Surface(
        color = NavigationBarDefaults.containerColor,
        tonalElevation = NavigationBarDefaults.Elevation
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items.forEach { item ->
                val selected = selectedTab == item.index
                Surface(
                    selected = selected,
                    onClick = { onTabSelected(item.index) },
                    shape = RoundedCornerShape(16.dp),
                    color = if (selected) MaterialTheme.colorScheme.secondaryContainer 
                            else MaterialTheme.colorScheme.surface,
                    modifier = Modifier.padding(horizontal = 2.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.label,
                            modifier = Modifier.size(26.dp),
                            tint = if (selected) MaterialTheme.colorScheme.onSecondaryContainer 
                                   else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = item.label,
                            style = MaterialTheme.typography.labelMedium,
                            color = if (selected) MaterialTheme.colorScheme.onSecondaryContainer 
                                   else MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

// Ensure NavDestination tracks the absolute index for routing
private data class NavDestination(val index: Int, val label: String, val icon: ImageVector)