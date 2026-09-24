package com.sw5e.datapad.data

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {
    private val gson = Gson()

    // --- Map Converters (For Force/Tech Powers) ---
    @TypeConverter
    fun fromIntListMap(value: Map<Int, List<String>>?): String = 
        gson.toJson(value ?: emptyMap<Int, List<String>>())

    @TypeConverter
    fun toIntListMap(value: String?): Map<Int, List<String>> {
        if (value.isNullOrEmpty()) return emptyMap()
        val type = object : TypeToken<Map<String, List<String>>>() {}.type
        val stringMap: Map<String, List<String>> = gson.fromJson(value, type) ?: emptyMap()
        return stringMap.mapKeys { it.key.toIntOrNull() ?: 0 }
    }

    // --- List<String> Converters (For Resistances, Immunities) ---
    @TypeConverter
    fun fromStringList(value: List<String>?): String = 
        gson.toJson(value ?: emptyList<String>())

    @TypeConverter
    fun toStringList(value: String?): List<String> {
        if (value.isNullOrEmpty()) return emptyList()
        val type = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(value, type) ?: emptyList()
    }

    // --- List<EquipmentItem> Converters ---
    @TypeConverter
    fun fromEquipmentList(value: List<EquipmentItem>?): String = 
        gson.toJson(value ?: emptyList<EquipmentItem>())

    @TypeConverter
    fun toEquipmentList(value: String?): List<EquipmentItem> {
        if (value.isNullOrEmpty()) return emptyList()
        val type = object : TypeToken<List<EquipmentItem>>() {}.type
        return gson.fromJson(value, type) ?: emptyList()
    }

    // --- List<ArmorProficiency> Converters ---
    @TypeConverter
    fun fromArmorProficiencyList(value: List<ArmorProficiency>?): String = 
        gson.toJson(value ?: emptyList<ArmorProficiency>())

    @TypeConverter
    fun toArmorProficiencyList(value: String?): List<ArmorProficiency> {
        if (value.isNullOrEmpty()) return emptyList()
        val type = object : TypeToken<List<ArmorProficiency>>() {}.type
        return gson.fromJson(value, type) ?: emptyList()
    }

    // --- List<CharacterSkillEntity> Converters ---
    @TypeConverter
    fun fromSkillList(value: List<CharacterSkillEntity>?): String = 
        gson.toJson(value ?: emptyList<CharacterSkillEntity>())

    @TypeConverter
    fun toSkillList(value: String?): List<CharacterSkillEntity> {
        if (value.isNullOrEmpty()) return emptyList()
        val type = object : TypeToken<List<CharacterSkillEntity>>() {}.type
        return gson.fromJson(value, type) ?: emptyList()
    }

    // --- List<CharacterFeature> Converters ---
    @TypeConverter
    fun fromFeatureList(value: List<CharacterFeature>?): String = 
        gson.toJson(value ?: emptyList<CharacterFeature>())

    @TypeConverter
    fun toFeatureList(value: String?): List<CharacterFeature> {
        if (value.isNullOrEmpty()) return emptyList()
        val type = object : TypeToken<List<CharacterFeature>>() {}.type
        return gson.fromJson(value, type) ?: emptyList()
    }
}