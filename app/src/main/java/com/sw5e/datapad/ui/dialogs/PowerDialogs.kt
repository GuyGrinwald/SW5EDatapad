package com.sw5e.datapad.ui.dialogs

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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.sw5e.datapad.data.PowerDefinition
import com.sw5e.datapad.ui.theme.*
    
@Composable
fun ForceAttackBonusEditDialog(
    currentBonus: Int,
    onDismiss: () -> Unit,
    onSave: (Int) -> Unit
) {
    var bonusText by remember { mutableStateOf(currentBonus.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Force Attack Special Bonus") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = bonusText,
                    onValueChange = { bonusText = it },
                    label = { Text("Force Special Bonus") },
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
fun TechAttackBonusEditDialog(
    currentBonus: Int,
    onDismiss: () -> Unit,
    onSave: (Int) -> Unit
) {
    var bonusText by remember { mutableStateOf(currentBonus.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Tech Attack Special Bonus") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = bonusText,
                    onValueChange = { bonusText = it },
                    label = { Text("Tech Special Bonus") },
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
fun PowerDetailDialog(
    powerName: String,
    powerDefinition: PowerDefinition?,
    onDismiss: () -> Unit,
    onAdd: (() -> Unit)? = null
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(powerDefinition?.name ?: powerName, color = HoloBlue, fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.fillMaxWidth().height(360.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (powerDefinition != null) {
                    val lvlText = if (powerDefinition.levelInt == 0) "At-Will" else "Level ${powerDefinition.levelInt}"
                    val alignStr = powerDefinition.type.takeIf { it.isNotBlank() }?.let { "Align: $it" } ?: ""
                    val castStr = powerDefinition.castingTime?.takeIf { it.isNotBlank() }?.let { "Cast: $it" } ?: ""
                    val rangeStr = powerDefinition.range.takeIf { it.isNotBlank() }?.let { "Range: $it" } ?: ""
                    val durStr = powerDefinition.duration.takeIf { it.isNotBlank() }?.let { "Dur: $it" } ?: ""
                    val metaInfo = listOf("Level: $lvlText", alignStr, castStr, rangeStr, durStr).filter { it.isNotEmpty() }.joinToString("  |  ")
                    
                    Text(metaInfo, style = MaterialTheme.typography.labelSmall, color = NeonAmber)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Description:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = HoloBlue)
                    Text(powerDefinition.description, style = MaterialTheme.typography.bodyMedium)
                } else {
                    Text("No additional details available in compendium.", style = MaterialTheme.typography.bodyMedium)
                }
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (onAdd != null) {
                    Button(onClick = {
                        onAdd()
                    }) {
                        Text("Add Power")
                    }
                }
                Button(onClick = onDismiss) {
                    Text("Close")
                }
            }
        }
    )
}

@Composable
fun ResourceEditDialog(character: com.sw5e.datapad.data.CharacterEntity, onDismiss: () -> Unit, onSave: (com.sw5e.datapad.data.CharacterEntity) -> Unit) {
    var maxForce by remember { mutableStateOf(character.maxForcePoints.toString()) }
    var curForce by remember { mutableStateOf(character.currentForcePoints.toString()) }
    var maxTech by remember { mutableStateOf(character.maxTechPoints.toString()) }
    var curTech by remember { mutableStateOf(character.currentTechPoints.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Casting Resources") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = maxForce, onValueChange = { maxForce = it }, label = { Text("Max Force Points") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                OutlinedTextField(value = curForce, onValueChange = { curForce = it }, label = { Text("Current Force Points") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                OutlinedTextField(value = maxTech, onValueChange = { maxTech = it }, label = { Text("Max Tech Points") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                OutlinedTextField(value = curTech, onValueChange = { curTech = it }, label = { Text("Current Tech Points") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
            }
        },
        confirmButton = {
            Button(onClick = {
                onSave(character.copy(
                    maxForcePoints = maxForce.toIntOrNull() ?: character.maxForcePoints,
                    currentForcePoints = curForce.toIntOrNull() ?: character.currentForcePoints,
                    maxTechPoints = maxTech.toIntOrNull() ?: character.maxTechPoints,
                    currentTechPoints = curTech.toIntOrNull() ?: character.currentTechPoints
                ))
            }) { Text("Save") }
        }
    )
}

@Composable
fun AddPowerDialog(
    title: String,
    powersDict: Map<String, PowerDefinition>,
    currentPowers: Map<Int, List<String>>,
    onDismiss: () -> Unit,
    onSave: (Int, String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedLevel by remember { mutableIntStateOf(0) }
    var viewingPower by remember { mutableStateOf<PowerDefinition?>(null) }

    if (viewingPower != null) {
        PowerDetailDialog(
            powerName = viewingPower!!.name,
            powerDefinition = viewingPower,
            onDismiss = { viewingPower = null },
            onAdd = {
                onSave(viewingPower!!.levelInt, viewingPower!!.name)
                viewingPower = null
            }
        )
    }

    val availablePowers = powersDict.values.filter { power: PowerDefinition ->
        val alreadyHave = currentPowers[power.levelInt]?.contains(power.name) == true
        !alreadyHave && power.name.contains(searchQuery, ignoreCase = true)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(modifier = Modifier.fillMaxWidth().height(400.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    (0..9).forEach { lvl ->
                        FilterChip(
                            selected = selectedLevel == lvl,
                            onClick = { selectedLevel = lvl },
                            label = { Text(if (lvl == 0) "At-Will" else "$lvl") }
                        )
                    }
                }
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Search Powers") },
                    modifier = Modifier.fillMaxWidth()
                )
                LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    items(availablePowers.filter { it.levelInt == selectedLevel }.sortedBy { it.name }) { power ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, CardBorder, RoundedCornerShape(8.dp))
                                .clickable { viewingPower = power },
                            colors = CardDefaults.cardColors(containerColor = SpaceBlack)
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text(power.name, fontWeight = FontWeight.Bold, color = HoloBlue)
                                val alignStr = power.type.takeIf { it.isNotBlank() }?.let { "Align: $it" } ?: ""
                                val castStr = power.castingTime?.takeIf { it.isNotBlank() }?.let { "Cast: $it" } ?: ""
                                val rangeStr = power.range.takeIf { it.isNotBlank() }?.let { "Range: $it" } ?: ""
                                val durStr = power.duration.takeIf { it.isNotBlank() }?.let { "Dur: $it" } ?: ""
                                val infoStr = listOf(alignStr, castStr, rangeStr, durStr).filter { it.isNotEmpty() }.joinToString(" | ")
                                Text(infoStr, style = MaterialTheme.typography.labelSmall, color = NeonAmber)
                                Text(power.description, style = MaterialTheme.typography.bodySmall, maxLines = 2)
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