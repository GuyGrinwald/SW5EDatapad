package com.sw5e.datapad.ui.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sw5e.datapad.data.*
import com.sw5e.datapad.ui.MainViewModel
import com.sw5e.datapad.ui.components.*
import com.sw5e.datapad.ui.dialogs.*
import com.sw5e.datapad.ui.theme.*

private data class CombinedProficiencyItem(
    val name: String,
    val isArmor: Boolean
)

// Helper to clean up repetitive dictionary searches ignoring case
private inline fun <T> Map<String, T>.findByName(name: String, getName: (T) -> String): T? {
    return this[name] ?: this.values.find { getName(it).equals(name, ignoreCase = true) }
}

@Composable
fun CharacterProficienciesScreen(viewModel: MainViewModel) {
    val character by viewModel.character.collectAsState()
    val profBonus = viewModel.getProficiencyBonus()

    val featsDict by viewModel.featsDictionary.collectAsState()
    val toolsDict by viewModel.toolsDictionary.collectAsState()
    val armorDict by viewModel.armorDictionary.collectAsState()
    val weaponDict by viewModel.weaponDictionary.collectAsState()

    val fightingStylesCompendium by viewModel.fightingStyles.collectAsState()
    val fightingMasteriesCompendium by viewModel.fightingMasteries.collectAsState()
    val lightsaberFormsCompendium by viewModel.lightsaberForms.collectAsState()

    var showAddFeatDialog by remember { mutableStateOf(false) }
    var showAddToolDialog by remember { mutableStateOf(false) }
    var showAddArmorWeaponDialog by remember { mutableStateOf(false) }
    var showAddStyleDialog by remember { mutableStateOf(false) }
    var showAddMasteryDialog by remember { mutableStateOf(false) }
    var showAddFormDialog by remember { mutableStateOf(false) }

    var selectedSkill by remember { mutableStateOf<CharacterSkillEntity?>(null) }
    var saveEditTarget by remember { mutableStateOf<String?>(null) }
    var selectedTabIndex by remember { mutableIntStateOf(0) }

    val tabs = listOf("Skills", "Saving Throws", "Feats", "Tools", "Armor & Weapons", "Combat Styles")

    // --- Dialogs ---
    if (showAddFeatDialog) {
        FeatAddDialog(
            featsDict = featsDict,
            currentFeats = character.feats,
            onDismiss = { showAddFeatDialog = false },
            onSave = { newFeatName ->
                if (!character.feats.contains(newFeatName)) {
                    viewModel.updateCharacter(character.copy(feats = character.feats + newFeatName))
                }
            }
        )
    }
    if (showAddToolDialog) {
        ToolProficiencyAddDialog(
            toolsDict = toolsDict,
            currentTools = character.toolProficiencies,
            onDismiss = { showAddToolDialog = false },
            onAdd = { viewModel.addToolProficiency(it) }
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
    if (showAddStyleDialog) {
        AddCombatOptionDialog(
            title = "Add Fighting Style",
            presetOptions = fightingStylesCompendium.map { it.name },
            currentItems = character.fightingStyles,
            onDismiss = { showAddStyleDialog = false },
            onAdd = { viewModel.toggleFightingStyle(it) }
        )
    }
    if (showAddMasteryDialog) {
        AddCombatOptionDialog(
            title = "Add Fighting Mastery",
            presetOptions = fightingMasteriesCompendium.map { it.name },
            currentItems = character.fightingMasteries,
            onDismiss = { showAddMasteryDialog = false },
            onAdd = { viewModel.toggleFightingMastery(it) }
        )
    }
    if (showAddFormDialog) {
        AddCombatOptionDialog(
            title = "Add Lightsaber Form",
            presetOptions = lightsaberFormsCompendium.map { it.name },
            currentItems = character.lightsaberForms,
            onDismiss = { showAddFormDialog = false },
            onAdd = { viewModel.toggleLightsaberForm(it) }
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
        val initialProficient = when (target) {
            "STR" -> character.saveProfStr; "DEX" -> character.saveProfDex; "CON" -> character.saveProfCon
            "INT" -> character.saveProfInt; "WIS" -> character.saveProfWis; "CHA" -> character.saveProfCha
            else -> false
        }
        SavingThrowEditDialog(
            target = target,
            initialProficient = initialProficient,
            onDismiss = { saveEditTarget = null },
            onSave = { isProficient ->
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
            }
        )
    }

    // --- Main Layout ---
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Character Proficiencies",
                    style = MaterialTheme.typography.titleLarge,
                    color = NeonAmber
                )
                Text(
                    text = "Proficiency Bonus: +$profBonus",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = HoloBlue
                )
            }
        }
        
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
            0 -> SkillsTab(character, profBonus, viewModel) { selectedSkill = it }
            1 -> SavingThrowsTab(character, profBonus, viewModel) { saveEditTarget = it }
            2 -> FeatsTab(character, featsDict, viewModel) { showAddFeatDialog = true }
            3 -> ToolsTab(character, toolsDict, viewModel) { showAddToolDialog = true }
            4 -> ArmorWeaponsTab(character, armorDict, weaponDict, viewModel) { showAddArmorWeaponDialog = true }
            5 -> CombatStylesTab(
                character, viewModel,
                fightingStylesCompendium, fightingMasteriesCompendium, lightsaberFormsCompendium,
                onAddStyle = { showAddStyleDialog = true },
                onAddMastery = { showAddMasteryDialog = true },
                onAddForm = { showAddFormDialog = true }
            )
        }
    }
}

@Composable
private fun SkillsTab(
    character: CharacterEntity,
    profBonus: Int,
    viewModel: MainViewModel,
    onSkillSelect: (CharacterSkillEntity) -> Unit
) {
    if (character.skills.isEmpty()) {
        Box(modifier = Modifier.fillMaxWidth().fillMaxHeight(), contentAlignment = Alignment.Center) {
            Text(
                "No skills found in database for this character.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxWidth().fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(character.skills, key = { it.skillName }) { skill ->
                val statMod = viewModel.getStatModifierByName(skill.associatedAttribute)
                val totalMod = statMod + (skill.proficiencyLevel * profBonus) + skill.manualOverride
                val modStr = if (totalMod >= 0) "+$totalMod" else "$totalMod"

                Card(modifier = Modifier.fillMaxWidth().clickable { onSkillSelect(skill) }) {
                    Row(
                        modifier = Modifier.padding(12.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(skill.skillName, fontWeight = FontWeight.Bold)
                                if (skill.proficiencyLevel == 1) Text(
                                    "(Proficient)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = NeonAmber
                                )
                                else if (skill.proficiencyLevel == 2) Text(
                                    "(Expertise)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = HoloBlue
                                )
                            }
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

@Composable
private fun SavingThrowsTab(
    character: CharacterEntity,
    profBonus: Int,
    viewModel: MainViewModel,
    onEditTarget: (String) -> Unit
) {
    val stats = listOf(
        Triple("STR", character.saveProfStr, character.str),
        Triple("DEX", character.saveProfDex, character.dex),
        Triple("CON", character.saveProfCon, character.con),
        Triple("INT", character.saveProfInt, character.intStat),
        Triple("WIS", character.saveProfWis, character.wis),
        Triple("CHA", character.saveProfCha, character.cha)
    )

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp).border(1.dp, CardBorder, RoundedCornerShape(12.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Saving Throw Modifiers", style = MaterialTheme.typography.titleMedium, color = NeonAmber)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    stats.forEach { (statName, isProficient, score) ->
                        Surface(modifier = Modifier.clickable { onEditTarget(statName) }, color = Color.Transparent) {
                            SaveBox(statName, isProficient, viewModel.getAttributeModifier(score), profBonus)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FeatsTab(
    character: CharacterEntity,
    featsDict: Map<String, FeatDefinition>,
    viewModel: MainViewModel,
    onAddClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionHeader("Feats", "+ Add Feat", onAddClick)
        LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            items(character.feats) { featName ->
                val featData = featsDict.findByName(featName) { it.name }
                val subtitle = featData?.prerequisite?.takeIf { it.isNotBlank() }?.let { "Prerequisite: $it" }
                ExpandableItemCard(
                    name = featData?.name ?: featName,
                    subtitle = subtitle,
                    description = featData?.description ?: "",
                    onRemove = { viewModel.updateCharacter(character.copy(feats = character.feats - featName)) }
                )
            }
        }
    }
}

@Composable
private fun ToolsTab(
    character: CharacterEntity,
    toolsDict: Map<String, ToolDefinition>,
    viewModel: MainViewModel,
    onAddClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionHeader("Tool Proficiencies", "+ Add Tool", onAddClick)
        if (character.toolProficiencies.isEmpty()) {
            Text(
                "No tool proficiencies added.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth().weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(character.toolProficiencies) { toolName ->
                    val toolData = toolsDict.findByName(toolName) { it.name }
                    ExpandableItemCard(
                        name = toolData?.name ?: toolName,
                        subtitle = toolData?.category?.takeIf { it.isNotBlank() }?.let { "Category: $it" }
                            ?: "Category: Tool",
                        description = toolData?.description ?: "",
                        onRemove = { viewModel.removeToolProficiency(toolName) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ArmorWeaponsTab(
    character: CharacterEntity,
    armorDict: Map<String, ArmorProficiency>,
    weaponDict: Map<String, WeaponProficiency>,
    viewModel: MainViewModel,
    onAddClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionHeader("Armor & Weapon Proficiencies", "+ Add Armor / Weapon", onAddClick)

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
                modifier = Modifier.fillMaxWidth().weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(combinedList, key = { "${if (it.isArmor) "armor" else "weapon"}_${it.name}" }) { item ->
                    if (item.isArmor) {
                        val armorData = armorDict.findByName(item.name) { it.name }
                        ExpandableItemCard(
                            name = armorData?.name ?: item.name,
                            subtitle = "Category: ${armorData?.category?.takeIf { it.isNotBlank() } ?: "Armor"}",
                            description = armorData?.description ?: "",
                            onRemove = { viewModel.removeArmorProficiency(item.name) }
                        )
                    } else {
                        val weaponData = weaponDict.findByName(item.name) { it.name }
                        ExpandableItemCard(
                            name = weaponData?.name ?: item.name,
                            subtitle = "Category: ${weaponData?.category?.takeIf { it.isNotBlank() } ?: "Weapon"}",
                            description = weaponData?.description ?: "",
                            onRemove = { viewModel.removeWeaponProficiency(item.name) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CombatStylesTab(
    character: CharacterEntity,
    viewModel: MainViewModel,
    fightingStylesCompendium: List<FightingStyleDefinition>,
    fightingMasteriesCompendium: List<FightingMasteryDefinition>,
    lightsaberFormsCompendium: List<LightsaberFormDefinition>,
    onAddStyle: () -> Unit,
    onAddMastery: () -> Unit,
    onAddForm: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        CombatSection(
            title = "Fighting Styles",
            addText = "+ Add Style",
            emptyText = "No fighting styles added.",
            items = character.fightingStyles,
            onAddClick = onAddStyle,
            onRemove = { viewModel.toggleFightingStyle(it) },
            getDetails = { name ->
                "Fighting Style" to (fightingStylesCompendium.find {
                    it.name.equals(
                        name,
                        ignoreCase = true
                    )
                }?.description ?: "")
            }
        )
        HorizontalDivider(color = CardBorder)
        CombatSection(
            title = "Fighting Masteries",
            addText = "+ Add Mastery",
            emptyText = "No fighting masteries added.",
            items = character.fightingMasteries,
            onAddClick = onAddMastery,
            onRemove = { viewModel.toggleFightingMastery(it) },
            getDetails = { name ->
                "Fighting Mastery" to (fightingMasteriesCompendium.find {
                    it.name.equals(
                        name,
                        ignoreCase = true
                    )
                }?.description ?: "")
            }
        )
        HorizontalDivider(color = CardBorder)
        CombatSection(
            title = "Lightsaber Forms Known",
            addText = "+ Add Form",
            emptyText = "No lightsaber forms added.",
            items = character.lightsaberForms,
            onAddClick = onAddForm,
            onRemove = { viewModel.toggleLightsaberForm(it) },
            getDetails = { name ->
                val form = lightsaberFormsCompendium.find { it.name.equals(name, ignoreCase = true) }
                val subtitle =
                    form?.prerequisite?.takeIf { it.isNotBlank() }?.let { "Prerequisite: $it" } ?: "Lightsaber Form"
                subtitle to (form?.description ?: "")
            }
        )
    }
}

@Composable
private fun SectionHeader(title: String, buttonText: String, onAddClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = HoloBlue)
        TextButton(onClick = onAddClick) {
            Text(buttonText, color = HoloBlue)
        }
    }
}

@Composable
private fun CombatSection(
    title: String,
    addText: String,
    emptyText: String,
    items: List<String>,
    onAddClick: () -> Unit,
    onRemove: (String) -> Unit,
    getDetails: (String) -> Pair<String?, String>
) {
    SectionHeader(title, addText, onAddClick)
    if (items.isEmpty()) {
        Text(emptyText, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    } else {
        items.forEach { itemName ->
            val (subtitle, desc) = getDetails(itemName)
            ExpandableItemCard(
                name = itemName,
                subtitle = "",
                description = desc,
                onRemove = { onRemove(itemName) }
            )
        }
    }
}

@Composable
fun ExpandableItemCard(
    name: String,
    subtitle: String?,
    description: String,
    onRemove: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = name, fontWeight = FontWeight.Bold)
                    if (!subtitle.isNullOrBlank()) {
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.labelSmall,
                            color = HoloBlue
                        )
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onRemove) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = "Remove",
                            tint = SithRed
                        )
                    }
                    Icon(
                        imageVector = if (expanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                        contentDescription = if (expanded) "Collapse" else "Expand",
                        tint = HoloBlue
                    )
                }
            }
            if (expanded && description.isNotBlank()) {
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    color = CardBorder
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}