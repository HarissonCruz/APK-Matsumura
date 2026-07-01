package com.example.data.repository

import com.example.data.local.AthleteDao
import com.example.data.model.Athlete
import kotlinx.coroutines.flow.Flow

class AthleteRepository(private val athleteDao: AthleteDao) {
    val allAthletes: Flow<List<Athlete>> = athleteDao.getAllAthletes()

    fun getAthleteById(id: Int): Flow<Athlete?> = athleteDao.getAthleteById(id)

    suspend fun insert(athlete: Athlete): Long = athleteDao.insertAthlete(athlete)

    suspend fun update(athlete: Athlete) = athleteDao.updateAthlete(athlete)

    suspend fun delete(athlete: Athlete) = athleteDao.deleteAthlete(athlete)
}
