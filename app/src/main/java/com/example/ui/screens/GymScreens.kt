package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Athlete
import com.example.ui.components.AthleteAvatar
import com.example.ui.viewmodel.GymViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import coil.compose.AsyncImage
import coil.request.ImageRequest
import java.util.Calendar
import java.util.Locale
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.ui.text.font.FontFamily
import java.text.SimpleDateFormat
import java.util.Date

// Helper for belt styling colors inside the Gym
fun getBeltColor(beltRank: String): Color {
    val clean = beltRank.lowercase()
    return when {
        clean.contains("white") || clean.contains("branca") -> Color(0xFFEFEFEF)
        clean.contains("blue") || clean.contains("azul") -> Color(0xFF0055D4)
        clean.contains("purple") || clean.contains("roxa") -> Color(0xFF7C52AA)
        clean.contains("brown") || clean.contains("marrom") -> Color(0xFF7E4A10)
        clean.contains("black") || clean.contains("preta") -> Color(0xFF1E1E1E)
        else -> Color(0xFFE040A0) // primary fallback
    }
}

fun getBeltTextColor(beltRank: String): Color {
    val clean = beltRank.lowercase()
    return when {
        clean.contains("white") || clean.contains("branca") -> Color(0xFF2E1A28)
        else -> Color.White
    }
}

// ==========================================
// 1. HOME SCREEN (DASHBOARD)
// ==========================================
@Composable
fun HomeScreen(
    viewModel: GymViewModel,
    athletes: List<Athlete>,
    onNavigateToAthletes: () -> Unit
) {
    val lang by viewModel.selectedLanguage.collectAsStateWithLifecycle()
    val currentEmail by viewModel.userEmail.collectAsStateWithLifecycle()

    val context = androidx.compose.ui.platform.LocalContext.current
    val bookingError by viewModel.bookingError.collectAsStateWithLifecycle()
    LaunchedEffect(bookingError) {
        bookingError?.let { error ->
            android.widget.Toast.makeText(context, error, android.widget.Toast.LENGTH_LONG).show()
            viewModel.clearBookingError()
        }
    }

    val primaryAthlete = remember(athletes, currentEmail) {
        athletes.find { it.email.trim().lowercase() == currentEmail.trim().lowercase() }
            ?: athletes.firstOrNull()
            ?: Athlete(
                name = "Alex Silva",
                beltRank = "Faixa Roxa",
                beltProgress = 72,
                academyName = "Gracie Barra Pinheiros",
                trainingHours = 128,
                streakDays = 14,
                nextGraduation = "Faixa Marrom",
                nextGraduationProgress = 82,
                paymentStatus = "Pago",
                lastPaymentDate = "15/06/2026",
                paymentValue = 189.00
            )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Hero Welcome Section
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                val displayName = if (primaryAthlete.nickname.isNotBlank()) primaryAthlete.nickname else (primaryAthlete.name.split(" ").firstOrNull() ?: "Alex")
                Text(
                    text = if (lang == "pt") "Bem-vindo de volta, $displayName! 👋" else "Welcome back, $displayName! 👋",
                    style = MaterialTheme.typography.displayLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 28.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (lang == "pt") {
                        "Faltam apenas ${50 - (primaryAthlete.trainingHours % 50)} aulas para a sua ${TranslationUtils.getLocalizedBelt(primaryAthlete.nextGraduation, lang)}."
                    } else {
                        "Only ${50 - (primaryAthlete.trainingHours % 50)} classes left for your ${TranslationUtils.getLocalizedBelt(primaryAthlete.nextGraduation, lang)}."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )
            }
            AthleteAvatar(
                imageUrl = primaryAthlete.imageUrl,
                name = primaryAthlete.name,
                modifier = Modifier.size(56.dp)
            )
        }

        // Plan Expiration Reminder Banner
        val calendar = remember { Calendar.getInstance() }
        val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
        val dueDay = remember(primaryAthlete.lastPaymentDate) {
            try {
                primaryAthlete.lastPaymentDate.split("/").firstOrNull()?.toInt() ?: 15
            } catch (e: Exception) {
                15
            }
        }
        val isPendingOrOverdue = primaryAthlete.paymentStatus != "Pago"
        val isNearDueDay = (dueDay - dayOfMonth) in 0..5 || isPendingOrOverdue

        if (isNearDueDay) {
            val bannerColor = if (isPendingOrOverdue) Color(0xFFFFEBEE) else Color(0xFFFFF9C4)
            val textColor = if (isPendingOrOverdue) Color(0xFFC62828) else Color(0xFFF57F17)
            val borderColor = if (isPendingOrOverdue) Color(0xFFFFCDD2) else Color(0xFFFFF59D)
            val titleText = if (isPendingOrOverdue) {
                if (lang == "pt") "Mensalidade Pendente!" else "Pending Payment!"
            } else {
                if (lang == "pt") "Aviso de Vencimento Próximo" else "Due Date Warning"
            }
            val bodyText = if (isPendingOrOverdue) {
                if (lang == "pt") {
                    "Sua mensalidade de R$ ${String.format(Locale.US, "%.2f", primaryAthlete.paymentValue)} está com o status ${primaryAthlete.paymentStatus}. Realize o pagamento para garantir seu acesso."
                } else {
                    "Your monthly payment of $ ${String.format(Locale.US, "%.2f", primaryAthlete.paymentValue)} is currently ${primaryAthlete.paymentStatus}. Please make your payment to guarantee access."
                }
            } else {
                if (lang == "pt") {
                    "Sua mensalidade vence em breve (dia $dueDay). Não se esqueça de renovar a tempo!"
                } else {
                    "Your monthly payment is due soon (day $dueDay). Don't forget to renew on time!"
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth().testTag("expiry_reminder_banner"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = bannerColor),
                border = BorderStroke(1.dp, borderColor)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(textColor.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isPendingOrOverdue) Icons.Default.Warning else Icons.Default.Info,
                            contentDescription = null,
                            tint = textColor
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = titleText,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = textColor
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = bodyText,
                            fontSize = 12.sp,
                            color = textColor.copy(alpha = 0.9f),
                            lineHeight = 16.sp
                        )
                    }

                    Button(
                        onClick = { viewModel.setTab("Wallet") },
                        colors = ButtonDefaults.buttonColors(containerColor = textColor),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Text(if (lang == "pt") "Ver Plano" else "View Plan", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }

        // Quick Stats row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Streak Card
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.secondary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Streak",
                            tint = Color.White
                        )
                    }
                    Column {
                        Text(
                            text = TranslationUtils.t("daily_streak", lang).uppercase(),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                        )
                        Text(
                            text = "${primaryAthlete.streakDays} " + (if (lang == "pt") "Dias" else "Days"),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }

            // Training hours Card
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.tertiary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = "Training",
                            tint = Color.White
                        )
                    }
                    Column {
                        Text(
                            text = if (lang == "pt") "TREINO" else "TRAINING",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f)
                        )
                        Text(
                            text = "${primaryAthlete.trainingHours} hrs",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                }
            }
        }

        // Graduation Progress Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column {
                        Box(
                            modifier = Modifier
                                .background(
                                    MaterialTheme.colorScheme.primary,
                                    RoundedCornerShape(12.dp)
                                )
                                .padding(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = TranslationUtils.t("next_graduation", lang).uppercase(),
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = TranslationUtils.getLocalizedBelt(primaryAthlete.nextGraduation, lang),
                            style = MaterialTheme.typography.displayMedium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .border(3.dp, MaterialTheme.colorScheme.secondary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Medal",
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (lang == "pt") "Progresso para a Faixa" else "Progress to Next Belt",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${primaryAthlete.nextGraduationProgress}%",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Black
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                LinearProgressIndicator(
                    progress = { primaryAthlete.nextGraduationProgress / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                        .clip(RoundedCornerShape(6.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.outlineVariant
                )
            }
        }

        // 6-Month Graduation Cycle Progress Card
        val daysInCycle = 180
        val daysElapsed = remember(primaryAthlete.registrationDate) {
            val diffMs = System.currentTimeMillis() - primaryAthlete.registrationDate
            val diffDays = (diffMs / (1000 * 60 * 60 * 24)).toInt()
            if (diffDays < 10) {
                100 + (primaryAthlete.id * 15) % 70
            } else {
                diffDays % daysInCycle
            }
        }
        val daysRemaining = daysInCycle - daysElapsed
        val cycleProgressPercent = ((daysElapsed.toFloat() / daysInCycle) * 100).toInt()
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.2f)),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.25f))
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Timeline,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary
                        )
                        Text(
                            text = if (lang == "pt") "CICLO DE GRADUAÇÃO (6 MESES)" else "GRADUATION CYCLE (6 MONTHS)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.secondary,
                            letterSpacing = 0.5.sp
                        )
                    }
                    Box(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.secondary, RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (lang == "pt") "Semestral" else "Bi-annual",
                            color = Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(14.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        Text(
                            text = if (lang == "pt") "$daysElapsed / $daysInCycle dias" else "$daysElapsed / $daysInCycle days",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = if (lang == "pt") "Tempo decorrido no ciclo atual" else "Time elapsed in current cycle",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    Text(
                        text = "$cycleProgressPercent%",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
                
                Spacer(modifier = Modifier.height(10.dp))
                
                LinearProgressIndicator(
                    progress = { daysElapsed.toFloat() / daysInCycle },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = MaterialTheme.colorScheme.secondary,
                    trackColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)),
                        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.FitnessCenter, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${primaryAthlete.trainingHours}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = if (lang == "pt") "Aulas Feitas" else "Classes Attended",
                                fontSize = 9.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)),
                        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.HourglassEmpty, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.secondary)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "$daysRemaining",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = if (lang == "pt") "Dias Restantes" else "Days Left",
                                fontSize = 9.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Booked Class Info / Dynamic Training Scheduler (Interactive!)
        val trainingSessions by viewModel.trainingSessions.collectAsState()

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "Classes",
                        tint = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text = "GRADE DE TREINOS DIÁRIOS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Agendamento de Treinos",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    fontSize = 20.sp
                )
                Text(
                    text = "Selecione o melhor horário para treinar hoje. Limite de 15 a 20 alunos por treino para manter a qualidade.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    trainingSessions.forEach { session ->
                        val isBooked = session.isBookedByMe
                        val isFull = session.isFull

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isBooked) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                            ),
                            border = BorderStroke(
                                width = 1.dp,
                                color = if (isBooked) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                            )
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Left: Time and class category
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        modifier = Modifier.weight(1.2f)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(44.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(
                                                    if (isBooked) MaterialTheme.colorScheme.primary
                                                    else if (isFull) MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
                                                    else MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = session.time,
                                                fontWeight = FontWeight.Black,
                                                fontSize = 13.sp,
                                                color = if (isBooked) Color.White
                                                        else if (isFull) MaterialTheme.colorScheme.error
                                                        else MaterialTheme.colorScheme.secondary
                                            )
                                        }

                                        Column {
                                            Text(
                                                text = session.category,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = session.coach,
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }

                                    // Right: Capacity, full badge & action button
                                    Column(
                                        horizontalAlignment = Alignment.End,
                                        verticalArrangement = Arrangement.spacedBy(4.dp),
                                        modifier = Modifier.weight(0.8f)
                                    ) {
                                        // Capacity text with colored state
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Person,
                                                contentDescription = null,
                                                tint = if (isFull) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(12.dp)
                                            )
                                            Text(
                                                text = "${session.totalAttendeesCount}/${session.maxCapacity}",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isFull) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }

                                        if (isFull && !isBooked) {
                                            // LOTADO Badge Button
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .background(MaterialTheme.colorScheme.error.copy(alpha = 0.15f))
                                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                                            ) {
                                                Text(
                                                    text = "LOTADO",
                                                    color = MaterialTheme.colorScheme.error,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Black
                                                )
                                            }
                                        } else {
                                            // Booking Active Button
                                            Button(
                                                onClick = { viewModel.toggleSessionBooking(session.id) },
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = if (isBooked) MaterialTheme.colorScheme.primary
                                                                    else MaterialTheme.colorScheme.secondary
                                                ),
                                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                                shape = RoundedCornerShape(12.dp),
                                                modifier = Modifier.height(32.dp)
                                            ) {
                                                Text(
                                                    text = if (isBooked) "Agendado ✓" else "Agendar",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // Small progress indicator for capacity
                                LinearProgressIndicator(
                                    progress = { session.totalAttendeesCount.toFloat() / session.maxCapacity },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(4.dp)
                                        .clip(RoundedCornerShape(2.dp)),
                                    color = if (isFull) MaterialTheme.colorScheme.error
                                            else if (isBooked) MaterialTheme.colorScheme.primary
                                            else MaterialTheme.colorScheme.secondary,
                                    trackColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                                )
                            }
                        }
                    }
                }
            }
        }

        // TODAY'S CLASS PLAN / ESQUEMA DE AULA (Dynamic!)
        val context = androidx.compose.ui.platform.LocalContext.current
        val sharedPrefs = remember(context) { context.getSharedPreferences("gym_class_plans", android.content.Context.MODE_PRIVATE) }
        val todayDateStr = remember { SimpleDateFormat("dd/06", Locale.getDefault()).format(Date()) }
        val timesList = listOf("08:00", "12:00", "18:00", "19:30", "21:00")
        var selectedTimeForPlan by remember { mutableStateOf("19:30") }

        val planTema = remember(selectedTimeForPlan) { sharedPrefs.getString("tema_${todayDateStr}_$selectedTimeForPlan", "") ?: "" }
        val planAquecimento = remember(selectedTimeForPlan) { sharedPrefs.getString("aquecimento_${todayDateStr}_$selectedTimeForPlan", "") ?: "" }
        val planTecnica = remember(selectedTimeForPlan) { sharedPrefs.getString("tecnica_${todayDateStr}_$selectedTimeForPlan", "") ?: "" }
        val planRola = remember(selectedTimeForPlan) { sharedPrefs.getString("rola_${todayDateStr}_$selectedTimeForPlan", "") ?: "" }

        val hasPlan = planTema.isNotBlank() || planAquecimento.isNotBlank() || planTecnica.isNotBlank() || planRola.isNotBlank()

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "🥋 " + (if (lang == "pt") "TREINO DE HOJE" else "TODAY'S TRAINING"),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Badge(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.primary
                    ) {
                        Text(todayDateStr, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }

                Text(
                    text = if (lang == "pt") "Esquema de Aula do Dia" else "Daily Class Schedule",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    fontSize = 20.sp
                )

                // Time selector row
                androidx.compose.foundation.lazy.LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(timesList) { time ->
                        val isSelected = selectedTimeForPlan == time
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                .border(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                .clickable { selectedTimeForPlan = time }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = time,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                if (hasPlan) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        if (planTema.isNotBlank()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Book,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Column {
                                    Text(if (lang == "pt") "Tema Principal / Técnica" else "Main Theme / Technique", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(planTema, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                                }
                            }
                        }

                        if (planAquecimento.isNotBlank()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.secondaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Timer,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Column {
                                    Text(if (lang == "pt") "Aquecimento" else "Warmup", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(planAquecimento, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                                }
                            }
                        }

                        if (planTecnica.isNotBlank()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.tertiaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.List,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.tertiary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Column {
                                    Text(if (lang == "pt") "Parte Técnica / Exercícios" else "Technical Exercises", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(planTecnica, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                                }
                            }
                        }

                        if (planRola.isNotBlank()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Column {
                                    Text(if (lang == "pt") "Rolas / Combates" else "Sparring / Combats", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(planRola, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                                }
                            }
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Text(
                            text = if (lang == "pt") {
                                "Nenhum planejamento cadastrado pelo professor para o horário de $selectedTimeForPlan hoje."
                            } else {
                                "No lesson plan registered by the professor for $selectedTimeForPlan today."
                            },
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }
            }
        }

        // Summer Challenge banner card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondary)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1.2f)) {
                    Text(
                        text = if (lang == "pt") "Desafio de Verão" else "Summer Challenge",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (lang == "pt") "Complete 20 aulas em Julho para conquistar a insígnia 'Samurai'!" else "Complete 20 classes in July to earn the 'Samurai' badge!",
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    // Overlapping avatars
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(24.dp).clip(CircleShape).background(Color.LightGray)) {
                            Text("A", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Center))
                        }
                        Box(modifier = Modifier.size(24.dp).offset(x = (-6).dp).clip(CircleShape).background(Color.Gray)) {
                            Text("M", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Center))
                        }
                        Text(
                            text = if (lang == "pt") "Faça parte você também!" else "Join the challenge now!",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Badge",
                        tint = Color.White,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }
        }

        // Attendance Activity Log
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(if (lang == "pt") "Frequência da Semana" else "Weekly Attendance", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                    Text(
                        text = if (lang == "pt") "Histórico Completo >" else "Full History >",
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { onNavigateToAthletes() }
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val weekDays = if (lang == "pt") {
                        listOf("Seg" to true, "Ter" to true, "Qua" to false, "Qui" to true, "Sex" to false, "Sáb" to true, "Dom" to false)
                    } else {
                        listOf("Mon" to true, "Tue" to true, "Wed" to false, "Thu" to true, "Fri" to false, "Sat" to true, "Sun" to false)
                    }
                    weekDays.forEach { (day, present) ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(day, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (present) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (present) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Presente",
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


// ==========================================
// 2. ATHLETES LIST & MANAGEMENT SCREEN
// ==========================================
@Composable
fun AthletesScreen(
    viewModel: GymViewModel,
    athletes: List<Athlete>,
    onAddAthlete: () -> Unit,
    onEditAthlete: (Athlete) -> Unit
) {
    val lang by viewModel.selectedLanguage.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedBeltFilter by viewModel.selectedBeltFilter.collectAsState()
    val selectedStatusFilter by viewModel.selectedStatusFilter.collectAsState()

    val beltOptions = listOf(
        "All" to (if (lang == "pt") "Todos" else "All"),
        "Faixa Branca" to TranslationUtils.getLocalizedBelt("Faixa Branca", lang),
        "Faixa Azul" to TranslationUtils.getLocalizedBelt("Faixa Azul", lang),
        "Faixa Roxa" to TranslationUtils.getLocalizedBelt("Faixa Roxa", lang),
        "Faixa Marrom" to TranslationUtils.getLocalizedBelt("Faixa Marrom", lang),
        "Faixa Preta" to TranslationUtils.getLocalizedBelt("Faixa Preta", lang)
    )

    val statusOptions = listOf(
        "All" to (if (lang == "pt") "Todos" else "All"),
        "Active" to (if (lang == "pt") "Ativo" else "Active"),
        "Inactive" to (if (lang == "pt") "Inativo" else "Inactive")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // Quick Roll-Call Shortcut Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { viewModel.setProfessorActiveTab("Frequencia") },
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f))
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (lang == "pt") "🥋 Fazer Chamada Rápida" else "🥋 Quick Roll Call",
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = if (lang == "pt") "Clique para abrir a lista e registrar presenças hoje." else "Click to view the roll call and record presence today.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.setSearchQuery(it) },
            placeholder = { Text(if (lang == "pt") "Pesquisar atleta ou academia..." else "Search athlete or academy...") },
            leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = "Search") },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("search_athlete_input"),
            shape = RoundedCornerShape(24.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Horizontal filter selectors
        Text(if (lang == "pt") "Filtrar por Graduação:" else "Filter by Belt Rank:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(4.dp))
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(beltOptions) { belt ->
                val isSelected = selectedBeltFilter == belt.first
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                        )
                        .clickable { viewModel.setBeltFilter(belt.first) }
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = belt.second,
                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Status Filter
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(if (lang == "pt") "Status:" else "Status:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
            statusOptions.forEach { status ->
                val isSelected = selectedStatusFilter == status.first
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surfaceVariant
                        )
                        .clickable { viewModel.setStatusFilter(status.first) }
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = status.second,
                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (athletes.isEmpty()) {
            // Empty state placeholder
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Nenhum atleta",
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                        modifier = Modifier.size(64.dp)
                    )
                    Text(
                        text = if (lang == "pt") "Nenhum atleta encontrado" else "No athlete found",
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = if (lang == "pt") "Tente limpar seus filtros ou adicione um novo atleta clicando no botão +" else "Try clearing your filters or add a new athlete by clicking the + button",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 32.dp)
                    )
                }
            }
        } else {
            // Athletes List
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(athletes, key = { it.id }) { athlete ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onEditAthlete(athlete) }
                            .testTag("athlete_item_card_${athlete.id}"),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(
                            1.dp,
                            if (athlete.isActive) MaterialTheme.colorScheme.outlineVariant else Color.LightGray.copy(alpha = 0.5f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Avatar
                            AthleteAvatar(
                                imageUrl = athlete.imageUrl,
                                name = athlete.name,
                                modifier = Modifier.size(52.dp)
                            )

                            // Athlete text content
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = athlete.name,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 16.sp,
                                        color = if (athlete.isActive) MaterialTheme.colorScheme.onBackground else Color.Gray,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )

                                    // Status tag (Active/Inactive)
                                    Box(
                                        modifier = Modifier
                                            .background(
                                                if (athlete.isActive) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else Color.LightGray,
                                                RoundedCornerShape(8.dp)
                                            )
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = if (athlete.isActive) {
                                                if (lang == "pt") "Ativo" else "Active"
                                            } else {
                                                if (lang == "pt") "Inativo" else "Inactive"
                                            },
                                            color = if (athlete.isActive) MaterialTheme.colorScheme.primary else Color.DarkGray,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                                Text(
                                    text = athlete.academyName,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                // Belt Rank Badge
                                Box(
                                    modifier = Modifier
                                        .background(
                                            getBeltColor(athlete.beltRank),
                                            RoundedCornerShape(8.dp)
                                        )
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = TranslationUtils.getLocalizedBelt(athlete.beltRank, lang),
                                        color = getBeltTextColor(athlete.beltRank),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            // Quick Stats
                            Column(
                                horizontalAlignment = Alignment.End,
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = "Streak",
                                        tint = MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text("${athlete.streakDays}d", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                                Text("${athlete.trainingHours} hrs", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                                Spacer(modifier = Modifier.height(4.dp))

                                // Edit button
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primaryContainer)
                                        .clickable { onEditAthlete(athlete) }
                                        .testTag("edit_athlete_button_${athlete.id}"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Editar",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


// ==========================================
// 3. GRADUATION & BELTS JOURNEY SCREEN
// ==========================================
@Composable
fun BeltsScreen(
    viewModel: GymViewModel,
    athletes: List<Athlete>
) {
    val lang by viewModel.selectedLanguage.collectAsStateWithLifecycle()
    val currentEmail by viewModel.userEmail.collectAsStateWithLifecycle()
    val primaryAthlete = remember(athletes, currentEmail) {
        athletes.find { it.email.trim().lowercase() == currentEmail.trim().lowercase() }
            ?: athletes.firstOrNull()
            ?: Athlete(
                name = "Alex Silva",
                beltRank = "Faixa Roxa",
                beltProgress = 72,
                academyName = "Gracie Barra Pinheiros",
                trainingHours = 128,
                streakDays = 14,
                nextGraduation = "Faixa Marrom",
                nextGraduationProgress = 82
            )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Hero Section
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.secondary,
                                MaterialTheme.colorScheme.tertiary
                            )
                        )
                    )
                    .padding(4.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.background),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .clip(CircleShape)
                        .background(getBeltColor(primaryAthlete.beltRank)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Belt Rank",
                        tint = getBeltTextColor(primaryAthlete.beltRank),
                        modifier = Modifier.size(48.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = TranslationUtils.getLocalizedBelt(primaryAthlete.beltRank, lang),
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = if (lang == "pt") "Cadastrado em Outubro de 2022 • Nível Avançado" else "Registered October 2022 • Advanced Level",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold
            )
        }

        // Progression Details Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            border = BorderStroke(2.dp, MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = if (lang == "pt") "PROGRESSÃO DE GRADUAÇÃO" else "GRADUATION PROGRESS",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = if (lang == "pt") "${primaryAthlete.beltProgress}% Concluído" else "${primaryAthlete.beltProgress}% Completed",
                        style = MaterialTheme.typography.displayMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 24.sp
                    )
                    Text(
                        text = (if (lang == "pt") "Faixa Atual: " else "Current Belt: ") + TranslationUtils.getLocalizedBelt(primaryAthlete.beltRank, lang),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                LinearProgressIndicator(
                    progress = { primaryAthlete.beltProgress / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                        .clip(RoundedCornerShape(6.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.outlineVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = (if (lang == "pt") "Atual: " else "Current: ") + TranslationUtils.getLocalizedBelt(primaryAthlete.beltRank, lang),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = (if (lang == "pt") "Próxima: " else "Next: ") + TranslationUtils.getLocalizedBelt(primaryAthlete.nextGraduation, lang),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // Estimated Promotion card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Event",
                    tint = MaterialTheme.colorScheme.onTertiaryContainer,
                    modifier = Modifier.size(36.dp)
                )
                Column {
                    Text(
                        text = if (lang == "pt") "PROMOÇÃO ESTIMADA" else "ESTIMATED PROMOTION",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f)
                    )
                    Text(
                        text = if (lang == "pt") "Agosto de 2026" else "August 2026",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                    Text(
                        text = if (lang == "pt") "Com base na frequência de treinos atual" else "Based on current training attendance",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f)
                    )
                }
            }
        }

        // Requirements Checklist
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(imageVector = Icons.Default.Check, contentDescription = "Reqs", tint = MaterialTheme.colorScheme.secondary)
                Text(
                    text = if (lang == "pt") "Requisitos para Nova Graduação" else "Requirements for Next Promotion",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            // Req 1
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.Home, contentDescription = "Gym", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(if (lang == "pt") "Aulas Frequentadas" else "Classes Attended", fontWeight = FontWeight.Bold, fontSize = 14.dp.value.sp)
                        Text(if (lang == "pt") "Frequentar pelo menos 50 aulas no ciclo atual." else "Attend at least 50 classes in the current cycle.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Text("38 / 50", fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                }
            }

            // Req 2
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.secondaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.Star, contentDescription = "Technical", tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(18.dp))
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(if (lang == "pt") "Seminário Técnico" else "Technical Seminar", fontWeight = FontWeight.Bold, fontSize = 14.dp.value.sp)
                        Text(if (lang == "pt") "Atendimento ao Seminário Geral de Defesa Pessoal." else "Attendance at the General Self-Defense Seminar.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Text(if (lang == "pt") "Pendente" else "Pending", fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.secondary)
                }
            }
        }
    }
}

// ==========================================
// 4. WALLET & PAYMENTS SCREEN
// ==========================================
@Composable
fun WalletScreen(viewModel: GymViewModel, athletes: List<Athlete>) {
    val lang by viewModel.selectedLanguage.collectAsStateWithLifecycle()
    var subTab by remember { mutableStateOf("Wallet") } // "Wallet", "Attendance", or "Contract"
    val currentEmail by viewModel.userEmail.collectAsStateWithLifecycle()
    val planRequests by viewModel.planRequests.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val primaryAthlete = remember(athletes, currentEmail) {
        athletes.find { it.email.trim().lowercase() == currentEmail.trim().lowercase() }
            ?: athletes.firstOrNull()
            ?: Athlete(
                name = "Alex Silva",
                email = "alex.silva@matsumura.com",
                paymentValue = 189.00,
                paymentStatus = "Pago",
                lastPaymentDate = "15/06/2026",
                beltRank = "Faixa Roxa",
                beltProgress = 72,
                academyName = "Gracie Barra Pinheiros",
                trainingHours = 128,
                streakDays = 14,
                nextGraduation = "Faixa Marrom",
                nextGraduationProgress = 82
            )
    }

    val activePlanName = remember(primaryAthlete.paymentValue) {
        when {
            primaryAthlete.paymentValue <= 120.0 -> "Plano Anual"
            primaryAthlete.paymentValue <= 135.0 -> "Plano Semestral"
            primaryAthlete.paymentValue <= 150.0 -> "Plano Trimestral"
            else -> "Plano Mensal"
        }
    }

    val activePlanNameLocalized = when (activePlanName) {
        "Plano Anual" -> if (lang == "pt") "Plano Anual" else "Annual Plan"
        "Plano Semestral" -> if (lang == "pt") "Plano Semestral" else "Semi-Annual Plan"
        "Plano Trimestral" -> if (lang == "pt") "Plano Trimestral" else "Quarterly Plan"
        else -> if (lang == "pt") "Plano Mensal" else "Monthly Plan"
    }

    val activePlanPrice = "R$ ${String.format(Locale.US, "%.2f", primaryAthlete.paymentValue)}"
    val activeBillingCycle = when (activePlanName) {
        "Plano Mensal" -> if (lang == "pt") "A cada mês" else "Every month"
        "Plano Trimestral" -> if (lang == "pt") "A cada 3 meses" else "Every 3 months"
        "Plano Semestral" -> if (lang == "pt") "A cada 6 meses" else "Every 6 months"
        "Plano Anual" -> if (lang == "pt") "Anualmente" else "Annually"
        else -> if (lang == "pt") "A cada mês" else "Every month"
    }

    val plans = if (lang == "pt") {
        listOf(
            Triple("Plano Mensal", "R$ 160,00", "Básico • Cobrado R$160 mensalmente. Sem fidelidade, cancele quando quiser."),
            Triple("Plano Trimestral", "R$ 140,00", "Fidelidade Média • Cobrado R$420 a cada 3 meses. Economia expressiva de 12.5%!"),
            Triple("Plano Semestral", "R$ 125,00", "Fidelidade Alta • Cobrado R$750 a cada 6 meses. Economia incrível de 21.8%!"),
            Triple("Plano Anual", "R$ 110,00", "Fidelidade Máxima • Cobrado R$1.320 a cada 12 meses. O melhor preço: 31.2% de desconto!")
        )
    } else {
        listOf(
            Triple("Plano Mensal", "R$ 160.00", "Basic • Charged $160 monthly. No contract, cancel anytime."),
            Triple("Plano Trimestral", "R$ 140.00", "Medium Contract • Charged $420 every 3 months. Significant saving of 12.5%!"),
            Triple("Plano Semestral", "R$ 125.00", "High Contract • Charged $750 every 6 months. Incredible saving of 21.8%!"),
            Triple("Plano Anual", "R$ 110.00", "Maximum Contract • Charged $1,320 every 12 months. The best price: 31.2% off!")
        )
    }

    var selectedPlanForRequest by remember(activePlanName) { mutableStateOf(activePlanName) }
    val activeRequest = remember(planRequests, primaryAthlete.id) {
        planRequests.find { it.athleteId == primaryAthlete.id }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Simple sub tab navigation bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (subTab == "Wallet") MaterialTheme.colorScheme.primary else Color.Transparent)
                    .clickable { subTab = "Wallet" }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (lang == "pt") "Financeiro" else "Finance",
                    color = if (subTab == "Wallet") Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (subTab == "Attendance") MaterialTheme.colorScheme.primary else Color.Transparent)
                    .clickable { subTab = "Attendance" }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (lang == "pt") "Frequência" else "Attendance",
                    color = if (subTab == "Attendance") Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (subTab == "Contract") MaterialTheme.colorScheme.primary else Color.Transparent)
                    .clickable { subTab = "Contract" }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (lang == "pt") "Contrato" else "Contract",
                    color = if (subTab == "Contract") Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (subTab == "Wallet") {
            // Finance content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Expiry reminder inside wallet too!
                val calendar = remember { Calendar.getInstance() }
                val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
                val dueDay = remember(primaryAthlete.lastPaymentDate) {
                    try {
                        primaryAthlete.lastPaymentDate.split("/").firstOrNull()?.toInt() ?: 15
                    } catch (e: Exception) {
                        15
                    }
                }
                val isPendingOrOverdue = primaryAthlete.paymentStatus != "Pago"
                val isNearDueDay = (dueDay - dayOfMonth) in 0..5 || isPendingOrOverdue

                if (isNearDueDay) {
                    val bannerColor = if (isPendingOrOverdue) Color(0xFFFFEBEE) else Color(0xFFFFF9C4)
                    val textColor = if (isPendingOrOverdue) Color(0xFFC62828) else Color(0xFFF57F17)
                    val borderColor = if (isPendingOrOverdue) Color(0xFFFFCDD2) else Color(0xFFFFF59D)
                    val titleText = if (isPendingOrOverdue) {
                        if (lang == "pt") "Mensalidade Pendente!" else "Monthly Fee Pending!"
                    } else {
                        if (lang == "pt") "Lembrete: Próximo ao Vencimento" else "Reminder: Near Due Date"
                    }
                    val bodyText = if (isPendingOrOverdue) {
                        if (lang == "pt") {
                            "Seu pagamento de R$ ${String.format(Locale.US, "%.2f", primaryAthlete.paymentValue)} consta como ${primaryAthlete.paymentStatus}. Copie o PIX abaixo ou pague diretamente na secretaria da academia para liberar seu acesso."
                        } else {
                            "Your payment of R$ ${String.format(Locale.US, "%.2f", primaryAthlete.paymentValue)} is marked as ${primaryAthlete.paymentStatus}. Copy the PIX below or pay directly at the front desk to resume access."
                        }
                    } else {
                        if (lang == "pt") {
                            "Sua mensalidade vence em breve no dia $dueDay. Mantenha os treinos ativos regularizando sua renovação."
                        } else {
                            "Your monthly fee is due soon on day $dueDay. Keep training active by regularizing your renewal."
                        }
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = bannerColor),
                        border = BorderStroke(1.dp, borderColor)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(textColor.copy(alpha = 0.1f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (isPendingOrOverdue) Icons.Default.Warning else Icons.Default.Info,
                                        contentDescription = null,
                                        tint = textColor,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Text(
                                    text = titleText,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = textColor
                                )
                            }
                            Text(
                                text = bodyText,
                                fontSize = 12.sp,
                                color = textColor.copy(alpha = 0.9f),
                                lineHeight = 16.sp
                            )

                            if (isPendingOrOverdue) {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.6f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            Text(if (lang == "pt") "Chave PIX (E-mail):" else "PIX Key (E-mail):", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                            Text("pix@matsumurajj.com", fontSize = 12.sp, fontWeight = FontWeight.Black)
                                        }
                                        Button(
                                            onClick = { /* Simulated copy */ },
                                            colors = ButtonDefaults.buttonColors(containerColor = textColor),
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                            modifier = Modifier.height(28.dp)
                                        ) {
                                            Text(if (lang == "pt") "Copiar" else "Copy", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Subscription Card Mock
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(if (lang == "pt") "PLANO ATIVO" else "ACTIVE PLAN", color = Color.White.copy(alpha = 0.7f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                Text(activePlanNameLocalized, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Black)
                            }
                            Box(
                                modifier = Modifier
                                    .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(if (lang == "pt") "ATIVO" else "ACTIVE", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(32.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(if (lang == "pt") "VALOR INDIVIDUAL" else "INDIVIDUAL VALUE", color = Color.White.copy(alpha = 0.7f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                Text(if (lang == "pt") "$activePlanPrice / mês" else "$activePlanPrice / month", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Black)
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(if (lang == "pt") "CICLO DE COBRANÇA" else "BILLING CYCLE", color = Color.White.copy(alpha = 0.7f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                Text(activeBillingCycle, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Black)
                            }
                        }
                    }
                }

                // Small stats
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(if (lang == "pt") "AULAS RESTANTES" else "REMAINING CLASSES", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("12 / 20", fontSize = 20.sp, fontWeight = FontWeight.Black)
                        }
                    }

                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(if (lang == "pt") "PONTOS ACUMULADOS" else "EARNED POINTS", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("2.450 pts", fontSize = 20.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }

                // Pending Change Request Banner
                if (activeRequest != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.HourglassEmpty,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (lang == "pt") "Solicitação de Mudança Enviada!" else "Change Request Sent!",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = if (lang == "pt") {
                                        "Você solicitou a mudança do ${activeRequest.currentPlan} para o ${activeRequest.requestedPlan}. O dono da academia está analisando seu pedido."
                                    } else {
                                        "You requested a change from ${activeRequest.currentPlan} to ${activeRequest.requestedPlan}. The gym owner is reviewing your request."
                                    },
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // Subtitle Plan Section
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(if (lang == "pt") "Nossos Planos de Assinatura" else "Our Subscription Plans", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                    Text(if (lang == "pt") "Escolha a melhor opção para treinar na Matsumura Team com descontos progressivos:" else "Choose the best option to train at Matsumura Team with progressive discounts:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                    plans.forEach { (name, price, desc) ->
                        val isCurrent = activePlanName == name
                        val isSelected = selectedPlanForRequest == name
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedPlanForRequest = name
                                },
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f) else Color.White
                            ),
                            border = BorderStroke(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        val localizedName = when (name) {
                                            "Plano Anual" -> if (lang == "pt") "Plano Anual" else "Annual Plan"
                                            "Plano Semestral" -> if (lang == "pt") "Plano Semestral" else "Semi-Annual Plan"
                                            "Plano Trimestral" -> if (lang == "pt") "Plano Trimestral" else "Quarterly Plan"
                                            else -> if (lang == "pt") "Plano Mensal" else "Monthly Plan"
                                        }
                                        Text(localizedName, fontWeight = FontWeight.Black, fontSize = 15.sp, color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface)
                                        if (isCurrent) {
                                            Box(
                                                modifier = Modifier
                                                    .background(MaterialTheme.colorScheme.secondary, RoundedCornerShape(6.dp))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(if (lang == "pt") "PLANO ATUAL" else "CURRENT PLAN", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Black)
                                            }
                                        } else if (isSelected) {
                                            Box(
                                                modifier = Modifier
                                                    .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(6.dp))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(if (lang == "pt") "SELECIONADO" else "SELECTED", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Black)
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(desc, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }

                                Column(horizontalAlignment = Alignment.End, modifier = Modifier.padding(start = 8.dp)) {
                                    Text(price, fontWeight = FontWeight.Black, fontSize = 18.sp, color = MaterialTheme.colorScheme.primary)
                                    Text(if (lang == "pt") "/mês" else "/month", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }

                    // Request Change Button
                    if (selectedPlanForRequest != activePlanName && activeRequest == null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                viewModel.requestPlanChange(
                                    athleteId = primaryAthlete.id,
                                    athleteName = primaryAthlete.name,
                                    currentPlan = activePlanName,
                                    requestedPlan = selectedPlanForRequest
                                )
                                // Launch WhatsApp Intent
                                try {
                                    val localizedRequestPlan = when (selectedPlanForRequest) {
                                        "Plano Anual" -> if (lang == "pt") "Plano Anual" else "Annual Plan"
                                        "Plano Semestral" -> if (lang == "pt") "Plano Semestral" else "Semi-Annual Plan"
                                        "Plano Trimestral" -> if (lang == "pt") "Plano Trimestral" else "Quarterly Plan"
                                        else -> if (lang == "pt") "Plano Mensal" else "Monthly Plan"
                                    }
                                    val localizedCurrentPlan = when (activePlanName) {
                                        "Plano Anual" -> if (lang == "pt") "Plano Anual" else "Annual Plan"
                                        "Plano Semestral" -> if (lang == "pt") "Plano Semestral" else "Semi-Annual Plan"
                                        "Plano Trimestral" -> if (lang == "pt") "Plano Trimestral" else "Quarterly Plan"
                                        else -> if (lang == "pt") "Plano Mensal" else "Monthly Plan"
                                    }
                                    val message = if (lang == "pt") {
                                        "Olá Matsumura Dojo! Eu, ${primaryAthlete.name}, gostaria de solicitar a mudança do meu plano de $localizedCurrentPlan para $localizedRequestPlan."
                                    } else {
                                        "Hello Matsumura Dojo! I, ${primaryAthlete.name}, would like to request to change my plan from $localizedCurrentPlan to $localizedRequestPlan."
                                    }
                                    val encodedMsg = Uri.encode(message)
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://api.whatsapp.com/send?phone=5511987654321&text=$encodedMsg"))
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    val errorMsg = if (lang == "pt") {
                                        "Não foi possível abrir o WhatsApp. Solicitação registrada no app!"
                                    } else {
                                        "Could not open WhatsApp. Request registered in app!"
                                    }
                                    Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(Icons.Default.Send, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            val localizedSelected = when (selectedPlanForRequest) {
                                "Plano Anual" -> if (lang == "pt") "Plano Anual" else "Annual Plan"
                                "Plano Semestral" -> if (lang == "pt") "Plano Semestral" else "Semi-Annual Plan"
                                "Plano Trimestral" -> if (lang == "pt") "Plano Trimestral" else "Quarterly Plan"
                                else -> if (lang == "pt") "Plano Mensal" else "Monthly Plan"
                            }
                            Text(
                                text = if (lang == "pt") "Solicitar Mudança para $localizedSelected" else "Request Change to $localizedSelected",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }


                // Payment History List
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(if (lang == "pt") "Histórico de Pagamentos" else "Payment History", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)

                    val paymentItems = if (lang == "pt") {
                        listOf(
                            "Mensalidade de Setembro" to "R$ 189,00" to "Pago em 15 Set, 2026",
                            "Mensalidade de Agosto" to "R$ 189,00" to "Pago em 15 Ago, 2026",
                            "Seminário com Mestre Silva" to "R$ 80,00" to "Pago em 02 Ago, 2026"
                        )
                    } else {
                        listOf(
                            "September Monthly Fee" to "R$ 189.00" to "Paid on Sep 15, 2026",
                            "August Monthly Fee" to "R$ 189.00" to "Paid on Aug 15, 2026",
                            "Seminar with Master Silva" to "R$ 80.00" to "Paid on Aug 02, 2026"
                        )
                    }
                    paymentItems.forEach { (titleAndValue, date) ->
                        val (title, value) = titleAndValue
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.primaryContainer),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(imageVector = Icons.Default.ShoppingCart, contentDescription = "Payment", tint = MaterialTheme.colorScheme.primary)
                                    }
                                    Column {
                                        Text(title, fontWeight = FontWeight.Bold)
                                        Text(date, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(value, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onBackground)
                                    Text(if (lang == "pt") "RECIBO" else "RECEIPT", fontSize = 9.sp, color = MaterialTheme.colorScheme.tertiary, fontWeight = FontWeight.Black)
                                }
                            }
                        }
                    }
                }
            }
        } else if (subTab == "Attendance") {
            // Attendance/Frequencia content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Dynamic Check-In Card for Athlete
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = "Marcar Presença (Check-In)",
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        
                        Text(
                            text = "Selecione o horário do seu treino para registrar sua presença de hoje.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        val todayDateFormatted = remember { java.text.SimpleDateFormat("dd/MM", java.util.Locale.getDefault()).format(java.util.Date()) }
                        val checkInTimes = listOf("08:00", "12:00", "18:00", "19:30", "21:00")
                        var selectedCheckInTime by remember { mutableStateOf("19:30") }
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            checkInTimes.forEach { time ->
                                val isSelected = selectedCheckInTime == time
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                                        .clickable { selectedCheckInTime = time }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = time,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                        
                        val presencePattern = "$todayDateFormatted ($selectedCheckInTime)"
                        val hasCheckedIn = primaryAthlete.attendanceHistory.contains(presencePattern)
                        
                        Button(
                            onClick = {
                                viewModel.toggleAthleteAttendance(primaryAthlete, presencePattern)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (hasCheckedIn) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.primary,
                                contentColor = if (hasCheckedIn) MaterialTheme.colorScheme.onSurfaceVariant else Color.White
                            )
                        ) {
                            Icon(
                                imageVector = if (hasCheckedIn) Icons.Default.CheckCircle else Icons.Default.AddCircle,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (hasCheckedIn) "Presença Registrada ($presencePattern)" else "Registrar Presença de Hoje ($presencePattern)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }

                // Calendar header Mock
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(if (lang == "pt") "Junho de 2026" else "June 2026", fontWeight = FontWeight.Black, fontSize = 18.sp)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.surfaceVariant),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("<", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.surfaceVariant),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(">", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Grid headers
                        Row(modifier = Modifier.fillMaxWidth()) {
                            val calendarDays = if (lang == "pt") {
                                listOf("D", "S", "T", "Q", "Q", "S", "S")
                            } else {
                                listOf("S", "M", "T", "W", "T", "F", "S")
                            }
                            calendarDays.forEach { day ->
                                Text(
                                    day,
                                    modifier = Modifier.weight(1f),
                                    textAlign = TextAlign.Center,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Render simple mock days
                        val daysList = (28..31).map { it to false } + (1..20).map { it to (it % 3 == 0 || it == 14) }
                        daysList.chunked(7).forEach { week ->
                            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                week.forEach { (day, present) ->
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .aspectRatio(1f)
                                            .padding(4.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (present) MaterialTheme.colorScheme.primary else Color.Transparent
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = day.toString(),
                                            color = if (present) Color.White else MaterialTheme.colorScheme.onBackground,
                                            fontSize = 12.sp,
                                            fontWeight = if (present) FontWeight.Black else FontWeight.Medium
                                        )
                                    }
                                }
                                // Pad partial weeks
                                if (week.size < 7) {
                                    repeat(7 - week.size) {
                                        Box(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }
                }

                // Recent check ins history list
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(if (lang == "pt") "Check-ins Recentes" else "Recent Check-ins", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)

                    val checkInItems = if (lang == "pt") {
                        listOf(
                            "Drills de Sparring Pesado" to "09 Set • 18:30" to "Verificado",
                            "Técnicas de Passagem de Guarda" to "07 Set • 19:15" to "Verificado"
                        )
                    } else {
                        listOf(
                            "Heavy Sparring Drills" to "Sep 09 • 18:30" to "Verified",
                            "Guard Passing Techniques" to "Sep 07 • 19:15" to "Verified"
                        )
                    }
                    checkInItems.forEach { (titleAndDate, status) ->
                        val (title, date) = titleAndDate
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.secondaryContainer),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(imageVector = Icons.Default.Home, contentDescription = "Gym", tint = MaterialTheme.colorScheme.secondary)
                                    }
                                    Column {
                                        Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Text(date, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                                Box(
                                    modifier = Modifier
                                        .background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(status, color = MaterialTheme.colorScheme.tertiary, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                                }
                             }
                         }
                     }
                 }
             }
         } else if (subTab == "Contract") {
             // Contract sub-tab content using our prebuilt ContractDetailView
             Column(
                 modifier = Modifier
                     .fillMaxSize()
                     .verticalScroll(rememberScrollState()),
                 verticalArrangement = Arrangement.spacedBy(16.dp)
             ) {
                 ContractDetailView(
                     athlete = primaryAthlete,
                     onSign = { signatureName ->
                         viewModel.signContract(primaryAthlete, signatureName)
                     }
                 )
             }
         }
     }
 }

@Composable
fun SettingsScreen(viewModel: GymViewModel, athletes: List<Athlete>) {
    val lang by viewModel.selectedLanguage.collectAsStateWithLifecycle()
    val currentEmail by viewModel.userEmail.collectAsStateWithLifecycle()
    val userRole by viewModel.userRole.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var selectedAthleteByProf by remember { mutableStateOf<Athlete?>(null) }

    val primaryAthlete = remember(athletes, currentEmail, selectedAthleteByProf, userRole) {
        if (userRole == "Professor") {
            selectedAthleteByProf ?: athletes.find { it.email.trim().lowercase() == currentEmail.trim().lowercase() } ?: athletes.firstOrNull() ?: Athlete(
                name = "Alex Silva",
                email = "alex.silva@matsumura.com",
                paymentValue = 189.00,
                paymentStatus = "Pago",
                lastPaymentDate = "15/06/2026",
                beltRank = "Faixa Roxa",
                beltProgress = 72,
                academyName = "Gracie Barra Pinheiros",
                trainingHours = 128,
                streakDays = 14,
                nextGraduation = "Faixa Marrom",
                nextGraduationProgress = 82
            )
        } else {
            athletes.find { it.email.trim().lowercase() == currentEmail.trim().lowercase() }
                ?: athletes.firstOrNull()
                ?: Athlete(
                    name = "Alex Silva",
                    email = "alex.silva@matsumura.com",
                    paymentValue = 189.00,
                    paymentStatus = "Pago",
                    lastPaymentDate = "15/06/2026",
                    beltRank = "Faixa Roxa",
                    beltProgress = 72,
                    academyName = "Gracie Barra Pinheiros",
                    trainingHours = 128,
                    streakDays = 14,
                    nextGraduation = "Faixa Marrom",
                    nextGraduationProgress = 82
                )
        }
    }

    var nickname by remember(primaryAthlete) { mutableStateOf(primaryAthlete.nickname) }
    var imageUrl by remember(primaryAthlete) { mutableStateOf(primaryAthlete.imageUrl) }
    var weightText by remember(primaryAthlete) { mutableStateOf(if (primaryAthlete.weight > 0.0) primaryAthlete.weight.toString() else "") }
    var heightText by remember(primaryAthlete) { mutableStateOf(if (primaryAthlete.height > 0.0) primaryAthlete.height.toString() else "") }
    var passwordText by remember(primaryAthlete) { mutableStateOf(primaryAthlete.password) }
    var isSaving by remember { mutableStateOf(false) }
    var showSnackbar by remember { mutableStateOf(false) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            val localPath = copyUriToInternalStorage(context, it)
            if (localPath != null) {
                imageUrl = localPath
            }
        }
    }

    val presetAvatars = listOf(
        "https://images.unsplash.com/photo-1544005313-94ddf0286df2?auto=format&fit=crop&q=80&w=200",
        "https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?auto=format&fit=crop&q=80&w=200",
        "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&q=80&w=200",
        "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&q=80&w=200",
        "https://images.unsplash.com/photo-1494790108377-be9c29b29330?auto=format&fit=crop&q=80&w=200"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        if (userRole == "Professor") {
            var dropdownExpanded by remember { mutableStateOf(false) }
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "⚙️ " + TranslationUtils.t("select_athlete_to_configure", lang),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                            .clickable { dropdownExpanded = true }
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = primaryAthlete.name.ifEmpty { if (lang == "pt") "Selecione um atleta..." else "Select an athlete..." },
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Seta de seleção",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        DropdownMenu(
                            expanded = dropdownExpanded,
                            onDismissRequest = { dropdownExpanded = false }
                        ) {
                            athletes.forEach { athlete ->
                                DropdownMenuItem(
                                    text = { Text(athlete.name) },
                                    onClick = {
                                        selectedAthleteByProf = athlete
                                        dropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = TranslationUtils.t("edit_profile", lang),
                    fontWeight = FontWeight.Black,
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.align(Alignment.Start)
                )

                // Avatar Select Area
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .clickable { galleryLauncher.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    if (imageUrl.isNotEmpty()) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(imageUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Foto de Perfil",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Sem Imagem",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.3f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Editar Foto",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Text(
                    text = if (lang == "pt") "Toque na foto para escolher da galeria" else "Tap photo to choose from gallery",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )

                // Preset Avatars Row
                Text(
                    text = if (lang == "pt") "Ou selecione um avatar rápido:" else "Or select a quick avatar:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.Start)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally)
                ) {
                    presetAvatars.forEach { url ->
                        val isSelected = imageUrl == url
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .border(
                                    BorderStroke(
                                        if (isSelected) 3.dp else 1.dp,
                                        if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
                                    ),
                                    CircleShape
                                )
                                .clickable { imageUrl = url }
                        ) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(url)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "Preset Avatar",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }

                Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                // Language Selection Selector (CHOOSE LANGUAGE SETTING)
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "🌐 " + TranslationUtils.t("select_language", lang),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.align(Alignment.Start)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        listOf("pt" to "Português", "en" to "English").forEach { (code, label) ->
                            val isSelected = lang == code
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                    .border(
                                        1.dp,
                                        if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                        RoundedCornerShape(14.dp)
                                    )
                                    .clickable { viewModel.setLanguage(code) }
                                    .padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                // Nickname / Como quer ser chamado
                OutlinedTextField(
                    value = nickname,
                    onValueChange = { nickname = it },
                    label = { Text(TranslationUtils.t("nickname", lang)) },
                    placeholder = { Text(if (lang == "pt") "Ex: Nickname do Atleta" else "Ex: Athlete Nickname") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary
                    ),
                    singleLine = true
                )

                // Peso e Altura Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedTextField(
                        value = weightText,
                        onValueChange = { weightText = it },
                        label = { Text(TranslationUtils.t("weight", lang)) },
                        placeholder = { Text("Ex: 78.5") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = heightText,
                        onValueChange = { heightText = it },
                        label = { Text(TranslationUtils.t("height", lang)) },
                        placeholder = { Text("Ex: 1.78") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                }

                // Senha de Acesso
                OutlinedTextField(
                    value = passwordText,
                    onValueChange = { passwordText = it },
                    label = { Text(TranslationUtils.t("access_password", lang)) },
                    placeholder = { Text(TranslationUtils.t("password_min_length", lang)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary
                    ),
                    singleLine = true
                )
 
                Spacer(modifier = Modifier.height(10.dp))
 
                // Stats summary (Read-only for info)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(if (lang == "pt") "Idade:" else "Age:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(if (lang == "pt") "${primaryAthlete.age} anos" else "${primaryAthlete.age} years", fontWeight = FontWeight.Black, fontSize = 13.sp)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(if (lang == "pt") "Categoria:" else "Category:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(primaryAthlete.category, fontWeight = FontWeight.Black, fontSize = 13.sp)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(if (lang == "pt") "Graduação:" else "Belt Rank:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(TranslationUtils.getLocalizedBelt(primaryAthlete.beltRank, lang), fontWeight = FontWeight.Black, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                    }
                }
 
                // Save Button
                Button(
                    onClick = {
                        isSaving = true
                        val updated = primaryAthlete.copy(
                            nickname = nickname,
                            imageUrl = imageUrl,
                            weight = weightText.toDoubleOrNull() ?: 0.0,
                            height = heightText.toDoubleOrNull() ?: 0.0,
                            password = passwordText,
                            passwordChanged = if (passwordText != primaryAthlete.password) true else primaryAthlete.passwordChanged
                        )
                        viewModel.saveAthlete(updated) {
                            isSaving = false
                            showSnackbar = true
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("save_profile_settings_button"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    enabled = !isSaving
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Save, contentDescription = null)
                            Text(TranslationUtils.t("save_changes", lang), fontWeight = FontWeight.Black, fontSize = 16.sp)
                        }
                    }
                }
            }
        }

        if (showSnackbar) {
            Snackbar(
                action = {
                    TextButton(onClick = { showSnackbar = false }) {
                        Text("OK", color = Color.White)
                    }
                },
                modifier = Modifier.padding(8.dp)
            ) {
                Text(TranslationUtils.t("password_saved_success", lang))
            }
        }
    }
}

@Composable
fun ContractDetailView(
    athlete: Athlete,
    onSign: ((String) -> Unit)? = null
) {
    var typedName by remember { mutableStateOf("") }
    var agreed by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(24.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFDFBF7)),
            border = BorderStroke(1.dp, Color(0xFFE5DCD0)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(18.dp)
                    .heightIn(max = 300.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "CONTRATO DE PRESTAÇÃO DE SERVIÇOS ESPORTIVOS",
                    fontWeight = FontWeight.Black,
                    fontSize = 15.sp,
                    textAlign = TextAlign.Center,
                    color = Color(0xFF3E2723),
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "CONTRATADA (EMPRESA):\n" +
                            "MATSUMURA TEAM DOJO\n" +
                            "Razão Social: Matsumura Judô e Jiu-Jitsu LTDA\n" +
                            "CNPJ: 24.891.032/0001-44\n" +
                            "Endereço: Rua das Amoreiras, 1420 - Pinheiros, São Paulo/SP\n" +
                            "Representante Legal: Sensei Harisson Resende\n\n" +
                            "CONTRATANTE (ATLETA):\n" +
                            "Nome: ${athlete.name}\n" +
                            "E-mail: ${athlete.email.ifEmpty { "Não cadastrado" }}\n" +
                            "Telefone: ${athlete.phone.ifEmpty { "Não cadastrado" }}\n" +
                            "CPF: ${athlete.cpf.ifEmpty { "Não cadastrado" }}\n" +
                            "Idade: ${if (athlete.age > 0) "${athlete.age} anos" else "Não informada"}",
                    fontSize = 11.sp,
                    lineHeight = 15.sp,
                    color = Color(0xFF4E342E),
                    fontFamily = FontFamily.Monospace
                )

                Divider(color = Color(0xFFE5DCD0))

                Text(
                    text = "CLÁUSULAS CONTRATUAIS:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = Color(0xFF3E2723)
                )

                Text(
                    text = "CLÁUSULA 1ª — DO OBJETO\n" +
                            "A CONTRATADA compromete-se a prestar serviços de treinamento prático e teórico de Artes Marciais (Karate Shorin-ryu) nas dependências físicas da academia Matsumura Team.",
                    fontSize = 11.sp,
                    lineHeight = 15.sp,
                    color = Color(0xFF5D4037)
                )

                Text(
                    text = "CLÁUSULA 2ª — DO CANCELAMENTO DE PLANO\n" +
                            "O cancelamento do plano Mensal poderá ser solicitado a qualquer momento pelo Contratante sem incidência de taxas ou penalidades.",
                    fontSize = 11.sp,
                    lineHeight = 15.sp,
                    color = Color(0xFF5D4037)
                )

                Text(
                    text = "CLÁUSULA 3ª — DA QUEBRA DE CONTRATO (MULTA DE FIDELIDADE)\n" +
                            "No caso de cancelamento imotivado e antecipado de planos com fidelidade ativa (Trimestral, Semestral ou Anual) antes do encerramento de sua vigência regulamentar, será devida pelo CONTRATANTE uma multa rescisória compensatória equivalente a 20% (vinte por cento) do valor total das parcelas restantes correspondentes ao término contratual pactuado.",
                    fontSize = 11.sp,
                    lineHeight = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFD84315)
                )

                Text(
                    text = "CLÁUSULA 4ª — DAS NORMAS INTERNAS E SEGURANÇA\n" +
                            "O CONTRATANTE declara gozar de plena saúde física e compromete-se a cumprir rigorosamente as normas de higiene, etiqueta tradicional das artes marciais, e uso correto dos uniformes do Dojo.",
                    fontSize = 11.sp,
                    lineHeight = 15.sp,
                    color = Color(0xFF5D4037)
                )

                Divider(color = Color(0xFFE5DCD0))

                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "ASSINADO DIGITALMENTE",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF2E7D32)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Matsumura Dojo Team",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = Color(0xFF3E2723)
                        )
                        Text(
                            text = "CNPJ 24.891.032/0001-44",
                            fontSize = 8.sp,
                            color = Color(0xFF795548)
                        )
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (athlete.contractSigned) {
                            Text(
                                text = "ASSINADO ELETRONICAMENTE",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF2E7D32)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = athlete.contractSignatureName,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = Color(0xFF3E2723)
                            )
                            Text(
                                text = "Data: ${athlete.contractSignatureDate}",
                                fontSize = 8.sp,
                                color = Color(0xFF795548)
                            )
                        } else {
                            Text(
                                text = "PENDENTE DE ASSINATURA",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Box(
                                modifier = Modifier
                                    .width(80.dp)
                                    .height(1.dp)
                                    .background(Color.Gray.copy(alpha = 0.5f))
                            )
                            Text(
                                text = "Assinatura do Atleta",
                                fontSize = 9.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }
        }

        if (!athlete.contractSigned && onSign != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Formalize seu Contrato de Adesão",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )

                    OutlinedTextField(
                        value = typedName,
                        onValueChange = { typedName = it },
                        placeholder = { Text("Ex: ${athlete.name}") },
                        label = { Text("Digite seu nome completo para assinar") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Checkbox(
                            checked = agreed,
                            onCheckedChange = { agreed = it }
                        )
                        Text(
                            text = "Eu concordo com os termos e com a multa de 20% de rescisão.",
                            fontSize = 11.sp,
                            lineHeight = 14.sp,
                            modifier = Modifier.clickable { agreed = !agreed }
                        )
                    }

                    Button(
                        onClick = {
                            if (typedName.isNotBlank() && agreed) {
                                onSign(typedName.trim())
                            }
                        },
                        enabled = typedName.isNotBlank() && agreed,
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.BorderColor, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Assinar Contrato Digital", fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else if (athlete.contractSigned) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                border = BorderStroke(1.dp, Color(0xFFC8E6C9))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Verified,
                        contentDescription = "Verificado",
                        tint = Color(0xFF2E7D32),
                        modifier = Modifier.size(32.dp)
                    )
                    Column {
                        Text(
                            text = "Contrato Assinado Digitalmente",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color(0xFF2E7D32)
                        )
                        Text(
                            text = "Este documento está ativo e vinculado ao seu cadastro na Matsumura Team.",
                            fontSize = 11.sp,
                            color = Color(0xFF2E7D32).copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }
    }
}
