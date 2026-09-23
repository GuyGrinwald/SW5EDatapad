package com.sw5e.datapad.data

import com.google.gson.annotations.SerializedName

data class CompendiumResponse(val compendium: CompendiumData)

data class CompendiumData(
    val skills: List<SkillDefinition> = emptyList(),
    val feats: List<FeatDefinition> = emptyList(),
    val fightingStyles: List<FightingStyleDefinition> = emptyList(),
    val fightingMasteries: List<FightingMasteryDefinition> = emptyList(),
    val lightsaberForms: List<LightsaberFormDefinition> = emptyList(),
    val forcePowers: List<PowerDefinition> = emptyList(),
    val techPowers: List<PowerDefinition> = emptyList(),
    val tools: List<ToolDefinition> = emptyList(),  
    val armor: List<ArmorProficiency> = emptyList(),
    val weapons: List<WeaponProficiency> = emptyList()
)

data class SkillDefinition(
    val name: String = "",
    val attribute: String = ""
)

data class FeatDefinition(
    val name: String = "",
    val prerequisite: String? = "",
    val description: String? = ""
)

data class PowerDefinition(
    val name: String,
    val level: String = "At-will",
    val type: String = "", // "Light Side" or "Dark Side" or "Tech"
    @SerializedName("casting time")
    val castingTime: String? = "",
    val range: String = "",
    val duration: String = "",
    val description: String = ""
){
    val levelInt: Int
        get() = when {
            level.equals("At-will", ignoreCase = true) -> 0
            else -> level.filter { it.isDigit() }.toIntOrNull() ?: 0
        }
}

data class FightingStyleDefinition(
    val name: String = "",
    val description: String = ""
)

data class FightingMasteryDefinition(
    val name: String = "",
    val description: String = ""
)

data class LightsaberFormDefinition(
    val name: String = "",
    val prerequisite: String? = null,
    val description: String = ""
)

data class ToolDefinition(
    val name: String = "",
    val category: String = "",
    val cost: String = "",
    val weight: String = "",
    val description: String = ""
)

data class WeaponProficiency(
    val name: String = "",
    val type: String = "", 
    val description: String = ""
){
    val category: String get() = type
}

data class ArmorProficiency(
    val name: String = "",
    val type: String = "", 
    val description: String = ""
){
    val category: String get() = type
}