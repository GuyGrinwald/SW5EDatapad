package com.sw5e.datapad.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import com.sw5e.datapad.ui.MainViewModel
import com.sw5e.datapad.ui.components.HeaderBadge
import com.sw5e.datapad.ui.components.StatBox
import com.sw5e.datapad.ui.dialogs.*
import com.sw5e.datapad.ui.theme.*

@Composable
fun CharacterSummaryScreen(viewModel: MainViewModel) {
    val character by viewModel.character.collectAsState()
    val profBonus = viewModel.getProficiencyBonus()

    // --- Header Dialog & Launcher Triggers ---
    var showNameDialog by remember { mutableStateOf(false) }
    var showIdentityDialog by remember { mutableStateOf(false) }
    var showHpDiceDialog by remember { mutableStateOf(false) }
    var showAcDialog by remember { mutableStateOf(false) }
    var showSpeedCreditsDialog by remember { mutableStateOf(false) }
    var selectedAttribute by remember { mutableStateOf<String?>(null) }
    var showRestDialog by remember { mutableStateOf(false) }
        
    // Media Picker for Character Portrait
    val context = LocalContext.current
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let {
            viewModel.updateCharacterImage(context, it)
        }
    }

    // --- Header Dialogs ---
    if (showNameDialog) {
        NameEditDialog(
            currentName = character.name,
            onDismiss = { showNameDialog = false },
            onSave = { newName ->
                viewModel.updateCharacter(character.copy(name = newName))
                showNameDialog = false
            }
        )
    }

    if (showIdentityDialog) {
        IdentityEditDialog(
            character = character,
            onDismiss = { showIdentityDialog = false },
            onSave = { updated ->
                viewModel.updateCharacter(updated)
                showIdentityDialog = false
            }
        )
    }

    if (showHpDiceDialog) {
        HpDiceEditDialog(
            character = character,
            onDismiss = { showHpDiceDialog = false },
            onSave = { updated ->
                viewModel.updateCharacter(updated)
                showHpDiceDialog = false
            }
        )
    }

    if (showAcDialog) {
        ArmorClassEditDialog(
            character = character,
            onDismiss = { showAcDialog = false },
            onSave = { updated ->
                viewModel.updateCharacter(updated)
                showAcDialog = false
            }
        )
    }

    if (showSpeedCreditsDialog) {
        SpeedCreditsEditDialog(
            currentSpeed = character.speed,
            currentSwimSpeed = character.swimSpeed,
            currentFlySpeed = character.flySpeed,
            currentClimbSpeed = character.climbSpeed,
            currentCredits = character.credits,
            onDismiss = { showSpeedCreditsDialog = false },
            onSave = { newSpeed, newSwimSpeed, newFlySpeed, newClimbSpeed, newCredits ->
                viewModel.updateCharacter(
                    character.copy(
                        speed = newSpeed,
                        swimSpeed = newSwimSpeed,
                        flySpeed = newFlySpeed,
                        climbSpeed = newClimbSpeed,
                        credits = newCredits
                    )
                )
                showSpeedCreditsDialog = false
            }
        )
    }
    
    if (showRestDialog) {
        RestActionDialog(
            viewModel = viewModel,
            onDismiss = { showRestDialog = false }
        )
    }

    selectedAttribute?.let { attr ->
        val currentScore = when (attr) {
            "STR" -> character.str
            "DEX" -> character.dex
            "CON" -> character.con
            "INT" -> character.intStat
            "WIS" -> character.wis
            "CHA" -> character.cha
            else -> 10
        }
        AttributeEditDialog(
            attributeName = attr,
            currentScore = currentScore,
            onDismiss = { selectedAttribute = null },
            onSave = { newScore ->
                viewModel.updateStat(attr, newScore)
                selectedAttribute = null
            }
        )
    }

    // Fully Scrollable Main View
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

// --- 1. Identity & Metadata Card ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, CardBorder, RoundedCornerShape(12.dp))
                .clickable { showIdentityDialog = true },
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .border(2.dp, HoloBlue, CircleShape)
                            .clickable {
                                imagePickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (!character.imageUri.isNullOrEmpty()) {
                            AsyncImage(
                                model = character.imageUri,
                                contentDescription = "Character Portrait",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(Icons.Default.Person, contentDescription = "Upload Avatar", tint = HoloBlue)
                        }
                    }

                    Text(
                        text = character.name.ifBlank { "Unnamed Character" },
                        style = MaterialTheme.typography.headlineSmall,
                        color = HoloBlue,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { showNameDialog = true }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = CardBorder.copy(alpha = 0.3f))
                Spacer(modifier = Modifier.height(12.dp))

                
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            LabeledData("CLASS & LEVEL", "${character.characterClass} ${character.level}")
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            LabeledData("SPECIES", character.species.ifBlank { "Unknown" })
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            LabeledData("BACKGROUND", character.background.ifBlank { "None" })
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            LabeledData("PROFICIENCY BONUS", "+$profBonus")
                        }
                    }
                }
            }
        }

        // --- 2. Combat Vitals Card (HP, AC, Hit Dice) ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, CardBorder, RoundedCornerShape(12.dp))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.clickable { showAcDialog = true }) {
                    HeaderBadge("ARMOR CLASS", "${viewModel.calculateArmorClass()}")
                }
                Box(modifier = Modifier.clickable { showHpDiceDialog = true }) {
                    HeaderBadge("HIT POINTS", "${character.currentHp} / ${character.maxHp}")
                }
                Box(modifier = Modifier.clickable { showHpDiceDialog = true }) {
                    HeaderBadge("TEMP HP", "${character.tempHp}")
                }
                Box(modifier = Modifier.clickable { showHpDiceDialog = true }) {
                    val availableDice = character.hitDieMaximum - character.hitDieSpent
                    // Clean up hitDieSize to prevent double digits/numbers (e.g. "1d8" -> "d8")
                    val cleanDieSize = "d${character.hitDieSize.substringAfter('d')}"
                    HeaderBadge("HIT DICE", "$availableDice$cleanDieSize")
                }
            }
        }
        // --- 3. Movement & Utility Card ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, CardBorder, RoundedCornerShape(12.dp))
                .clickable { showSpeedCreditsDialog = true }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                HeaderBadge("SPEED", "${character.speed} ft")
                if (character.swimSpeed > 0) HeaderBadge("SWIM", "${character.swimSpeed} ft")
                if (character.flySpeed > 0) HeaderBadge("FLY", "${character.flySpeed} ft")
                if (character.climbSpeed > 0) HeaderBadge("CLIMB", "${character.climbSpeed} ft")
                HeaderBadge("CREDITS", "${character.credits} ¢")
            }
        }

        // Rest Trigger Actions
        RestTriggerCard(onClick = { showRestDialog = true })

        // Ability Score Ribbon
        Text(
            "Ability Scores",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = HoloBlue
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            StatBox("STR", character.str, viewModel.getAttributeModifier(character.str)) { selectedAttribute = "STR" }
            StatBox("DEX", character.dex, viewModel.getAttributeModifier(character.dex)) { selectedAttribute = "DEX" }
            StatBox("CON", character.con, viewModel.getAttributeModifier(character.con)) { selectedAttribute = "CON" }
            StatBox("INT", character.intStat, viewModel.getAttributeModifier(character.intStat)) { selectedAttribute = "INT" }
            StatBox("WIS", character.wis, viewModel.getAttributeModifier(character.wis)) { selectedAttribute = "WIS" }
            StatBox("CHA", character.cha, viewModel.getAttributeModifier(character.cha)) { selectedAttribute = "CHA" }
        }
    }
}

@Composable
fun RestTriggerCard(onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, CardBorder, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = SpaceBlack)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Build, 
                contentDescription = "Rest",
                tint = NeonAmber,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "TAKE A SHORT OR LONG REST",
                style = MaterialTheme.typography.labelLarge,
                color = NeonAmber,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun LabeledData(label: String, value: String) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            fontWeight = FontWeight.Bold
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Medium
        )
    }
}