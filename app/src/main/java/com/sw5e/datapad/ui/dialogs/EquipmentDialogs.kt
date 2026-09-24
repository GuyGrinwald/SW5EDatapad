package com.sw5e.datapad.ui.dialogs
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
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
import com.sw5e.datapad.ui.screens.formatCategoryName
import com.sw5e.datapad.ui.screens.getTypesForCategory
import com.sw5e.datapad.ui.theme.*
    
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