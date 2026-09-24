package com.sw5e.datapad.ui.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.sw5e.datapad.data.CharacterEntity
import com.sw5e.datapad.data.CharacterFeature
import com.sw5e.datapad.ui.theme.*
import com.sw5e.datapad.ui.MainViewModel

@Composable
fun NameEditDialog(
    currentName: String,
    onDismiss: () -> Unit,
    onSave: (newName: String) -> Unit
) {
    var nameText by remember { mutableStateOf(currentName) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Character Name", color = HoloBlue, fontWeight = FontWeight.Bold) },
        text = {
            OutlinedTextField(
                value = nameText,
                onValueChange = { nameText = it },
                label = { Text("Character Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(onClick = { onSave(nameText) }) {
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
fun IdentityEditDialog(
    character: CharacterEntity,
    onDismiss: () -> Unit,
    onSave: (updatedCharacter: CharacterEntity) -> Unit
) {
    var species by remember { mutableStateOf(character.species) }
    var background by remember { mutableStateOf(character.background) }
    var levelText by remember { mutableStateOf(character.level.toString()) }
    var characterClass by remember { mutableStateOf(character.characterClass) }

    // Retrieve existing background feature to pre-fill if present
    val existingBgFeature = remember { character.features.find { it.source.startsWith("Background:") } }
    var bgFeatureName by remember { mutableStateOf(existingBgFeature?.name ?: "") }
    var bgFeatureDesc by remember { mutableStateOf(existingBgFeature?.description ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Identity & Level", color = HoloBlue, fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = levelText,
                    onValueChange = { levelText = it },
                    label = { Text("Level") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = species,
                    onValueChange = { species = it },
                    label = { Text("Species") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = characterClass,
                    onValueChange = { characterClass = it },
                    label = { Text("Class") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = background,
                    onValueChange = { background = it },
                    label = { Text("Background Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = bgFeatureName,
                    onValueChange = { bgFeatureName = it },
                    label = { Text("Background Feature Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = bgFeatureDesc,
                    onValueChange = { bgFeatureDesc = it },
                    label = { Text("Background Feature Description") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val newLevel = levelText.toIntOrNull()?.coerceAtLeast(1) ?: character.level
                    
                    // Enforce 1 background max: remove old one before appending
                    val otherFeatures = character.features.filter { !it.source.startsWith("Background:") }
                    val newFeatures = if (background.isNotBlank() && bgFeatureName.isNotBlank()) {
                        otherFeatures + CharacterFeature(
                            id = existingBgFeature?.id ?: java.util.UUID.randomUUID().toString(),
                            name = bgFeatureName,
                            source = "Background: $background",
                            description = bgFeatureDesc,
                            usesCharges = false // Explicitly disabling charges
                        )
                    } else {
                        otherFeatures
                    }

                    val updated = character.copy(
                        species = species,
                        background = background,
                        level = newLevel,
                        characterClass = characterClass,
                        hitDieMaximum = newLevel,
                        features = newFeatures
                    )
                    onSave(updated)
                }
            ) {
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
fun HpDiceEditDialog(
    character: CharacterEntity,
    onDismiss: () -> Unit,
    onSave: (updatedCharacter: CharacterEntity) -> Unit
) {
    var currentHp by remember { mutableStateOf(character.currentHp.toString()) }
    var maxHp by remember { mutableStateOf(character.maxHp.toString()) }
    var tempHp by remember { mutableStateOf(character.tempHp.toString()) }
    var currentHitDice by remember { mutableStateOf((character.hitDieMaximum - character.hitDieSpent).toString()) }
    var totalHitDice by remember { mutableStateOf(character.hitDieMaximum.toString()) }
    var hitDieType by remember { mutableStateOf(character.hitDieSize) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit HP & Hit Dice", color = HoloBlue, fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Hit Points", style = MaterialTheme.typography.labelLarge, color = NeonAmber, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = currentHp,
                        onValueChange = { currentHp = it },
                        label = { Text("Current HP") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = maxHp,
                        onValueChange = { maxHp = it },
                        label = { Text("Max HP") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = tempHp,
                        onValueChange = { tempHp = it },
                        label = { Text("Temp HP") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }

                HorizontalDivider(color = CardBorder, modifier = Modifier.padding(vertical = 4.dp))

                Text("Hit Dice", style = MaterialTheme.typography.labelLarge, color = NeonAmber, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = currentHitDice,
                        onValueChange = { currentHitDice = it },
                        label = { Text("Current HD") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = totalHitDice,
                        onValueChange = { totalHitDice = it },
                        label = { Text("Max HD") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = hitDieType,
                        onValueChange = { hitDieType = it },
                        label = { Text("Die (e.g. d8)") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val parsedTotalHD = totalHitDice.toIntOrNull() ?: character.hitDieMaximum
                    val parsedCurrentHD = currentHitDice.toIntOrNull() ?: (character.hitDieMaximum - character.hitDieSpent)
                    val calculatedSpentHD = (parsedTotalHD - parsedCurrentHD).coerceAtLeast(0)

                    val updated = character.copy(
                        currentHp = currentHp.toIntOrNull() ?: character.currentHp,
                        maxHp = maxHp.toIntOrNull() ?: character.maxHp,
                        tempHp = tempHp.toIntOrNull() ?: character.tempHp,
                        hitDieSpent = calculatedSpentHD,
                        hitDieMaximum = parsedTotalHD,
                        hitDieSize = hitDieType
                    )
                    onSave(updated)
                }
            ) {
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
fun ArmorClassEditDialog(
    character: CharacterEntity,
    onDismiss: () -> Unit,
    onSave: (updatedCharacter: CharacterEntity) -> Unit
) {
    var armorBase by remember { mutableStateOf(character.armorBase.toString()) }
    var dexCap by remember { mutableStateOf(character.dexCap.toString()) }
    var shieldBonus by remember { mutableStateOf(character.shieldBonus.toString()) }
    var armorClassOverride by remember { mutableStateOf(character.armorClassOverride.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Armor Class Settings", color = HoloBlue, fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = armorBase,
                    onValueChange = { armorBase = it },
                    label = { Text("Base Armor Class (e.g. 10)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                HorizontalDivider(color = CardBorder, modifier = Modifier.padding(vertical = 4.dp))
                OutlinedTextField(
                    value = armorClassOverride,
                    onValueChange = { armorClassOverride = it },
                    label = { Text("Flat AC Override (0 = Use Formula)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val updated = character.copy(
                        armorBase = armorBase.toIntOrNull() ?: character.armorBase,
                        dexCap = dexCap.toIntOrNull() ?: character.dexCap,
                        shieldBonus = shieldBonus.toIntOrNull() ?: character.shieldBonus,
                        armorClassOverride = armorClassOverride.toIntOrNull() ?: character.armorClassOverride
                    )
                    onSave(updated)
                }
            ) {
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
fun SpeedCreditsEditDialog(
    currentSpeed: Int,
    currentSwimSpeed: Int,
    currentFlySpeed: Int,
    currentClimbSpeed: Int,
    currentCredits: Int,
    onDismiss: () -> Unit,
    onSave: (speed: Int, swimSpeed: Int, flySpeed: Int, climbSpeed: Int, credits: Int) -> Unit
) {
    var speedText by remember { mutableStateOf(currentSpeed.toString()) }
    var swimSpeedText by remember { mutableStateOf(currentSwimSpeed.toString()) }
    var flySpeedText by remember { mutableStateOf(currentFlySpeed.toString()) }
    var climbSpeedText by remember { mutableStateOf(currentClimbSpeed.toString()) }
    var creditsText by remember { mutableStateOf(currentCredits.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Speeds & Credits") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = speedText,
                    onValueChange = { speedText = it },
                    label = { Text("Walk Speed (ft)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
                OutlinedTextField(
                    value = swimSpeedText,
                    onValueChange = { swimSpeedText = it },
                    label = { Text("Swim Speed (ft)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
                OutlinedTextField(
                    value = flySpeedText,
                    onValueChange = { flySpeedText = it },
                    label = { Text("Fly Speed (ft)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
                OutlinedTextField(
                    value = climbSpeedText,
                    onValueChange = { climbSpeedText = it },
                    label = { Text("Climb Speed (ft)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
                OutlinedTextField(
                    value = creditsText,
                    onValueChange = { creditsText = it },
                    label = { Text("Credits (¢)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val newSpeed = speedText.toIntOrNull() ?: currentSpeed
                    val newSwim = swimSpeedText.toIntOrNull() ?: currentSwimSpeed
                    val newFly = flySpeedText.toIntOrNull() ?: currentFlySpeed
                    val newClimb = climbSpeedText.toIntOrNull() ?: currentClimbSpeed
                    val newCredits = creditsText.toIntOrNull() ?: currentCredits
                    onSave(newSpeed, newSwim, newFly, newClimb, newCredits)
                }
            ) {
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
fun AttributeEditDialog(attributeName: String, currentScore: Int, onDismiss: () -> Unit, onSave: (Int) -> Unit) {
    var scoreText by remember { mutableStateOf(currentScore.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit $attributeName Score") },
        text = {
            OutlinedTextField(
                value = scoreText,
                onValueChange = { scoreText = it },
                label = { Text("Score") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        },
        confirmButton = {
            Button(onClick = { onSave(scoreText.toIntOrNull() ?: currentScore) }) { Text("Save") }
        }
    )
}

@Composable
fun RestActionDialog(
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    // State to toggle between the rest selection and short rest inputs
    var showShortRestOptions by remember { mutableStateOf(false) }
    
    // State for the user's input
    var hitDiceToSpend by remember { mutableStateOf("") }
    var healingReceived by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (showShortRestOptions) "Short Rest Details" else "Take a Rest", 
                color = HoloBlue, 
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (!showShortRestOptions) {
                    Text("Select the type of rest you want to take.")
                    
                    Button(
                        onClick = { showShortRestOptions = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Short Rest (1 Hour)")
                    }
                    
                    Button(
                        onClick = {
                            viewModel.takeLongRest()
                            onDismiss()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Long Rest (8 Hours)")
                    }
                } else {
                    Text("Enter the hit dice spent and the total healing rolled.")
                    
                    OutlinedTextField(
                        value = hitDiceToSpend,
                        onValueChange = { hitDiceToSpend = it },
                        label = { Text("Hit Dice Spent") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    OutlinedTextField(
                        value = healingReceived,
                        onValueChange = { healingReceived = it },
                        label = { Text("Healing Received") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            // Only show the confirm button if we are in the Short Rest input view
            if (showShortRestOptions) {
                Button(
                    onClick = {
                        val dice = hitDiceToSpend.toIntOrNull() ?: 0
                        val healing = healingReceived.toIntOrNull() ?: 0
                        viewModel.takeShortRest(dice, healing)
                        onDismiss()
                    }
                ) {
                    Text("Confirm")
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    if (showShortRestOptions) {
                        // Go back to the main rest selection
                        showShortRestOptions = false 
                    } else {
                        onDismiss()
                    }
                }
            ) {
                Text(if (showShortRestOptions) "Back" else "Cancel")
            }
        }
    )
}