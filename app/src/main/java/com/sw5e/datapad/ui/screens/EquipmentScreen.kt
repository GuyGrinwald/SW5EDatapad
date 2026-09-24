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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EquipmentFormDialog(
    title: String,
    initialItem: EquipmentItem,
    availableWeaponProperties: List<WeaponPropertyDefinition> = emptyList(),
    availableArmorProperties: List<ArmorPropertyDefinition> = emptyList(),
    onDismiss: () -> Unit,
    onSave: (EquipmentItem) -> Unit
) {
    var name by remember { mutableStateOf(initialItem.name) }
    var category by remember { mutableStateOf(initialItem.category) }

    val availableTypes = remember(category) { getTypesForCategory(category) }

    var type by remember { 
        mutableStateOf(
            if (initialItem.type in availableTypes) initialItem.type else availableTypes.first()
        ) 
    }

    var isEquipped by remember { mutableStateOf(initialItem.isEquipped) }
    var crText by remember { mutableStateOf(initialItem.cr.toString()) }
    var weightText by remember { mutableStateOf(initialItem.weight.toString()) }
    var quantityText by remember { mutableStateOf(initialItem.quantity.toString()) }

    // Armor Fields
    var baseAcText by remember { mutableStateOf(initialItem.baseAc.toString()) }
    var dexCapText by remember {
        mutableStateOf(
            if (initialItem.dexCap == null || initialItem.dexCap == 99) "" else initialItem.dexCap.toString()
        )
    }

    // Weapon Fields
    var primaryDice by remember { mutableStateOf(initialItem.primaryDamageDice) }
    var secondaryDice by remember { mutableStateOf(initialItem.secondaryDamageDice ?: "") }
    var damageType by remember { mutableStateOf(initialItem.damageType) }
    var attackBonusText by remember { mutableStateOf(initialItem.attackBonus.toString()) }
    var damageBonusText by remember { mutableStateOf(initialItem.damageBonus.toString()) }
    var customAbilityOverride by remember { mutableStateOf(initialItem.customAbilityOverride ?: "") }

    // Selected Property Tags (Shared for Weapons & Armor)
    var selectedProperties by remember { mutableStateOf(initialItem.properties) }
    var expandedPropertyDropdown by remember { mutableStateOf(false) }

    val hasVersatile = remember(selectedProperties) {
        selectedProperties.any { it.equals("versatile", ignoreCase = true) }
    }

    var expandedTypeDropdown by remember { mutableStateOf(false) }  
    var expandedCategoryDropdown by remember { mutableStateOf(false) }

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

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedButton(onClick = { expandedCategoryDropdown = true }, modifier = Modifier.fillMaxWidth()) {
                            Text(formatCategoryName(category))
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                        }
                        DropdownMenu(expanded = expandedCategoryDropdown, onDismissRequest = { expandedCategoryDropdown = false }) {
                            EquipmentCategory.values().forEach { cat ->
                                DropdownMenuItem(
                                    text = { Text(formatCategoryName(cat)) },
                                    onClick = {
                                        category = cat
                                        val newTypes = getTypesForCategory(cat)
                                        if (type !in newTypes) {
                                            type = newTypes.first()
                                        }
                                        expandedCategoryDropdown = false
                                    }
                                )
                            }
                        }
                    }

                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedButton(onClick = { expandedTypeDropdown = true }, modifier = Modifier.fillMaxWidth()) {
                            Text(type, maxLines = 1)
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                        }
                        DropdownMenu(expanded = expandedTypeDropdown, onDismissRequest = { expandedTypeDropdown = false }) {
                            availableTypes.forEach { t ->
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

                // Armor & Shield Specific Configuration
                if (category == EquipmentCategory.ARMOR || category == EquipmentCategory.SHIELD) {
                    HorizontalDivider(color = CardBorder)
                    Text("Armor & Shield Properties", color = NeonAmber, fontWeight = FontWeight.Bold)

                    OutlinedTextField(
                        value = baseAcText,
                        onValueChange = { baseAcText = it },
                        label = { Text("Base Armor Class (e.g. 14)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = dexCapText,
                        onValueChange = { dexCapText = it },
                        label = { Text("DEX Modifier Cap (Leave blank for no cap)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Armor Property Tags Selector
                    Text("Armor Property Tags", style = MaterialTheme.typography.bodyMedium, color = HoloBlue)

                    if (selectedProperties.isNotEmpty()) {
                        Row(
                            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            selectedProperties.forEach { propName ->
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = SpaceBlack,
                                    border = BorderStroke(1.dp, HoloBlue)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(propName, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Remove tag",
                                            tint = SithRed,
                                            modifier = Modifier
                                                .size(14.dp)
                                                .clickable {
                                                    selectedProperties = selectedProperties.filter { !it.equals(propName, ignoreCase = true) }
                                                }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    val unusedArmorProperties = availableArmorProperties.filter { propDef ->
                        selectedProperties.none { it.equals(propDef.name, ignoreCase = true) }
                    }

                    Box {
                        OutlinedButton(
                            onClick = { expandedPropertyDropdown = true },
                            enabled = unusedArmorProperties.isNotEmpty()
                        ) {
                            Text(if (unusedArmorProperties.isEmpty()) "All Properties Added" else "+ Add Armor Property Tag")
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                        }
                        DropdownMenu(
                            expanded = expandedPropertyDropdown,
                            onDismissRequest = { expandedPropertyDropdown = false }
                        ) {
                            unusedArmorProperties.forEach { propDef ->
                                DropdownMenuItem(
                                    text = { Text(propDef.name, fontWeight = FontWeight.Bold) },
                                    onClick = {
                                        selectedProperties = selectedProperties + propDef.name
                                        expandedPropertyDropdown = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Weapon Specific Configuration
                if (category == EquipmentCategory.WEAPON) {
                    HorizontalDivider(color = CardBorder)
                    Text("Weapon Combat Properties", color = NeonAmber, fontWeight = FontWeight.Bold)

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = primaryDice,
                            onValueChange = { primaryDice = it },
                            label = { Text("Primary Dice (e.g. 1d8)") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = damageType,
                            onValueChange = { damageType = it },
                            label = { Text("Type (Kinetic)") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Text("Weapon Property Tags", style = MaterialTheme.typography.bodyMedium, color = HoloBlue)

                    if (selectedProperties.isNotEmpty()) {
                        Row(
                            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            selectedProperties.forEach { propName ->
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = SpaceBlack,
                                    border = BorderStroke(1.dp, HoloBlue)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(propName, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Remove tag",
                                            tint = SithRed,
                                            modifier = Modifier
                                                .size(14.dp)
                                                .clickable {
                                                    selectedProperties = selectedProperties.filter { !it.equals(propName, ignoreCase = true) }
                                                }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    val unusedWeaponProperties = availableWeaponProperties.filter { propDef ->
                        selectedProperties.none { it.equals(propDef.name, ignoreCase = true) }
                    }

                    Box {
                        OutlinedButton(
                            onClick = { expandedPropertyDropdown = true },
                            enabled = unusedWeaponProperties.isNotEmpty()
                        ) {
                            Text(if (unusedWeaponProperties.isEmpty()) "All Properties Added" else "+ Add Property Tag")
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                        }
                        DropdownMenu(
                            expanded = expandedPropertyDropdown,
                            onDismissRequest = { expandedPropertyDropdown = false }
                        ) {
                            unusedWeaponProperties.forEach { propDef ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(propDef.name, fontWeight = FontWeight.Bold)
                                            if (!propDef.attackAbilityScore.isNullOrBlank()) {
                                                Text("Ability: ${propDef.attackAbilityScore}", style = MaterialTheme.typography.labelSmall, color = NeonAmber)
                                            }
                                        }
                                    },
                                    onClick = {
                                        selectedProperties = selectedProperties + propDef.name
                                        expandedPropertyDropdown = false
                                    }
                                )
                            }
                        }
                    }

                    if (hasVersatile) {
                        OutlinedTextField(
                            value = secondaryDice,
                            onValueChange = { secondaryDice = it },
                            label = { Text("Versatile Dice (e.g. 1d10)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = attackBonusText,
                            onValueChange = { attackBonusText = it },
                            label = { Text("Item Atk Bonus") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = damageBonusText,
                            onValueChange = { damageBonusText = it },
                            label = { Text("Item Dmg Bonus") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    OutlinedTextField(
                        value = customAbilityOverride,
                        onValueChange = { customAbilityOverride = it },
                        label = { Text("Custom Ability Override (e.g. WIS)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isEquipped, onCheckedChange = { isEquipped = it })
                    Text("Equipped", fontWeight = FontWeight.Bold)
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                if (name.isNotBlank()) {
                    val cr = crText.toDoubleOrNull() ?: 0.0
                    val weight = weightText.toDoubleOrNull() ?: 0.0
                    val quantity = quantityText.toIntOrNull()?.coerceAtLeast(1) ?: 1

                    onSave(
                        EquipmentItem(
                            id = initialItem.id,
                            name = name.trim(),
                            type = type,
                            category = category,
                            isEquipped = isEquipped,
                            cr = cr,
                            weight = weight,
                            quantity = quantity,
                            baseAc = baseAcText.toIntOrNull() ?: 0,
                            dexCap = dexCapText.toIntOrNull(),
                            primaryDamageDice = primaryDice.ifBlank { "1d6" },
                            secondaryDamageDice = if (hasVersatile) secondaryDice.ifBlank { null } else null,
                            damageType = damageType.ifBlank { "Kinetic" },
                            properties = if (category == EquipmentCategory.WEAPON || category == EquipmentCategory.ARMOR || category == EquipmentCategory.SHIELD) selectedProperties else emptyList(),
                            attackBonus = attackBonusText.toIntOrNull() ?: 0,
                            damageBonus = damageBonusText.toIntOrNull() ?: 0,
                            customAbilityOverride = customAbilityOverride.trim().ifBlank { null }
                        )
                    )
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