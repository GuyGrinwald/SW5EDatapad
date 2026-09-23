package com.sw5e.datapad.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.sw5e.datapad.data.PowerDefinition
import com.sw5e.datapad.ui.MainViewModel
import com.sw5e.datapad.ui.theme.*


@Composable
fun PowersScreen(viewModel: MainViewModel) {
    val character by viewModel.character.collectAsState()
    val forcePowersDict by viewModel.forcePowersDictionary.collectAsState()
    val techPowersDict by viewModel.techPowersDictionary.collectAsState()

    var showResourceDialog by remember { mutableStateOf(false) }
    var showAddForceDialog by remember { mutableStateOf(false) }
    var showAddTechDialog by remember { mutableStateOf(false) }
    var showForceAttackBonusDialog by remember { mutableStateOf(false) }
    var showTechAttackBonusDialog by remember { mutableStateOf(false) }

    // Sub-tab state (0: Force Powers, 1: Tech Powers)
    var selectedPowerTabIndex by remember { mutableIntStateOf(0) }
    val powerTabs = listOf("Force Powers", "Tech Powers")

    // Dynamic Save DC & Attack Auto-Calculations via ViewModel helper methods
    val profBonus = viewModel.getProficiencyBonus()

    val wisMod = viewModel.getAttributeModifier(character.wis)
    val chaMod = viewModel.getAttributeModifier(character.cha)
    val intMod = viewModel.getAttributeModifier(character.intStat)

    val lightForceDc = 8 + profBonus + wisMod
    val darkForceDc = 8 + profBonus + chaMod
    val universalForceDc = 8 + profBonus + maxOf(wisMod, chaMod)
    val techSaveDc = 8 + profBonus + intMod

    // Force Attack Calculations
    val forceSpecialBonus = character.forceAttackSpecialBonus
    val lightForceAttack = wisMod + profBonus + forceSpecialBonus
    val darkForceAttack = chaMod + profBonus + forceSpecialBonus
    val universalForceAttack = maxOf(wisMod, chaMod) + profBonus + forceSpecialBonus

    // Tech Attack Calculations
    val techSpecialBonus = character.techAttackSpecialBonus
    val techAttackBonus = intMod + profBonus + techSpecialBonus

    fun formatBonus(v: Int) = if (v >= 0) "+$v" else "$v"

    if (showForceAttackBonusDialog) {
        ForceAttackBonusEditDialog(
            currentBonus = character.forceAttackSpecialBonus,
            onDismiss = { showForceAttackBonusDialog = false },
            onSave = { newBonus ->
                viewModel.updateCharacter(character.copy(forceAttackSpecialBonus = newBonus))
                showForceAttackBonusDialog = false
            }
        )
    }

    if (showTechAttackBonusDialog) {
        TechAttackBonusEditDialog(
            currentBonus = character.techAttackSpecialBonus,
            onDismiss = { showTechAttackBonusDialog = false },
            onSave = { newBonus ->
                viewModel.updateCharacter(character.copy(techAttackSpecialBonus = newBonus))
                showTechAttackBonusDialog = false
            }
        )
    }

    if (showResourceDialog) {
        ResourceEditDialog(
            character = character,
            onDismiss = { showResourceDialog = false },
            onSave = { updated -> 
                viewModel.updateCharacter(updated)
                showResourceDialog = false 
            }
        )
    }

    if (showAddForceDialog) {
        AddPowerDialog(
            title = "Add Force Power",
            powersDict = forcePowersDict,
            currentPowers = character.forcePowers,
            onDismiss = { showAddForceDialog = false },
            onSave = { powerLevel, powerName ->
                val map = character.forcePowers.toMutableMap()
                val list = map[powerLevel]?.toMutableList() ?: mutableListOf()
                if (!list.contains(powerName)) {
                    list.add(powerName)
                    map[powerLevel] = list
                    viewModel.updateCharacter(character.copy(forcePowers = map))
                }
                // Removed closing dialog here so the modal stays open for more adding
            }
        )
    }

    if (showAddTechDialog) {
        AddPowerDialog(
            title = "Add Tech Power",
            powersDict = techPowersDict,
            currentPowers = character.techPowers,
            onDismiss = { showAddTechDialog = false },
            onSave = { powerLevel, powerName ->
                val map = character.techPowers.toMutableMap()
                val list = map[powerLevel]?.toMutableList() ?: mutableListOf()
                if (!list.contains(powerName)) {
                    list.add(powerName)
                    map[powerLevel] = list
                    viewModel.updateCharacter(character.copy(techPowers = map))
                }
                // Removed closing dialog here so the modal stays open for more adding
            }
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            // Point Trackers Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box(modifier = Modifier.weight(1f).clickable { showResourceDialog = true }) {
                    ResourceBox("Force Points", "${character.currentForcePoints}/${character.maxForcePoints}")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Box(modifier = Modifier.weight(1f).clickable { showResourceDialog = true }) {
                    ResourceBox("Tech Points", "${character.currentTechPoints}/${character.maxTechPoints}")
                }
            }
        }

        item {
            // Force Attack Bonuses Summary Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, CardBorder, RoundedCornerShape(12.dp))
                    .clickable { showForceAttackBonusDialog = true },
                colors = CardDefaults.cardColors(containerColor = SpaceBlack)
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("FORCE ATTACK BONUSES", style = MaterialTheme.typography.titleSmall, color = NeonAmber, fontWeight = FontWeight.Bold)
                        Text("Special: ${formatBonus(forceSpecialBonus)} (Tap to edit)", style = MaterialTheme.typography.labelSmall, color = HoloBlue)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Light: ${formatBonus(lightForceAttack)}", style = MaterialTheme.typography.labelSmall, color = HoloBlue)
                            Text("Dark: ${formatBonus(darkForceAttack)}", style = MaterialTheme.typography.labelSmall, color = SithRed)
                        }
                        Column {
                            Text("Universal: ${formatBonus(universalForceAttack)}", style = MaterialTheme.typography.labelSmall, color = HoloBlue)
                        }
                    }
                }
            }
        }

        item {
            // Tech Attack Bonus Summary Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, CardBorder, RoundedCornerShape(12.dp))
                    .clickable { showTechAttackBonusDialog = true },
                colors = CardDefaults.cardColors(containerColor = SpaceBlack)
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("TECH ATTACK BONUS", style = MaterialTheme.typography.titleSmall, color = NeonAmber, fontWeight = FontWeight.Bold)
                        Text("Special: ${formatBonus(techSpecialBonus)} (Tap to edit)", style = MaterialTheme.typography.labelSmall, color = HoloBlue)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Tech Attack: ${formatBonus(techAttackBonus)}", style = MaterialTheme.typography.labelSmall, color = NeonAmber)
                    }
                }
            }
        }

        item {
            // Auto-calculated Save DCs Summary Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, CardBorder, RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = SpaceBlack)
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("CASTING SAVE DCs", style = MaterialTheme.typography.titleSmall, color = NeonAmber, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Force (Light): $lightForceDc", style = MaterialTheme.typography.labelSmall, color = HoloBlue)
                            Text("Force (Dark): $darkForceDc", style = MaterialTheme.typography.labelSmall, color = SithRed)
                        }
                        Column {
                            Text("Force (Univ): $universalForceDc", style = MaterialTheme.typography.labelSmall, color = HoloBlue)
                            Text("Tech Save DC: $techSaveDc", style = MaterialTheme.typography.labelSmall, color = NeonAmber)
                        }
                    }
                }
            }
        }

        item {
            // Power Category Sub-Tabs
            TabRow(selectedTabIndex = selectedPowerTabIndex, containerColor = SpaceBlack, contentColor = HoloBlue) {
                powerTabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedPowerTabIndex == index,
                        onClick = { selectedPowerTabIndex = index },
                        text = { Text(title, fontWeight = FontWeight.Bold) }
                    )
                }
            }
        }

        when (selectedPowerTabIndex) {
            0 -> {
                // Force Powers Tab Header
                item {
                    Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Force Powers", style = MaterialTheme.typography.titleMedium, color = NeonAmber)
                        TextButton(onClick = { showAddForceDialog = true }) {
                            Text("+ Add Force Power", color = HoloBlue)
                        }
                    }
                }
                
                // Force Powers List
                character.forcePowers.toSortedMap().forEach { (powerLevel, powers) ->
                    item {
                        val levelLabel = if (powerLevel == 0) "At-Will" else "Level $powerLevel"
                        Text(levelLabel, style = MaterialTheme.typography.labelMedium, color = HoloBlue, modifier = Modifier.padding(vertical = 4.dp))
                    }
                    items(powers) { powerName ->
                        val powerData = forcePowersDict[powerName]
                        // Fix 5: Replaced static clickable card with ExpandablePowerCard
                        ExpandablePowerCard(
                            powerName = powerName,
                            powerData = powerData,
                            saveDc = universalForceDc, // pass standard or specific DC here
                            onRemove = {
                                val map = character.forcePowers.toMutableMap()
                                val list = map[powerLevel]?.toMutableList() ?: mutableListOf()
                                list.remove(powerName)
                                if (list.isEmpty()) map.remove(powerLevel) else map[powerLevel] = list
                                viewModel.updateCharacter(character.copy(forcePowers = map))
                            }
                        )
                    }
                }
            }
            1 -> {
                // Tech Powers Tab Header
                item {
                    Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Tech Powers", style = MaterialTheme.typography.titleMedium, color = NeonAmber)
                        TextButton(onClick = { showAddTechDialog = true }) {
                            Text("+ Add Tech Power", color = HoloBlue)
                        }
                    }
                }
                
                // Tech Powers List
                character.techPowers.toSortedMap().forEach { (powerLevel, powers) ->
                    item {
                        val levelLabel = if (powerLevel == 0) "At-Will" else "Level $powerLevel"
                        Text(levelLabel, style = MaterialTheme.typography.labelMedium, color = HoloBlue, modifier = Modifier.padding(vertical = 4.dp))
                    }
                    items(powers) { powerName ->
                        val powerData = techPowersDict[powerName]
                        // Fix 5: Replaced static clickable card with ExpandablePowerCard
                        ExpandablePowerCard(
                            powerName = powerName,
                            powerData = powerData,
                            saveDc = techSaveDc, // pass standard or specific DC here
                            onRemove = {
                                val map = character.techPowers.toMutableMap()
                                val list = map[powerLevel]?.toMutableList() ?: mutableListOf()
                                list.remove(powerName)
                                if (list.isEmpty()) map.remove(powerLevel) else map[powerLevel] = list
                                viewModel.updateCharacter(character.copy(techPowers = map))
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ForceAttackBonusEditDialog(
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
private fun TechAttackBonusEditDialog(
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
                    val alignStr = powerDefinition.type?.takeIf { it.isNotBlank() }?.let { "Align: $it" } ?: ""
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
fun ResourceBox(label: String, value: String) {
    Card(modifier = Modifier.fillMaxWidth().border(1.dp, CardBorder, RoundedCornerShape(8.dp))) {
        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, style = MaterialTheme.typography.labelSmall)
            Text(value, style = MaterialTheme.typography.titleMedium, color = HoloBlue, fontWeight = FontWeight.Bold)
        }
    }
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
                viewingPower = null // Close detail to go back to selection
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
                    items(availablePowers.filter { it.levelInt == selectedLevel }) { power ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, CardBorder, RoundedCornerShape(8.dp))
                                .clickable { viewingPower = power },
                            colors = CardDefaults.cardColors(containerColor = SpaceBlack)
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text(power.name, fontWeight = FontWeight.Bold, color = HoloBlue)
                                val alignStr = power.type?.takeIf { it.isNotBlank() }?.let { "Align: $it" } ?: ""
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

@Composable
fun ExpandablePowerCard(
    powerName: String,
    powerData: PowerDefinition?,
    saveDc: Int,
    onRemove: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
            .clickable { expanded = !expanded }
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(powerData?.name ?: powerName, fontWeight = FontWeight.Bold, color = HoloBlue)
                    val alignStr = powerData?.type?.takeIf { it.isNotBlank() }?.let { "Align: $it" } ?: ""
                    val castStr = powerData?.castingTime?.takeIf { it.isNotBlank() }?.let { "Cast: $it" } ?: ""
                    val rangeStr = powerData?.range?.takeIf { it.isNotBlank() }?.let { "Range: $it" } ?: ""
                    val durStr = powerData?.duration?.takeIf { it.isNotBlank() }?.let { "Dur: $it" } ?: ""
                    val metaInfo = listOf("DC: $saveDc", alignStr, castStr, rangeStr, durStr).filter { it.isNotEmpty() }.joinToString(" | ")
                    Text(metaInfo, style = MaterialTheme.typography.labelSmall, color = NeonAmber)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = if (expanded) "Collapse" else "Expand",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    IconButton(onClick = onRemove) {
                        Icon(Icons.Default.Delete, contentDescription = "Remove Power", tint = SithRed)
                    }
                }
            }
            AnimatedVisibility(visible = expanded && powerData != null) {
                Text(
                    text = powerData?.description ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}