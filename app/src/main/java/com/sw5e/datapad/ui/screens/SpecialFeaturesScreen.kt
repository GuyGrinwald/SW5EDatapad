package com.sw5e.datapad.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.sw5e.datapad.data.CharacterFeature
import com.sw5e.datapad.data.ChargeResetCondition
import com.sw5e.datapad.data.MaxChargesScaling
import com.sw5e.datapad.ui.MainViewModel
import com.sw5e.datapad.ui.theme.*

@Composable
fun SpecialFeaturesScreen(viewModel: MainViewModel) {
    val character by viewModel.character.collectAsState()
    val compendiumSkills by viewModel.compendiumSkills.collectAsState()

    // Extract sorted list of skills from compendium (with fallback to character sheet skills)
    val availableSkills = remember(compendiumSkills, character.skills) {
        if (compendiumSkills.isNotEmpty()) {
            compendiumSkills.map { it.name }.sorted()
        } else {
            character.skills.map { it.skillName }.sorted()
        }
    }

    val attributeOptions = remember { listOf("STR", "DEX", "CON", "INT", "WIS", "CHA") }

    // Filter states
    var selectedScalingFilter by remember { mutableStateOf<MaxChargesScaling?>(null) }
    var selectedResetFilter by remember { mutableStateOf<ChargeResetCondition?>(null) }
    var selectedSkillFilter by remember { mutableStateOf<String?>(null) }
    var selectedSaveFilter by remember { mutableStateOf<String?>(null) }

    // Filter dropdown expansion states
    var showScalingFilterMenu by remember { mutableStateOf(false) }
    var showResetFilterMenu by remember { mutableStateOf(false) }
    var showSkillFilterMenu by remember { mutableStateOf(false) }
    var showSaveFilterMenu by remember { mutableStateOf(false) }

    var showAddDialog by remember { mutableStateOf(false) }
    var featureToEdit by remember { mutableStateOf<CharacterFeature?>(null) }

    val filteredFeatures = remember(
        character.features,
        selectedScalingFilter,
        selectedResetFilter,
        selectedSkillFilter,
        selectedSaveFilter
    ) {
        character.features.filter { feature ->
            val matchesScaling = selectedScalingFilter == null || (feature.usesCharges && feature.scalingType == selectedScalingFilter)
            val matchesReset = selectedResetFilter == null || (feature.usesCharges && feature.resetCondition == selectedResetFilter)
            val matchesSkill = selectedSkillFilter == null || feature.linkedSkillName == selectedSkillFilter
            val matchesSave = selectedSaveFilter == null || feature.linkedSaveAttribute == selectedSaveFilter
            matchesScaling && matchesReset && matchesSkill && matchesSave
        }
    }

    if (showAddDialog) {
        FeatureFormDialog(
            title = "Add Feature",
            initialFeature = CharacterFeature(),
            availableSkills = availableSkills,
            onDismiss = { showAddDialog = false },
            onSave = { newFeature ->
                val maxCharges = viewModel.calculateMaxCharges(newFeature)
                val featureWithCharges = newFeature.copy(currentCharges = maxCharges)
                viewModel.updateCharacter(character.copy(features = character.features + featureWithCharges))
                showAddDialog = false
            }
        )
    }

    featureToEdit?.let { target ->
        FeatureFormDialog(
            title = "Edit Feature",
            initialFeature = target,
            availableSkills = availableSkills,
            onDismiss = { featureToEdit = null },
            onSave = { updatedFeature ->
                val updatedList = character.features.map { 
                    if (it.id == updatedFeature.id) updatedFeature else it 
                }
                viewModel.updateCharacter(character.copy(features = updatedList))
                featureToEdit = null
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Rest Action Controls Header
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, CardBorder, RoundedCornerShape(12.dp)),
            colors = CardDefaults.cardColors(containerColor = SpaceBlack)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "FEATURES & TRAITS",
                    style = MaterialTheme.typography.titleSmall,
                    color = NeonAmber,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = { viewModel.performRest(isLongRest = false) },
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        Text("Short Rest", color = HoloBlue)
                    }

                    Button(
                        onClick = { viewModel.performRest(isLongRest = true) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = HoloBlue,
                            contentColor = SpaceBlack
                        ),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        Text("Long Rest", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Character Features", style = MaterialTheme.typography.titleMedium, color = HoloBlue, fontWeight = FontWeight.Bold)
            Button(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add Feature")
            }
        }

        // Horizontal Filter Controls
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.FilterList,
                contentDescription = "Filter",
                tint = HoloBlue,
                modifier = Modifier.size(20.dp)
            )

            // Charges Formula / Scaling Filter
            Box {
                FilterChip(
                    selected = selectedScalingFilter != null,
                    onClick = { showScalingFilterMenu = true },
                    label = { Text(selectedScalingFilter?.let { "Charges: ${it.name}" } ?: "Charges Formula") },
                    trailingIcon = {
                        if (selectedScalingFilter != null) {
                            IconButton(
                                onClick = { selectedScalingFilter = null },
                                modifier = Modifier.size(16.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Clear")
                            }
                        } else {
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                        }
                    }
                )
                DropdownMenu(
                    expanded = showScalingFilterMenu,
                    onDismissRequest = { showScalingFilterMenu = false }
                ) {
                    MaxChargesScaling.values().forEach { scale ->
                        DropdownMenuItem(
                            text = { Text(scale.name) },
                            onClick = {
                                selectedScalingFilter = scale
                                showScalingFilterMenu = false
                            }
                        )
                    }
                }
            }

            // Reset Period Filter
            Box {
                FilterChip(
                    selected = selectedResetFilter != null,
                    onClick = { showResetFilterMenu = true },
                    label = { Text(selectedResetFilter?.let { "Reset: ${it.name.replace('_', ' ')}" } ?: "Reset Period") },
                    trailingIcon = {
                        if (selectedResetFilter != null) {
                            IconButton(
                                onClick = { selectedResetFilter = null },
                                modifier = Modifier.size(16.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Clear")
                            }
                        } else {
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                        }
                    }
                )
                DropdownMenu(
                    expanded = showResetFilterMenu,
                    onDismissRequest = { showResetFilterMenu = false }
                ) {
                    ChargeResetCondition.values().forEach { condition ->
                        DropdownMenuItem(
                            text = { Text(condition.name.replace('_', ' ')) },
                            onClick = {
                                selectedResetFilter = condition
                                showResetFilterMenu = false
                            }
                        )
                    }
                }
            }

            // Skill Filter
            Box {
                FilterChip(
                    selected = selectedSkillFilter != null,
                    onClick = { showSkillFilterMenu = true },
                    label = { Text(selectedSkillFilter?.let { "Skill: $it" } ?: "Skill") },
                    trailingIcon = {
                        if (selectedSkillFilter != null) {
                            IconButton(
                                onClick = { selectedSkillFilter = null },
                                modifier = Modifier.size(16.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Clear")
                            }
                        } else {
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                        }
                    }
                )
                DropdownMenu(
                    expanded = showSkillFilterMenu,
                    onDismissRequest = { showSkillFilterMenu = false },
                    modifier = Modifier.heightIn(max = 240.dp)
                ) {
                    availableSkills.forEach { skill ->
                        DropdownMenuItem(
                            text = { Text(skill) },
                            onClick = {
                                selectedSkillFilter = skill
                                showSkillFilterMenu = false
                            }
                        )
                    }
                }
            }

            // Save Filter
            Box {
                FilterChip(
                    selected = selectedSaveFilter != null,
                    onClick = { showSaveFilterMenu = true },
                    label = { Text(selectedSaveFilter?.let { "Save: $it" } ?: "Save") },
                    trailingIcon = {
                        if (selectedSaveFilter != null) {
                            IconButton(
                                onClick = { selectedSaveFilter = null },
                                modifier = Modifier.size(16.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Clear")
                            }
                        } else {
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                        }
                    }
                )
                DropdownMenu(
                    expanded = showSaveFilterMenu,
                    onDismissRequest = { showSaveFilterMenu = false }
                ) {
                    attributeOptions.forEach { attr ->
                        DropdownMenuItem(
                            text = { Text(attr) },
                            onClick = {
                                selectedSaveFilter = attr
                                showSaveFilterMenu = false
                            }
                        )
                    }
                }
            }
        }

        if (filteredFeatures.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text(
                    text = if (character.features.isEmpty()) "No features recorded. Tap '+ Add Feature' to create one."
                           else "No features match the active filters.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredFeatures, key = { it.id }) { feature ->
                    FeatureCard(
                        feature = feature,
                        maxCharges = viewModel.calculateMaxCharges(feature),
                        onIncrementCharges = { viewModel.spendFeatureCharge(feature.id, 1) },
                        onDecrementCharges = { viewModel.spendFeatureCharge(feature.id, -1) },
                        onEdit = { featureToEdit = feature },
                        onDelete = {
                            val updatedList = character.features.filter { it.id != feature.id }
                            viewModel.updateCharacter(character.copy(features = updatedList))
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun FeatureCard(
    feature: CharacterFeature,
    maxCharges: Int,
    onIncrementCharges: () -> Unit,
    onDecrementCharges: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, CardBorder, RoundedCornerShape(8.dp))
            .clickable { expanded = !expanded },
        colors = CardDefaults.cardColors(containerColor = SpaceBlack)
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(feature.name.ifBlank { "Unnamed Feature" }, fontWeight = FontWeight.Bold, color = HoloBlue)
                    if (feature.source.isNotBlank()) {
                        Text(feature.source, style = MaterialTheme.typography.labelSmall, color = NeonAmber)
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Feature", tint = HoloBlue)
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete Feature", tint = SithRed)
                    }
                    IconButton(onClick = { expanded = !expanded }) {
                        Icon(
                            imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = if (expanded) "Collapse" else "Expand",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            if (feature.usesCharges) {
                HorizontalDivider(color = CardBorder)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Charges: ${feature.currentCharges} / $maxCharges", style = MaterialTheme.typography.titleMedium, color = NeonAmber, fontWeight = FontWeight.Bold)
                        Text("Reset: ${feature.resetCondition.name.replace('_', ' ')} | Scaling: ${feature.scalingType.name}", style = MaterialTheme.typography.labelSmall, color = HoloBlue)
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                        OutlinedIconButton(
                            onClick = onDecrementCharges,
                            enabled = feature.currentCharges > 0,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = "Spend Charge", tint = if (feature.currentCharges > 0) SithRed else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f))
                        }
                        OutlinedIconButton(
                            onClick = onIncrementCharges,
                            enabled = feature.currentCharges < maxCharges,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Restore Charge", tint = if (feature.currentCharges < maxCharges) HoloBlue else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f))
                        }
                    }
                }
            }

            AnimatedVisibility(visible = expanded) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(top = 4.dp)) {
                    if (feature.description.isNotBlank()) {
                        Text(feature.description, style = MaterialTheme.typography.bodyMedium)
                    }
                    if (!feature.linkedSkillName.isNullOrBlank() || !feature.linkedSaveAttribute.isNullOrBlank()) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            feature.linkedSkillName?.takeIf { it.isNotBlank() }?.let { skill ->
                                SuggestionChip(onClick = {}, label = { Text("Skill: $skill") })
                            }
                            feature.linkedSaveAttribute?.takeIf { it.isNotBlank() }?.let { save ->
                                SuggestionChip(onClick = {}, label = { Text("Save: $save") })
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FeatureFormDialog(
    title: String,
    initialFeature: CharacterFeature,
    availableSkills: List<String>,
    onDismiss: () -> Unit,
    onSave: (CharacterFeature) -> Unit
) {
    var name by remember { mutableStateOf(initialFeature.name) }
    var source by remember { mutableStateOf(initialFeature.source) }
    var description by remember { mutableStateOf(initialFeature.description) }

    var usesCharges by remember { mutableStateOf(initialFeature.usesCharges) }
    var fixedMaxChargesText by remember { mutableStateOf(initialFeature.fixedMaxCharges.toString()) }
    var scalingType by remember { mutableStateOf(initialFeature.scalingType) }
    var bonusOffsetText by remember { mutableStateOf(initialFeature.chargesBonusOffset.toString()) }
    var resetCondition by remember { mutableStateOf(initialFeature.resetCondition) }

    var linkedSkillName by remember { mutableStateOf(initialFeature.linkedSkillName ?: "") }
    var linkedSaveAttribute by remember { mutableStateOf(initialFeature.linkedSaveAttribute ?: "") }

    var expandedScalingDropdown by remember { mutableStateOf(false) }
    var expandedResetDropdown by remember { mutableStateOf(false) }
    var expandedSkillDropdown by remember { mutableStateOf(false) }
    var expandedSaveDropdown by remember { mutableStateOf(false) }

    val attributeOptions = remember { listOf("STR", "DEX", "CON", "INT", "WIS", "CHA") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, color = HoloBlue) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Feature Name") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = source,
                    onValueChange = { source = it },
                    label = { Text("Source (e.g. Class: Guardian)") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = usesCharges, onCheckedChange = { usesCharges = it })
                    Text("Uses Charges / Resource Pool", fontWeight = FontWeight.Bold)
                }

                if (usesCharges) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(onClick = { expandedScalingDropdown = true }, modifier = Modifier.fillMaxWidth()) {
                            Text("Charges: ${scalingType.name}")
                            Spacer(modifier = Modifier.weight(1f))
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                        }
                        DropdownMenu(expanded = expandedScalingDropdown, onDismissRequest = { expandedScalingDropdown = false }) {
                            MaxChargesScaling.values().forEach { scale ->
                                DropdownMenuItem(
                                    text = { Text(scale.name) },
                                    onClick = {
                                        scalingType = scale
                                        expandedScalingDropdown = false
                                    }
                                )
                            }
                        }
                    }

                    if (scalingType == MaxChargesScaling.FIXED) {
                        OutlinedTextField(
                            value = fixedMaxChargesText,
                            onValueChange = { fixedMaxChargesText = it },
                            label = { Text("Base Fixed Charges") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    OutlinedTextField(
                        value = bonusOffsetText,
                        onValueChange = { bonusOffsetText = it },
                        label = { Text("Charge Flat Offset (+/-)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(onClick = { expandedResetDropdown = true }, modifier = Modifier.fillMaxWidth()) {
                            Text("Reset On: ${resetCondition.name.replace('_', ' ')}")
                            Spacer(modifier = Modifier.weight(1f))
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                        }
                        DropdownMenu(expanded = expandedResetDropdown, onDismissRequest = { expandedResetDropdown = false }) {
                            ChargeResetCondition.values().forEach { condition ->
                                DropdownMenuItem(
                                    text = { Text(condition.name.replace('_', ' ')) },
                                    onClick = {
                                        resetCondition = condition
                                        expandedResetDropdown = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Compendium-Driven Linked Skill Check Dropdown Selection
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(onClick = { expandedSkillDropdown = true }, modifier = Modifier.fillMaxWidth()) {
                        Text(if (linkedSkillName.isNotBlank()) "Linked Skill: $linkedSkillName" else "Linked Skill Check (None)")
                        Spacer(modifier = Modifier.weight(1f))
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                    }
                    DropdownMenu(
                        expanded = expandedSkillDropdown,
                        onDismissRequest = { expandedSkillDropdown = false },
                        modifier = Modifier.heightIn(max = 240.dp)
                    ) {
                        DropdownMenuItem(
                            text = { Text("None", fontWeight = FontWeight.Bold) },
                            onClick = {
                                linkedSkillName = ""
                                expandedSkillDropdown = false
                            }
                        )
                        availableSkills.forEach { skillName ->
                            DropdownMenuItem(
                                text = { Text(skillName) },
                                onClick = {
                                    linkedSkillName = skillName
                                    expandedSkillDropdown = false
                                }
                            )
                        }
                    }
                }

                // Compendium-Driven Linked Save Attribute Dropdown Selection
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(onClick = { expandedSaveDropdown = true }, modifier = Modifier.fillMaxWidth()) {
                        Text(if (linkedSaveAttribute.isNotBlank()) "Linked Save: $linkedSaveAttribute" else "Linked Save Attribute (None)")
                        Spacer(modifier = Modifier.weight(1f))
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                    }
                    DropdownMenu(expanded = expandedSaveDropdown, onDismissRequest = { expandedSaveDropdown = false }) {
                        DropdownMenuItem(
                            text = { Text("None", fontWeight = FontWeight.Bold) },
                            onClick = {
                                linkedSaveAttribute = ""
                                expandedSaveDropdown = false
                            }
                        )
                        attributeOptions.forEach { attr ->
                            DropdownMenuItem(
                                text = { Text(attr) },
                                onClick = {
                                    linkedSaveAttribute = attr
                                    expandedSaveDropdown = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                if (name.isNotBlank()) {
                    onSave(
                        initialFeature.copy(
                            name = name.trim(),
                            source = source.trim(),
                            description = description.trim(),
                            usesCharges = usesCharges,
                            fixedMaxCharges = fixedMaxChargesText.toIntOrNull() ?: 1,
                            scalingType = scalingType,
                            chargesBonusOffset = bonusOffsetText.toIntOrNull() ?: 0,
                            resetCondition = resetCondition,
                            linkedSkillName = linkedSkillName.trim().ifBlank { null },
                            linkedSaveAttribute = linkedSaveAttribute.trim().ifBlank { null }
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