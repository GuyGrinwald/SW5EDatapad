package com.sw5e.datapad.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.sw5e.datapad.data.CharacterFeature
import com.sw5e.datapad.data.EquipmentItem
import com.sw5e.datapad.data.ProcessedWeaponCombat
import com.sw5e.datapad.data.WeaponPropertyDefinition
import com.sw5e.datapad.ui.MainViewModel
import com.sw5e.datapad.ui.theme.*

@Composable
fun CombatScreen(viewModel: MainViewModel) {
    val character by viewModel.character.collectAsState()
    val weaponPropertiesDict by viewModel.weaponPropertiesDict.collectAsState()
    
    // Dialog state for setting global flat attack/damage boost
    var showBoostDialog by remember { mutableStateOf(false) }

    // Base combat stats calculations
    val profBonus = viewModel.getProficiencyBonus()
    val strMod = viewModel.getStatModifierByName("STR")
    val dexMod = viewModel.getStatModifierByName("DEX")
    val flatBoost = character.attackSpecialBonus

    // Base Attack Modifiers for the Tactical HUD
    val meleeAttack = profBonus + strMod + flatBoost
    val rangedAttack = profBonus + dexMod + flatBoost
    val finesseAttack = profBonus + maxOf(strMod, dexMod) + flatBoost
    val thrownAttack = profBonus + strMod + flatBoost

    val formatBonus = { v: Int -> if (v >= 0) "+$v" else "$v" }

    // Process equipped weapons dynamically with proper attack type logic
    val processedWeapons = remember(character, flatBoost) {
        character.equipment.filter { it.isEquipped && it.category == com.sw5e.datapad.data.EquipmentCategory.WEAPON }.map { weapon ->
            processWeaponCombat(weapon, viewModel, flatBoost, profBonus, strMod, dexMod)
        }
    }

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Weapons")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 1. Tactical HUD (Sticky Top)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, CardBorder, RoundedCornerShape(12.dp)),
            colors = CardDefaults.cardColors(containerColor = SpaceBlack)
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Header Row with Flat Boost Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val hpText = "${character.currentHp}/${character.maxHp}${if (character.tempHp > 0) " (+${character.tempHp})" else ""}"
                    StatBadge("HP", hpText, SithRed)
                    StatBadge("AC", "${viewModel.calculateArmorClass()}", HoloBlue)

                    // Flat Boost Toggle / Button
                    Button(
                        onClick = { showBoostDialog = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NeonAmber
                        ),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Flat Boost",
                            tint = SpaceBlack,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Boost: ${formatBonus(flatBoost)}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = SpaceBlack
                        )
                    }
                }

                HorizontalDivider(color = CardBorder.copy(alpha = 0.5f))

                // Attack Type Summary Row (All 4 Attack Types)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StatBadge("MELEE", formatBonus(meleeAttack), NeonAmber)
                    StatBadge("RANGED", formatBonus(rangedAttack), HoloBlue)
                    StatBadge("FINESSE", formatBonus(finesseAttack), NeonAmber)
                    StatBadge("THROWN", formatBonus(thrownAttack), SithRed)
                }

                HorizontalDivider(color = CardBorder.copy(alpha = 0.5f))

                // Mini Table for Equipped Weapons
                if (processedWeapons.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Text("WEAPON", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(2f))
                            Text("ATK", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
                            Text("DAMAGE", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(2.5f))
                        }
                        processedWeapons.forEach { pw ->
                            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                Text(pw.weapon.name, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = HoloBlue, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(2f))
                                Text(formatBonus(pw.attackBonus), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                                Text(pw.primaryDamageText, style = MaterialTheme.typography.bodySmall, color = NeonAmber, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(2.5f))
                            }
                        }
                    }
                } else {
                    Text("No weapons equipped.", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        // 2. Quick Resources (Middle Ribbon)
        val resourceFeatures = character.features.filter { it.usesCharges }
        if (resourceFeatures.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Quick Resources", style = MaterialTheme.typography.titleSmall, color = HoloBlue, fontWeight = FontWeight.Bold)
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(resourceFeatures, key = { it.id }) { feature ->
                        ResourceRibbonChip(
                            feature = feature,
                            maxCharges = viewModel.calculateMaxCharges(feature),
                            onSpend = { viewModel.spendFeatureCharge(feature.id, -1) }
                        )
                    }
                }
            }
        }

        // 3. Action Tabs & Weapon List
        Column(modifier = Modifier.weight(1f)) {
            ScrollableTabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = SpaceBlack,
                contentColor = HoloBlue,
                edgePadding = 0.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(title, fontWeight = FontWeight.Bold) }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))

            if (processedWeapons.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No equipped weapons found. Equip weapons in Equipment Screen.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(processedWeapons, key = { it.weapon.id }) { processed ->
                        WeaponCombatCard(
                            processed = processed,
                            weaponPropertiesDict = weaponPropertiesDict
                        )
                    }
                }
            }
        }
    }

    // Flat Boost Edit Dialog
    if (showBoostDialog) {
        FlatBoostDialog(
            currentBoost = flatBoost,
            onDismiss = { showBoostDialog = false },
            onConfirm = { newBoost ->
                viewModel.updateCharacter(character.copy(attackSpecialBonus = newBoost))
                showBoostDialog = false
            }
        )
    }
}

/**
 * Calculates attack and damage metrics per weapon based on weapon type, properties,
 * custom ability overrides, and global flat boosts.
 */
private fun processWeaponCombat(
    weapon: EquipmentItem,
    viewModel: MainViewModel,
    flatBoost: Int,
    profBonus: Int,
    strMod: Int,
    dexMod: Int
): ProcessedWeaponCombat {
    val overrideAbility = weapon.customAbilityOverride?.takeIf { it.isNotBlank() }
    val overrideMod = overrideAbility?.let { viewModel.getStatModifierByName(it) }

    val propertiesLower = weapon.properties.map { it.lowercase() }
    val isFinesse = "finesse" in propertiesLower
    val isRanged = weapon.type.contains("blaster", ignoreCase = true) || 
                   weapon.type.contains("ranged", ignoreCase = true) || 
                   "range" in propertiesLower || 
                   "ammunition" in propertiesLower
    val isThrown = "thrown" in propertiesLower

    // Ability Modifier Resolution
    val (chosenAbility, abilityMod) = when {
        overrideAbility != null && overrideMod != null -> {
            overrideAbility.uppercase() to overrideMod
        }
        isFinesse -> {
            val bestMod = maxOf(strMod, dexMod)
            val name = if (bestMod == dexMod) "DEX" else "STR"
            name to bestMod
        }
        isRanged -> {
            "DEX" to dexMod
        }
        isThrown -> {
            "STR" to strMod
        }
        else -> { // Melee / Vibroweapons / Lightweapons / Improvised / Default
            "STR" to strMod
        }
    }

    val totalAtkBonus = profBonus + abilityMod + weapon.attackBonus + flatBoost
    val totalDmgBonus = weapon.damageBonus + flatBoost

    val formatBonus = { v: Int -> if (v >= 0) "+$v" else "$v" }
    val formatDmgBonus = { v: Int -> if (v > 0) " + $v" else if (v < 0) " - ${-v}" else "" }

    val attackBonusBreakdown = "Prof (${formatBonus(profBonus)}) + $chosenAbility (${formatBonus(abilityMod)})" +
            (if (weapon.attackBonus != 0) " + Weapon (${formatBonus(weapon.attackBonus)})" else "") +
            (if (flatBoost != 0) " + Boost (${formatBonus(flatBoost)})" else "")

    val primaryDmgText = "${weapon.primaryDamageDice}${formatDmgBonus(totalDmgBonus)} ${weapon.damageType}"
    val versatileDmgText = weapon.secondaryDamageDice?.let {
        "$it${formatDmgBonus(totalDmgBonus)} ${weapon.damageType}"
    }

    return ProcessedWeaponCombat(
        weapon = weapon,
        attackBonus = totalAtkBonus,
        attackBonusBreakdown = attackBonusBreakdown,
        primaryDamageText = primaryDmgText,
        versatileDamageText = versatileDmgText,
        chosenAbility = chosenAbility
    )
}

@Composable
private fun StatBadge(label: String, value: String, valueColor: androidx.compose.ui.graphics.Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = valueColor)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun ResourceRibbonChip(
    feature: CharacterFeature,
    maxCharges: Int,
    onSpend: () -> Unit
) {
    val hasCharges = feature.currentCharges > 0
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(enabled = hasCharges, onClick = onSpend)
            .border(1.dp, if (hasCharges) HoloBlue else CardBorder, RoundedCornerShape(16.dp)),
        color = if (hasCharges) HoloBlue.copy(alpha = 0.1f) else SpaceBlack
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = feature.name,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = if (hasCharges) HoloBlue else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Box(
                modifier = Modifier
                    .background(if (hasCharges) NeonAmber else CardBorder, CircleShape)
                    .padding(horizontal = 6.dp, vertical = 2.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${feature.currentCharges}/$maxCharges",
                    style = MaterialTheme.typography.labelSmall,
                    color = SpaceBlack,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun WeaponCombatCard(
    processed: ProcessedWeaponCombat,
    weaponPropertiesDict: Map<String, WeaponPropertyDefinition> = emptyMap()
) {
    val weapon = processed.weapon
    val formatBonus = { v: Int -> if (v >= 0) "+$v" else "$v" }
    var isExpanded by remember { mutableStateOf(false) }

    // State for tracking selected property tag description dialog
    var selectedPropertyInfo by remember { mutableStateOf<Pair<String, String>?>(null) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, CardBorder, RoundedCornerShape(10.dp))
            .clickable { isExpanded = !isExpanded },
        colors = CardDefaults.cardColors(containerColor = SpaceBlack)
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            // Weapon Header Title & Expand Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = weapon.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = HoloBlue
                )
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = HoloBlue
                )
            }

            // Expanded Details Section
            if (isExpanded) {
                Text(
                    text = "${weapon.type} | Ability: ${processed.chosenAbility}",
                    style = MaterialTheme.typography.labelSmall,
                    color = NeonAmber
                )

                // Weapon Properties Chips with Click Handler
                if (weapon.properties.isNotEmpty()) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(weapon.properties) { prop ->
                            // Look up base key in dictionary (handles properties with numbers/brackets e.g. "Burst 3")
                            val cleanKey = prop.lowercase().trim()
                            val baseKey = cleanKey.split(" ", "(").firstOrNull() ?: cleanKey
                            val propDef = weaponPropertiesDict[cleanKey] ?: weaponPropertiesDict[baseKey]

                            SuggestionChip(
                                onClick = {
                                    val title = propDef?.name ?: prop.replaceFirstChar { 
                                        if (it.isLowerCase()) it.titlecase() else it.toString() 
                                    }
                                    val description = propDef?.description 
                                        ?: propDef?.description 
                                        ?: "No description available for this property."
                                    selectedPropertyInfo = title to description
                                },
                                label = { 
                                    Text(
                                        prop.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }, 
                                        style = MaterialTheme.typography.labelSmall
                                    ) 
                                }
                            )
                        }
                    }
                }

                HorizontalDivider(color = CardBorder)

                // Attack & Damage Stats
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Attack Modifier: ${formatBonus(processed.attackBonus)}", fontWeight = FontWeight.Bold, color = HoloBlue)
                    Text("Breakdown: ${processed.attackBonusBreakdown}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                    Spacer(modifier = Modifier.height(2.dp))

                    Text("Primary Damage: ${processed.primaryDamageText}", fontWeight = FontWeight.Bold, color = NeonAmber)

                    val versatileDmg = processed.versatileDamageText
                    if (versatileDmg != null) {
                        Text("Two-Handed / Versatile: $versatileDmg", fontWeight = FontWeight.Bold, color = SithRed)
                    }
                }
            }
        }
    }

    // Tag Description Dialog
    selectedPropertyInfo?.let { (title, description) ->
        AlertDialog(
            onDismissRequest = { selectedPropertyInfo = null },
            title = {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = HoloBlue
                )
            },
            text = {
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            confirmButton = {
                TextButton(onClick = { selectedPropertyInfo = null }) {
                    Text("Close", color = NeonAmber, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = SpaceBlack,
            shape = RoundedCornerShape(12.dp)
        )
    }
}

@Composable
private fun FlatBoostDialog(
    currentBoost: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var boostText by remember { mutableStateOf(currentBoost.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Set Global Attack & Damage Boost", color = HoloBlue) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "Apply a flat modifier to all attack and damage rolls.",
                    style = MaterialTheme.typography.bodyMedium
                )
                OutlinedTextField(
                    value = boostText,
                    onValueChange = { boostText = it },
                    label = { Text("Flat Bonus") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val parsed = boostText.toIntOrNull() ?: 0
                    onConfirm(parsed)
                }
            ) {
                Text("Apply", color = NeonAmber, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        containerColor = SpaceBlack,
        shape = RoundedCornerShape(12.dp)
    )
}