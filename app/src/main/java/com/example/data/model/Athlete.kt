package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "athletes")
data class Athlete(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val beltRank: String,          // e.g. "White Belt", "Blue Belt", "Purple Belt", "Brown Belt", "Black Belt"
    val beltProgress: Int = 0,     // Percentage 0-100
    val academyName: String,       // Academy name
    val phone: String = "",
    val email: String = "",
    val imageUrl: String = "",     // Direct image URL
    val isActive: Boolean = true,
    val trainingHours: Int = 0,
    val streakDays: Int = 0,
    val nextGraduation: String = "",
    val nextGraduationProgress: Int = 0, // Percentage 0-100
    val notes: String = "",
    val registrationDate: Long = System.currentTimeMillis(),
    val paymentStatus: String = "Pago",          // "Pago", "Pendente", "Atrasado"
    val lastPaymentDate: String = "15/06/2026",
    val paymentValue: Double = 189.00,
    val attendanceHistory: String = "",            // Comma-separated dates like "25/06,27/06"
    val cpf: String = "",
    val birthDate: String = "",
    val age: Int = 0,
    val category: String = "",
    val nickname: String = "",
    val weight: Double = 0.0,
    val height: Double = 0.0,
    val emergencyContactName1: String = "",
    val emergencyContactPhone1: String = "",
    val emergencyContactName2: String = "",
    val emergencyContactPhone2: String = "",
    val contractSigned: Boolean = false,
    val contractSignatureDate: String = "",
    val contractSignatureName: String = "",
    val password: String = "",
    val passwordChanged: Boolean = false,
    val bookedClasses: String = ""
)
