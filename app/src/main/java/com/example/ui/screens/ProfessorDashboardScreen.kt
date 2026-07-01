package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.Athlete
import com.example.ui.components.AthleteAvatar
import com.example.ui.viewmodel.GymViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfessorDashboardScreen(
    viewModel: GymViewModel,
    onEditAthlete: (Athlete) -> Unit
) {
    val athletes by viewModel.filteredAthletes.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val isSyncing by viewModel.isSyncing.collectAsStateWithLifecycle()
    val isSupabaseOnline by viewModel.isSupabaseOnline.collectAsStateWithLifecycle()
    val planRequests by viewModel.planRequests.collectAsStateWithLifecycle()

    val activeSubTab by viewModel.professorActiveTab.collectAsStateWithLifecycle()
    val userEmail by viewModel.userEmail.collectAsStateWithLifecycle()
    val userRole by viewModel.userRole.collectAsStateWithLifecycle()
    val isOwner = remember(userEmail) {
        val email = userEmail.trim().lowercase()
        email == "harissonresende@gmail.com" || email == "harrisonresende@gmail.com"
    }
    val isFinanceiro = userRole == "Financeiro"
    var financeFilter by remember { mutableStateOf("Todos") }

    var showPaymentDialogForAthlete by remember { mutableStateOf<Athlete?>(null) }
    var showContractDialogForAthlete by remember { mutableStateOf<Athlete?>(null) }

    // Date formatter for recording attendance
    val todayDate = remember { SimpleDateFormat("dd/MM", Locale.getDefault()).format(Date()) }

    // Analytics Metrics
    val totalStudents = athletes.size
    val activeStudents = athletes.count { it.isActive }
    val pendingPayments = athletes.count { it.paymentStatus == "Pendente" || it.paymentStatus == "Atrasado" }
    val totalRevenue = athletes.filter { it.paymentStatus == "Pago" }.sumOf { it.paymentValue }
    val totalProjectedRevenue = athletes.sumOf { it.paymentValue }

    if (showPaymentDialogForAthlete != null) {
        val athlete = showPaymentDialogForAthlete!!
        var paymentStatus by remember { mutableStateOf(athlete.paymentStatus) }
        var paymentValueText by remember { mutableStateOf(athlete.paymentValue.toString()) }
        var lastPaymentDate by remember { mutableStateOf(athlete.lastPaymentDate) }

        AlertDialog(
            onDismissRequest = { showPaymentDialogForAthlete = null },
            title = { Text("Lançar Pagamento - ${athlete.name}", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Defina o valor e status da mensalidade do aluno.", fontSize = 13.sp)

                    // Status buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Pago", "Pendente", "Atrasado").forEach { status ->
                            val isSelected = paymentStatus == status
                            val color = when (status) {
                                "Pago" -> Color(0xFF4CAF50)
                                "Pendente" -> Color(0xFFFF9800)
                                else -> Color(0xFFF44336)
                            }
                            Button(
                                onClick = { paymentStatus = status },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isSelected) color else MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                contentPadding = PaddingValues(vertical = 8.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(status, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    OutlinedTextField(
                        value = paymentValueText,
                        onValueChange = { paymentValueText = it },
                        label = { Text("Valor do Plano (R$)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = lastPaymentDate,
                        onValueChange = { lastPaymentDate = it },
                        label = { Text("Data de Referência / Vencimento") },
                        placeholder = { Text("dd/mm/aaaa") },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val parsedVal = paymentValueText.toDoubleOrNull() ?: 189.00
                        viewModel.updateAthletePayment(athlete, paymentStatus, parsedVal, lastPaymentDate)
                        showPaymentDialogForAthlete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Salvar", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showPaymentDialogForAthlete = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (showContractDialogForAthlete != null) {
        val athlete = showContractDialogForAthlete!!
        AlertDialog(
            onDismissRequest = { showContractDialogForAthlete = null },
            title = { Text("Contrato de Adesão", fontWeight = FontWeight.Bold) },
            text = {
                Box(modifier = Modifier.fillMaxWidth().heightIn(max = 450.dp)) {
                    ContractDetailView(
                        athlete = athlete,
                        onSign = { signatureName ->
                            viewModel.signContract(athlete, signatureName)
                            showContractDialogForAthlete = null
                        }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showContractDialogForAthlete = null },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Fechar")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Professor Summary Top Panel
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "PAINEL DO PROFESSOR",
                            fontWeight = FontWeight.Black,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.secondary,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Gestão Geral de Alunos",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }

                    // Sync button with Supabase status
                    IconButton(
                        onClick = { viewModel.syncWithSupabase() },
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.6f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sync,
                            contentDescription = "Sincronizar",
                            tint = if (isSyncing) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Status Connection Banner
                val supabaseStatus by viewModel.supabaseConnectionStatus.collectAsStateWithLifecycle()
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSupabaseOnline) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, if (isSupabaseOnline) Color(0xFFC8E6C9) else Color(0xFFFFCDD2))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = if (isSupabaseOnline) Icons.Default.CheckCircle else Icons.Default.Warning,
                            contentDescription = null,
                            tint = if (isSupabaseOnline) Color(0xFF2E7D32) else Color(0xFFC62828),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = supabaseStatus,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSupabaseOnline) Color(0xFF2E7D32) else Color(0xFFC62828),
                            modifier = Modifier.weight(1f)
                        )
                        if (!isSupabaseOnline) {
                            Text(
                                text = "TESTAR",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFFC62828),
                                modifier = Modifier.clickable { viewModel.syncWithSupabase() }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Analytics KPI row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // KPI: Students
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Total Alunos", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("$totalStudents", fontSize = 22.sp, fontWeight = FontWeight.Black)
                            Text("$activeStudents ativos", fontSize = 10.sp, color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold)
                        }
                    }

                    // KPI: Pending payments
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Inadimplentes", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("$pendingPayments", fontSize = 22.sp, fontWeight = FontWeight.Black, color = if (pendingPayments > 0) Color(0xFFF44336) else MaterialTheme.colorScheme.onSurface)
                            Text("Aguardando pag.", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    // KPI: Revenue
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Mensalidades", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(4.dp))
                            if (isOwner || isFinanceiro) {
                                Text("R$ ${totalRevenue.toInt()}", fontSize = 18.sp, fontWeight = FontWeight.Black, color = Color(0xFF4CAF50))
                                Text("Proj: R$ ${totalProjectedRevenue.toInt()}", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            } else {
                                Text("R$ ---", fontSize = 18.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("Acesso Restrito", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (activeSubTab == "Equipe") {
            TeamManagementPanel(viewModel = viewModel)
        } else if (activeSubTab == "Atletas") {
            AthletesScreen(
                viewModel = viewModel,
                athletes = athletes,
                onAddAthlete = {}, // Handled by outer Scaffold FAB
                onEditAthlete = onEditAthlete
            )
        } else if (activeSubTab == "Frequencia") {
            ClassPlanningAndRollCallScreen(viewModel = viewModel, athletes = athletes)
        } else {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                placeholder = { Text("Pesquisar aluno ou academia...", fontSize = 14.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setSearchQuery("") }) {
                            Icon(Icons.Default.Close, contentDescription = "Limpar")
                        }
                    }
                },
                shape = RoundedCornerShape(20.dp),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            )

            val filteredAthletesForTab = remember(athletes, activeSubTab, financeFilter) {
                if (activeSubTab == "Financeiro") {
                    when (financeFilter) {
                        "Pago" -> athletes.filter { it.paymentStatus == "Pago" }
                        "Pendente" -> athletes.filter { it.paymentStatus == "Pendente" }
                        "Atrasado" -> athletes.filter { it.paymentStatus == "Atrasado" }
                        else -> athletes
                    }
                } else {
                    athletes
                }
            }

            // List of Athletes
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (activeSubTab == "Financeiro" && (isOwner || isFinanceiro)) {
                    item {
                        FinanceDashboardPanel(
                            totalRevenue = totalRevenue,
                            totalProjected = totalProjectedRevenue,
                            activeCount = activeStudents,
                            inactiveCount = athletes.count { !it.isActive },
                            pendingCount = pendingPayments
                        )
                    }
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Filtrar por Status de Pagamento",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                listOf(
                                    Triple("Todos", "Todos", Icons.Default.Groups),
                                    Triple("Pago", "Pago", Icons.Default.CheckCircle),
                                    Triple("Pendente", "Pendente", Icons.Default.Info),
                                    Triple("Atrasado", "Atrasado", Icons.Default.Cancel)
                                ).forEach { (status, label, icon) ->
                                    val isSelected = financeFilter == status
                                    val color = when (status) {
                                        "Pago" -> Color(0xFF4CAF50)
                                        "Pendente" -> Color(0xFFFF9800)
                                        "Atrasado" -> Color(0xFFF44336)
                                        else -> MaterialTheme.colorScheme.primary
                                    }
                                    Card(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable { financeFilter = status },
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isSelected) color.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface
                                        ),
                                        border = BorderStroke(
                                            width = 1.dp,
                                            color = if (isSelected) color else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                        )
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(
                                                imageVector = icon,
                                                contentDescription = null,
                                                tint = if (isSelected) color else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Text(
                                                text = label,
                                                color = if (isSelected) color else MaterialTheme.colorScheme.onSurfaceVariant,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    if (planRequests.isNotEmpty()) {
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 12.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    text = "Solicitações de Mudança de Plano (${planRequests.size})",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )
                                planRequests.forEach { req ->
                                    Card(
                                        modifier = Modifier.fillMaxWidth().testTag("plan_request_card_${req.athleteId}"),
                                        shape = RoundedCornerShape(16.dp),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)),
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                                    ) {
                                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column {
                                                    Text(req.athleteName, fontWeight = FontWeight.Black, fontSize = 15.sp)
                                                    Text("Data do pedido: ${req.requestDate}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                }
                                                Box(
                                                    modifier = Modifier
                                                        .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(8.dp))
                                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                                ) {
                                                    Text("MUDANÇA", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                            
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(req.currentPlan, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                                                Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                                Text(req.requestedPlan, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary, fontSize = 13.sp)
                                            }

                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.End,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                val newPrice = when (req.requestedPlan) {
                                                    "Plano Anual" -> 110.00
                                                    "Plano Semestral" -> 125.00
                                                    "Plano Trimestral" -> 140.00
                                                    else -> 160.00
                                                }
                                                TextButton(
                                                    onClick = { viewModel.resolvePlanRequest(req.athleteId, approve = false, newPrice = 0.0) }
                                                ) {
                                                    Text("Recusar", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                                                }
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Button(
                                                    onClick = { viewModel.resolvePlanRequest(req.athleteId, approve = true, newPrice = newPrice) },
                                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                                    shape = RoundedCornerShape(8.dp),
                                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                                    modifier = Modifier.height(34.dp)
                                                ) {
                                                    Text("Aprovar", fontWeight = FontWeight.Bold, color = Color.White)
                                                }
                                            }
                                        }
                                    }
                                }
                                Divider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                            }
                        }
                    }
                }

                items(filteredAthletesForTab, key = { it.id }) { athlete ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            // Athlete Header Row
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                AthleteAvatar(
                                    imageUrl = athlete.imageUrl,
                                    name = athlete.name,
                                    modifier = Modifier.size(50.dp)
                                )

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            text = athlete.name,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        // Status tag
                                        Box(
                                            modifier = Modifier
                                                .background(
                                                    if (athlete.isActive) Color(0xFFE8F5E9) else Color(0xFFFFEBEE),
                                                    RoundedCornerShape(6.dp)
                                                )
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = if (athlete.isActive) "ATIVO" else "INATIVO",
                                                color = if (athlete.isActive) Color(0xFF4CAF50) else Color(0xFFF44336),
                                                fontSize = 8.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                    Text(
                                        text = "${athlete.beltRank} • ${athlete.academyName}",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (isOwner) {
                                        OutlinedButton(
                                            onClick = { showContractDialogForAthlete = athlete },
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier.height(32.dp),
                                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary),
                                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Description,
                                                contentDescription = "Contrato",
                                                modifier = Modifier.size(12.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Contrato", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }

                                    // Edit Actions popup triggers
                                    Button(
                                        onClick = { onEditAthlete(athlete) },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                        ),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.height(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "Editar Cadastro",
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Editar", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            // Tab specific sub-content
                            when (activeSubTab) {
                                "Geral" -> {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(Icons.Default.Phone, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(14.dp))
                                            Text(athlete.phone.ifEmpty { "Sem telefone" }, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        }

                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(Icons.Default.Email, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(14.dp))
                                            Text(athlete.email.ifEmpty { "Sem e-mail" }, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        }
                                    }

                                    if (athlete.cpf.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(Icons.Default.Fingerprint, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                                            Text("CPF: ${athlete.cpf}", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }

                                    if (athlete.birthDate.isNotEmpty() || athlete.age > 0 || athlete.category.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                            if (athlete.birthDate.isNotEmpty() || athlete.age > 0) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                ) {
                                                    Icon(Icons.Default.Cake, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                                                     val ageStr = if (athlete.age > 0) " (${athlete.age} anos)" else ""
                                                    Text("Nasc.: ${athlete.birthDate}$ageStr", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                }
                                            }
                                            if (athlete.category.isNotEmpty()) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                ) {
                                                     Icon(Icons.Default.Category, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                                                    Text("Categoria: ${athlete.category}", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                }
                                            }
                                        }
                                    }

                                    if (athlete.emergencyContactName1.isNotEmpty() || athlete.emergencyContactName2.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text("Contatos de Emergência:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                                        
                                        if (athlete.emergencyContactName1.isNotEmpty()) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth().padding(top = 2.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text("1. ${athlete.emergencyContactName1}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                Text(athlete.emergencyContactPhone1, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                            }
                                        }
                                        if (athlete.emergencyContactName2.isNotEmpty()) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth().padding(top = 2.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text("2. ${athlete.emergencyContactName2}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                Text(athlete.emergencyContactPhone2, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                            }
                                        }
                                    }
                                }

                                "Frequencia" -> {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                                    Spacer(modifier = Modifier.height(10.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text("Frequência Mensal", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Text(
                                                text = "${athlete.trainingHours} presenças",
                                                fontWeight = FontWeight.Black,
                                                fontSize = 14.sp,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            // Attendance history list display
                                            Text(
                                                text = if (athlete.attendanceHistory.isBlank()) "Sem registros recentes" else "Datas: ${athlete.attendanceHistory}",
                                                fontSize = 10.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }

                                        // Mark presence quick action
                                        val hasAttendedToday = athlete.attendanceHistory.contains(todayDate)
                                        Button(
                                            onClick = { viewModel.toggleAthleteAttendance(athlete, todayDate) },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (hasAttendedToday) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.primary,
                                                contentColor = if (hasAttendedToday) MaterialTheme.colorScheme.onSurfaceVariant else Color.White
                                            ),
                                            shape = RoundedCornerShape(12.dp),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                        ) {
                                            Icon(
                                                imageVector = if (hasAttendedToday) Icons.Default.CheckCircle else Icons.Default.AddCircle,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(if (hasAttendedToday) "Presença Hoje" else "Lançar Freq", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }

                                "Financeiro" -> {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                                    Spacer(modifier = Modifier.height(10.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text("Status Financeiro", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                val badgeColor = when (athlete.paymentStatus) {
                                                    "Pago" -> Color(0xFFE8F5E8)
                                                    "Pendente" -> Color(0xFFFFF3E0)
                                                    else -> Color(0xFFFFEBEE)
                                                }
                                                val textColor = when (athlete.paymentStatus) {
                                                    "Pago" -> Color(0xFF4CAF50)
                                                    "Pendente" -> Color(0xFFFF9800)
                                                    else -> Color(0xFFF44336)
                                                }
                                                Box(
                                                    modifier = Modifier
                                                        .background(badgeColor, RoundedCornerShape(6.dp))
                                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                                ) {
                                                    Text(
                                                        athlete.paymentStatus.uppercase(),
                                                        color = textColor,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 9.sp
                                                    )
                                                }
                                                Text(
                                                    text = if (isOwner || isFinanceiro) "R$ ${athlete.paymentValue}" else "R$ ***",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.sp
                                                )
                                            }
                                            Text(
                                                text = "Ref/Venc: ${athlete.lastPaymentDate}",
                                                fontSize = 10.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }

                                        // Launch payment & Cancel action (Owner or Financeiro)
                                        if (isOwner || isFinanceiro) {
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                OutlinedButton(
                                                    onClick = { showPaymentDialogForAthlete = athlete },
                                                    shape = RoundedCornerShape(12.dp),
                                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                                                ) {
                                                    Icon(Icons.Default.Payments, contentDescription = null, modifier = Modifier.size(14.dp))
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text("Financeiro", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                                }

                                                if (athlete.isActive) {
                                                    Button(
                                                        onClick = {
                                                            val updated = athlete.copy(isActive = false)
                                                            viewModel.saveAthlete(updated)
                                                        },
                                                        shape = RoundedCornerShape(12.dp),
                                                        colors = ButtonDefaults.buttonColors(
                                                            containerColor = MaterialTheme.colorScheme.errorContainer,
                                                            contentColor = MaterialTheme.colorScheme.error
                                                        ),
                                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                                                    ) {
                                                        Icon(Icons.Default.Cancel, contentDescription = null, modifier = Modifier.size(14.dp))
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Text("Cancelar Aluno", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                                    }
                                                } else {
                                                    Button(
                                                        onClick = {
                                                            val updated = athlete.copy(isActive = true)
                                                            viewModel.saveAthlete(updated)
                                                        },
                                                        shape = RoundedCornerShape(12.dp),
                                                        colors = ButtonDefaults.buttonColors(
                                                            containerColor = Color(0xFFE8F5E9),
                                                            contentColor = Color(0xFF2E7D32)
                                                        ),
                                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                                                    ) {
                                                        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(14.dp))
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Text("Ativar Aluno", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                                    }
                                                }
                                            }
                                        } else {
                                            Box(
                                                modifier = Modifier
                                                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Lock,
                                                        contentDescription = null,
                                                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                    Text(
                                                        text = "Apenas Dono",
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
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
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamManagementPanel(viewModel: GymViewModel) {
    val authorizedProfessors by viewModel.authorizedProfessors.collectAsStateWithLifecycle()
    var newProfessorEmail by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Gerenciar Professores Habilitados",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Adicione e-mails de outros professores que terão acesso irrestrito ao Portal do Professor. Atletas não habilitados serão restritos à visão de aluno.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = newProfessorEmail,
                onValueChange = { 
                    newProfessorEmail = it
                    errorMessage = null
                    successMessage = null
                },
                placeholder = { Text("email@matsumura.com") },
                label = { Text("E-mail do Professor") },
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                modifier = Modifier.weight(1f)
            )

            Button(
                onClick = {
                    val email = newProfessorEmail.trim().lowercase()
                    if (email.isEmpty()) {
                        errorMessage = "O e-mail não pode estar vazio."
                    } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                        errorMessage = "E-mail inválido."
                    } else if (authorizedProfessors.contains(email)) {
                        errorMessage = "Este professor já está habilitado."
                    } else {
                        viewModel.authorizeProfessor(email)
                        successMessage = "Professor adicionado com sucesso!"
                        newProfessorEmail = ""
                    }
                },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.height(56.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Adicionar")
                Spacer(modifier = Modifier.width(4.dp))
                Text("Habilitar")
            }
        }

        errorMessage?.let {
            Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
        }

        successMessage?.let {
            Text(it, color = Color(0xFF4CAF50), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
        }

        Text(
            text = "Professores Autorizados (${authorizedProfessors.size})",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 8.dp)
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth().weight(1f)
        ) {
            items(authorizedProfessors.toList()) { email ->
                val isOwner = email == "harissonresende@gmail.com" || email == "harrisonresende@gmail.com"
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(if (isOwner) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isOwner) Icons.Default.Star else Icons.Default.School,
                                    contentDescription = null,
                                    tint = if (isOwner) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = email,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = if (isOwner) "Proprietário / Dono" else "Professor Habilitado",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        if (!isOwner) {
                            IconButton(
                                onClick = { 
                                    viewModel.removeProfessor(email)
                                    successMessage = "Acesso removido com sucesso."
                                    errorMessage = null
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Remover Professor",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ClassPlanningAndRollCallScreen(viewModel: GymViewModel, athletes: List<Athlete>) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val sharedPrefs = remember(context) { context.getSharedPreferences("gym_class_plans", android.content.Context.MODE_PRIVATE) }
    
    val todayDate = remember { SimpleDateFormat("dd/MM", Locale.getDefault()).format(Date()) }
    val timesList = listOf("08:00", "12:00", "18:00", "19:30", "21:00")
    var selectedTime by remember { mutableStateOf("19:30") }
    var showCustomTimeDialog by remember { mutableStateOf(false) }
    var customTimeText by remember { mutableStateOf("") }
    val schedules = remember(customTimeText) {
        if (customTimeText.isNotBlank() && !timesList.contains(customTimeText)) {
            timesList + customTimeText
        } else {
            timesList
        }
    }
    
    var activeTab by remember { mutableStateOf("Chamada") } // "Chamada" or "Planejamento"
    
    // Class Plan form states
    var classTema by remember(selectedTime) { 
        mutableStateOf(sharedPrefs.getString("tema_${todayDate}_$selectedTime", "") ?: "") 
    }
    var classAquecimento by remember(selectedTime) { 
        mutableStateOf(sharedPrefs.getString("aquecimento_${todayDate}_$selectedTime", "") ?: "") 
    }
    var classTecnica by remember(selectedTime) { 
        mutableStateOf(sharedPrefs.getString("tecnica_${todayDate}_$selectedTime", "") ?: "") 
    }
    var classRola by remember(selectedTime) { 
        mutableStateOf(sharedPrefs.getString("rola_${todayDate}_$selectedTime", "") ?: "") 
    }
    
    var showSavedMessage by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Upper Card Header
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "Frequência & Planejamento",
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Badge(
                        containerColor = MaterialTheme.colorScheme.secondary,
                        contentColor = Color.White,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    ) {
                        Text("Hoje: $todayDate", modifier = Modifier.padding(4.dp), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }
                
                Text(
                    text = "Selecione o horário da aula para planejar as atividades e realizar a chamada rápida dos atletas.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        // Schedule Selection row of chips
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = "⏰ Horário da Aula",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 4.dp)
            )
            
            androidx.compose.foundation.lazy.LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                items(schedules) { time ->
                    val isSelected = selectedTime == time
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                            .border(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(14.dp))
                            .clickable { 
                                selectedTime = time 
                                showSavedMessage = false
                            }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = time,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                item {
                    IconButton(
                        onClick = { showCustomTimeDialog = true },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Adicionar Horário", modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
        
        // Two Tab Switcher: Chamada or Planejamento
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                .padding(4.dp)
        ) {
            listOf("Chamada", "Planejamento").forEach { section ->
                val isSelected = activeTab == section
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                        .clickable { activeTab = section }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (section == "Chamada") "📝 Fazer Chamada" else "🥋 Esquema de Aula",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        
        // Content based on active tab
        if (activeTab == "Chamada") {
            // Chamada Section
            var showAddAthleteDialog by remember { mutableStateOf(false) }
            val expectedPresencePattern = "$todayDate ($selectedTime)"
            var attendanceViewMode by remember { mutableStateOf("Todos") } // "Todos" or "Agendados"
            
            val scheduledAthletes = remember(athletes, selectedTime) {
                athletes.filter { it.bookedClasses.contains(expectedPresencePattern) }
            }

            val listToDisplay = remember(athletes, selectedTime, attendanceViewMode) {
                if (attendanceViewMode == "Agendados") {
                    scheduledAthletes
                } else {
                    athletes.filter { it.isActive || it.bookedClasses.contains(expectedPresencePattern) }
                }
            }
            
            val presentCount = listToDisplay.count { it.attendanceHistory.contains(expectedPresencePattern) }

            // View Mode selection Segmented Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                listOf("Todos" to "Todos os Alunos", "Agendados" to "Apenas Agendados").forEach { (mode, label) ->
                    val isSelected = attendanceViewMode == mode
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                            .clickable { attendanceViewMode = mode }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (attendanceViewMode == "Agendados") "Agendados para esta Aula (${scheduledAthletes.size})" else "Alunos Ativos (${listToDisplay.size})",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "$presentCount presentes de ${listToDisplay.size}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
            
            // Inclusion Button for athletes without scheduling
            if (attendanceViewMode == "Agendados") {
                Button(
                    onClick = { showAddAthleteDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary,
                        contentColor = Color.White
                    )
                ) {
                    Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Incluir Atleta Sem Agendamento", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
            
            var rollCallSearchQuery by remember { mutableStateOf("") }
            
            // Inline Search for Roll Call
            OutlinedTextField(
                value = rollCallSearchQuery,
                onValueChange = { rollCallSearchQuery = it },
                placeholder = { Text("Pesquisar atleta...", fontSize = 13.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp)) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                )
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = {
                        viewModel.markAllPresent(listToDisplay, expectedPresencePattern)
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Marcar Todos", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = {
                        viewModel.clearAllAttendance(listToDisplay, expectedPresencePattern)
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.8f),
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    ),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    Icon(Icons.Default.RemoveCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Limpar Todos", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
            
            val filteredForRollCall = remember(listToDisplay, rollCallSearchQuery) {
                if (rollCallSearchQuery.isBlank()) {
                    listToDisplay
                } else {
                    listToDisplay.filter { it.name.contains(rollCallSearchQuery, ignoreCase = true) || it.nickname.contains(rollCallSearchQuery, ignoreCase = true) }
                }
            }
            
            // Manual Add Athlete Dialog
            if (showAddAthleteDialog) {
                var dialogSearchQuery by remember { mutableStateOf("") }
                val eligibleAthletes = remember(athletes, scheduledAthletes, dialogSearchQuery) {
                    val notScheduled = athletes.filter { !scheduledAthletes.any { scheduled -> scheduled.id == it.id } }
                    if (dialogSearchQuery.isBlank()) {
                        notScheduled
                    } else {
                        notScheduled.filter { it.name.contains(dialogSearchQuery, ignoreCase = true) || it.nickname.contains(dialogSearchQuery, ignoreCase = true) }
                    }
                }
                
                AlertDialog(
                    onDismissRequest = { showAddAthleteDialog = false },
                    title = {
                        Text(
                            text = "Incluir Atleta na Aula das $selectedTime",
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp
                        )
                    },
                    text = {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth().heightIn(max = 350.dp)
                        ) {
                            Text(
                                text = "Selecione um atleta registrado para incluí-lo nesta aula e confirmar sua presença imediatamente.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            
                            OutlinedTextField(
                                value = dialogSearchQuery,
                                onValueChange = { dialogSearchQuery = it },
                                placeholder = { Text("Pesquisar atleta...") },
                                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp)) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp)
                            )
                            
                            if (eligibleAthletes.isEmpty()) {
                                Box(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("Nenhum atleta disponível.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                                }
                            } else {
                                LazyColumn(
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.weight(1f).fillMaxWidth()
                                ) {
                                    items(eligibleAthletes) { athlete ->
                                        Card(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    viewModel.addAthleteToClass(athlete, todayDate, selectedTime)
                                                    showAddAthleteDialog = false
                                                },
                                            shape = RoundedCornerShape(12.dp),
                                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(12.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                                            ) {
                                                AthleteAvatar(
                                                    imageUrl = athlete.imageUrl,
                                                    name = athlete.name,
                                                    modifier = Modifier.size(36.dp)
                                                )
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = athlete.name,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 13.sp
                                                    )
                                                    Text(
                                                        text = athlete.beltRank.split("—").firstOrNull()?.trim() ?: athlete.beltRank,
                                                        fontSize = 10.sp,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                                Icon(
                                                    imageVector = Icons.Default.AddCircle,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.secondary
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showAddAthleteDialog = false }) {
                            Text("Cancelar", fontWeight = FontWeight.Bold)
                        }
                    }
                )
            }

            if (filteredForRollCall.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f)),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Event,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                modifier = Modifier.size(48.dp)
                            )
                            Text(
                                text = if (rollCallSearchQuery.isNotBlank()) "Nenhum atleta agendado atende à pesquisa." else "Nenhum atleta agendado para esta aula.",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "Você pode incluir um atleta manualmente que não fez o agendamento prévio.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                                textAlign = TextAlign.Center
                            )
                            Button(
                                onClick = { showAddAthleteDialog = true },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                            ) {
                                Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Incluir Atleta na Aula", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredForRollCall) { athlete ->
                        val isPresent = athlete.attendanceHistory.contains(expectedPresencePattern)
                        
                        Card(
                            modifier = Modifier
                                .fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isPresent) Color(0xFFE8F5E9).copy(alpha = 0.4f) else MaterialTheme.colorScheme.surface
                            ),
                            border = BorderStroke(
                                1.dp, 
                                if (isPresent) Color(0xFF4CAF50).copy(alpha = 0.5f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    AthleteAvatar(
                                        imageUrl = athlete.imageUrl,
                                        name = athlete.name,
                                        modifier = Modifier.size(36.dp)
                                    )
                                    Column {
                                        Text(
                                            text = athlete.name,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = athlete.beltRank.split("—").firstOrNull()?.trim() ?: athlete.beltRank,
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Presente Button (Green highlighted if present, otherwise outline)
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isPresent) Color(0xFFE8F5E9) else Color.Transparent)
                                            .border(
                                                1.dp, 
                                                if (isPresent) Color(0xFF4CAF50) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), 
                                                RoundedCornerShape(8.dp)
                                            )
                                            .clickable { 
                                                if (!isPresent) {
                                                    viewModel.toggleAthleteAttendance(athlete, expectedPresencePattern)
                                                }
                                            }
                                            .padding(horizontal = 10.dp, vertical = 6.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                tint = if (isPresent) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                                modifier = Modifier.size(12.dp)
                                            )
                                            Text(
                                                text = "Presente",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isPresent) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    // Faltou Button (Red highlighted if absent, otherwise outline)
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (!isPresent) Color(0xFFFFEBEE) else Color.Transparent)
                                            .border(
                                                1.dp, 
                                                if (!isPresent) Color(0xFFE57373) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), 
                                                RoundedCornerShape(8.dp)
                                            )
                                            .clickable { 
                                                if (isPresent) {
                                                    viewModel.toggleAthleteAttendance(athlete, expectedPresencePattern)
                                                }
                                            }
                                            .padding(horizontal = 10.dp, vertical = 6.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = null,
                                                tint = if (!isPresent) Color(0xFFC62828) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                                modifier = Modifier.size(12.dp)
                                            )
                                            Text(
                                                text = "Faltou",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (!isPresent) Color(0xFFC62828) else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Planejamento Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "🥋 Organização do Treino - Aula de $selectedTime",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                
                OutlinedTextField(
                    value = classTema,
                    onValueChange = { classTema = it },
                    label = { Text("Tema Principal / Técnica do Dia") },
                    placeholder = { Text("Ex: Passagem de Guarda de Lapela") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary)
                )

                OutlinedTextField(
                    value = classAquecimento,
                    onValueChange = { classAquecimento = it },
                    label = { Text("Aquecimento (Warm-up)") },
                    placeholder = { Text("Ex: 10 min de corrida, alongamentos e polichinelos") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary)
                )

                OutlinedTextField(
                    value = classTecnica,
                    onValueChange = { classTecnica = it },
                    label = { Text("Exercícios / Treinamento Técnico") },
                    placeholder = { Text("Ex: Drill de passagem 3x10 reps, defesa de queda") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary)
                )

                OutlinedTextField(
                    value = classRola,
                    onValueChange = { classRola = it },
                    label = { Text("Rolas / Lutas / Sparring") },
                    placeholder = { Text("Ex: 5 rolas de 6 minutos com 1 min de descanso") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary)
                )
                
                if (showSavedMessage) {
                    Text(
                        text = "✅ Planejamento salvo com sucesso para as $selectedTime!",
                        color = Color(0xFF2E7D32),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }
                
                Button(
                    onClick = {
                        sharedPrefs.edit().apply {
                            putString("tema_${todayDate}_$selectedTime", classTema)
                            putString("aquecimento_${todayDate}_$selectedTime", classAquecimento)
                            putString("tecnica_${todayDate}_$selectedTime", classTecnica)
                            putString("rola_${todayDate}_$selectedTime", classRola)
                            apply()
                        }
                        showSavedMessage = true
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Save, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Salvar Planejamento", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
    
    // Custom Time Dialog
    if (showCustomTimeDialog) {
        AlertDialog(
            onDismissRequest = { showCustomTimeDialog = false },
            title = { Text("Adicionar Novo Horário", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = customTimeText,
                    onValueChange = { customTimeText = it },
                    label = { Text("Horário (Ex: 09:30)") },
                    placeholder = { Text("HH:MM") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (customTimeText.isNotBlank()) {
                            selectedTime = customTimeText
                        }
                        showCustomTimeDialog = false
                    },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Adicionar")
                }
            }
        )
    }
}

@Composable
fun FinanceDashboardPanel(
    totalRevenue: Double,
    totalProjected: Double,
    activeCount: Int,
    inactiveCount: Int,
    pendingCount: Int
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
            .testTag("finance_dashboard_panel"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            val monthName = remember {
                SimpleDateFormat("MMMM yyyy", Locale("pt", "BR")).format(Date()).replaceFirstChar { it.uppercase() }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "📊 Dashboard Financeiro",
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Text(
                        text = monthName,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold
                    )
                }
                Box(
                    modifier = Modifier
                        .background(
                            brush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                                colors = listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "Sincronizado",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            // Collection Progress Bar / Health indicator
            val collectionRate = if (totalProjected > 0) (totalRevenue / totalProjected) else 0.0
            val progressPercent = (collectionRate * 100).toInt()
            
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.04f))
                    .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = "Taxa de Arrecadação",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = "$progressPercent% Recebido",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF2E7D32)
                    )
                }
                
                // Custom Progress Bar with gradient
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(collectionRate.toFloat().coerceIn(0f, 1f))
                            .background(
                                brush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primary,
                                        Color(0xFF4CAF50)
                                    )
                                ),
                                shape = CircleShape
                            )
                    )
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Falta arrecadar: R$ ${(totalProjected - totalRevenue).toInt()}",
                        fontSize = 9.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Meta: R$ ${totalProjected.toInt()}",
                        fontSize = 9.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, Color(0xFFE8F5E9))
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Default.TrendingUp, contentDescription = null, tint = Color(0xFF4CAF50), modifier = Modifier.size(16.dp))
                            Text("Faturamento", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                        }
                        Text(
                            text = "R$ ${totalRevenue.toInt()}",
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp,
                            color = Color(0xFF2E7D32)
                        )
                        Text("Pago/Confirmado", fontSize = 9.sp, color = Color.Gray)
                    }
                }

                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Default.Payments, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                            Text("Projetado", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                        }
                        Text(
                            text = "R$ ${totalProjected.toInt()}",
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text("Total Mensalidades", fontSize = 9.sp, color = Color.Gray)
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .background(Color(0xFFE8F5E9), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Group, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(14.dp))
                        }
                        Column {
                            Text("Ativos (In)", fontSize = 9.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                            Text("$activeCount alunos", fontSize = 12.sp, fontWeight = FontWeight.Black, color = Color.Black)
                        }
                    }
                }

                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .background(Color(0xFFFFEBEE), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Cancel, contentDescription = null, tint = Color(0xFFC62828), modifier = Modifier.size(14.dp))
                        }
                        Column {
                            Text("Cancelados", fontSize = 9.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                            Text("$inactiveCount alunos", fontSize = 12.sp, fontWeight = FontWeight.Black, color = Color.Black)
                        }
                    }
                }

                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .background(Color(0xFFFFF3E0), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFE65100), modifier = Modifier.size(14.dp))
                        }
                        Column {
                            Text("Pendentes", fontSize = 9.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                            Text("$pendingCount pend.", fontSize = 12.sp, fontWeight = FontWeight.Black, color = Color.Black)
                        }
                    }
                }
            }
        }
    }
}
