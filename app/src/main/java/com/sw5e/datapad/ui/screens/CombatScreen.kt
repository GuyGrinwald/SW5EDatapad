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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.sw5e.datapad.data.CharacterFeature
import com.sw5e.datapad.data.EquipmentCategory
import com.sw5e.datapad.data.EquipmentItem
import com.sw5e.datapad.data.ProcessedWeaponCombat
import com.sw5e.datapad.data.WeaponPropertyDefinition
import com.sw5e.datapad.ui.MainViewModel
import com.sw5e.datapad.ui.dialogs.*
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
        character.equipment.filter { weapon ->
            weapon.isEquipped && (
                weapon.category == EquipmentCategory.WEAPON ||
                weapon.type.contains("blaster", ignoreCase = true) ||
                weapon.type.contains("weapon", ignoreCase = true) ||
                weapon.type.contains("vibro", ignoreCase = true) ||
                weapon.type.contains("lightweapon", ignoreCase = true)
            )
        }.map { weapon ->
            processWeaponCombat(weapon, viewModel, flatBoost, profBonus)
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
        // Unified Standard Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Combat Operations",
                    style = MaterialTheme.typography.titleLarge,
                    color = NeonAmber
                )
                Text(
                    text = "Armor Class: ${viewModel.calculateArmorClass()} | HP: ${character.currentHp}/${character.maxHp}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = HoloBlue
                )
            }
        }

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
                            Text(
                                text = "WEAPON",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.weight(4f)
                            )
                            Text(
                                text = "ATK",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.weight(1.2f)
                            )
                            Text(
                                text = "DAMAGE",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.weight(2.8f)
                            )
                        }
                        processedWeapons.forEach { pw ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = pw.weapon.name,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = HoloBlue,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(4f)
                                )
                                Text(
                                    text = formatBonus(pw.attackBonus),
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.weight(1.2f)
                                )
                                Text(
                                    text = pw.primaryDamageText,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = NeonAmber,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(2.8f)
                                )
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

private fun processWeaponCombat(
    weapon: EquipmentItem,
    viewModel: MainViewModel,
    flatBoost: Int,
    profBonus: Int,
): ProcessedWeaponCombat {
    val overrideAbility = weapon.customAbilityOverride?.takeIf { it.isNotBlank() }
    val overrideMod = overrideAbility?.let { viewModel.getStatModifierByName(it) }

    val weaponPropsDict = viewModel.weaponPropertiesDict.value
    val propertiesLower = weapon.properties.map { it.lowercase().trim() }
    
    val isRanged = weapon.type.contains("blaster", ignoreCase = true) || 
                   weapon.type.contains("ranged", ignoreCase = true) || 
                   "range" in propertiesLower || 
                   "ammunition" in propertiesLower

    val defaultAbility = if (isRanged) "DEX" else "STR"
    val candidateAbilities = mutableSetOf(defaultAbility)

    if ("finesse" in propertiesLower) {
        candidateAbilities.add("DEX")
        candidateAbilities.add("STR")
    }

    for (prop in propertiesLower) {
        val baseKey = prop.split(" ", "(").firstOrNull() ?: prop
        val propDef = weaponPropsDict[prop] ?: weaponPropsDict[baseKey]
        propDef?.attackAbilityScore?.takeIf { it.isNotBlank() }?.let {
            candidateAbilities.add(it.uppercase())
        }
    }

    val (chosenAbility, abilityMod) = when {
        overrideAbility != null && overrideMod != null -> {
            overrideAbility.uppercase() to overrideMod
        }
        else -> {
            var bestAbility = defaultAbility
            var maxMod = viewModel.getStatModifierByName(defaultAbility)

            for (ability in candidateAbilities) {
                val mod = viewModel.getStatModifierByName(ability)
                if (mod > maxMod || (mod == maxMod && ability == "DEX" && bestAbility == "STR")) {
                    maxMod = mod
                    bestAbility = ability
                }
            }
            bestAbility to maxMod
        }
    }

    val totalAtkBonus = profBonus + abilityMod + weapon.attackBonus + flatBoost
    val totalDmgBonus = abilityMod + weapon.damageBonus + flatBoost

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

    var selectedPropertyInfo by remember { mutableStateOf<Pair<String, String>?>(null) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, CardBorder, RoundedCornerShape(10.dp))
            .clickable { isExpanded = !isExpanded },
        colors = CardDefaults.cardColors(containerColor = SpaceBlack)
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
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

            if (isExpanded) {
                Text(
                    text = "${weapon.type} | Ability: ${processed.chosenAbility}",
                    style = MaterialTheme.typography.labelSmall,
                    color = NeonAmber
                )

                if (weapon.properties.isNotEmpty()) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(weapon.properties) { prop ->
                            val cleanKey = prop.lowercase().trim()
                            val baseKey = cleanKey.split(" ", "(").firstOrNull() ?: cleanKey
                            val propDef = weaponPropertiesDict[cleanKey] ?: weaponPropertiesDict[baseKey]

                            SuggestionChip(
                                onClick = {
                                    val title = propDef?.name ?: prop.replaceFirstChar { 
                                        if (it.isLowerCase()) it.titlecase() else it.toString() 
                                    }
                                    val description = propDef?.description 
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