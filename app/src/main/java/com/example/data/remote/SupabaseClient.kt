package com.example.data.remote

import android.util.Log
import com.example.BuildConfig
import com.example.data.model.Athlete
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.*
import java.util.concurrent.TimeUnit

interface SupabaseApi {
    @POST("auth/v1/signup")
    suspend fun signUp(
        @Body request: SignUpRequest
    ): AuthResponse

    @POST("auth/v1/token?grant_type=password")
    suspend fun signIn(
        @Body request: SignInRequest
    ): AuthResponse

    @GET("rest/v1/athletes")
    suspend fun testConnection(
        @Query("limit") limit: Int = 1,
        @Query("select") select: String = "id"
    ): List<Map<String, Any>>

    @GET("rest/v1/athletes")
    suspend fun getAthletes(
        @Query("select") select: String = "*"
    ): List<AthleteDto>

    @POST("rest/v1/athletes")
    suspend fun insertAthlete(
        @Body athlete: AthleteDto,
        @Header("Prefer") prefer: String = "return=representation"
    ): List<AthleteDto>

    @PATCH("rest/v1/athletes")
    suspend fun updateAthlete(
        @Query("id") idFilter: String, // e.g. "eq.1"
        @Body athlete: AthleteDto
    ): List<AthleteDto>

    @DELETE("rest/v1/athletes")
    suspend fun deleteAthlete(
        @Query("id") idFilter: String // e.g. "eq.1"
    )

    @PUT("auth/v1/user")
    suspend fun updateUser(
        @Body request: UpdateUserRequest
    ): AuthUser
}

data class UpdateUserRequest(
    val password: String
)

// Data models for Supabase Authentication
data class SignUpRequest(
    val email: String,
    val password: String,
    val data: Map<String, String>? = null
)

data class SignInRequest(
    val email: String,
    val password: String
)

data class AuthUser(
    val id: String,
    val email: String?,
    val user_metadata: Map<String, Any>? = null
)

data class AuthResponse(
    val access_token: String? = null,
    val token_type: String? = null,
    val expires_in: Int? = null,
    val user: AuthUser? = null
)

// Data Transfer Object for Supabase serialization to ensure full field match
data class AthleteDto(
    val id: Int? = null,
    val name: String,
    val beltRank: String,
    val beltProgress: Int = 0,
    val academyName: String,
    val phone: String = "",
    val email: String = "",
    val imageUrl: String = "",
    val isActive: Boolean = true,
    val trainingHours: Int = 0,
    val streakDays: Int = 0,
    val nextGraduation: String = "",
    val nextGraduationProgress: Int = 0,
    val notes: String = "",
    val registrationDate: Long = System.currentTimeMillis(),
    val paymentStatus: String = "Pago",
    val lastPaymentDate: String = "15/06/2026",
    val paymentValue: Double = 189.0,
    val attendanceHistory: String = "",
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
) {
    fun toDomain(): Athlete {
        return Athlete(
            id = id ?: 0,
            name = name,
            beltRank = beltRank,
            beltProgress = beltProgress,
            academyName = academyName,
            phone = phone,
            email = email,
            imageUrl = imageUrl,
            isActive = isActive,
            trainingHours = trainingHours,
            streakDays = streakDays,
            nextGraduation = nextGraduation,
            nextGraduationProgress = nextGraduationProgress,
            notes = notes,
            registrationDate = registrationDate,
            paymentStatus = paymentStatus,
            lastPaymentDate = lastPaymentDate,
            paymentValue = paymentValue,
            attendanceHistory = attendanceHistory,
            cpf = cpf,
            birthDate = birthDate,
            age = age,
            category = category,
            nickname = nickname,
            weight = weight,
            height = height,
            emergencyContactName1 = emergencyContactName1,
            emergencyContactPhone1 = emergencyContactPhone1,
            emergencyContactName2 = emergencyContactName2,
            emergencyContactPhone2 = emergencyContactPhone2,
            contractSigned = contractSigned,
            contractSignatureDate = contractSignatureDate,
            contractSignatureName = contractSignatureName,
            password = password,
            passwordChanged = passwordChanged,
            bookedClasses = bookedClasses
        )
    }

    companion object {
        fun fromDomain(athlete: Athlete): AthleteDto {
            return AthleteDto(
                id = if (athlete.id == 0) null else athlete.id,
                name = athlete.name,
                beltRank = athlete.beltRank,
                beltProgress = athlete.beltProgress,
                academyName = athlete.academyName,
                phone = athlete.phone,
                email = athlete.email,
                imageUrl = athlete.imageUrl,
                isActive = athlete.isActive,
                trainingHours = athlete.trainingHours,
                streakDays = athlete.streakDays,
                nextGraduation = athlete.nextGraduation,
                nextGraduationProgress = athlete.nextGraduationProgress,
                notes = athlete.notes,
                registrationDate = athlete.registrationDate,
                paymentStatus = athlete.paymentStatus,
                lastPaymentDate = athlete.lastPaymentDate,
                paymentValue = athlete.paymentValue,
                attendanceHistory = athlete.attendanceHistory,
                cpf = athlete.cpf,
                birthDate = athlete.birthDate,
                age = athlete.age,
                category = athlete.category,
                nickname = athlete.nickname,
                weight = athlete.weight,
                height = athlete.height,
                emergencyContactName1 = athlete.emergencyContactName1,
                emergencyContactPhone1 = athlete.emergencyContactPhone1,
                emergencyContactName2 = athlete.emergencyContactName2,
                emergencyContactPhone2 = athlete.emergencyContactPhone2,
                contractSigned = athlete.contractSigned,
                contractSignatureDate = athlete.contractSignatureDate,
                contractSignatureName = athlete.contractSignatureName,
                password = athlete.password,
                passwordChanged = athlete.passwordChanged,
                bookedClasses = athlete.bookedClasses
            )
        }
    }
}

object SupabaseClient {
    private const val TAG = "SupabaseClient"

    // Authenticated user JWT token for secure Row Level Security (RLS) policies
    var sessionToken: String? = null

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    var customUrl: String? = null
    var customAnonKey: String? = null

    fun getUrl(): String = customUrl ?: BuildConfig.SUPABASE_URL
    fun getAnonKey(): String = customAnonKey ?: BuildConfig.SUPABASE_ANON_KEY

    private val authInterceptor = Interceptor { chain ->
        val original = chain.request()
        
        val supabaseAnonKey = getAnonKey()

        // Use custom sessionToken if authenticated, otherwise fallback to anon key
        val token = sessionToken ?: supabaseAnonKey

        val requestBuilder = original.newBuilder()
            .header("apikey", supabaseAnonKey)
            .header("Authorization", "Bearer $token")
            .header("Content-Type", "application/json")
        
        val request = requestBuilder.build()
        chain.proceed(request)
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.HEADERS
        })
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    private var _api: SupabaseApi? = null

    val api: SupabaseApi
        get() {
            val currentApi = _api
            if (currentApi != null) return currentApi
            
            val newApi = createApi()
            _api = newApi
            return newApi
        }

    private fun createApi(): SupabaseApi {
        var baseUrl = getUrl()
        if (!baseUrl.endsWith("/")) {
            baseUrl += "/"
        }
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(SupabaseApi::class.java)
    }

    fun updateConfig(url: String?, anonKey: String?) {
        customUrl = if (url.isNullOrBlank()) null else url.trim()
        customAnonKey = if (anonKey.isNullOrBlank()) null else anonKey.trim()
        _api = null
    }
}
