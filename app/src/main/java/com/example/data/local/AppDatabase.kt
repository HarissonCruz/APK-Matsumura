package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.Athlete
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [Athlete::class], version = 7, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun athleteDao(): AthleteDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "gym_buddy_database"
                )
                .fallbackToDestructiveMigration()
                .addCallback(AppDatabaseCallback(scope))
                .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class AppDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            scope.launch(Dispatchers.IO) {
                var database = INSTANCE
                for (i in 1..20) {
                    if (database != null) break
                    kotlinx.coroutines.delay(100)
                    database = INSTANCE
                }
                database?.let {
                    populateDatabase(it.athleteDao())
                }
            }
        }

        private suspend fun populateDatabase(athleteDao: AthleteDao) {
            // Initial mock data from HTML examples so the application is highly polished on boot!
            athleteDao.insertAthlete(
                Athlete(
                    name = "Alex Silva",
                    beltRank = "Purple Belt",
                    beltProgress = 72,
                    academyName = "Gracie Barra Pinheiros",
                    phone = "(11) 98765-4321",
                    email = "alex.silva@gymbuddy.com",
                    imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuDasxsPPVeXcDBGqca1z5F4MuTTxR0ajSo0hTvcPNDguwIHJuQKzuxYdxXZ6ts-N7v_OcHIL1BkmZs75QFXSvA1Lmb7EN2a8KNmxMfZqwjnikCXTaK1i-TDIP1v1BHt3qISj-hquNax9W6C6tDanqej9wPTKjK0BdynWkp7Pf96rr97LEPTb1_kHpmeb-_A6jq8Rx4IrBdcGV_SeMSchrJ9MnwsCprVGECQEtZx9Zh6wM_bteTeIOVvFiExxiw-_9l2mmgmQO_6Puo",
                    isActive = true,
                    trainingHours = 128,
                    streakDays = 14,
                    nextGraduation = "Brown Belt",
                    nextGraduationProgress = 82,
                    notes = "Atleta de alto rendimento. Treinando focado para o próximo campeonato estadual.",
                    birthDate = "15/04/1998",
                    age = 28,
                    category = "Adulto - Peso Leve"
                )
            )
            athleteDao.insertAthlete(
                Athlete(
                    name = "Marcus Vinícius",
                    beltRank = "Black Belt",
                    beltProgress = 100,
                    academyName = "Gracie Barra Pinheiros",
                    phone = "(11) 99999-1111",
                    email = "marcus.v@graciebarra.com",
                    imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuCDglZsDi820sXZ3zD7FAyzMkTEF5NIdYX-HHbXyUQIBpYzyCzWUJ76Y5USACq41TgD2_KAAdCz_1vHKRIsEec54Perq0okLijN-uik4wEAy3MB-AvgUZBefnohYAPE-Y3hlicRiKEH4MWLXdKlCN-53uf-bTz3Phugzb4rf0HcVxtZaM-k3XPa2_6bphIx2h0BzYV6ZPQ5PlHRhWCAghn44ocpYXS9rK-1W1yhXtWa-DY4xNNoneVxFd_e0Jy46ZrwSxNu_frqiIk",
                    isActive = true,
                    trainingHours = 450,
                    streakDays = 25,
                    nextGraduation = "1º Grau",
                    nextGraduationProgress = 15,
                    notes = "Professor titular e mentor técnico dos atletas competidores.",
                    birthDate = "20/11/1986",
                    age = 39,
                    category = "Master 2 - Peso Pesado"
                )
            )
            athleteDao.insertAthlete(
                Athlete(
                    name = "Sarah Chen",
                    beltRank = "Blue Belt",
                    beltProgress = 50,
                    academyName = "Siam Muay Thai",
                    phone = "(11) 98888-2222",
                    email = "sarah.chen@siam.com",
                    imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuDIYIZxG6S33OFK_Zt9m_pEbuO_r1uoTjonF6wodt3lg7ZdNu1TaxO9HC7hNltDz286_H0AgXFkOFYh6BIbJHWFpidch0ry6IxLI4uzJcfHZNQVp5ChcD0KpB9JKd73fBZAGx0SSf-0F0Qupga_FlPVaPnybbR4jEFhpypHXkTCzzBL6PHDJUmvSA0b8MgryGCzKNoW5ZpFoam0LTTzCYY3QSKwEctWFuiPclE-WhtYNuVu1hEKeiggq2kHlvFZdGVWLM4EfBEQKIg",
                    isActive = true,
                    trainingHours = 96,
                    streakDays = 8,
                    nextGraduation = "Purple Belt",
                    nextGraduationProgress = 40,
                    notes = "Excelente batedora de manopla com postura e chutes de grande potência.",
                    birthDate = "05/09/2001",
                    age = 24,
                    category = "Adulto - Peso Pena"
                )
            )
            athleteDao.insertAthlete(
                Athlete(
                    name = "Elena Ross",
                    beltRank = "Purple Belt",
                    beltProgress = 15,
                    academyName = "Lotus Yoga & Fitness",
                    phone = "(11) 97777-3333",
                    email = "elena.ross@lotus.com",
                    imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuA5yFZ1p10F3jDpIuQ9FxsHvjhM5kW68HeRk8paE9d5dAGCNlyWSc1vDeQiSN48QT3QiSL9wIzZ9a-e2zgH9Yzqr0-5iVycO0N-BcWO74ACXc0ABF5N2utApFIotUX9gJuEilf7h1euwLYkVI6sAjVc8hpE_cKJXHXyTJZT1S7b31g9juqhddQvLHA8KeGqxNkS22TZDZKL0k58aBd7tmgK-Jd7Wz3okk4Ph9edOMYINzQ_WnIgQPeBRB2G97kNEdMRBazpht4L5QQ",
                    isActive = true,
                    trainingHours = 210,
                    streakDays = 3,
                    nextGraduation = "Brown Belt",
                    nextGraduationProgress = 12,
                    notes = "Atleta de Yoga focado em reabilitação postural e controle respiratório.",
                    birthDate = "12/07/1992",
                    age = 33,
                    category = "Adulto - Peso Médio"
                )
            )
        }
    }
}
