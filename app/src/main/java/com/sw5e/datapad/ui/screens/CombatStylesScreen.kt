package com.sw5e.datapad.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.sw5e.datapad.ui.MainViewModel
import com.sw5e.datapad.ui.theme.*

@Composable
fun CombatStylesScreen(viewModel: MainViewModel) {
    val character by viewModel.character.collectAsState()
    val fightingStyles by viewModel.fightingStyles.collectAsState()
    val fightingMasteries by viewModel.fightingMasteries.collectAsState()
    val lightsaberForms by viewModel.lightsaberForms.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }

    var showAddStyleDialog by remember { mutableStateOf(false) }
    var showAddMasteryDialog by remember { mutableStateOf(false) }
    var showAddFormDialog by remember { mutableStateOf(false) }
    var showAttackBonusDialog by remember { mutableStateOf(false) }

    // Dialog for editing Special Attack Bonus
    if (showAttackBonusDialog) {
        AttackBonusEditDialog(
            currentBonus = character.attackSpecialBonus,
            onDismiss = { showAttackBonusDialog = false },
            onSave = { newBonus ->
                viewModel.updateCharacter(character.copy(attackSpecialBonus = newBonus))
                showAttackBonusDialog = false
            }
        )
    }

    // Dialog for adding Styles
    if (showAddStyleDialog) {
        val availableStyles = fightingStyles.filter { !character.fightingStyles.contains(it.name) }
        TechniqueAddDialog(
            title = "Add Fighting Style",
            items = availableStyles,
            getName = { it.name },
            getPrerequisite = { null },
            getDescription = { it.description },
            onDismiss = { showAddStyleDialog = false },
            onAdd = { name ->
                viewModel.toggleFightingStyle(name)
                // Modal stays open to allow adding more
            }
        )
    }

    // Dialog for adding Masteries
    if (showAddMasteryDialog) {
        val availableMasteries = fightingMasteries.filter { !character.fightingMasteries.contains(it.name) }
        TechniqueAddDialog(
            title = "Add Fighting Mastery",
            items = availableMasteries,
            getName = { it.name },
            getPrerequisite = { null },
            getDescription = { it.description },
            onDismiss = { showAddMasteryDialog = false },
            onAdd = { name ->
                viewModel.toggleFightingMastery(name)
                // Modal stays open to allow adding more
            }
        )
    }

    // Dialog for adding Forms
    if (showAddFormDialog) {
        val availableForms = lightsaberForms.filter { !character.lightsaberForms.contains(it.name) }
        TechniqueAddDialog(
            title = "Add Lightsaber Form",
            items = availableForms,
            getName = { it.name },
            getPrerequisite = { it.prerequisite },
            getDescription = { it.description },
            onDismiss = { showAddFormDialog = false },
            onAdd = { name ->
                viewModel.toggleLightsaberForm(name)
                // Modal stays open to allow adding more
            }
        )
    }

    // Calculations for Attack Bonuses
    val profBonus = viewModel.getProficiencyBonus()
    val strMod = viewModel.getAttributeModifier(character.str)
    val dexMod = viewModel.getAttributeModifier(character.dex)
    val specialBonus = character.attackSpecialBonus

    val meleeBonus = strMod + profBonus + specialBonus
    val rangedBonus = dexMod + profBonus + specialBonus
    val finesseBonus = maxOf(strMod, dexMod) + profBonus + specialBonus
    val thrownBonus = strMod + profBonus + specialBonus

    fun formatBonus(v: Int) = if (v >= 0) "+$v" else "$v"

    Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Attack Bonuses Summary Card (Similar to PowersScreen Save DCs card)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, CardBorder, RoundedCornerShape(12.dp))
                .clickable { showAttackBonusDialog = true },
            colors = CardDefaults.cardColors(containerColor = SpaceBlack)
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("ATTACK BONUSES", style = MaterialTheme.typography.titleSmall, color = NeonAmber, fontWeight = FontWeight.Bold)
                    Text("Special: ${formatBonus(specialBonus)} (Tap to edit)", style = MaterialTheme.typography.labelSmall, color = HoloBlue)
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Melee: ${formatBonus(meleeBonus)}", style = MaterialTheme.typography.labelSmall, color = HoloBlue)
                        Text("Ranged: ${formatBonus(rangedBonus)}", style = MaterialTheme.typography.labelSmall, color = HoloBlue)
                    }
                    Column {
                        Text("Finesse: ${formatBonus(finesseBonus)}", style = MaterialTheme.typography.labelSmall, color = HoloBlue)
                        Text("Thrown: ${formatBonus(thrownBonus)}", style = MaterialTheme.typography.labelSmall, color = HoloBlue)
                    }
                }
            }
        }

        TabRow(selectedTabIndex = selectedTab, containerColor = SpaceBlack, contentColor = HoloBlue) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Styles (${character.fightingStyles.size})") }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Masteries (${character.fightingMasteries.size})") }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text("Forms (${character.lightsaberForms.size})") }
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        when (selectedTab) {
            0 -> CombatTechniqueTabContent(
                title = "Fighting Styles",
                addedItemNames = character.fightingStyles,
                availableItems = fightingStyles,
                getName = { it.name },
                getPrerequisite = { null },
                getDescription = { it.description },
                onAddClick = { showAddStyleDialog = true },
                onRemove = { viewModel.toggleFightingStyle(it) }
            )
            1 -> CombatTechniqueTabContent(
                title = "Fighting Masteries",
                addedItemNames = character.fightingMasteries,
                availableItems = fightingMasteries,
                getName = { it.name },
                getPrerequisite = { null },
                getDescription = { it.description },
                onAddClick = { showAddMasteryDialog = true },
                onRemove = { viewModel.toggleFightingMastery(it) }
            )
            2 -> CombatTechniqueTabContent(
                title = "Lightsaber Forms",
                addedItemNames = character.lightsaberForms,
                availableItems = lightsaberForms,
                getName = { it.name },
                getPrerequisite = { it.prerequisite },
                getDescription = { it.description },
                onAddClick = { showAddFormDialog = true },
                onRemove = { viewModel.toggleLightsaberForm(it) }
            )
        }
    }
}

@Composable
private fun AttackBonusEditDialog(
    currentBonus: Int,
    onDismiss: () -> Unit,
    onSave: (Int) -> Unit
) {
    var bonusText by remember { mutableStateOf(currentBonus.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Attack Special Bonus") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = bonusText,
                    onValueChange = { bonusText = it },
                    label = { Text("Special Bonus") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                val parsed = bonusText.toIntOrNull() ?: currentBonus
                onSave(parsed)
            }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun <T> CombatTechniqueTabContent(
    title: String,
    addedItemNames: List<String>,
    availableItems: List<T>,
    getName: (T) -> String,
    getPrerequisite: (T) -> String?,
    getDescription: (T) -> String,
    onAddClick: () -> Unit,
    onRemove: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            TextButton(onClick = onAddClick) {
                Text("+ Add", color = MaterialTheme.colorScheme.primary)
            }
        }

        if (addedItemNames.isEmpty()) {
            Text(
                "None added.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(addedItemNames) { itemName ->
                    val itemData = availableItems.find { getName(it) == itemName }
                    val subtitle = itemData?.let { getPrerequisite(it) }?.takeIf { it.isNotBlank() }?.let { "Prerequisite: $it" }

                    ExpandableActiveTechniqueCard(
                        name = itemName,
                        subtitle = subtitle,
                        description = itemData?.let { getDescription(it) } ?: "",
                        onRemove = { onRemove(itemName) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ExpandableActiveTechniqueCard(
    name: String,
    subtitle: String?,
    description: String,
    onRemove: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded }
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    if (!subtitle.isNullOrBlank()) {
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = if (expanded) "Collapse" else "Expand",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    IconButton(onClick = onRemove) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Remove",
                            tint = SithRed
                        )
                    }
                }
            }
            AnimatedVisibility(visible = expanded) {
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun <T> TechniqueAddDialog(
    title: String,
    items: List<T>,
    getName: (T) -> String,
    getPrerequisite: (T) -> String?,
    getDescription: (T) -> String,
    onDismiss: () -> Unit,
    onAdd: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var viewingItem by remember { mutableStateOf<T?>(null) }

    if (viewingItem != null) {
        AlertDialog(
            onDismissRequest = { viewingItem = null },
            title = { Text(getName(viewingItem!!), color = HoloBlue, fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth().height(360.dp).verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    getPrerequisite(viewingItem!!)?.takeIf { it.isNotBlank() }?.let {
                        Text("Prerequisite: $it", style = MaterialTheme.typography.labelSmall, color = NeonAmber)
                    }
                    Text("Description:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = HoloBlue)
                    Text(getDescription(viewingItem!!), style = MaterialTheme.typography.bodyMedium)
                }
            },
            confirmButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = {
                        onAdd(getName(viewingItem!!))
                        viewingItem = null
                    }) {
                        Text("Add")
                    }
                    Button(onClick = { viewingItem = null }) {
                        Text("Close")
                    }
                }
            }
        )
    }

    val availableItems = items.filter { getName(it).contains(searchQuery, ignoreCase = true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(modifier = Modifier.fillMaxWidth().height(400.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Search") },
                    modifier = Modifier.fillMaxWidth()
                )
                LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    items(availableItems) { item ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, CardBorder, RoundedCornerShape(8.dp))
                                .clickable { viewingItem = item },
                            colors = CardDefaults.cardColors(containerColor = SpaceBlack)
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text(getName(item), fontWeight = FontWeight.Bold, color = HoloBlue)
                                getPrerequisite(item)?.takeIf { it.isNotBlank() }?.let {
                                    Text("Prerequisite: $it", style = MaterialTheme.typography.labelSmall, color = NeonAmber)
                                }
                                Text(getDescription(item), style = MaterialTheme.typography.bodySmall, maxLines = 2)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}