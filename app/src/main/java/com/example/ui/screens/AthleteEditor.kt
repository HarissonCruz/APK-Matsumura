package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.Athlete
import android.content.Context
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.UUID

fun copyUriToInternalStorage(context: Context, uri: android.net.Uri): String? {
    return try {
        val contentResolver = context.contentResolver
        val inputStream: InputStream? = contentResolver.openInputStream(uri)
        if (inputStream != null) {
            val fileName = "athlete_${UUID.randomUUID()}.jpg"
            val file = File(context.filesDir, fileName)
            val outputStream = FileOutputStream(file)
            val buffer = ByteArray(4 * 1024)
            var read: Int
            while (inputStream.read(buffer).also { read = it } != -1) {
                outputStream.write(buffer, 0, read)
            }
            outputStream.flush()
            outputStream.close()
            inputStream.close()
            file.absolutePath
        } else {
            null
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

fun getCategoryByAge(age: Int): String {
    return when (age) {
        in 0..4 -> "Fraldinha"
        in 5..6 -> "Mirim A"
        in 7..8 -> "Mirim B"
        in 9..10 -> "Mirim C"
        in 11..12 -> "Infantil"
        in 13..14 -> "Infanto-Juvenil"
        in 15..17 -> "Juvenil"
        in 18..35 -> "Adulto"
        in 36..40 -> "Master A"
        in 41..45 -> "Master B"
        in 46..50 -> "Master C"
        else -> "Master D"
    }
}

fun calculateAgeAndTolerance(birthDateStr: String): Pair<Int, Int> {
    try {
        val parts = birthDateStr.split("/")
        if (parts.size == 3) {
            val day = parts[0].trim().toInt()
            val month = parts[1].trim().toInt() - 1 // Calendar month is 0-indexed
            val year = parts[2].trim().toInt()
            
            val birthCalendar = java.util.Calendar.getInstance()
            birthCalendar.set(year, month, day)
            
            val today = java.util.Calendar.getInstance()
            
            var ageYears = today.get(java.util.Calendar.YEAR) - birthCalendar.get(java.util.Calendar.YEAR)
            val birthMonth = birthCalendar.get(java.util.Calendar.MONTH)
            val birthDay = birthCalendar.get(java.util.Calendar.DAY_OF_MONTH)
            val currMonth = today.get(java.util.Calendar.MONTH)
            val currDay = today.get(java.util.Calendar.DAY_OF_MONTH)
            
            if (currMonth < birthMonth || (currMonth == birthMonth && currDay < birthDay)) {
                ageYears--
            }
            
            val totalMonthsDiff = (today.get(java.util.Calendar.YEAR) - year) * 12 + (currMonth - month)
            val dayAdjustedMonths = if (currDay < day) totalMonthsDiff - 1 else totalMonthsDiff
            
            val actualAge = dayAdjustedMonths / 12
            
            val monthsUntil10thBirthday = 120 - dayAdjustedMonths
            val ageWithTolerance = if (monthsUntil10thBirthday in 1..2) {
                10
            } else {
                actualAge
            }
            
            return Pair(actualAge, ageWithTolerance)
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return Pair(0, 0)
}

fun getShorinRyuBelts(age: Int, ageWithTolerance: Int): List<String> {
    val list = mutableListOf<String>()
    list.add("7º Kyu — Faixa Branca (Iniciante)")
    
    if (ageWithTolerance < 10) {
        list.add("7º Kyu — Vermelhinha")
    }
    
    list.add("6º Kyu — Faixa Amarela")
    list.add("5º Kyu — Faixa Laranja")
    list.add("4º Kyu — Faixa Azul")
    list.add("3º Kyu — Faixa Verde")
    
    if (ageWithTolerance < 18) {
        list.add("1º Estágio da faixa verde")
        list.add("2º Estágio da faixa verde")
        list.add("3º Estágio da faixa verde")
    }
    
    list.add("2º Kyu — Faixa Roxa")
    list.add("1º Kyu — Faixa Marrom")
    for (i in 1..10) {
        list.add("${i}º Dan — Faixa Preta")
    }
    return list
}

fun formatCPF(digits: String): String {
    val clean = digits.filter { it.isDigit() }.take(11)
    val sb = java.lang.StringBuilder()
    for (i in clean.indices) {
        sb.append(clean[i])
        if (i == 2 || i == 5) {
            sb.append(".")
        } else if (i == 8) {
            sb.append("-")
        }
    }
    return sb.toString()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AthleteEditorDialog(
    athlete: Athlete?, // Null means creating a new athlete
    isReadOnly: Boolean = false,
    onDismiss: () -> Unit,
    onSave: (Athlete) -> Unit
) {
    val context = LocalContext.current
    var name by remember { mutableStateOf(athlete?.name ?: "") }
    var academyName by remember { mutableStateOf(athlete?.academyName ?: "Shorin-ryu Karate") }
    var beltRank by remember { mutableStateOf(athlete?.beltRank ?: "7º Kyu — Faixa Branca (Iniciante)") }
    var beltProgress by remember { mutableFloatStateOf(athlete?.beltProgress?.toFloat() ?: 0f) }
    var phone by remember { mutableStateOf(athlete?.phone ?: "") }
    var email by remember { mutableStateOf(athlete?.email ?: "") }
    var password by remember { mutableStateOf(athlete?.password ?: "") }
    var passwordChanged by remember { mutableStateOf(athlete?.passwordChanged ?: false) }
    var imageUrl by remember { mutableStateOf(athlete?.imageUrl ?: "") }
    var isActive by remember { mutableStateOf(athlete?.isActive ?: true) }
    var trainingHours by remember { mutableStateOf(athlete?.trainingHours?.toString() ?: "0") }
    var streakDays by remember { mutableStateOf(athlete?.streakDays?.toString() ?: "0") }
    var nextGraduation by remember { mutableStateOf(athlete?.nextGraduation ?: "6º Kyu — Faixa Amarela") }
    var nextGraduationProgress by remember { mutableFloatStateOf(athlete?.nextGraduationProgress?.toFloat() ?: 0f) }
    var notes by remember { mutableStateOf(athlete?.notes ?: "") }
    var cpf by remember { mutableStateOf(athlete?.cpf ?: "") }
    var birthDate by remember { mutableStateOf(athlete?.birthDate ?: "") }
    var age by remember { mutableStateOf(athlete?.age?.toString() ?: "") }
    var category by remember { mutableStateOf(athlete?.category ?: "") }
    LaunchedEffect(name, birthDate) {
        if (athlete == null && !passwordChanged && name.isNotBlank() && birthDate.length >= 10) {
            password = getDefaultPasswordForAthlete(name, birthDate)
        }
    }
    LaunchedEffect(birthDate) {
        val cleanDate = birthDate.trim()
        if (cleanDate.length == 10) {
            val (calcAge, ageWithTol) = calculateAgeAndTolerance(cleanDate)
            if (calcAge >= 0) {
                age = calcAge.toString()
                category = getCategoryByAge(calcAge)
            }
        }
    }
    var emergencyContactName1 by remember { mutableStateOf(athlete?.emergencyContactName1 ?: "") }
    var emergencyContactPhone1 by remember { mutableStateOf(athlete?.emergencyContactPhone1 ?: "") }
    var emergencyContactName2 by remember { mutableStateOf(athlete?.emergencyContactName2 ?: "") }
    var emergencyContactPhone2 by remember { mutableStateOf(athlete?.emergencyContactPhone2 ?: "") }

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

    val ageInt = age.toIntOrNull() ?: 20
    val (_, ageWithTolerance) = calculateAgeAndTolerance(birthDate)
    val beltOptions = remember(ageInt, ageWithTolerance) {
        getShorinRyuBelts(ageInt, if (ageWithTolerance > 0) ageWithTolerance else ageInt)
    }
    var showBeltDropdown by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 24.dp), // Safe margin for the edge-to-edge notch
            color = MaterialTheme.colorScheme.background,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {
                            Text(
                                text = if (athlete == null) "Novo Atleta" else "Editar Atleta",
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 24.sp
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = onDismiss) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Fechar",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.background
                        )
                    )
                }
            ) { paddingValues ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .verticalScroll(rememberScrollState())
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    if (isReadOnly) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.25f)
                            ),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.4f))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "🔒 Apenas Leitura",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.error
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Como Financeiro, você não tem autorização para alterar os dados cadastrais ou o progresso técnico deste atleta. Use a aba Financeiro para lançar pagamentos ou gerenciar cancelamentos.",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // Profile Image Preview
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.secondaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        if (imageUrl.isNotEmpty()) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(imageUrl)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "Preview do Atleta",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Sem Imagem",
                                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }

                    // Athlete Name
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Nome do Atleta") },
                        placeholder = { Text("Ex: Alex Silva") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                        )
                    )

                    // CPF
                    OutlinedTextField(
                        value = cpf,
                        onValueChange = { input ->
                            val digits = input.filter { it.isDigit() }.take(11)
                            cpf = formatCPF(digits)
                        },
                        label = { Text("CPF") },
                        placeholder = { Text("Ex: 123.456.789-00") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                        )
                    )

                    // Data de Nascimento & Idade Row
                    val datePickerDialog = remember {
                        val calendar = java.util.Calendar.getInstance()
                        android.app.DatePickerDialog(
                            context,
                            { _, selectedYear, selectedMonth, selectedDay ->
                                val formattedDate = String.format(java.util.Locale.US, "%02d/%02d/%04d", selectedDay, selectedMonth + 1, selectedYear)
                                birthDate = formattedDate
                                val (calcAge, ageWithTol) = calculateAgeAndTolerance(formattedDate)
                                age = calcAge.toString()
                                category = getCategoryByAge(calcAge)
                            },
                            calendar.get(java.util.Calendar.YEAR) - 18,
                            calendar.get(java.util.Calendar.MONTH),
                            calendar.get(java.util.Calendar.DAY_OF_MONTH)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(modifier = Modifier.weight(1.2f)) {
                            OutlinedTextField(
                                value = birthDate,
                                onValueChange = { input ->
                                    val digits = input.filter { it.isDigit() }
                                    val formatted = when {
                                        digits.length <= 2 -> digits
                                        digits.length <= 4 -> "${digits.substring(0, 2)}/${digits.substring(2)}"
                                        else -> {
                                            val yearPart = digits.substring(4, minOf(digits.length, 8))
                                            "${digits.substring(0, 2)}/${digits.substring(2, 4)}/$yearPart"
                                        }
                                    }
                                    birthDate = formatted
                                },
                                label = { Text("Nascimento (DD/MM/AAAA)") },
                                placeholder = { Text("Ex: 15/08/1997") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                singleLine = true,
                                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                                ),
                                trailingIcon = {
                                    IconButton(onClick = { datePickerDialog.show() }) {
                                        Icon(
                                            imageVector = Icons.Default.CalendarMonth,
                                            contentDescription = "Selecionar data"
                                        )
                                    }
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary
                                )
                            )
                        }

                        OutlinedTextField(
                            value = age,
                            onValueChange = { input ->
                                age = input
                                val ageInt = input.toIntOrNull()
                                if (ageInt != null) {
                                    category = getCategoryByAge(ageInt)
                                }
                            },
                            label = { Text("Idade") },
                            placeholder = { Text("Ex: 28") },
                            modifier = Modifier.weight(0.8f),
                            shape = RoundedCornerShape(16.dp),
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary
                                )
                            )
                        }

                    // Categoria
                    OutlinedTextField(
                        value = category,
                        onValueChange = { category = it },
                        label = { Text("Categoria (Calculada automaticamente)") },
                        placeholder = { Text("Ex: Adulto") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        readOnly = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary
                        )
                    )

                    // Graduação & Progresso Card Section
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Graduação Atual",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 14.sp
                            )

                            // Belt Selection (Dropdown)
                            Box(modifier = Modifier.fillMaxWidth()) {
                                OutlinedTextField(
                                    value = beltRank,
                                    onValueChange = {},
                                    label = { Text("Faixa / Graduação Atual") },
                                    readOnly = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { showBeltDropdown = true },
                                    enabled = false,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                        disabledBorderColor = MaterialTheme.colorScheme.outlineVariant,
                                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                Box(
                                    modifier = Modifier
                                        .matchParentSize()
                                        .clickable { showBeltDropdown = true }
                                )
                                DropdownMenu(
                                    expanded = showBeltDropdown,
                                    onDismissRequest = { showBeltDropdown = false },
                                    modifier = Modifier.fillMaxWidth(0.9f)
                                ) {
                                    beltOptions.forEach { option ->
                                        DropdownMenuItem(
                                            text = { Text(option) },
                                            onClick = {
                                                beltRank = option
                                                showBeltDropdown = false
                                                val currentIndex = beltOptions.indexOf(option)
                                                val autoNext = if (currentIndex != -1 && currentIndex < beltOptions.size - 1) {
                                                    beltOptions[currentIndex + 1]
                                                } else {
                                                    option
                                                }
                                                nextGraduation = autoNext
                                                
                                                // Reset progression when belt is changed!
                                                if (athlete?.beltRank != option) {
                                                    beltProgress = 0f
                                                    nextGraduationProgress = 0f
                                                }
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }



                    // Contact Details Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = phone,
                            onValueChange = { input ->
                                val digits = input.filter { it.isDigit() }
                                if (digits.length <= 11) {
                                    phone = formatPortuguesePhone(digits)
                                }
                            },
                            label = { Text("Telefone") },
                            placeholder = { Text("(11) 98765-4321") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary
                            ),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )

                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Email") },
                            placeholder = { Text("atleta@email.com") },
                            modifier = Modifier.weight(1.2f),
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }

                    // Credenciais de Acesso Section
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("Credenciais de Acesso", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        OutlinedTextField(
                            value = password,
                            onValueChange = { 
                                password = it 
                                passwordChanged = true
                            },
                            label = { Text("Senha de Acesso") },
                            placeholder = { Text("Senha inicial") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary
                            ),
                            singleLine = true
                        )
                        if (athlete == null && password.isNotEmpty() && !passwordChanged) {
                            Text(
                                text = "Senha sugerida: primeiro nome + ano de nascimento",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(start = 4.dp)
                            )
                        }
                    }

                    // Contatos de Segurança Header
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Contatos de Segurança (2 Pessoas)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    }

                    // Emergency Contact 1
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("Contato de Segurança 1", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedTextField(
                                value = emergencyContactName1,
                                onValueChange = { emergencyContactName1 = it },
                                label = { Text("Nome") },
                                placeholder = { Text("Nome do contato 1") },
                                modifier = Modifier.weight(1.2f),
                                shape = RoundedCornerShape(16.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary
                                )
                            )

                            OutlinedTextField(
                                value = emergencyContactPhone1,
                                onValueChange = { input ->
                                    val digits = input.filter { it.isDigit() }
                                    if (digits.length <= 11) {
                                        emergencyContactPhone1 = formatPortuguesePhone(digits)
                                    }
                                },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                label = { Text("Contato/Telefone") },
                                placeholder = { Text("(11) 98765-4321") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(16.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary
                                )
                            )
                        }
                    }

                    // Emergency Contact 2
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("Contato de Segurança 2", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedTextField(
                                value = emergencyContactName2,
                                onValueChange = { emergencyContactName2 = it },
                                label = { Text("Nome") },
                                placeholder = { Text("Nome do contato 2") },
                                modifier = Modifier.weight(1.2f),
                                shape = RoundedCornerShape(16.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary
                                )
                            )

                            OutlinedTextField(
                                value = emergencyContactPhone2,
                                onValueChange = { input ->
                                    val digits = input.filter { it.isDigit() }
                                    if (digits.length <= 11) {
                                        emergencyContactPhone2 = formatPortuguesePhone(digits)
                                    }
                                },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                label = { Text("Contato/Telefone") },
                                placeholder = { Text("(11) 98765-4321") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(16.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary
                                )
                            )
                        }
                    }

                    // Active Toggle Switch
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                RoundedCornerShape(16.dp)
                            )
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Status do Atleta", fontWeight = FontWeight.Bold)
                            Text(
                                text = if (isActive) "Ativo - Acesso Liberado" else "Inativo - Acesso Bloqueado",
                                fontSize = 12.sp,
                                color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = isActive,
                            onCheckedChange = { isActive = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                                checkedTrackColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }

                    // Notes & Medical Observations
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Observações / Notas Médicas") },
                        placeholder = { Text("Adicione observações sobre alimentação, treinos ou restrições de saúde...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        shape = RoundedCornerShape(16.dp),
                        maxLines = 4,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary
                        )
                    )

                    // Action Buttons (Save & Cancel)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (isReadOnly) {
                            Button(
                                onClick = onDismiss,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp),
                                shape = RoundedCornerShape(26.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                )
                            ) {
                                Text("Voltar", fontWeight = FontWeight.Bold)
                            }
                        } else {
                            OutlinedButton(
                                onClick = onDismiss,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(52.dp),
                                shape = RoundedCornerShape(26.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Text("Cancelar", fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = {
                                    if (name.isNotBlank()) {
                                        onSave(
                                            Athlete(
                                                id = athlete?.id ?: 0,
                                                name = name,
                                                beltRank = beltRank,
                                                beltProgress = beltProgress.toInt(),
                                                academyName = academyName,
                                                phone = phone,
                                                email = email,
                                                imageUrl = imageUrl,
                                                isActive = isActive,
                                                trainingHours = trainingHours.toIntOrNull() ?: 0,
                                                streakDays = streakDays.toIntOrNull() ?: 0,
                                                nextGraduation = nextGraduation,
                                                nextGraduationProgress = nextGraduationProgress.toInt(),
                                                notes = notes,
                                                registrationDate = athlete?.registrationDate ?: System.currentTimeMillis(),
                                                cpf = cpf,
                                                birthDate = birthDate,
                                                age = age.toIntOrNull() ?: 0,
                                                category = category,
                                                emergencyContactName1 = emergencyContactName1,
                                                emergencyContactPhone1 = emergencyContactPhone1,
                                                emergencyContactName2 = emergencyContactName2,
                                                emergencyContactPhone2 = emergencyContactPhone2,
                                                password = password,
                                                passwordChanged = passwordChanged
                                            )
                                        )
                                    }
                                },
                                enabled = name.isNotBlank(),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(52.dp),
                                shape = RoundedCornerShape(26.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                )
                            ) {
                                Text("Salvar", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

fun formatPortuguesePhone(digits: String): String {
    if (digits.isEmpty()) return ""
    return buildString {
        append("(")
        append(digits.take(2))
        if (digits.length > 2) {
            append(") ")
            val rest = digits.drop(2)
            if (rest.length > 5) {
                append(rest.take(5))
                append("-")
                append(rest.drop(5))
            } else {
                append(rest)
            }
        }
    }
}

fun getDefaultPasswordForAthlete(name: String, birthDateStr: String): String {
    val firstName = name.trim().split(" ").firstOrNull()?.replaceFirstChar { it.lowercase() } ?: "atleta"
    val year = try {
        val parts = birthDateStr.split("/")
        if (parts.size >= 3) {
            parts[2].trim()
        } else {
            "2026"
        }
    } catch (e: Exception) {
        "2026"
    }
    val rawPassword = "$firstName$year"
    val cleanPassword = rawPassword.filter { it.isLetterOrDigit() }
    return if (cleanPassword.length >= 6) cleanPassword else "${cleanPassword}123"
}
