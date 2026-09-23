package com.sw5e.datapad.data

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CharacterDao {
    @Query("SELECT * FROM character_sheet WHERE id = :characterId LIMIT 1")
    suspend fun getCharacterById(characterId: String): CharacterEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCharacter(character: CharacterEntity)
}

@Database(entities = [CharacterEntity::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class) 
abstract class AppDatabase : RoomDatabase() {
    abstract fun characterDao(): CharacterDao

    companion object {
        @Volatile private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "sw5e_db"
                ).fallbackToDestructiveMigration().build().also { instance = it }
            }
        }
    }
}