package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Athlete
import kotlinx.coroutines.flow.Flow

@Dao
interface AthleteDao {
    @Query("SELECT * FROM athletes ORDER BY name ASC")
    fun getAllAthletes(): Flow<List<Athlete>>

    @Query("SELECT * FROM athletes WHERE id = :id")
    fun getAthleteById(id: Int): Flow<Athlete?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAthlete(athlete: Athlete): Long

    @Update
    suspend fun updateAthlete(athlete: Athlete)

    @Delete
    suspend fun deleteAthlete(athlete: Athlete)
}
