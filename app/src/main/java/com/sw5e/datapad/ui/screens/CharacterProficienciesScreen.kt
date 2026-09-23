package com.sw5e.datapad.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sw5e.datapad.data.CharacterSkillEntity
import com.sw5e.datapad.ui.MainViewModel
import com.sw5e.datapad.ui.components.SaveBox
import com.sw5e.datapad.ui.dialogs.FeatAddDialog
import com.sw5e.datapad.ui.dialogs.SkillEditDialog
import com.sw5e.datapad.ui.dialogs.ToolProficiencyAddDialog
import com.sw5e.datapad.ui.dialogs.AddArmorWeaponProficiencyDialog
import com.sw5e.datapad.ui.theme.*

private data class CombinedProficiencyItem(
    val name: String,
    val isArmor: Boolean
)

@Composable
fun CharacterProficienciesScreen(viewModel: MainViewModel) {
    val character by viewModel.character.collectAsState()
    val skills by viewModel.skills.collectAsState()
    val profBonus = viewModel.getProficiencyBonus()
    val featsDict by viewModel.featsDictionary.collectAsState()
    val toolsDict by viewModel.toolsDictionary.collectAsState()
    val armorDict by viewModel.armorDictionary.collectAsState()
    val weaponDict by viewModel.weaponDictionary.collectAsState()

    var showAddFeatDialog by remember { mutableStateOf(false) }
    var showAddToolDialog by remember { mutableStateOf(false) }
    var showAddArmorWeaponDialog by remember { mutableStateOf(false) }

    var selectedSkill by remember { mutableStateOf<CharacterSkillEntity?>(null) }
    var saveEditTarget by remember { mutableStateOf<String?>(null) }

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Skills", "Saving Throws", "Feats", "Tools", "Armor & Weapons")

    if (showAddFeatDialog) {
        FeatAddDialog(
            featsDict = featsDict,
            currentFeats = character.feats,
            onDismiss = { showAddFeatDialog = false },
            onSave = { newFeatName ->
                val updatedFeats = character.feats.toMutableList().apply { add(newFeatName) }
                viewModel.updateCharacter(character.copy(feats = updatedFeats))
            }
        )
    }

    if (showAddToolDialog) {
        ToolProficiencyAddDialog(
            toolsDict = toolsDict,
            currentTools = character.toolProficiencies,
            onDismiss = { showAddToolDialog = false },
            onAdd = { tool ->
                viewModel.addToolProficiency(tool)
                // Kept open on add so user can continue adding tools
            }
        )
    }

    if (showAddArmorWeaponDialog) {
        AddArmorWeaponProficiencyDialog(
            armorDict = armorDict,
            weaponDict = weaponDict,
            currentArmor = character.armorProficiencies,
            currentWeapons = character.weaponProficiencies,
            onDismiss = { showAddArmorWeaponDialog = false },
            onAddArmor = { viewModel.addArmorProficiency(it) },
            onAddWeapon = { viewModel.addWeaponProficiency(it) }
        )
    }

    selectedSkill?.let { skill ->
        SkillEditDialog(
            skill = skill,
            onDismiss = { selectedSkill = null },
            onSave = { updated ->
                viewModel.updateSkill(updated)
                selectedSkill = null
            }
        )
    }

    saveEditTarget?.let { target ->
        var isProficient by remember {
            mutableStateOf(
                when (target) {
                    "STR" -> character.saveProfStr
                    "DEX" -> character.saveProfDex
                    "CON" -> character.saveProfCon
                    "INT" -> character.saveProfInt
                    "WIS" -> character.saveProfWis
                    "CHA" -> character.saveProfCha
                    else -> false
                }
            )
        }

        AlertDialog(
            onDismissRequest = { saveEditTarget = null },
            title = { Text("Edit $target Saving Throw") },
            text = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isProficient, onCheckedChange = { isProficient = it })
                    Text("Proficient in $target Saves")
                }
            },
            confirmButton = {
                Button(onClick = {
                    val updated = when (target) {
                        "STR" -> character.copy(saveProfStr = isProficient)
                        "DEX" -> character.copy(saveProfDex = isProficient)
                        "CON" -> character.copy(saveProfCon = isProficient)
                        "INT" -> character.copy(saveProfInt = isProficient)
                        "WIS" -> character.copy(saveProfWis = isProficient)
                        "CHA" -> character.copy(saveProfCha = isProficient)
                        else -> character
                    }
                    viewModel.updateCharacter(updated)
                    saveEditTarget = null
                }) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { saveEditTarget = null }) { Text("Cancel") }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ScrollableTabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = SpaceBlack,
            contentColor = HoloBlue,
            edgePadding = 0.dp
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = { Text(title, fontWeight = FontWeight.Bold) }
                )
            }
        }

        when (selectedTabIndex) {
            0 -> {
                if (skills.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "No skills found in database for this character.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(skills, key = { it.skillName }) { skill ->
                            val statMod = viewModel.getStatModifierByName(skill.associatedAttribute)
                            val totalMod = statMod + (skill.proficiencyLevel * profBonus) + skill.manualOverride
                            val modStr = if (totalMod >= 0) "+$totalMod" else "$totalMod"

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedSkill = skill }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .padding(12.dp)
                                        .fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(skill.skillName, fontWeight = FontWeight.Bold)
                                        Text(
                                            skill.associatedAttribute,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = NeonAmber
                                        )
                                    }
                                    Text(
                                        modStr,
                                        color = HoloBlue,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            1 -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                            .border(1.dp, CardBorder, RoundedCornerShape(12.dp))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                "Saving Throw Modifiers",
                                style = MaterialTheme.typography.titleMedium,
                                color = NeonAmber
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                Surface(modifier = Modifier.clickable { saveEditTarget = "STR" }, color = Color.Transparent) {
                                    SaveBox("STR", character.saveProfStr, viewModel.getAttributeModifier(character.str), profBonus)
                                }
                                Surface(modifier = Modifier.clickable { saveEditTarget = "DEX" }, color = Color.Transparent) {
                                    SaveBox("DEX", character.saveProfDex, viewModel.getAttributeModifier(character.dex), profBonus)
                                }
                                Surface(modifier = Modifier.clickable { saveEditTarget = "CON" }, color = Color.Transparent) {
                                    SaveBox("CON", character.saveProfCon, viewModel.getAttributeModifier(character.con), profBonus)
                                }
                                Surface(modifier = Modifier.clickable { saveEditTarget = "INT" }, color = Color.Transparent) {
                                    SaveBox("INT", character.saveProfInt, viewModel.getAttributeModifier(character.intStat), profBonus)
                                }
                                Surface(modifier = Modifier.clickable { saveEditTarget = "WIS" }, color = Color.Transparent) {
                                    SaveBox("WIS", character.saveProfWis, viewModel.getAttributeModifier(character.wis), profBonus)
                                }
                                Surface(modifier = Modifier.clickable { saveEditTarget = "CHA" }, color = Color.Transparent) {
                                    SaveBox("CHA", character.saveProfCha, viewModel.getAttributeModifier(character.cha), profBonus)
                                }
                            }
                        }
                    }
                }
            }

            2 -> {
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
                            "Feats",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = HoloBlue
                        )
                        TextButton(onClick = { showAddFeatDialog = true }) {
                            Text("+ Add Feat", color = HoloBlue)
                        }
                    }

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(character.feats) { featName ->
                            val featData = featsDict[featName] ?: featsDict.values.find { it.name.equals(featName, ignoreCase = true) }
                            val subtitle = featData?.prerequisite?.takeIf { it.isNotBlank() }?.let { "Prerequisite: $it" }
                            
                            ExpandableItemCard(
                                name = featData?.name ?: featName,
                                subtitle = subtitle,
                                description = featData?.description ?: "",
                                onRemove = {
                                    val updatedFeats = character.feats.toMutableList().apply { remove(featName) }
                                    viewModel.updateCharacter(character.copy(feats = updatedFeats))
                                }
                            )
                        }
                    }
                }
            }

            3 -> {
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
                            "Tool Proficiencies",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = HoloBlue
                        )
                        TextButton(onClick = { showAddToolDialog = true }) {
                            Text("+ Add Tool", color = HoloBlue)
                        }
                    }

                    if (character.toolProficiencies.isEmpty()) {
                        Text(
                            "No tool proficiencies added.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            items(character.toolProficiencies) { toolName ->
                                val toolData = toolsDict[toolName] ?: toolsDict.values.find { it.name.equals(toolName, ignoreCase = true) }
                                val subtitle = toolData?.category?.takeIf { it.isNotBlank() }?.let { "Category: $it" } ?: "Category: Tool"
                                
                                ExpandableItemCard(
                                    name = toolData?.name ?: toolName,
                                    subtitle = subtitle,
                                    description = toolData?.description ?: "",
                                    onRemove = { viewModel.removeToolProficiency(toolName) }
                                )
                            }
                        }
                    }
                }
            }

            4 -> {
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
                            "Armor & Weapon Proficiencies",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = HoloBlue
                        )
                        TextButton(onClick = { showAddArmorWeaponDialog = true }) {
                            Text("+ Add Armor / Weapon", color = HoloBlue)
                        }
                    }

                    val combinedList = remember(character.armorProficiencies, character.weaponProficiencies) {
                        character.armorProficiencies.map { CombinedProficiencyItem(it, isArmor = true) } +
                        character.weaponProficiencies.map { CombinedProficiencyItem(it, isArmor = false) }
                    }

                    if (combinedList.isEmpty()) {
                        Text(
                            "No armor or weapon proficiencies added.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            items(combinedList, key = { "${if (it.isArmor) "armor" else "weapon"}_${it.name}" }) { item ->
                                if (item.isArmor) {
                                    val armorData = armorDict[item.name]
                                        ?: armorDict.values.find { it.name.equals(item.name, ignoreCase = true) }
                                    val categoryText = armorData?.category?.takeIf { it.isNotBlank() } ?: "Armor"

                                    ExpandableItemCard(
                                        name = armorData?.name ?: item.name,
                                        subtitle = "Category: $categoryText",
                                        description = armorData?.description ?: "",
                                        onRemove = { viewModel.removeArmorProficiency(item.name) }
                                    )
                                } else {
                                    val weaponData = weaponDict[item.name]
                                        ?: weaponDict.values.find { it.name.equals(item.name, ignoreCase = true) }
                                    val categoryText = weaponData?.category?.takeIf { it.isNotBlank() } ?: "Weapon"

                                    ExpandableItemCard(
                                        name = weaponData?.name ?: item.name,
                                        subtitle = "Category: $categoryText",
                                        description = weaponData?.description ?: "",
                                        onRemove = { viewModel.removeWeaponProficiency(item.name) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ExpandableItemCard(
    name: String,
    subtitle: String? = null,
    description: String = "",
    onRemove: (() -> Unit)? = null
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        colors = CardDefaults.cardColors(containerColor = SpaceBlack)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = name, fontWeight = FontWeight.Bold, color = HoloBlue)
                    if (!subtitle.isNullOrBlank()) {
                        Text(text = subtitle, style = MaterialTheme.typography.labelSmall, color = NeonAmber)
                    }
                }
                if (onRemove != null) {
                    IconButton(onClick = onRemove) {
                        Icon(Icons.Default.Delete, contentDescription = "Remove", tint = SithRed)
                    }
                }
            }
            if (expanded && description.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = description, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}