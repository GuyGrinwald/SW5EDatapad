package com.sw5e.datapad.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

enum class ChargeResetCondition { SHORT_REST, LONG_REST, MANUAL }

enum class MaxChargesScaling {
    FIXED,              // Uses fixedMaxCharges
    PROFICIENCY_BONUS,  // Scales with character proficiency bonus
    STR_MOD, DEX_MOD, CON_MOD, INT_MOD, WIS_MOD, CHA_MOD,
    CHARACTER_LEVEL
}

enum class EquipmentCategory { ARMOR, WEAPON, SHIELD, AMMUNITION, EXPLOSIVES, STORAGE, COMMUNICATIONS, MEDICAL, OTHER }

@Entity(tableName = "character_sheet")
data class CharacterEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val species: String = "",
    val background: String = "",
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

    // Skills and Proficiencies
    val skills: List<CharacterSkillEntity> = emptyList(),
    val toolProficiencies: List<String> = emptyList(),
    val features: List<CharacterFeature> = emptyList(),
    
    // Saving Throw Proficiencies
    val saveProfStr: Boolean = false,
    val saveProfDex: Boolean = false,
    val saveProfCon: Boolean = false,
    val saveProfInt: Boolean = false,
    val saveProfWis: Boolean = false,
    val saveProfCha: Boolean = false,
    
    // Movement Speeds
    val speed: Int = 30,
    val swimSpeed: Int = 0,
    val flySpeed: Int = 0,
    val climbSpeed: Int = 0,

    // Combat & Health
    val armorClassOverride: Int = 0,
    val armorBase: Int = 10,
    val dexCap: Int = 99,
    val shieldBonus: Int = 0,
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

    // Combat Proficiencies
    val fightingStyles: List<String> = emptyList(),
    val fightingMasteries: List<String> = emptyList(),
    val lightsaberForms: List<String> = emptyList(),

    // Equipment and Inventory
    val equipment: List<EquipmentItem> = emptyList(),
)

data class CharacterSkillEntity(
    val skillName: String,
    val associatedAttribute: String,
    val proficiencyLevel: Int = 0, // 0=None, 1=Proficient, 2=Expertise
    val manualOverride: Int = 0    // Flat additive bonus/penalty
)

data class EquipmentItem(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val type: String = "Adventuring Gear", // e.g. "Medium Armor", "Vibroweapons"
    val category: EquipmentCategory = EquipmentCategory.OTHER,
    val isEquipped: Boolean = false,
    val cr: Double = 0.0,
    val weight: Double = 0.0,
    val quantity: Int = 1,

    // Armor Stats
    val baseAc: Int = 0,
    val dexCap: Int? = 99, // null = no cap (Light), 2 = Medium, 0 = Heavy

    // Weapon Stats
    val primaryDamageDice: String = "1d6",      // e.g. "1d8"
    val secondaryDamageDice: String? = null,    // Used for Versatile weapons (e.g. "1d10")
    val damageType: String = "Kinetic",         // e.g., Kinetic, Energy, Ion
    val properties: List<String> = emptyList(), // e.g., ["finesse", "versatile", "light", "heavy"]
    val attackBonus: Int = 0,                   // Item attack modifier (+1, +2 weapon)
    val damageBonus: Int = 0,                   // Item damage modifier
    val customAbilityOverride: String? = null   // e.g., "WIS" for Force-empowered strikes
)

data class CharacterFeature(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val source: String = "", // e.g., "Class: Guardian", "Species: Human", "Item: Belt of Strength"
    val description: String = "",
    
    // Charge Management
    val usesCharges: Boolean = true,
    val currentCharges: Int = 0,
    val fixedMaxCharges: Int = 1,
    val scalingType: MaxChargesScaling = MaxChargesScaling.FIXED,
    val chargesBonusOffset: Int = 0, // Flat bonus added to formula (e.g. Prof Bonus + 2)
    val resetCondition: ChargeResetCondition = ChargeResetCondition.LONG_REST,
    
    // Skill / Roll Linkage
    val linkedSkillName: String? = null,      // e.g. "Athletics" to trigger a skill check
    val linkedSaveAttribute: String? = null   // e.g. "DEX" for save DC calculation
)

data class ProcessedWeaponCombat(
    val weapon: EquipmentItem,
    val attackBonus: Int,
    val attackBonusBreakdown: String,
    val primaryDamageText: String,      // e.g. "1d6 + 4 Kinetic"
    val versatileDamageText: String?,   // e.g. "1d8 + 4 Kinetic" (if Versatile)
    val chosenAbility: String           // e.g. "DEX" or "STR"
)