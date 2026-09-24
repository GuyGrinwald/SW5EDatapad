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
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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

    // Media Picker for Character Portrait
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let {
            viewModel.updateCharacter(character.copy(imageUri = it.toString()))
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
        // Structured Header Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, CardBorder, RoundedCornerShape(12.dp))
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // --- Row 1: Portrait Avatar + Character Name ---
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Avatar Frame
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .border(1.5.dp, HoloBlue, CircleShape)
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
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Upload Avatar",
                                tint = HoloBlue,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }

                    // Name Column
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { showNameDialog = true }
                    ) {
                        Text(
                            text = character.name.ifBlank { "Unnamed Character" },
                            style = MaterialTheme.typography.titleLarge,
                            color = HoloBlue,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                HorizontalDivider(color = CardBorder.copy(alpha = 0.5f))

                // --- Row 2: Species, Level, Class, Proficiency Bonus ---
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showIdentityDialog = true }
                        .padding(vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "${character.species} • Lvl ${character.level} ${character.characterClass}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        HeaderBadge("PROF BONUS", "+$profBonus")
                    }
                }

                HorizontalDivider(color = CardBorder.copy(alpha = 0.5f))

                // --- Row 3: HP, Temp HP, Hit Dice ---
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showHpDiceDialog = true }
                        .padding(vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        HeaderBadge("HP", "${character.currentHp}/${character.maxHp}")
                        HeaderBadge("TEMP HP", "${character.tempHp}")
                        HeaderBadge(
                            "HIT DICE",
                            "${character.hitDieMaximum - character.hitDieSpent}/${character.hitDieMaximum} (${character.hitDieSize})"
                        )
                    }
                }

                HorizontalDivider(color = CardBorder.copy(alpha = 0.5f))

                // --- Row 4: Calculated AC ---
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showAcDialog = true }
                        .padding(vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        HeaderBadge("ARMOR CLASS", "${viewModel.calculateArmorClass()}")
                    }
                }

                HorizontalDivider(color = CardBorder.copy(alpha = 0.5f))

                // --- Row 5: Movement Speeds and Credits UI ---
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showSpeedCreditsDialog = true }
                        .padding(vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        HeaderBadge("WALK", "${character.speed} ft")
                        if (character.swimSpeed > 0) {
                            HeaderBadge("SWIM", "${character.swimSpeed} ft")
                        }
                        if (character.flySpeed > 0) {
                            HeaderBadge("FLY", "${character.flySpeed} ft")
                        }
                        if (character.climbSpeed > 0) {
                            HeaderBadge("CLIMB", "${character.climbSpeed} ft")
                        }
                        HeaderBadge("CREDITS", "${character.credits} ¢")
                    }
                }
            }
        }

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