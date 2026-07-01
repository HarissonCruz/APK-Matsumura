package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import java.io.File
import com.example.data.model.Athlete
import com.example.ui.components.AthleteAvatar
import com.example.ui.viewmodel.GymViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GymAppLayout(viewModel: GymViewModel) {
    val isLoggedIn by viewModel.isLoggedIn.collectAsStateWithLifecycle()
    val userRole by viewModel.userRole.collectAsStateWithLifecycle()
    val userEmail by viewModel.userEmail.collectAsStateWithLifecycle()
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val athletes by viewModel.filteredAthletes.collectAsStateWithLifecycle()
    val selectedAthlete by viewModel.selectedAthlete.collectAsStateWithLifecycle()
    val profViewMode by viewModel.professorViewMode.collectAsStateWithLifecycle()

    val professorName by viewModel.professorName.collectAsStateWithLifecycle()
    val professorPhoto by viewModel.professorPhoto.collectAsStateWithLifecycle()
    var showProfSettings by remember { mutableStateOf(false) }

    val isOwner = remember(userEmail) {
        val email = userEmail.trim().lowercase()
        email == "harissonresende@gmail.com" || email == "harrisonresende@gmail.com"
    }

    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }

    val loggedInAthlete = remember(athletes, userEmail) {
        athletes.find { it.email.trim().lowercase() == userEmail.trim().lowercase() }
    }

    if (isLoggedIn && userRole == "Athlete" && loggedInAthlete != null && !loggedInAthlete.passwordChanged) {
        var newPsw by remember { mutableStateOf("") }
        var confirmNewPsw by remember { mutableStateOf("") }
        var errorMsg by remember { mutableStateOf<String?>(null) }
        var isSavingPsw by remember { mutableStateOf(false) }

        var agreedToTerms by remember { mutableStateOf(false) }
        var showDistPolicyDialog by remember { mutableStateOf(false) }
        var showTermsDialog by remember { mutableStateOf(false) }

        if (showDistPolicyDialog) {
            LegalDocumentDialog(
                title = "Contrato de Distribuição",
                content = DIST_POLICY_TEXT,
                onDismiss = { showDistPolicyDialog = false }
            )
        }

        if (showTermsDialog) {
            LegalDocumentDialog(
                title = "Termos e Condições",
                content = TERMS_CONDITIONS_TEXT,
                onDismiss = { showTermsDialog = false }
            )
        }

        AlertDialog(
            onDismissRequest = { /* Não permite fechar */ },
            title = {
                Text(
                    text = "🔐 Primeiro Acesso: Alterar Senha",
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = "Olá, ${loggedInAthlete.name}! Para garantir a segurança de seus dados, altere a senha padrão antes de prosseguir.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    OutlinedTextField(
                        value = newPsw,
                        onValueChange = { 
                            newPsw = it
                            errorMsg = null
                        },
                        label = { Text("Nova Senha") },
                        placeholder = { Text("Digite sua nova senha") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary
                        )
                    )

                    OutlinedTextField(
                        value = confirmNewPsw,
                        onValueChange = { 
                            confirmNewPsw = it
                            errorMsg = null
                        },
                        label = { Text("Confirmar Nova Senha") },
                        placeholder = { Text("Digite novamente") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary
                        )
                    )

                    // Requisitos de senha dinâmicos
                    PasswordRequirementsList(password = newPsw, email = loggedInAthlete.email)

                    // Checkbox de Concordância com Termos e Políticas
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { agreedToTerms = !agreedToTerms }
                            .padding(vertical = 4.dp)
                    ) {
                        Checkbox(
                            checked = agreedToTerms,
                            onCheckedChange = { agreedToTerms = it }
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Eu concordo com a",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "Política de Distribuição",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.clickable { showDistPolicyDialog = true }
                                )
                                Text(
                                    text = "e os",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Termos e Condições",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.clickable { showTermsDialog = true }
                                )
                            }
                            Text(
                                text = "do Matsumura Connect",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    if (errorMsg != null) {
                        Text(
                            text = errorMsg ?: "",
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val cleanPsw = newPsw.trim()
                        if (!validateAllRules(cleanPsw, loggedInAthlete.email)) {
                            errorMsg = "A senha não atende a todos os requisitos."
                            return@Button
                        }
                        if (cleanPsw != confirmNewPsw.trim()) {
                            errorMsg = "As senhas não coincidem."
                            return@Button
                        }
                        if (!agreedToTerms) {
                            errorMsg = "Você deve aceitar a Política de Distribuição e os Termos e Condições."
                            return@Button
                        }
                        isSavingPsw = true
                        val updated = loggedInAthlete.copy(
                            password = cleanPsw,
                            passwordChanged = true
                        )
                        viewModel.saveAthlete(updated) {
                            isSavingPsw = false
                        }
                    },
                    enabled = !isSavingPsw && newPsw.isNotEmpty() && confirmNewPsw.isNotEmpty() && agreedToTerms && validateAllRules(newPsw.trim(), loggedInAthlete.email) && newPsw.trim() == confirmNewPsw.trim(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isSavingPsw) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Salvar Senha")
                    }
                }
            }
        )
    }

    // Dialog for adding an athlete
    if (showAddDialog) {
        AthleteEditorDialog(
            athlete = null,
            onDismiss = { showAddDialog = false },
            onSave = { newAthlete ->
                viewModel.saveAthlete(newAthlete)
                showAddDialog = false
            }
        )
    }

    // Dialog for editing an athlete
    if (showEditDialog && selectedAthlete != null) {
        AthleteEditorDialog(
            athlete = selectedAthlete,
            isReadOnly = userRole == "Financeiro" && !isOwner,
            onDismiss = {
                showEditDialog = false
                viewModel.selectAthlete(null)
            },
            onSave = { updatedAthlete ->
                viewModel.saveAthlete(updatedAthlete)
                showEditDialog = false
                viewModel.selectAthlete(null)
            }
        )
    }

    if (showProfSettings) {
        var tempName by remember { mutableStateOf(professorName) }
        var tempPhoto by remember { mutableStateOf(professorPhoto) }
        var newPsw by remember { mutableStateOf("") }
        var confirmNewPsw by remember { mutableStateOf("") }
        var errorMsg by remember { mutableStateOf<String?>(null) }
        var successMsg by remember { mutableStateOf<String?>(null) }
        var isSaving by remember { mutableStateOf(false) }

        var agreedToTerms by remember { mutableStateOf(false) }
        var showDistPolicyDialog by remember { mutableStateOf(false) }
        var showTermsDialog by remember { mutableStateOf(false) }

        if (showDistPolicyDialog) {
            LegalDocumentDialog(
                title = "Contrato de Distribuição",
                content = DIST_POLICY_TEXT,
                onDismiss = { showDistPolicyDialog = false }
            )
        }

        if (showTermsDialog) {
            LegalDocumentDialog(
                title = "Termos e Condições",
                content = TERMS_CONDITIONS_TEXT,
                onDismiss = { showTermsDialog = false }
            )
        }

        val context = LocalContext.current
        val imagePickerLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent()
        ) { uri ->
            uri?.let { selectedUri ->
                try {
                    val inputStream = context.contentResolver.openInputStream(selectedUri)
                    val cleanEmail = userEmail.trim().lowercase().replace("@", "_").replace(".", "_")
                    val outputFile = File(context.filesDir, "prof_photo_$cleanEmail.jpg")
                    inputStream?.use { input ->
                        outputFile.outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }
                    tempPhoto = outputFile.absolutePath
                    errorMsg = null
                    successMsg = null
                } catch (e: Exception) {
                    e.printStackTrace()
                    errorMsg = "Erro ao carregar a imagem da galeria."
                }
            }
        }

        val presetAvatars = listOf(
            "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=200&q=80",
            "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&w=200&q=80",
            "https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?auto=format&fit=crop&w=200&q=80",
            "https://images.unsplash.com/photo-1494790108377-be9c29b29330?auto=format&fit=crop&w=200&q=80"
        )

        ModalBottomSheet(
            onDismissRequest = { if (!isSaving) showProfSettings = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 8.dp)
                    .navigationBarsPadding(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Title
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "⚙️ Configurações do Professor",
                        fontWeight = FontWeight.Black,
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    IconButton(
                        onClick = { if (!isSaving) showProfSettings = false },
                        enabled = !isSaving
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Fechar")
                    }
                }

                // Avatar Selection Area
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.secondaryContainer)
                            .clickable { imagePickerLauncher.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        if (tempPhoto.isNotEmpty()) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(tempPhoto)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "Foto de Perfil",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.size(50.dp)
                            )
                        }
                        
                        // Small edit overlay
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                                .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Escolher da Galeria",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = { imagePickerLauncher.launch("image/*") },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Escolher da Galeria", fontSize = 13.sp)
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Ou escolha uma das sugestões abaixo:",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(6.dp))
                    
                    // Row of Presets
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        presetAvatars.forEach { url ->
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color.Gray)
                                    .clickable { tempPhoto = url }
                                    .let {
                                        if (tempPhoto == url) {
                                            it.border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                                        } else it
                                    }
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
                }

                // Input fields
                OutlinedTextField(
                    value = tempName,
                    onValueChange = { 
                        tempName = it
                        errorMsg = null
                        successMsg = null
                    },
                    label = { Text("Nome Completo") },
                    placeholder = { Text("Seu Nome") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                Text(
                    text = "Alterar Senha (Opcional)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.primary
                )

                OutlinedTextField(
                    value = newPsw,
                    onValueChange = { 
                        newPsw = it
                        errorMsg = null
                        successMsg = null
                    },
                    label = { Text("Nova Senha") },
                    placeholder = { Text("Digite para alterar") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                OutlinedTextField(
                    value = confirmNewPsw,
                    onValueChange = { 
                        confirmNewPsw = it
                        errorMsg = null
                        successMsg = null
                    },
                    label = { Text("Confirmar Nova Senha") },
                    placeholder = { Text("Digite novamente") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                if (newPsw.isNotEmpty()) {
                    // Requisitos de senha dinâmicos
                    PasswordRequirementsList(password = newPsw, email = userEmail)

                    // Checkbox de Concordância com Termos e Políticas
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { agreedToTerms = !agreedToTerms }
                            .padding(vertical = 4.dp)
                    ) {
                        Checkbox(
                            checked = agreedToTerms,
                            onCheckedChange = { agreedToTerms = it }
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Eu concordo com a",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "Política de Distribuição",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.clickable { showDistPolicyDialog = true }
                                )
                                Text(
                                    text = "e os",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Termos e Condições",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.clickable { showTermsDialog = true }
                                )
                            }
                            Text(
                                text = "do Matsumura Connect",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                if (errorMsg != null) {
                    Text(
                        text = errorMsg ?: "",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (successMsg != null) {
                    Text(
                        text = successMsg ?: "",
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = { showProfSettings = false },
                        enabled = !isSaving,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Fechar")
                    }

                    Button(
                        onClick = {
                            if (tempName.isBlank()) {
                                errorMsg = "O nome não pode estar em branco."
                                return@Button
                            }
                            if (newPsw.isNotEmpty()) {
                                if (!validateAllRules(newPsw, userEmail)) {
                                    errorMsg = "A nova senha não atende a todos os requisitos."
                                    return@Button
                                }
                                if (newPsw != confirmNewPsw) {
                                    errorMsg = "As senhas não coincidem."
                                    return@Button
                                }
                                if (!agreedToTerms) {
                                    errorMsg = "Você deve aceitar a Política de Distribuição e os Termos e Condições."
                                    return@Button
                                }
                            }
                            
                            isSaving = true
                            viewModel.updateProfessorProfile(
                                name = tempName,
                                photoUrl = tempPhoto,
                                newPassword = newPsw.ifEmpty { null }
                            ) { success, message ->
                                isSaving = false
                                if (success) {
                                    successMsg = message
                                    errorMsg = null
                                    newPsw = ""
                                    confirmNewPsw = ""
                                    agreedToTerms = false
                                } else {
                                    errorMsg = message
                                    successMsg = null
                                }
                            }
                        },
                        enabled = !isSaving && (newPsw.isEmpty() || (confirmNewPsw.isNotEmpty() && agreedToTerms && validateAllRules(newPsw, userEmail) && newPsw == confirmNewPsw)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Salvar")
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Auth gating: If not logged in, show Login Screen
    if (!isLoggedIn) {
        LoginScreen(viewModel = viewModel)
        return
    }

    // Gated layout depending on userRole
    if ((userRole == "Professor" || userRole == "Financeiro") && profViewMode == "Professor") {
        // Professor Workspace Layout
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            floatingActionButton = {
                val activeProfTab by viewModel.professorActiveTab.collectAsStateWithLifecycle()
                val isOwner = remember(userEmail) {
                    val email = userEmail.trim().lowercase()
                    email == "harissonresende@gmail.com" || email == "harrisonresende@gmail.com"
                }
                val isFinanceiro = userRole == "Financeiro"
                val canAddAthlete = isOwner || isFinanceiro
                if (activeProfTab == "Atletas" && canAddAthlete) {
                    FloatingActionButton(
                        onClick = { showAddDialog = true },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.White,
                        shape = CircleShape,
                        modifier = Modifier.testTag("add_athlete_fab")
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Adicionar Atleta")
                    }
                }
            },
            topBar = {
                TopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.clickable { showProfSettings = true }
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.secondary),
                                contentAlignment = Alignment.Center
                            ) {
                                if (professorPhoto.isNotEmpty()) {
                                    AsyncImage(
                                        model = ImageRequest.Builder(LocalContext.current)
                                            .data(professorPhoto)
                                            .crossfade(true)
                                            .build(),
                                        contentDescription = "Foto de Perfil",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.School,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Column {
                                Text(
                                    text = professorName,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.onBackground,
                                    fontSize = 16.sp,
                                    letterSpacing = (-0.5).sp
                                )
                                Text(
                                    text = userEmail,
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    },
                    actions = {
                        // Settings Button to edit profile
                        IconButton(onClick = { showProfSettings = true }) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Configurações de Perfil",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        // Switch View Mode Button
                        IconButton(onClick = { viewModel.setProfessorViewMode("Athlete") }) {
                            Icon(
                                imageVector = Icons.Default.SwapHoriz,
                                contentDescription = "Ver como Atleta",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        // Logout Button
                        IconButton(onClick = { viewModel.logout() }) {
                            Icon(
                                imageVector = Icons.Default.ExitToApp,
                                contentDescription = "Sair",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
            },
            bottomBar = {
                val activeProfTab by viewModel.professorActiveTab.collectAsStateWithLifecycle()
                val isOwner = remember(userEmail) {
                    val email = userEmail.trim().lowercase()
                    email == "harissonresende@gmail.com" || email == "harrisonresende@gmail.com"
                }
                val isFinanceiro = userRole == "Financeiro"
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.background,
                    tonalElevation = 8.dp,
                    modifier = Modifier
                        .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                        .windowInsetsPadding(WindowInsets.navigationBars)
                ) {
                    val tabs = remember(isOwner, isFinanceiro) {
                        if (isOwner || isFinanceiro) {
                            listOf(
                                Triple("Atletas", "Atletas", Icons.Default.Person),
                                Triple("Geral", "Geral", Icons.Default.Groups),
                                Triple("Frequencia", "Frequência", Icons.Default.CalendarMonth),
                                Triple("Financeiro", "Financeiro", Icons.Default.Payments),
                                Triple("Equipe", "Equipe", Icons.Default.Security)
                            )
                        } else {
                            listOf(
                                Triple("Atletas", "Atletas", Icons.Default.Person),
                                Triple("Geral", "Geral", Icons.Default.Groups),
                                Triple("Frequencia", "Frequência", Icons.Default.CalendarMonth)
                            )
                        }
                    }
                    tabs.forEach { (tabId, label, icon) ->
                        NavigationBarItem(
                            selected = activeProfTab == tabId,
                            onClick = { viewModel.setProfessorActiveTab(tabId) },
                            icon = {
                                val planRequests by viewModel.planRequests.collectAsStateWithLifecycle()
                                Box {
                                    Icon(imageVector = icon, contentDescription = label)
                                    if (tabId == "Financeiro" && planRequests.isNotEmpty() && (isOwner || isFinanceiro)) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .background(Color.Red, CircleShape)
                                                .align(Alignment.TopEnd)
                                        )
                                    }
                                }
                            },
                            label = { Text(label, fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            ),
                            modifier = Modifier.testTag("prof_nav_${tabId.lowercase()}_button")
                        )
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                ProfessorDashboardScreen(
                    viewModel = viewModel,
                    onEditAthlete = { athlete ->
                        viewModel.selectAthlete(athlete)
                        showEditDialog = true
                    }
                )
            }
        }
    } else {
        // Normal Athlete View
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                TopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            val firstAthlete = athletes.firstOrNull()
                            AthleteAvatar(
                                imageUrl = firstAthlete?.imageUrl,
                                name = firstAthlete?.name ?: "Matsumura Team",
                                modifier = Modifier.size(38.dp)
                            )
                            Column {
                                Text(
                                    text = "Matsumura Team",
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontSize = 20.sp,
                                    letterSpacing = (-0.5).sp
                                )
                                Text(
                                    text = if (userRole == "Professor") "Prof. (Modo Atleta)" else "Atleta: $userEmail",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    },
                    actions = {
                        if (userRole == "Professor" || userRole == "Financeiro" || isOwner) {
                            IconButton(onClick = { viewModel.setProfessorViewMode("Professor") }) {
                                Icon(
                                    imageVector = Icons.Default.SwapHoriz,
                                    contentDescription = "Ver como Professor",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        IconButton(onClick = {}) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Notificações",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        IconButton(onClick = { viewModel.logout() }) {
                            Icon(
                                imageVector = Icons.Default.ExitToApp,
                                contentDescription = "Sair",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
            },
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.background,
                    tonalElevation = 8.dp,
                    modifier = Modifier
                        .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                        .windowInsetsPadding(WindowInsets.navigationBars)
                ) {
                    // Home Tab
                    NavigationBarItem(
                        selected = currentTab == "Home",
                        onClick = { viewModel.setTab("Home") },
                        icon = { Icon(imageVector = Icons.Default.Home, contentDescription = "Home") },
                        label = { Text("Início", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        ),
                        modifier = Modifier.testTag("nav_home_button")
                    )

                    // Graduation Tab
                    NavigationBarItem(
                        selected = currentTab == "Belts",
                        onClick = { viewModel.setTab("Belts") },
                        icon = { Icon(imageVector = Icons.Default.Star, contentDescription = "Graduação") },
                        label = { Text("Graduação", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        ),
                        modifier = Modifier.testTag("nav_belts_button")
                    )

                    // Wallet Tab
                    NavigationBarItem(
                        selected = currentTab == "Wallet",
                        onClick = { viewModel.setTab("Wallet") },
                        icon = { Icon(imageVector = Icons.Default.ShoppingCart, contentDescription = "Financeiro") },
                        label = { Text("Painel", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        ),
                        modifier = Modifier.testTag("nav_wallet_button")
                    )

                    // Settings Tab
                    NavigationBarItem(
                        selected = currentTab == "Settings",
                        onClick = { viewModel.setTab("Settings") },
                        icon = { Icon(imageVector = Icons.Default.Settings, contentDescription = "Ajustes") },
                        label = { Text("Ajustes", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        ),
                        modifier = Modifier.testTag("nav_settings_button")
                    )
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (currentTab) {
                    "Home" -> HomeScreen(
                        viewModel = viewModel,
                        athletes = athletes,
                        onNavigateToAthletes = { viewModel.setTab("Belts") }
                    )
                    "Belts" -> BeltsScreen(viewModel = viewModel, athletes = athletes)
                    "Wallet" -> WalletScreen(viewModel = viewModel, athletes = athletes)
                    "Settings" -> SettingsScreen(viewModel = viewModel, athletes = athletes)
                }
            }
        }
    }
}
