package com.sw5e.datapad.ui.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.sw5e.datapad.data.EquipmentItem
import com.sw5e.datapad.ui.MainViewModel
import com.sw5e.datapad.ui.theme.*

val EQUIPMENT_TYPES = listOf(
    "Light Armor", "Medium Armor", "Heavy Armor", "Shields",
    "Blasters", "Lightweapons", "Vibroweapons",
    "Focuses", "Adventuring Gear", "Tools & Kits",
    "Substances", "Cybernetics", "Droids"
)

val FILTER_CATEGORIES = listOf("All", "Armor", "Weapons", "Focuses", "Adventuring Gear", "Tools & Kits", "Substances", "Cybernetics", "Droids")
val EQUIPPED_FILTERS = listOf("All", "Equipped", "Unequipped")
val SORT_OPTIONS = listOf("Name", "Value (cr)", "Weight", "Type")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EquipmentScreen(viewModel: MainViewModel) {
    val character by viewModel.character.collectAsState()

    // Dialog states
    var showAddDialog by remember { mutableStateOf(false) }
    var itemToEdit by remember { mutableStateOf<Pair<Int, EquipmentItem>?>(null) }
    var selectedEquipmentForDetail by remember { mutableStateOf<EquipmentItem?>(null) }

    // Filter & Sort states
    var selectedCategory by remember { mutableStateOf("All") }
    var selectedEquippedFilter by remember { mutableStateOf("All") }
    var selectedSortOption by remember { mutableStateOf("Name") }

    // Detail Dialog
    selectedEquipmentForDetail?.let { item ->
        AlertDialog(
            onDismissRequest = { selectedEquipmentForDetail = null },
            title = { Text(item.name, color = HoloBlue, fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Type: ${item.type}", style = MaterialTheme.typography.bodyMedium, color = NeonAmber)
                    Text("Equipped: ${if (item.isEquipped) "Yes" else "No"}", style = MaterialTheme.typography.bodyMedium)
                    Text("Value: ${item.cr} cr", style = MaterialTheme.typography.bodyMedium)
                    Text("Weight: ${item.weight} lb", style = MaterialTheme.typography.bodyMedium)
                }
            },
            confirmButton = {
                Button(onClick = { selectedEquipmentForDetail = null }) {
                    Text("Close")
                }
            }
        )
    }

    // Add Dialog
    if (showAddDialog) {
        EquipmentFormDialog(
            title = "Add Equipment",
            initialItem = EquipmentItem(),
            onDismiss = { showAddDialog = false },
            onSave = { newItem ->
                val currentList = character.equipment
                
                // Fuzzy/Smart match: Check case-insensitive match or plural variations
                val existingIndex = currentList.indexOfFirst { item ->
                    val existingNormalized = item.name.trim().lowercase().removeSuffix("s")
                    val newNormalized = newItem.name.trim().lowercase().removeSuffix("s")
                    existingNormalized == newNormalized
                }

                val updatedList = if (existingIndex != -1) {
                    val existingItem = currentList[existingIndex]
                    currentList.toMutableList().apply {
                        this[existingIndex] = existingItem.copy(quantity = existingItem.quantity + newItem.quantity)
                    }
                } else {
                    currentList + newItem
                }

                viewModel.updateCharacter(character.copy(equipment = updatedList))
                showAddDialog = false
            }
        )
    }

    // Edit Dialog
    itemToEdit?.let { (index, item) ->
        EquipmentFormDialog(
            title = "Edit Equipment",
            initialItem = item,
            onDismiss = { itemToEdit = null },
            onSave = { updatedItem ->
                val updatedList = character.equipment.toMutableList()
                if (index in updatedList.indices) {
                    updatedList[index] = updatedItem
                    viewModel.updateCharacter(character.copy(equipment = updatedList))
                }
                itemToEdit = null
            }
        )
    }

    // Weight Calculation
    val totalWeight = character.equipment.sumOf { it.weight * it.quantity }
    val maxWeight = character.str * 15
    val isEncumbered = totalWeight >= maxWeight

    Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Equipment Inventory", style = MaterialTheme.typography.titleLarge, color = NeonAmber)
                Text(
                    text = "Total Weight: ${if (totalWeight % 1.0 == 0.0) totalWeight.toInt() else totalWeight} / $maxWeight lb",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isEncumbered) SithRed else HoloBlue
                )
            }
            Button(onClick = { showAddDialog = true }) {
                Text("+ Add Item")
            }
        }

        // Filters and Sorting Row / Chips
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            // Category Filter Chips
            Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                FILTER_CATEGORIES.forEach { category ->
                    FilterChip(
                        selected = selectedCategory == category,
                        onClick = { selectedCategory = category },
                        label = { Text(category) }
                    )
                }
            }

            // Equipped & Sort Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                var expandedEquipped by remember { mutableStateOf(false) }
                Box {
                    OutlinedButton(onClick = { expandedEquipped = true }) {
                        Text("Status: $selectedEquippedFilter")
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                    }
                    DropdownMenu(expanded = expandedEquipped, onDismissRequest = { expandedEquipped = false }) {
                        EQUIPPED_FILTERS.forEach { filter ->
                            DropdownMenuItem(
                                text = { Text(filter) },
                                onClick = {
                                    selectedEquippedFilter = filter
                                    expandedEquipped = false
                                }
                            )
                        }
                    }
                }

                var expandedSort by remember { mutableStateOf(false) }
                Box {
                    OutlinedButton(onClick = { expandedSort = true }) {
                        Text("Sort: $selectedSortOption")
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                    }
                    DropdownMenu(expanded = expandedSort, onDismissRequest = { expandedSort = false }) {
                        SORT_OPTIONS.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    selectedSortOption = option
                                    expandedSort = false
                                }
                            )
                        }
                    }
                }
            }
        }

        // Filter and Sort Processing
        val filteredList = character.equipment.filter { item ->
            val matchesCategory = when (selectedCategory) {
                "All" -> true
                "Armor" -> item.type in listOf("Light Armor", "Medium Armor", "Heavy Armor", "Shields")
                "Weapons" -> item.type in listOf("Blasters", "Lightweapons", "Vibroweapons")
                else -> item.type == selectedCategory
            }
            val matchesEquipped = when (selectedEquippedFilter) {
                "All" -> true
                "Equipped" -> item.isEquipped
                "Unequipped" -> !item.isEquipped
                else -> true
            }
            matchesCategory && matchesEquipped
        }

        val sortedList = filteredList.sortedWith(
            when (selectedSortOption) {
                "Value (cr)" -> compareBy<EquipmentItem> { it.cr }.thenBy { it.name.lowercase() }
                "Weight" -> compareBy<EquipmentItem> { it.weight }.thenBy { it.name.lowercase() }
                "Type" -> compareBy<EquipmentItem> { it.type }.thenBy { it.name.lowercase() }
                else -> compareBy { it.name.lowercase() }
            }
        )

        // Equipment List
        LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(sortedList) { item ->
                val originalIndex = character.equipment.indexOf(item)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, CardBorder, RoundedCornerShape(8.dp))
                        .clickable { selectedEquipmentForDetail = item },
                    colors = CardDefaults.cardColors(containerColor = SpaceBlack)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Checkbox(
                                checked = item.isEquipped,
                                onCheckedChange = { checked ->
                                    if (originalIndex != -1) {
                                        val updatedList = character.equipment.toMutableList()
                                        updatedList[originalIndex] = item.copy(isEquipped = checked)
                                        viewModel.updateCharacter(character.copy(equipment = updatedList))
                                    }
                                }
                            )
                            Column {
                                Text("${if (item.quantity > 1) "${item.quantity}x " else ""}${item.name}", fontWeight = FontWeight.Bold, color = HoloBlue)
                                Text("${item.type} | ${item.cr} cr | ${item.weight} lb", style = MaterialTheme.typography.labelSmall, color = NeonAmber)
                            }
                        }

                        Row {
                            IconButton(onClick = {
                                if (originalIndex != -1) {
                                    itemToEdit = Pair(originalIndex, item)
                                }
                            }) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit Equipment", tint = HoloBlue)
                            }
                            
                            IconButton(onClick = {
                                if (originalIndex != -1) {
                                    val updatedList = character.equipment.toMutableList().apply { removeAt(originalIndex) }
                                    viewModel.updateCharacter(character.copy(equipment = updatedList))
                                }
                            }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete Equipment", tint = SithRed)
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EquipmentFormDialog(
    title: String,
    initialItem: EquipmentItem,
    onDismiss: () -> Unit,
    onSave: (EquipmentItem) -> Unit
) {
    var name by remember { mutableStateOf(initialItem.name) }
    var type by remember { mutableStateOf(initialItem.type) }
    var isEquipped by remember { mutableStateOf(initialItem.isEquipped) }
    var crText by remember { mutableStateOf(initialItem.cr.toString()) }
    var weightText by remember { mutableStateOf(initialItem.weight.toString()) }
    var quantityText by remember { mutableStateOf(initialItem.quantity.toString()) }
    var expandedTypeDropdown by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, color = HoloBlue) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Item Name") },
                    modifier = Modifier.fillMaxWidth()
                )

                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(onClick = { expandedTypeDropdown = true }, modifier = Modifier.fillMaxWidth()) {
                        Text("Type: $type")
                        Spacer(modifier = Modifier.weight(1f))
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                    }
                    DropdownMenu(expanded = expandedTypeDropdown, onDismissRequest = { expandedTypeDropdown = false }) {
                        EQUIPMENT_TYPES.forEach { t ->
                            DropdownMenuItem(
                                text = { Text(t) },
                                onClick = {
                                    type = t
                                    expandedTypeDropdown = false
                                }
                            )
                        }
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = crText,
                        onValueChange = { crText = it },
                        label = { Text("Value (cr)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = weightText,
                        onValueChange = { weightText = it },
                        label = { Text("Weight (lb)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = quantityText,
                    onValueChange = { quantityText = it },
                    label = { Text("Quantity") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isEquipped, onCheckedChange = { isEquipped = it })
                    Text("Equipped")
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                if (name.isNotBlank()) {
                    val cr = crText.toDoubleOrNull() ?: 0.0
                    val weight = weightText.toDoubleOrNull() ?: 0.0
                    val quantity = quantityText.toIntOrNull()?.coerceAtLeast(1) ?: 1
                    onSave(EquipmentItem(name = name.trim(), type = type, isEquipped = isEquipped, cr = cr, weight = weight, quantity = quantity))
                }
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