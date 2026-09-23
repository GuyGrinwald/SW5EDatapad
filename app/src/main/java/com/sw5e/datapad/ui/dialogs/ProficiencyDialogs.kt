package com.sw5e.datapad.ui.dialogs

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.sw5e.datapad.data.CharacterSkillEntity
import com.sw5e.datapad.data.ArmorProficiency
import com.sw5e.datapad.data.FeatDefinition
import com.sw5e.datapad.data.ToolDefinition
import com.sw5e.datapad.data.WeaponProficiency
import com.sw5e.datapad.ui.theme.*

private data class ProficiencyOption(
    val id: String,
    val name: String,
    val category: String,
    val isArmor: Boolean
)

@Composable
fun SkillEditDialog(
    skill: CharacterSkillEntity,
    onDismiss: () -> Unit,
    onSave: (CharacterSkillEntity) -> Unit
) {
    var prof by remember { mutableIntStateOf(skill.proficiencyLevel) }
    var override by remember { mutableStateOf(skill.manualOverride.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit ${skill.skillName}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Proficiency Level")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = prof == 0, onClick = { prof = 0 }, label = { Text("None") })
                    FilterChip(selected = prof == 1, onClick = { prof = 1 }, label = { Text("Prof") })
                    FilterChip(selected = prof == 2, onClick = { prof = 2 }, label = { Text("Expert") })
                }
                OutlinedTextField(
                    value = override,
                    onValueChange = { override = it },
                    label = { Text("Flat Bonus Override") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                onSave(
                    skill.copy(
                        proficiencyLevel = prof,
                        manualOverride = override.toIntOrNull() ?: 0
                    )
                )
            }) { Text("Save") }
        }
    )
}

@Composable
fun FeatAddDialog(
    featsDict: Map<String, FeatDefinition>,
    currentFeats: List<String>,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var viewingFeat by remember { mutableStateOf<FeatDefinition?>(null) }

    if (viewingFeat != null) {
        val feat = viewingFeat!!
        val featName = feat.name
        AlertDialog(
            onDismissRequest = { viewingFeat = null },
            title = { Text(featName, color = HoloBlue, fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(360.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val prereq = feat.prerequisite
                    if (!prereq.isNullOrBlank()) {
                        Text("Prerequisite: $prereq", style = MaterialTheme.typography.labelSmall, color = NeonAmber)
                    }
                    Text("Description:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = HoloBlue)
                    Text(feat.description ?: "", style = MaterialTheme.typography.bodyMedium)
                }
            },
            confirmButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = {
                        if (featName.isNotBlank()) {
                            onSave(featName)
                        }
                        viewingFeat = null
                    }) {
                        Text("Add")
                    }
                    Button(onClick = { viewingFeat = null }) {
                        Text("Close")
                    }
                }
            }
        )
    }

    val availableFeats = featsDict.values.filter { feat ->
        val name = feat.name ?: return@filter false
        name !in currentFeats && name.contains(searchQuery, ignoreCase = true)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Feat from Compendium") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Search Feats") },
                    modifier = Modifier.fillMaxWidth()
                )
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(availableFeats) { feat ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, CardBorder, RoundedCornerShape(8.dp))
                                .clickable { viewingFeat = feat },
                            colors = CardDefaults.cardColors(containerColor = SpaceBlack)
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text(feat.name ?: "", fontWeight = FontWeight.Bold, color = HoloBlue)
                                val prereq = feat.prerequisite
                                if (!prereq.isNullOrBlank()) {
                                    Text("Prereq: $prereq", style = MaterialTheme.typography.labelSmall, color = NeonAmber)
                                }
                                Text(feat.description ?: "", style = MaterialTheme.typography.bodySmall, maxLines = 2)
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
fun ToolProficiencyAddDialog(
    toolsDict: Map<String, ToolDefinition>,
    currentTools: List<String>,
    onDismiss: () -> Unit,
    onAdd: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var viewingTool by remember { mutableStateOf<ToolDefinition?>(null) }

    if (viewingTool != null) {
        val tool = viewingTool!!
        val toolName = tool.name
        AlertDialog(
            onDismissRequest = { viewingTool = null },
            title = { Text(toolName, color = HoloBlue, fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(360.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val category = tool.category
                    if (category.isNotBlank()) {
                        Text("Category: $category", style = MaterialTheme.typography.labelSmall, color = NeonAmber)
                    }
                    Text("Description:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = HoloBlue)
                    Text(tool.description, style = MaterialTheme.typography.bodyMedium)
                }
            },
            confirmButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = {
                        if (toolName.isNotBlank()) {
                            onAdd(toolName)
                        }
                        viewingTool = null
                    }) {
                        Text("Add")
                    }
                    Button(onClick = { viewingTool = null }) {
                        Text("Close")
                    }
                }
            }
        )
    }

    val availableTools = toolsDict.values.filter { tool ->
        val name = tool.name ?: return@filter false
        name !in currentTools && name.contains(searchQuery, ignoreCase = true)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Tool Proficiency") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Search Tools") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(availableTools) { tool ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, CardBorder, RoundedCornerShape(8.dp))
                                .clickable { viewingTool = tool },
                            colors = CardDefaults.cardColors(containerColor = SpaceBlack)
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text(tool.name, fontWeight = FontWeight.Bold, color = HoloBlue)
                                val category = tool.category
                                if (category.isNotBlank()) {
                                    Text(category, style = MaterialTheme.typography.labelSmall, color = NeonAmber)
                                }
                                val desc = tool.description
                                if (desc.isNotBlank()) {
                                    Text(desc, style = MaterialTheme.typography.bodySmall, maxLines = 2)
                                }
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
fun AddArmorWeaponProficiencyDialog(
    armorDict: Map<String, *>,
    weaponDict: Map<String, *>,
    currentArmor: List<String>,
    currentWeapons: List<String>,
    onDismiss: () -> Unit,
    onAddArmor: (String) -> Unit,
    onAddWeapon: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableIntStateOf(0) } // 0: All, 1: Armor, 2: Weapons

    val standardArmorCategories = listOf("Light Armor", "Medium Armor", "Heavy Armor", "Shields")
    val standardWeaponCategories = listOf(
        "Simple Blasters", "Martial Blasters",
        "Simple Vibroweapons", "Martial Vibroweapons",
        "Simple Lightweapons", "Martial Lightweapons",
    )

    val allOptions = remember(armorDict, weaponDict, currentArmor, currentWeapons) {
        val options = mutableListOf<ProficiencyOption>()

        standardArmorCategories.forEach { cat ->
            if (!currentArmor.contains(cat)) {
                options.add(ProficiencyOption(id = cat, name = cat, category = "Armor Category", isArmor = true))
            }
        }

        standardWeaponCategories.forEach { cat ->
            if (!currentWeapons.contains(cat)) {
                options.add(ProficiencyOption(id = cat, name = cat, category = "Weapon Category", isArmor = false))
            }
        }

        armorDict.forEach { (key, item) ->
            val name = item?.let { runCatching { it.javaClass.getMethod("getName").invoke(it) as? String }.getOrNull() } ?: key
            val category = item?.let { runCatching { it.javaClass.getMethod("getCategory").invoke(it) as? String }.getOrNull() } ?: "Armor"

            if (!currentArmor.contains(key) && !currentArmor.contains(name) && !options.any { it.name.equals(name, ignoreCase = true) }) {
                options.add(ProficiencyOption(id = key, name = name, category = category, isArmor = true))
            }
        }

        weaponDict.forEach { (key, item) ->
            val name = item?.let { runCatching { it.javaClass.getMethod("getName").invoke(it) as? String }.getOrNull() } ?: key
            val category = item?.let { runCatching { it.javaClass.getMethod("getCategory").invoke(it) as? String }.getOrNull() } ?: "Weapon"

            if (!currentWeapons.contains(key) && !currentWeapons.contains(name) && !options.any { it.name.equals(name, ignoreCase = true) }) {
                options.add(ProficiencyOption(id = key, name = name, category = category, isArmor = false))
            }
        }

        options
    }

    val filteredOptions = remember(searchQuery, selectedFilter, allOptions) {
        allOptions.filter { option ->
            val matchesFilter = when (selectedFilter) {
                1 -> option.isArmor
                2 -> !option.isArmor
                else -> true
            }
            val matchesSearch = searchQuery.isBlank() ||
                    option.name.contains(searchQuery, ignoreCase = true) ||
                    option.category.contains(searchQuery, ignoreCase = true)
            matchesFilter && matchesSearch
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Armor or Weapon Proficiency") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Search") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedFilter == 0,
                        onClick = { selectedFilter = 0 },
                        label = { Text("All") }
                    )
                    FilterChip(
                        selected = selectedFilter == 1,
                        onClick = { selectedFilter = 1 },
                        label = { Text("Armor") }
                    )
                    FilterChip(
                        selected = selectedFilter == 2,
                        onClick = { selectedFilter = 2 },
                        label = { Text("Weapons") }
                    )
                }

                if (filteredOptions.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "No matching options found.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(filteredOptions, key = { "${if (it.isArmor) "a" else "w"}_${it.id}" }) { option ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (option.isArmor) {
                                            onAddArmor(option.id)
                                        } else {
                                            onAddWeapon(option.id)
                                        }
                                        onDismiss()
                                    }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = option.name,
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.bodyLarge
                                        )
                                        Text(
                                            text = "Category: ${option.category} • ${if (option.isArmor) "Armor" else "Weapon"}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = NeonAmber
                                        )
                                    }
                                    Icon(
                                        Icons.Default.Add,
                                        contentDescription = "Add",
                                        tint = HoloBlue
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}