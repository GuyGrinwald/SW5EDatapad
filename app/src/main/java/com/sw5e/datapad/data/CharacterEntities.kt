package com.sw5e.datapad.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "character_sheet")
data class CharacterEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val species: String = "",
    val characterClass: String = "",
    val level: Int = 1,
    val imageUri: String? = null,
    
    // Core Attributes
    val str: Int = 10,
    val dex: Int = 10,
    val con: Int = 10,
    val intStat: Int = 10,
    val wis: Int = 10,
    val cha: Int = 10,
    
    // Saving Throw Proficiencies
    val saveProfStr: Boolean = false,
    val saveProfDex: Boolean = false,
    val saveProfCon: Boolean = false,
    val saveProfInt: Boolean = false,
    val saveProfWis: Boolean = false,
    val saveProfCha: Boolean = false,

    // Combat & Health
    val armorClassOverride: Int = 0,
    val armorBase: Int = 10,
    val dexCap: Int = 99,
    val isArmorProficient: Boolean = true,
    val shieldBonus: Int = 0,
    val speed: Int = 30,
    val currentHp: Int = 10,
    val maxHp: Int = 10,
    val tempHp: Int = 0,
    val hitDieSpent: Int = 0,
    val hitDieMaximum: Int = 1,
    val hitDieSize: String = "1d8",
    val attackSpecialBonus: Int = 0,
    val armorProficiencies: List<String> = emptyList(),
    val weaponProficiencies: List<String> = emptyList(),

    // Casting Resources
    val currentForcePoints: Int = 0,
    val maxForcePoints: Int = 0,
    val currentTechPoints: Int = 0,
    val maxTechPoints: Int = 0,
    val forceAttackSpecialBonus: Int = 0,
    val techAttackSpecialBonus: Int = 0,
    
    // Economy
    val credits: Int = 0,
    
    // Damage Resistances and Immunities
    val resistances: List<String> = emptyList(),
    val immunities: List<String> = emptyList(),
    
    // Powers mapped by Level (0 = At-Will, 1-9 = Leveled)
    val forcePowers: Map<Int, List<String>> = emptyMap(),
    val techPowers: Map<Int, List<String>> = emptyMap(),

    // Feats, Traits, and other features can be added here as needed
    val feats: List<String> = emptyList(),

    // Proficiencies
    val toolProficiencies: List<String> = emptyList(),
    val fightingStyles: List<String> = emptyList(),
    val fightingMasteries: List<String> = emptyList(),
    val lightsaberForms: List<String> = emptyList(),

    // Equipment and Inventory
    val equipment: List<EquipmentItem> = emptyList(),
)

@Entity(tableName = "character_skills", primaryKeys = ["characterId", "skillName"])
data class CharacterSkillEntity(
    val characterId: String = "",
    val skillName: String,
    val associatedAttribute: String,
    val proficiencyLevel: Int = 0, // 0=None, 1=Proficient, 2=Expertise
    val manualOverride: Int = 0    // Flat additive bonus/penalty
)

data class EquipmentItem(
    val name: String = "",
    val type: String = "Adventuring Gear",
    val isEquipped: Boolean = false,
    val cr: Double = 0.0,
    val weight: Double = 0.0,
    val quantity: Int = 1
)