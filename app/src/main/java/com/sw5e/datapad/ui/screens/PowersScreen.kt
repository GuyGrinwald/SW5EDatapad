package com.sw5e.datapad.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sw5e.datapad.data.PowerDefinition
import com.sw5e.datapad.ui.MainViewModel
import com.sw5e.datapad.ui.dialogs.*
import com.sw5e.datapad.ui.theme.*

val Silver = Color(0xFFC0C0C0)

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

    // Concentration State (Only one power can be concentrated on at a time)
    var concentratingPower by remember { mutableStateOf<String?>(null) }

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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Unified Standard Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Powers & Abilities",
                    style = MaterialTheme.typography.titleLarge,
                    color = NeonAmber
                )
                Text(
                    text = "Force Points: ${character.currentForcePoints}/${character.maxForcePoints} | Tech Points: ${character.currentTechPoints}/${character.maxTechPoints}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = HoloBlue,
                    modifier = Modifier.clickable { showResourceDialog = true }
                )
            }
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Active Concentration Summary Banner
            concentratingPower?.let { powerName ->
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = SpaceBlack),
                        border = androidx.compose.foundation.BorderStroke(1.dp, NeonAmber)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("CONCENTRATING ON", style = MaterialTheme.typography.labelSmall, color = NeonAmber, fontWeight = FontWeight.Bold)
                                Text(powerName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            }
                            IconButton(onClick = { concentratingPower = null }) {
                                Icon(Icons.Default.Close, contentDescription = "End Concentration", tint = SithRed)
                            }
                        }
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
                                Text("Universal: ${formatBonus(universalForceAttack)}", style = MaterialTheme.typography.labelSmall, color = Silver)
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
                                Text("Force (Univ): $universalForceDc", style = MaterialTheme.typography.labelSmall, color = Silver)
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
                            val isConcentrating = concentratingPower == powerName
                            val pointCost = if (powerLevel == 0) 0 else powerLevel + 1

                            ExpandablePowerCard(
                                powerName = powerName,
                                powerData = powerData,
                                saveDc = universalForceDc,
                                powerLevel = powerLevel,
                                isForcePower = true,
                                isConcentrating = isConcentrating,
                                onActivate = {
                                    val newFp = (character.currentForcePoints - pointCost).coerceAtLeast(0)
                                    viewModel.updateCharacter(character.copy(currentForcePoints = newFp))
                                    if (powerData?.duration?.contains("Concentration", ignoreCase = true) == true) {
                                        concentratingPower = powerName
                                    }
                                },
                                onRemove = {
                                    if (isConcentrating) concentratingPower = null
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
                            val isConcentrating = concentratingPower == powerName
                            val pointCost = if (powerLevel == 0) 0 else powerLevel + 1

                            ExpandablePowerCard(
                                powerName = powerName,
                                powerData = powerData,
                                saveDc = techSaveDc,
                                powerLevel = powerLevel,
                                isForcePower = false,
                                isConcentrating = isConcentrating,
                                onActivate = {
                                    val newTp = (character.currentTechPoints - pointCost).coerceAtLeast(0)
                                    viewModel.updateCharacter(character.copy(currentTechPoints = newTp))
                                    if (powerData?.duration?.contains("Concentration", ignoreCase = true) == true) {
                                        concentratingPower = powerName
                                    }
                                },
                                onRemove = {
                                    if (isConcentrating) concentratingPower = null
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
}

@Composable
fun ExpandablePowerCard(
    powerName: String,
    powerData: PowerDefinition?,
    saveDc: Int,
    powerLevel: Int,
    isForcePower: Boolean,
    isConcentrating: Boolean,
    onActivate: () -> Unit,
    onRemove: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    val titleColor = if (isForcePower) {
        when {
            powerData?.type?.contains("Dark", ignoreCase = true) == true -> SithRed
            powerData?.type?.contains("Light", ignoreCase = true) == true -> HoloBlue
            powerData?.type?.contains("Universal", ignoreCase = true) == true -> Silver
            else -> Silver
        }
    } else {
        HoloBlue
    }

    val pointCost = if (powerLevel == 0) 0 else powerLevel + 1
    val costLabel = if (isForcePower) "$pointCost FP" else "$pointCost TP"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
            .then(if (isConcentrating) Modifier.border(1.dp, NeonAmber, CardDefaults.shape) else Modifier)
            .clickable { expanded = !expanded }
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = powerData?.name ?: powerName,
                            fontWeight = FontWeight.Bold,
                            color = titleColor
                        )
                        if (isConcentrating) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = NeonAmber,
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "CONCENTRATING",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = SpaceBlack,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                    }

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

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onActivate,
                    colors = ButtonDefaults.buttonColors(containerColor = HoloBlue, contentColor = SpaceBlack),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                    modifier = Modifier.height(30.dp)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Activate ($costLabel)", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
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