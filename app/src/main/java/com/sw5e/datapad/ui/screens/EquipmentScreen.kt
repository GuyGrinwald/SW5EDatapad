package com.sw5e.datapad.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.sw5e.datapad.data.ArmorPropertyDefinition
import com.sw5e.datapad.data.EquipmentCategory
import com.sw5e.datapad.data.EquipmentItem
import com.sw5e.datapad.data.WeaponPropertyDefinition
import com.sw5e.datapad.ui.MainViewModel
import com.sw5e.datapad.ui.theme.*
import com.sw5e.datapad.ui.dialogs.*

fun getTypesForCategory(category: EquipmentCategory): List<String> {
    return when (category) {
        EquipmentCategory.ARMOR -> listOf("Light Armor", "Medium Armor", "Heavy Armor")
        EquipmentCategory.WEAPON -> listOf("Improvised", "Blasters", "Lightweapons", "Vibroweapons", "Special")
        EquipmentCategory.SHIELD -> listOf("Shields")
        EquipmentCategory.AMMUNITION -> listOf("Ammunition")
        EquipmentCategory.EXPLOSIVES -> listOf("Explosives")   
        EquipmentCategory.STORAGE -> listOf("Storage")
        EquipmentCategory.COMMUNICATIONS -> listOf("Communications")
        EquipmentCategory.MEDICAL -> listOf("Medical")
        EquipmentCategory.OTHER -> listOf(
            "Focuses", "Adventuring Gear", "Tools & Kits",
            "Substances", "Enhancements", "Other"
        )
    }
}

fun formatCategoryName(category: EquipmentCategory): String {
    return when (category) {
        EquipmentCategory.ARMOR -> "Armor"
        EquipmentCategory.WEAPON -> "Weapon"
        EquipmentCategory.SHIELD -> "Shield"
        EquipmentCategory.AMMUNITION -> "Ammunition"
        EquipmentCategory.EXPLOSIVES -> "Explosives"
        EquipmentCategory.STORAGE -> "Storage"
        EquipmentCategory.COMMUNICATIONS -> "Communications"
        EquipmentCategory.MEDICAL -> "Medical"
        EquipmentCategory.OTHER -> "Other"
    }
}

val FILTER_CATEGORIES = listOf(
    "All", "Armor", "Weapons", "Shields", "Other"
)

val EQUIPPED_FILTERS = listOf("All", "Equipped", "Unequipped")
val SORT_OPTIONS = listOf("Name", "Value (cr)", "Weight", "Type")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EquipmentScreen(viewModel: MainViewModel) {
    val character by viewModel.character.collectAsState()
    val availableWeaponProperties by viewModel.weaponPropertiesList.collectAsState()
    val availableArmorProperties by viewModel.armorPropertiesList.collectAsState()
    val weaponPropertiesDict by viewModel.weaponPropertiesDict.collectAsState()
    val armorPropertiesDict by viewModel.armorPropertiesDict.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var itemToEdit by remember { mutableStateOf<Pair<Int, EquipmentItem>?>(null) }
    var selectedEquipmentForDetail by remember { mutableStateOf<EquipmentItem?>(null) }

    // Dialog state for viewing property details
    var selectedPropertyInfo by remember { mutableStateOf<Pair<String, String>?>(null) }

    var selectedCategory by remember { mutableStateOf("All") }
    var selectedEquippedFilter by remember { mutableStateOf("All") }
    var selectedSortOption by remember { mutableStateOf("Name") }

    selectedEquipmentForDetail?.let { item ->
        AlertDialog(
            onDismissRequest = { selectedEquipmentForDetail = null },
            title = { Text(item.name, color = HoloBlue, fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Type: ${item.type} (${formatCategoryName(item.category)})", style = MaterialTheme.typography.bodyMedium, color = NeonAmber)
                    Text("Equipped: ${if (item.isEquipped) "Yes" else "No"}", style = MaterialTheme.typography.bodyMedium)
                    Text("Value: ${item.cr} cr | Weight: ${item.weight} lb", style = MaterialTheme.typography.bodyMedium)

                    if (item.category == EquipmentCategory.ARMOR || item.category == EquipmentCategory.SHIELD) {
                        Text("Base AC: ${item.baseAc}", style = MaterialTheme.typography.bodyMedium)
                        Text("DEX Cap: ${item.dexCap ?: "No Cap"}", style = MaterialTheme.typography.bodyMedium)
                    }

                    if (item.category == EquipmentCategory.WEAPON) {
                        Text("Primary Damage: ${item.primaryDamageDice} ${item.damageType}", style = MaterialTheme.typography.bodyMedium)
                        item.secondaryDamageDice?.takeIf { it.isNotBlank() }?.let {
                            Text("Versatile Damage: $it ${item.damageType}", style = MaterialTheme.typography.bodyMedium)
                        }
                        Text("Bonus: +${item.attackBonus} Atk / +${item.damageBonus} Dmg", style = MaterialTheme.typography.bodyMedium)
                    }

                    // Properties Chip Row (Works for Armor and Weapon)
                    if (item.properties.isNotEmpty()) {
                        Text("Properties:", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = HoloBlue)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(item.properties) { prop ->
                                val cleanKey = prop.lowercase().trim()
                                val baseKey = cleanKey.split(" ", "(").firstOrNull() ?: cleanKey
                                
                                val isWeapon = item.category == EquipmentCategory.WEAPON
                                val title = if (isWeapon) {
                                    weaponPropertiesDict[cleanKey]?.name ?: weaponPropertiesDict[baseKey]?.name ?: prop
                                } else {
                                    armorPropertiesDict[cleanKey]?.name ?: armorPropertiesDict[baseKey]?.name ?: prop
                                }
                                val desc = if (isWeapon) {
                                    weaponPropertiesDict[cleanKey]?.description ?: weaponPropertiesDict[baseKey]?.description ?: "No description available."
                                } else {
                                    armorPropertiesDict[cleanKey]?.description ?: armorPropertiesDict[baseKey]?.description ?: "No description available."
                                }

                                SuggestionChip(
                                    onClick = { selectedPropertyInfo = title to desc },
                                    label = { Text(prop, style = MaterialTheme.typography.labelSmall) }
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { selectedEquipmentForDetail = null }) {
                    Text("Close")
                }
            }
        )
    }

    if (showAddDialog) {
        EquipmentFormDialog(
            title = "Add Equipment",
            initialItem = EquipmentItem(),
            availableWeaponProperties = availableWeaponProperties,
            availableArmorProperties = availableArmorProperties,
            onDismiss = { showAddDialog = false },
            onSave = { newItem ->
                val currentList = character.equipment
                val existingIndex = currentList.indexOfFirst { item ->
                    item.name.trim().lowercase().removeSuffix("s") == newItem.name.trim().lowercase().removeSuffix("s")
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

    itemToEdit?.let { (index, item) ->
        EquipmentFormDialog(
            title = "Edit Equipment",
            initialItem = item,
            availableWeaponProperties = availableWeaponProperties,
            availableArmorProperties = availableArmorProperties,
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

        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                FILTER_CATEGORIES.forEach { category ->
                    FilterChip(
                        selected = selectedCategory == category,
                        onClick = { selectedCategory = category },
                        label = { Text(category) }
                    )
                }
            }

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

        val filteredList = character.equipment.filter { item ->
            val matchesCategory = when (selectedCategory) {
                "All" -> true
                "Armor" -> item.category == EquipmentCategory.ARMOR
                "Shields" -> item.category == EquipmentCategory.SHIELD
                "Weapons" -> item.category == EquipmentCategory.WEAPON
                "Other" -> item.category != EquipmentCategory.ARMOR &&
                        item.category != EquipmentCategory.SHIELD &&
                        item.category != EquipmentCategory.WEAPON
                else -> item.type.equals(selectedCategory, ignoreCase = true)
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
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
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
                                    Text("${item.type} (${formatCategoryName(item.category)}) | ${item.cr} cr | ${item.weight} lb", style = MaterialTheme.typography.labelSmall, color = NeonAmber)
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

                        // Display property chips directly on the card
                        if (item.properties.isNotEmpty()) {
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                items(item.properties) { prop ->
                                    val cleanKey = prop.lowercase().trim()
                                    val baseKey = cleanKey.split(" ", "(").firstOrNull() ?: cleanKey
                                    
                                    val isWeapon = item.category == EquipmentCategory.WEAPON
                                    val title = if (isWeapon) {
                                        weaponPropertiesDict[cleanKey]?.name ?: weaponPropertiesDict[baseKey]?.name ?: prop
                                    } else {
                                        armorPropertiesDict[cleanKey]?.name ?: armorPropertiesDict[baseKey]?.name ?: prop
                                    }
                                    val desc = if (isWeapon) {
                                        weaponPropertiesDict[cleanKey]?.description ?: weaponPropertiesDict[baseKey]?.description ?: "No description available."
                                    } else {
                                        armorPropertiesDict[cleanKey]?.description ?: armorPropertiesDict[baseKey]?.description ?: "No description available."
                                    }

                                    SuggestionChip(
                                        onClick = { selectedPropertyInfo = title to desc },
                                        label = { Text(prop, style = MaterialTheme.typography.labelSmall) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Tag Description Dialog
    selectedPropertyInfo?.let { (title, description) ->
        AlertDialog(
            onDismissRequest = { selectedPropertyInfo = null },
            title = { Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = HoloBlue) },
            text = { Text(description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface) },
            confirmButton = {
                TextButton(onClick = { selectedPropertyInfo = null }) {
                    Text("Close", color = NeonAmber, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = SpaceBlack,
            shape = RoundedCornerShape(12.dp)
        )
    }
}