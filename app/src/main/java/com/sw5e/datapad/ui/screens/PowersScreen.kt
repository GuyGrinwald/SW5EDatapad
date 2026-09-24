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
import com.sw5e.datapad.ui.dialogs.*


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
                        val pointCost = if (powerLevel == 0) 0 else powerLevel + 1
                        val levelLabel = if (powerLevel == 0) "At-Will (0 Force Points)" else "Level $powerLevel ($pointCost Force Points)"
                        Text(levelLabel, style = MaterialTheme.typography.labelMedium, color = HoloBlue, modifier = Modifier.padding(vertical = 4.dp))
                    }
                    items(powers.sorted()) { powerName ->
                        val powerData = forcePowersDict[powerName]
                        ExpandablePowerCard(
                            powerName = powerName,
                            powerData = powerData,
                            saveDc = universalForceDc,
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
                        val pointCost = if (powerLevel == 0) 0 else powerLevel + 1
                        val levelLabel = if (powerLevel == 0) "At-Will (0 Tech Points)" else "Level $powerLevel ($pointCost Tech Points)"
                        Text(levelLabel, style = MaterialTheme.typography.labelMedium, color = HoloBlue, modifier = Modifier.padding(vertical = 4.dp))
                    }
                    items(powers.sorted()) { powerName ->
                        val powerData = techPowersDict[powerName]
                        ExpandablePowerCard(
                            powerName = powerName,
                            powerData = powerData,
                            saveDc = techSaveDc,
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
fun ResourceBox(label: String, value: String) {
    Card(modifier = Modifier.fillMaxWidth().border(1.dp, CardBorder, RoundedCornerShape(8.dp))) {
        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, style = MaterialTheme.typography.labelSmall)
            Text(value, style = MaterialTheme.typography.titleMedium, color = HoloBlue, fontWeight = FontWeight.Bold)
        }
    }
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
                    val metaInfo = powerData?.let { p ->
                    val alignStr = p.type.takeIf { it.isNotBlank() }?.let { "Align: $it" } ?: ""
                    val castStr = p.castingTime?.takeIf { it.isNotBlank() }?.let { "Cast: $it" } ?: ""
                    val rangeStr = p.range.takeIf { it.isNotBlank() }?.let { "Range: $it" } ?: ""
                    val durStr = p.duration.takeIf { it.isNotBlank() }?.let { "Dur: $it" } ?: ""
                        listOf("DC: $saveDc", alignStr, castStr, rangeStr, durStr).filter { it.isNotEmpty() }.joinToString(" | ")
                    } ?: "DC: $saveDc"

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
                    text = powerData?.description.orEmpty(),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}