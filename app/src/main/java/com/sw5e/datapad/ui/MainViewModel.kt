package com.sw5e.datapad.ui

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.sw5e.datapad.data.*
import com.sw5e.datapad.ui.screens.Character
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.io.InputStreamReader
import java.util.UUID

enum class AdvantageMode { NORMAL, ADVANTAGE, DISADVANTAGE }

data class RollResult(val breakdownText: String)

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = AppDatabase.getInstance(application).characterDao()

    private val _character = MutableStateFlow(CharacterEntity())
    val character: StateFlow<CharacterEntity> = _character

    private val _compendiumSkills = MutableStateFlow<List<SkillDefinition>>(emptyList())
    val compendiumSkills: StateFlow<List<SkillDefinition>> = _compendiumSkills.asStateFlow()

    private val _lastRoll = MutableStateFlow<RollResult?>(null)
    val lastRoll: StateFlow<RollResult?> = _lastRoll

    private val _featsDictionary = MutableStateFlow<Map<String, FeatDefinition>>(emptyMap())
    val featsDictionary: StateFlow<Map<String, FeatDefinition>> = _featsDictionary

    private val _forcePowersDictionary = MutableStateFlow<Map<String, PowerDefinition>>(emptyMap())
    val forcePowersDictionary: StateFlow<Map<String, PowerDefinition>> = _forcePowersDictionary

    private val _techPowersDictionary = MutableStateFlow<Map<String, PowerDefinition>>(emptyMap())
    val techPowersDictionary: StateFlow<Map<String, PowerDefinition>> = _techPowersDictionary

    private val _toolsDictionary = MutableStateFlow<Map<String, ToolDefinition>>(emptyMap())
    val toolsDictionary: StateFlow<Map<String, ToolDefinition>> = _toolsDictionary

    private val _fightingStyles = MutableStateFlow<List<FightingStyleDefinition>>(emptyList())
    val fightingStyles: StateFlow<List<FightingStyleDefinition>> = _fightingStyles

    private val _fightingMasteries = MutableStateFlow<List<FightingMasteryDefinition>>(emptyList())
    val fightingMasteries: StateFlow<List<FightingMasteryDefinition>> = _fightingMasteries

    private val _lightsaberForms = MutableStateFlow<List<LightsaberFormDefinition>>(emptyList())
    val lightsaberForms: StateFlow<List<LightsaberFormDefinition>> = _lightsaberForms

    private val _armorDictionary = MutableStateFlow<Map<String, ArmorProficiency>>(emptyMap())
    val armorDictionary: StateFlow<Map<String, ArmorProficiency>> = _armorDictionary

    private val _weaponDictionary = MutableStateFlow<Map<String, WeaponProficiency>>(emptyMap())
    val weaponDictionary: StateFlow<Map<String, WeaponProficiency>> = _weaponDictionary

    private val _weaponPropertiesList = MutableStateFlow<List<WeaponPropertyDefinition>>(emptyList())
    val weaponPropertiesList: StateFlow<List<WeaponPropertyDefinition>> = _weaponPropertiesList

    private val _weaponPropertiesDict = MutableStateFlow<Map<String, WeaponPropertyDefinition>>(emptyMap())
    val weaponPropertiesDict: StateFlow<Map<String, WeaponPropertyDefinition>> = _weaponPropertiesDict

    private val _armorPropertiesList = MutableStateFlow<List<ArmorPropertyDefinition>>(emptyList())
    val armorPropertiesList: StateFlow<List<ArmorPropertyDefinition>> = _armorPropertiesList

    private val _armorPropertiesDict = MutableStateFlow<Map<String, ArmorPropertyDefinition>>(emptyMap())
    val armorPropertiesDict: StateFlow<Map<String, ArmorPropertyDefinition>> = _armorPropertiesDict

    private val _characters = MutableStateFlow<List<Character>>(emptyList())
    val characters: StateFlow<List<Character>> = _characters.asStateFlow()

    private val _selectedCharacter = MutableStateFlow<Character?>(null)
    val selectedCharacter: StateFlow<Character?> = _selectedCharacter.asStateFlow()

    init { 
        loadData()
        loadCompendium()
    }

    private fun loadData() {
        viewModelScope.launch(Dispatchers.IO) {
            val savedEntities = dao.getAllCharacters()
            val rosterList = savedEntities.map { entity ->
                Character(
                    id = entity.id,
                    name = entity.name,
                    level = entity.level,
                    species = entity.species,
                    className = entity.characterClass
                )
            }
            _characters.value = rosterList
        }
    }

    fun loadCompendium() {
        viewModelScope.launch(Dispatchers.IO) {
            val stream = getApplication<Application>().assets.open("compendium.json")
            val reader = InputStreamReader(stream)
            val response = Gson().fromJson(reader, CompendiumResponse::class.java)
            
            val compendiumSkills = response?.compendium?.skills ?: emptyList()
            val feats = response?.compendium?.feats ?: emptyList()
            val force = response?.compendium?.forcePowers ?: emptyList()
            val tech = response?.compendium?.techPowers ?: emptyList()
            val tools = response?.compendium?.tools ?: emptyList()
            val styles = response?.compendium?.fightingStyles ?: emptyList()     
            val masteries = response?.compendium?.fightingMasteries ?: emptyList()
            val forms = response?.compendium?.lightsaberForms ?: emptyList()
            val armorProfs = response?.compendium?.armor ?: emptyList()
            val weaponProfs = response?.compendium?.weapons ?: emptyList()
            val weaponProps = response?.compendium?.weaponProperties ?: emptyList()
            val armorProps = response?.compendium?.armorProperties ?: emptyList()

            _compendiumSkills.value = compendiumSkills
            _featsDictionary.value = feats.associateBy { it.name }
            _forcePowersDictionary.value = force.associateBy { it.name }
            _techPowersDictionary.value = tech.associateBy { it.name }
            _toolsDictionary.value = tools.associateBy { it.name }
            _fightingStyles.value = styles
            _fightingMasteries.value = masteries
            _lightsaberForms.value = forms
            _armorDictionary.value = armorProfs.associateBy { it.name }
            _weaponDictionary.value = weaponProfs.associateBy { it.name }
            _weaponPropertiesList.value = weaponProps
            _weaponPropertiesDict.value = weaponProps.associateBy { it.name.lowercase() }
            _armorPropertiesList.value = armorProps
            _armorPropertiesDict.value = armorProps.associateBy { it.name.lowercase() }

            reader.close()
        }
    }

    fun createNewCharacter(name: String, level: Int, species: String, className: String) {
        val newId = UUID.randomUUID().toString()
        val newChar = Character(
            id = newId,
            name = name,
            level = level,
            species = species,
            className = className
        )

        addCharacterToRoster(newChar)

        viewModelScope.launch(Dispatchers.IO) {
            val newEntity = CharacterEntity(
                id = newId,
                name = name,
                level = level,
                species = species,
                characterClass = className,
                skills = getDefaultSkillsFromCompendium()
            )
            dao.insertCharacter(newEntity)
        }
    }

    fun deleteCharacter(character: Character) {
        _characters.value = _characters.value.filter { it.id != character.id }
        
        if (_selectedCharacter.value?.id == character.id) {
            _selectedCharacter.value = null
            _character.value = CharacterEntity(name = "", species = "", characterClass = "")
        }

        viewModelScope.launch(Dispatchers.IO) {
            dao.deleteCharacterById(character.id)
        }
    }

    fun selectCharacter(character: Character) {
        _selectedCharacter.value = character
        
        viewModelScope.launch(Dispatchers.IO) {
            val existingCharacter = dao.getCharacterById(character.id)
            
            val fullCharacter = existingCharacter ?: CharacterEntity(
                id = character.id,
                name = character.name,
                species = character.species,
                characterClass = character.className,
                level = character.level,
                skills = getDefaultSkillsFromCompendium()
            )
            
            if (existingCharacter == null) {
                dao.insertCharacter(fullCharacter)
            }
            
            _character.value = fullCharacter
        }
    }

    fun addCharacterToRoster(character: Character) {
        val exists = _characters.value.any { it.id == character.id }
        if (!exists) {
            _characters.value = _characters.value + character
        }
    }

    fun updateCharacter(updated: CharacterEntity) {
        _character.value = updated
        
        _characters.value = _characters.value.map {
            if (it.id == updated.id) {
                it.copy(name = updated.name, species = updated.species, className = updated.characterClass, level = updated.level)
            } else it
        }

        viewModelScope.launch(Dispatchers.IO) {
            dao.insertCharacter(updated)
        }
    }

    private fun getDefaultSkillsFromCompendium(): List<CharacterSkillEntity> {
        return _compendiumSkills.value.map {
            CharacterSkillEntity(
                skillName = it.name,
                associatedAttribute = it.attribute
            )
        }
    }

    fun updateSkill(updatedSkill: CharacterSkillEntity) {
        val currentCharacter = _character.value
        val updatedSkills = currentCharacter.skills.map { 
            if (it.skillName == updatedSkill.skillName) updatedSkill else it 
        }
        updateCharacter(currentCharacter.copy(skills = updatedSkills))
    }
    
    fun updateClass(newClass: String) {
        val curr = _character.value
        updateCharacter(curr.copy(characterClass = newClass))
    }

    fun updateSpecies(newSpecies: String) {
        val curr = _character.value
        updateCharacter(curr.copy(species = newSpecies))
    }

    fun updateStat(statName: String, newValue: Int) {
        val curr = _character.value
        val updated = when (statName.uppercase()) {
            "STR" -> curr.copy(str = newValue)
            "DEX" -> curr.copy(dex = newValue)
            "CON" -> curr.copy(con = newValue)
            "INT" -> curr.copy(intStat = newValue)
            "WIS" -> curr.copy(wis = newValue)
            "CHA" -> curr.copy(cha = newValue)
            else -> curr
        }
        updateCharacter(updated)
    }

    fun addToolProficiency(tool: String) {
        val trimmed = tool.trim()
        if (trimmed.isBlank()) return
        val current = _character.value.toolProficiencies
        if (!current.contains(trimmed)) {
            updateCharacter(_character.value.copy(toolProficiencies = current + trimmed))
        }
    }

    fun removeToolProficiency(tool: String) {
        val current = _character.value.toolProficiencies
        updateCharacter(_character.value.copy(toolProficiencies = current - tool))
    }

    fun addArmorProficiency(armorName: String) {    
        val current = character.value.armorProficiencies
        if (!current.contains(armorName)) {
            updateCharacter(character.value.copy(armorProficiencies = current + armorName))
        }
    }

    fun removeArmorProficiency(armorName: String) {
        val current = character.value.armorProficiencies
        updateCharacter(character.value.copy(armorProficiencies = current - armorName))
    }

    fun addWeaponProficiency(weaponName: String) {
        val current = character.value.weaponProficiencies
        if (!current.contains(weaponName)) {
            updateCharacter(character.value.copy(weaponProficiencies = current + weaponName))
        }
    }

    fun removeWeaponProficiency(weaponName: String) {
        val current = character.value.weaponProficiencies
        updateCharacter(character.value.copy(weaponProficiencies = current - weaponName))
    }

    fun calculateArmorClass(): Int {
        val c = _character.value
        if (c.armorClassOverride > 0) return c.armorClassOverride

        val equippedArmor = c.equipment.firstOrNull { it.isEquipped && it.category == EquipmentCategory.ARMOR }
        val equippedShield = c.equipment.firstOrNull { it.isEquipped && it.category == EquipmentCategory.SHIELD }

        val dexMod = getStatModifierByName("DEX")
        
        val baseAc = equippedArmor?.baseAc ?: 10
        val cap = equippedArmor?.dexCap ?: 99
        val effectiveDex = minOf(dexMod, cap)
        val shieldBonus = equippedShield?.baseAc ?: 0

        return baseAc + effectiveDex + shieldBonus
    }

    fun getProficiencyBonus() = 1 + kotlin.math.ceil(_character.value.level / 4.0).toInt()

    fun getAttributeModifier(score: Int) = kotlin.math.floor((score - 10) / 2.0).toInt()

    fun getStatModifierByName(name: String): Int {
        val c = _character.value
        val score = when (name.uppercase()) {
            "STR" -> c.str; "DEX" -> c.dex; "CON" -> c.con
            "INT" -> c.intStat; "WIS" -> c.wis; "CHA" -> c.cha
            else -> 10
        }
        return getAttributeModifier(score)
    }
    
    fun getPassiveWisdom(isProficientInPerception: Boolean): Int {
        val wisMod = getAttributeModifier(_character.value.wis)
        val profBonus = if (isProficientInPerception) getProficiencyBonus() else 0
        return 10 + wisMod + profBonus
    }

    fun getTechSaveDC(): Int {
        val intMod = getAttributeModifier(_character.value.intStat)
        return 8 + getProficiencyBonus() + intMod
    }

    fun getForceSaveDC(useCharisma: Boolean): Int {
        val castingStat = if (useCharisma) _character.value.cha else _character.value.wis
        val castingMod = getAttributeModifier(castingStat)
        return 8 + getProficiencyBonus() + castingMod
    }

    fun applyHealthDelta(amount: Int) {
        val curr = _character.value
        var newTempHp = curr.tempHp
        var newHp = curr.currentHp

        if (amount < 0) {
            val damage = -amount
            if (newTempHp >= damage) {
                newTempHp -= damage
            } else {
                val remainingDamage = damage - newTempHp
                newTempHp = 0
                newHp = (newHp - remainingDamage).coerceAtLeast(0)
            }
        } else {
            newHp = (newHp + amount).coerceAtMost(curr.maxHp)
        }

        updateCharacter(curr.copy(currentHp = newHp, tempHp = newTempHp))
    }

    fun toggleFightingStyle(styleName: String) {
        val char = _character.value
        val updatedList = if (char.fightingStyles.contains(styleName)) {
            char.fightingStyles - styleName
        } else {
            char.fightingStyles + styleName
        }
        updateCharacter(char.copy(fightingStyles = updatedList))
    }

    fun toggleFightingMastery(masteryName: String) {
        val char = _character.value
        val updatedList = if (char.fightingMasteries.contains(masteryName)) {
            char.fightingMasteries - masteryName
        } else {
            char.fightingMasteries + masteryName
        }
        updateCharacter(char.copy(fightingMasteries = updatedList))
    }

    fun toggleLightsaberForm(formName: String) {
        val char = _character.value
        val updatedList = if (char.lightsaberForms.contains(formName)) {
            char.lightsaberForms - formName
        } else {
            char.lightsaberForms + formName
        }
        updateCharacter(char.copy(lightsaberForms = updatedList))
    }

    fun calculateMaxCharges(feature: CharacterFeature): Int {
        if (!feature.usesCharges) return 0
        
        val baseValue = when (feature.scalingType) {
            MaxChargesScaling.FIXED -> feature.fixedMaxCharges
            MaxChargesScaling.PROFICIENCY_BONUS -> getProficiencyBonus()
            MaxChargesScaling.STR_MOD -> getStatModifierByName("STR")
            MaxChargesScaling.DEX_MOD -> getStatModifierByName("DEX")
            MaxChargesScaling.CON_MOD -> getStatModifierByName("CON")
            MaxChargesScaling.INT_MOD -> getStatModifierByName("INT")
            MaxChargesScaling.WIS_MOD -> getStatModifierByName("WIS")
            MaxChargesScaling.CHA_MOD -> getStatModifierByName("CHA")
            MaxChargesScaling.CHARACTER_LEVEL -> _character.value.level
        }
        
        return maxOf(1, baseValue + feature.chargesBonusOffset)
    }

    fun spendFeatureCharge(featureId: String, delta: Int) {
        val current = _character.value
        val updatedFeatures = current.features.map { feat ->
            if (feat.id == featureId) {
                val maxCharges = calculateMaxCharges(feat)
                val newCharges = (feat.currentCharges + delta).coerceIn(0, maxCharges)
                feat.copy(currentCharges = newCharges)
            } else feat
        }
        updateCharacter(current.copy(features = updatedFeatures))
    }

    fun performRest(isLongRest: Boolean) {
        val current = _character.value
        val updatedFeatures = current.features.map { feat ->
            val shouldReset = when (feat.resetCondition) {
                ChargeResetCondition.SHORT_REST -> true
                ChargeResetCondition.LONG_REST -> isLongRest
                ChargeResetCondition.MANUAL -> false
            }
            if (shouldReset) {
                feat.copy(currentCharges = calculateMaxCharges(feat))
            } else feat
        }
        
        val updatedHp = if (isLongRest) current.maxHp else current.currentHp
        updateCharacter(current.copy(features = updatedFeatures, currentHp = updatedHp))
    }

    fun getProcessedWeapons(): List<ProcessedWeaponCombat> {
        val c = _character.value
        val profBonus = getProficiencyBonus()
        val propsDict = _weaponPropertiesDict.value

        return c.equipment.filter { it.isEquipped && it.category == EquipmentCategory.WEAPON }.map { weapon ->
            val props = weapon.properties.map { it.lowercase().trim() }
            val isRanged = weapon.type.contains("Blaster", ignoreCase = true) || "ranged" in props
            val defaultAbility = if (isRanged) "DEX" else "STR"

            // Inspect the hidden attackAbilityScore field of each property tag
            val candidateAbilities = mutableSetOf(defaultAbility)
            for (prop in props) {
                val propDef = propsDict[prop]
                val attr = propDef?.attackAbilityScore
                if (!attr.isNullOrBlank()) {
                    candidateAbilities.add(attr.uppercase())
                }
            }

            // Determine governing ability score using existing modifier comparison logic
            val (abilityMod, abilityName) = if (weapon.customAbilityOverride != null) {
                Pair(getStatModifierByName(weapon.customAbilityOverride), weapon.customAbilityOverride.uppercase())
            } else {
                var bestAbility = defaultAbility
                var maxMod = getStatModifierByName(defaultAbility)

                for (ability in candidateAbilities) {
                    val mod = getStatModifierByName(ability)
                    if (mod > maxMod) {
                        maxMod = mod
                        bestAbility = ability
                    } else if (mod == maxMod && ability == "DEX" && bestAbility == "STR") {
                        // Maintain tie-breaker preference for DEX over STR when modifiers match
                        bestAbility = "DEX"
                    }
                }
                Pair(maxMod, bestAbility)
            }

            val isProficient = c.weaponProficiencies.any { it.equals(weapon.type, ignoreCase = true) || it.equals(weapon.name, ignoreCase = true) }
            val weaponProfBonus = if (isProficient) profBonus else 0

            val totalAttackBonus = abilityMod + weaponProfBonus + weapon.attackBonus + c.attackSpecialBonus
            val totalDamageBonus = abilityMod + weapon.damageBonus + c.attackSpecialBonus

            val dmgModText = when {
                totalDamageBonus > 0 -> " + $totalDamageBonus"
                totalDamageBonus < 0 -> " - ${kotlin.math.abs(totalDamageBonus)}"
                else -> ""
            }

            val primaryDmg = "${weapon.primaryDamageDice}$dmgModText ${weapon.damageType}"
            val versatileDmg = weapon.secondaryDamageDice?.let { secondary ->
                "$secondary$dmgModText ${weapon.damageType}"
            }

            ProcessedWeaponCombat(
                weapon = weapon,
                attackBonus = totalAttackBonus,
                attackBonusBreakdown = "$abilityName (${if (abilityMod >= 0) "+$abilityMod" else "$abilityMod"}) + Prof (${if (weaponProfBonus >= 0) "+$weaponProfBonus" else "$weaponProfBonus"}) + Item (${if (weapon.attackBonus >= 0) "+${weapon.attackBonus}" else "${weapon.attackBonus}"})",
                primaryDamageText = primaryDmg,
                versatileDamageText = versatileDmg,
                chosenAbility = abilityName
            )
        }
    }

    fun process3DRollResults(rollValues: List<Int>, modifier: Int, advantageMode: AdvantageMode) {
        if (rollValues.isEmpty()) return

        var total = modifier
        if (advantageMode != AdvantageMode.NORMAL && rollValues.size >= 2) {
            val chosen = if (advantageMode == AdvantageMode.ADVANTAGE) rollValues.maxOrNull()!! else rollValues.minOrNull()!!
            total += chosen
        } else {
            total += rollValues.sum()
        }

        val rollsText = rollValues.joinToString(", ")
        val modText = when {
            modifier > 0 -> " + $modifier"
            modifier < 0 -> " - ${kotlin.math.abs(modifier)}"
            else -> ""
        }

        val breakdownText = "Total: $total | Rolls: [$rollsText]$modText"
        _lastRoll.value = RollResult(breakdownText = breakdownText)
    }

    fun executeRoll(dicePool: Map<Int, Int>, modifier: Int, advantageMode: AdvantageMode) {
        val adjustedPool = dicePool.toMutableMap()

        if (advantageMode != AdvantageMode.NORMAL) {
            adjustedPool.clear()
            adjustedPool[20] = 2
        } else if (adjustedPool.isEmpty()) {
            adjustedPool[20] = 1
        }

        var total = modifier
        val rollValues = mutableListOf<Int>()

        adjustedPool.forEach { (sides, count) ->
            if (count > 0) {
                val rolls = List(count) { (1..sides).random() }
                
                if (sides == 20 && advantageMode != AdvantageMode.NORMAL) {
                    val chosen = if (advantageMode == AdvantageMode.ADVANTAGE) rolls.maxOrNull()!! else rolls.minOrNull()!!
                    total += chosen
                    rollValues.addAll(rolls)
                } else {
                    val sum = rolls.sum()
                    total += sum
                    rollValues.addAll(rolls)
                }
            }
        }

        val rollsText = rollValues.joinToString(", ")
        val modText = when {
            modifier > 0 -> " + $modifier"
            modifier < 0 -> " - ${kotlin.math.abs(modifier)}"
            else -> ""
        }

        val breakdownText = "Total: $total | Rolls: [$rollsText]$modText"
        _lastRoll.value = RollResult(breakdownText = breakdownText)
    }

    fun exportToFile(context: Context, uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val c = _character.value
                val json = JSONObject().apply {
                    put("id", c.id)
                    put("name", c.name)
                    put("species", c.species)
                    put("class", c.characterClass)
                    put("characterClass", c.characterClass)
                    put("level", c.level)
                    put("speed", c.speed)
                    put("str", c.str); put("dex", c.dex); put("con", c.con)
                    put("intStat", c.intStat); put("wis", c.wis); put("cha", c.cha)
                    
                    put("saveProfStr", c.saveProfStr); put("saveProfDex", c.saveProfDex); put("saveProfCon", c.saveProfCon)
                    put("saveProfInt", c.saveProfInt); put("saveProfWis", c.saveProfWis); put("saveProfCha", c.saveProfCha)

                    put("currentHp", c.currentHp); put("maxHp", c.maxHp); put("tempHp", c.tempHp)
                    put("acOverride", c.armorClassOverride)
                    put("armorClassOverride", c.armorClassOverride)
                    put("armorBase", c.armorBase); put("dexCap", c.dexCap); put("shieldBonus", c.shieldBonus)
                    put("hitDieSpent", c.hitDieSpent)
                    put("hitDiceCount", c.hitDieMaximum)
                    put("hitDieMaximum", c.hitDieMaximum)
                    put("credits", c.credits)
                    put("techPoints", c.currentTechPoints)
                    put("currentTechPoints", c.currentTechPoints)
                    put("maxTechPoints", c.maxTechPoints)
                    put("forcePoints", c.currentForcePoints)
                    put("currentForcePoints", c.currentForcePoints)
                    put("maxForcePoints", c.maxForcePoints)
                    
                    put("resistances", JSONArray(c.resistances))
                    put("immunities", JSONArray(c.immunities))

                    val equipmentArray = JSONArray()
                    c.equipment.forEach { item ->
                        equipmentArray.put(JSONObject().apply {
                            put("name", item.name)
                            put("type", item.type)
                            put("isEquipped", item.isEquipped)
                            put("cr", item.cr)
                            put("weight", item.weight)
                            put("quantity", item.quantity)
                        })
                    }
                    put("equipment", equipmentArray)

                    put("toolProficiencies", JSONArray(c.toolProficiencies))
                    put("armorProficiencies", JSONArray(c.armorProficiencies))
                    put("weaponProficiencies", JSONArray(c.weaponProficiencies))
                    put("feats", JSONArray(c.feats))

                    val skillsArray = JSONArray()
                    c.skills.forEach { skill ->
                        skillsArray.put(JSONObject().apply {
                            put("skillName", skill.skillName)
                            put("associatedAttribute", skill.associatedAttribute)
                            put("proficiencyLevel", skill.proficiencyLevel)
                            put("manualOverride", skill.manualOverride)
                        })
                    }
                    put("skills", skillsArray)

                    val forceJson = JSONObject()
                    c.forcePowers.forEach { (k, v) -> forceJson.put(k.toString(), JSONArray(v)) }
                    put("forcePowers", forceJson)

                    val techJson = JSONObject()
                    c.techPowers.forEach { (k, v) -> techJson.put(k.toString(), JSONArray(v)) }
                    put("techPowers", techJson)

                    put("fightingStyles", JSONArray(c.fightingStyles))
                    put("fightingMasteries", JSONArray(c.fightingMasteries))
                    put("lightsaberForms", JSONArray(c.lightsaberForms))
                }

                context.contentResolver.openOutputStream(uri, "w")?.use { outputStream ->
                    outputStream.bufferedWriter(Charsets.UTF_8).use { writer ->
                        writer.write(json.toString(4))
                        writer.flush()
                    }
                }
            } catch (e: Exception) { 
                e.printStackTrace() 
            }
        }
    }

    fun importFromFile(context: Context, uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    val text = stream.bufferedReader().readText()
                    val obj = JSONObject(text)

                    fun parseStringList(key: String): List<String> {
                        val arr = obj.optJSONArray(key) ?: return emptyList()
                        return List(arr.length()) { arr.getString(it) }
                    }
                    
                    fun parseMap(key: String): Map<Int, List<String>> {
                        val mapObj = obj.optJSONObject(key) ?: return emptyMap()
                        val result = mutableMapOf<Int, List<String>>()
                        mapObj.keys().forEach { k ->
                            val level = k.toIntOrNull()
                            if (level != null) {
                                val arr = mapObj.optJSONArray(k)
                                if (arr != null) result[level] = List(arr.length()) { arr.getString(it) }
                            }
                        }
                        return result
                    }

                    fun parseSkillsList(key: String): List<CharacterSkillEntity> {
                        val arr = obj.optJSONArray(key) ?: return emptyList()
                        val list = mutableListOf<CharacterSkillEntity>()
                        for (i in 0 until arr.length()) {
                            val itemObj = arr.optJSONObject(i)
                            if (itemObj != null) {
                                list.add(
                                    CharacterSkillEntity(
                                        skillName = itemObj.optString("skillName", itemObj.optString("name", "")),
                                        associatedAttribute = itemObj.optString("associatedAttribute", itemObj.optString("attribute", "")),
                                        proficiencyLevel = itemObj.optInt("proficiencyLevel", 0),
                                        manualOverride = itemObj.optInt("manualOverride", 0)
                                    )
                                )
                            }
                        }
                        return list
                    }

                    fun parseEquipmentList(key: String): List<EquipmentItem> {
                        val arr = obj.optJSONArray(key) ?: return emptyList()
                        val list = mutableListOf<EquipmentItem>()
                        for (i in 0 until arr.length()) {
                            val itemObj = arr.optJSONObject(i)
                            if (itemObj != null) {
                                list.add(
                                    EquipmentItem(
                                        name = itemObj.optString("name", ""),
                                        type = itemObj.optString("type", "Adventuring Gear"),
                                        isEquipped = itemObj.optBoolean("isEquipped", false),
                                        cr = itemObj.optDouble("cr", 0.0),
                                        weight = itemObj.optDouble("weight", 0.0),
                                        quantity = itemObj.optInt("quantity", 1)
                                    )
                                )
                            } else {
                                val strVal = arr.optString(i, "")
                                if (strVal.isNotBlank()) {
                                    list.add(EquipmentItem(name = strVal))
                                }
                            }
                        }
                        return list
                    }

                    val updated = _character.value.copy(
                        id = obj.optString("id", _character.value.id.ifBlank { UUID.randomUUID().toString() }),
                        name = obj.optString("name", "Unknown"), 
                        species = obj.optString("species", "Human"), 
                        characterClass = obj.optString("characterClass", obj.optString("class", "Fighter")),
                        level = obj.optInt("level", 1), 
                        speed = obj.optInt("speed", 30),
                        str = obj.optInt("str", 10), dex = obj.optInt("dex", 10), con = obj.optInt("con", 10),
                        intStat = obj.optInt("intStat", obj.optInt("int", 10)), wis = obj.optInt("wis", 10), cha = obj.optInt("cha", 10),
                        
                        saveProfStr = obj.optBoolean("saveProfStr", false), saveProfDex = obj.optBoolean("saveProfDex", false), saveProfCon = obj.optBoolean("saveProfCon", false),
                        saveProfInt = obj.optBoolean("saveProfInt", false), saveProfWis = obj.optBoolean("saveProfWis", false), saveProfCha = obj.optBoolean("saveProfCha", false),
                        
                        currentHp = obj.optInt("currentHp", 10), maxHp = obj.optInt("maxHp", 10), tempHp = obj.optInt("tempHp", 0),
                        armorClassOverride = obj.optInt("armorClassOverride", obj.optInt("acOverride", 0)),
                        armorBase = obj.optInt("armorBase", 10), dexCap = obj.optInt("dexCap", 99), shieldBonus = obj.optInt("shieldBonus", 0),
                        hitDieSpent = obj.optInt("hitDieSpent", 0), hitDieMaximum = obj.optInt("hitDieMaximum", obj.optInt("hitDiceCount", 1)),
                        credits = obj.optInt("credits", 0),
                        currentTechPoints = obj.optInt("currentTechPoints", obj.optInt("techPoints", 0)), maxTechPoints = obj.optInt("maxTechPoints", 0),
                        currentForcePoints = obj.optInt("currentForcePoints", obj.optInt("forcePoints", 0)), maxForcePoints = obj.optInt("maxForcePoints", 0),

                        resistances = parseStringList("resistances"),
                        immunities = parseStringList("immunities"),
                        toolProficiencies = parseStringList("toolProficiencies"),
                        armorProficiencies = parseStringList("armorProficiencies"),
                        weaponProficiencies = parseStringList("weaponProficiencies"),
                        feats = parseStringList("feats"),
                        skills = parseSkillsList("skills"),

                        fightingStyles = parseStringList("fightingStyles"),
                        fightingMasteries = parseStringList("fightingMasteries"),
                        lightsaberForms = parseStringList("lightsaberForms"),

                        forcePowers = parseMap("forcePowers"),
                        techPowers = parseMap("techPowers"),

                        equipment = parseEquipmentList("equipment"),
                    )
                    
                    val rosterChar = Character(
                        id = updated.id,
                        name = updated.name,
                        level = updated.level,
                        species = updated.species,
                        className = updated.characterClass
                    )
                    
                    addCharacterToRoster(rosterChar)
                    _selectedCharacter.value = rosterChar
                    updateCharacter(updated)
                }
            } catch (e: Exception) { 
                e.printStackTrace() 
            }
        }
    }
}