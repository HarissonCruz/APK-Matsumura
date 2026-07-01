package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.viewmodel.GymViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(viewModel: GymViewModel) {
    var isSignUp by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf("Athlete") } // "Athlete" or "Professor"
    var passwordVisible by remember { mutableStateOf(false) }
    var showConfigDialog by remember { mutableStateOf(false) }

    val loginError by viewModel.loginError.collectAsStateWithLifecycle()
    val isSupabaseOnline by viewModel.isSupabaseOnline.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Settings Button for Custom Supabase Config
        IconButton(
            onClick = { showConfigDialog = true },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
                .testTag("login_settings_button")
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Configurações de Conexão",
                tint = MaterialTheme.colorScheme.primary
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            // App Brand/Logo (Matsumura Logo)
            MatsumuraLogo()

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Matsumura Team",
                fontWeight = FontWeight.Black,
                fontSize = 32.sp,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = (-1).sp
            )

            Text(
                text = "Escola de Artes Marciais • Desde 2024",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                modifier = Modifier.padding(top = 4.dp),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Box for fields
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (isSignUp) "Criar Nova Conta" else "Acesse sua Conta",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Role Selector
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (selectedRole == "Athlete") MaterialTheme.colorScheme.primary else Color.Transparent)
                                .clickable { selectedRole = "Athlete" }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = if (selectedRole == "Athlete") Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "Atleta",
                                    color = if (selectedRole == "Athlete") Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (selectedRole == "Professor") MaterialTheme.colorScheme.secondary else Color.Transparent)
                                .clickable { selectedRole = "Professor" }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.School,
                                    contentDescription = null,
                                    tint = if (selectedRole == "Professor") Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "Professor",
                                    color = if (selectedRole == "Professor") Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (selectedRole == "Financeiro") MaterialTheme.colorScheme.tertiary else Color.Transparent)
                                .clickable { selectedRole = "Financeiro" }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Payments,
                                    contentDescription = null,
                                    tint = if (selectedRole == "Financeiro") Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "Financeiro",
                                    color = if (selectedRole == "Financeiro") Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Error Display
                    loginError?.let { err ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Error,
                                    contentDescription = "Erro",
                                    tint = MaterialTheme.colorScheme.error
                                )
                                Text(
                                    text = err,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    // Fields
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("E-mail") },
                        placeholder = { Text("atleta@gymbuddy.com") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("login_email_input")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Senha") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = "Alternar Visibilidade"
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("login_password_input")
                    )

                    if (isSignUp) {
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = confirmPassword,
                            onValueChange = { confirmPassword = it },
                            label = { Text("Confirmar Senha") },
                            leadingIcon = { Icon(Icons.Default.LockReset, contentDescription = null) },
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            shape = RoundedCornerShape(16.dp),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("login_confirm_password_input")
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Action Button
                    Button(
                        onClick = {
                            if (isSignUp) {
                                viewModel.register(email, password, confirmPassword, selectedRole) { success ->
                                    if (success) {
                                        // Login automatico no VM
                                    }
                                }
                            } else {
                                viewModel.login(email, password, selectedRole) { success ->
                                    if (success) {
                                        // Login efetuado
                                    }
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("login_action_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedRole == "Professor") MaterialTheme.colorScheme.secondary else if (selectedRole == "Financeiro") MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = if (isSignUp) "Criar Conta" else "Entrar",
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Switch Mode
                    Text(
                        text = if (isSignUp) "Já tem uma conta? Faça Login" else "Ainda não tem conta? Cadastre-se",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        modifier = Modifier
                            .clickable { 
                                isSignUp = !isSignUp 
                                viewModel.clearLoginError()
                            }
                            .padding(8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showConfigDialog) {
        val customUrl by viewModel.customSupabaseUrl.collectAsStateWithLifecycle()
        val customKey by viewModel.customSupabaseAnonKey.collectAsStateWithLifecycle()
        var tempUrl by remember { mutableStateOf(customUrl) }
        var tempKey by remember { mutableStateOf(customKey) }

        AlertDialog(
            onDismissRequest = { showConfigDialog = false },
            title = {
                Text(
                    text = "⚙️ Configuração do Supabase",
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Conecte o aplicativo à sua própria instância do Supabase para salvar seus dados na nuvem.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    OutlinedTextField(
                        value = tempUrl,
                        onValueChange = { tempUrl = it },
                        label = { Text("URL do Supabase") },
                        placeholder = { Text("https://your-project.supabase.co") },
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = tempKey,
                        onValueChange = { tempKey = it },
                        label = { Text("Chave Anon (Anon Key)") },
                        placeholder = { Text("your-anon-key") },
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateSupabaseConfig(tempUrl, tempKey)
                        showConfigDialog = false
                    },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Salvar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        viewModel.updateSupabaseConfig("", "")
                        showConfigDialog = false
                    }
                ) {
                    Text("Resetar Padrão", color = MaterialTheme.colorScheme.error)
                }
            }
        )
    }
}

@Composable
fun MatsumuraLogo(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(130.dp)
            .clip(CircleShape)
            .background(Color(0xFF0F172A)), // Deep slate background from the brand
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val cx = width / 2
            val cy = height / 2
            
            // Zen brush-stroke circular border
            drawArc(
                color = Color.White.copy(alpha = 0.85f),
                startAngle = 15f,
                sweepAngle = 330f,
                useCenter = false,
                style = Stroke(width = 5.dp.toPx()),
                size = androidx.compose.ui.geometry.Size(width * 0.82f, height * 0.82f),
                topLeft = androidx.compose.ui.geometry.Offset(width * 0.09f, height * 0.09f)
            )
            
            // Inner brush ring
            drawArc(
                color = Color.White.copy(alpha = 0.4f),
                startAngle = -45f,
                sweepAngle = 310f,
                useCenter = false,
                style = Stroke(width = 2.dp.toPx()),
                size = androidx.compose.ui.geometry.Size(width * 0.74f, height * 0.74f),
                topLeft = androidx.compose.ui.geometry.Offset(width * 0.13f, height * 0.13f)
            )

            // Draw Sakura (Cherry Blossom) 5-point red flower
            val petalRadius = width * 0.13f
            val distance = width * 0.14f
            for (i in 0 until 5) {
                val angle = Math.toRadians((i * 72 - 90).toDouble())
                val px = cx + Math.cos(angle) * distance
                val py = cy + Math.sin(angle) * distance
                
                // Red Petal
                drawCircle(
                    color = Color(0xFFDC2626), // Vibrant Crimson Red
                    radius = petalRadius,
                    center = androidx.compose.ui.geometry.Offset(px.toFloat(), py.toFloat())
                )
                // Outline accent
                drawCircle(
                    color = Color(0xFF7F1D1D),
                    radius = petalRadius,
                    center = androidx.compose.ui.geometry.Offset(px.toFloat(), py.toFloat()),
                    style = Stroke(width = 1.dp.toPx())
                )
            }
            
            // Core accent
            drawCircle(
                color = Color(0xFF0F172A),
                radius = width * 0.08f,
                center = androidx.compose.ui.geometry.Offset(cx, cy)
            )
            drawCircle(
                color = Color(0xFFDC2626),
                radius = width * 0.04f,
                center = androidx.compose.ui.geometry.Offset(cx, cy)
            )
        }
    }
}
