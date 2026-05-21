package com.example.ui

import android.content.Context
import android.widget.Toast
import androidx.biometric.BiometricPrompt
import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.fragment.app.FragmentActivity
import com.example.data.Transaction
import com.example.data.TransactionType
import com.example.security.BiometricPromptManager
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

// Dynamic custom color accents for modern neon feel
val GreenNeon = Color(0xFF00E676)
val PinkNeon = Color(0xFFFF2D55)
val TealGlow = Color(0xFFD0BCFF)
val OrangeFlame = Color(0xFFFF9100)
val PurpleNeon = Color(0xFF381E72)
val YellowGlow = Color(0xFFFFD600)

val DarkBackground = Color(0xFF1C1B1F)
val DarkSurface = Color(0xFF2B2930)
val DarkSurfaceElevated = Color(0xFF49454F)
val LightBackground = Color(0xFFF7F9FC)
val LightSurface = Color(0xFFFFFFFF)
val LightSurfaceElevated = Color(0xFFCAC4D0)

val ChartCategoryColors = listOf(
    TealGlow, PinkNeon, GreenNeon, OrangeFlame, PurpleNeon, YellowGlow, Color(0xFFAC92EB), Color(0xFF4FC1E9)
)

@Composable
fun FinanceDashboard(
    viewModel: TransactionViewModel,
    activity: FragmentActivity,
    modifier: Modifier = Modifier
) {
    val transactions by viewModel.transactions.collectAsState()
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val isBiometricEnabled by viewModel.isBiometricEnabled.collectAsState()
    val isUserAuthenticated by viewModel.isUserAuthenticated.collectAsState()

    val context = LocalContext.current
    var isNewTransactionOpen by remember { mutableStateOf(false) }
    var transactionPresetType by remember { mutableStateOf(TransactionType.EXPENSE) }
    var isSettingsOpen by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf(0) } // 0: History, 1: Analytics

    // Biometric Trigger
    val biometricPromptManager = remember(activity) { BiometricPromptManager(activity) }

    val tryBiometricUnlock = {
        if (biometricPromptManager.isBiometricAvailable()) {
            biometricPromptManager.showBiometricPrompt(
                title = "Unlock CapitalFlow",
                subtitle = "Authenticate to access financial details",
                description = "Secure end-to-end encrypted financial datastore",
                onSuccess = {
                    viewModel.setAuthenticated(true)
                    Toast.makeText(context, "Welcome back!", Toast.LENGTH_SHORT).show()
                },
                onError = { error ->
                    Toast.makeText(context, "Unlock failed: $error", Toast.LENGTH_SHORT).show()
                },
                onFailed = {
                    Toast.makeText(context, "Authentication failed", Toast.LENGTH_SHORT).show()
                }
            )
        } else {
            // No biometric hardware, fall through to bypass
            viewModel.setAuthenticated(true)
        }
    }

    // Attempt biometric prompt once visible/auth-blocked
    LaunchedEffect(isUserAuthenticated, isBiometricEnabled) {
        if (!isUserAuthenticated && isBiometricEnabled) {
            tryBiometricUnlock()
        }
    }

    // Material theme mapping based on custom dark mode setting
    val surfaceColor = if (isDarkMode) DarkSurface else LightSurface
    val backgroundColor = if (isDarkMode) DarkBackground else LightBackground
    val textColor = if (isDarkMode) Color.White else Color(0xFF1C1B1F)
    val textSecondary = if (isDarkMode) Color(0xFF8B949E) else Color(0xFF57606A)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor)
            .windowInsetsPadding(WindowInsets.statusBars)
    ) {
        if (!isUserAuthenticated && isBiometricEnabled) {
            // Locked screen for biometric security
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .background(
                            Brush.sweepGradient(listOf(TealGlow, PurpleNeon, TealGlow)),
                            shape = CircleShape
                        )
                        .padding(3.dp)
                        .background(backgroundColor, shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Encrypted Lock Icon",
                        tint = TealGlow,
                        modifier = Modifier.size(48.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Vault Locked",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "This datastore is fully encrypted using hardware-backed AES-256 keys to preserve your privacy.",
                    fontSize = 14.sp,
                    color = textSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(48.dp))

                Button(
                    onClick = { tryBiometricUnlock() },
                    colors = ButtonDefaults.buttonColors(containerColor = TealGlow),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .height(50.dp)
                        .testTag("biometric_unlock_button")
                ) {
                    Icon(Icons.Default.Security, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Unlock with Biometrics", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        } else {
            // Primary Application Interface (Unlocked)
            Scaffold(
                containerColor = Color.Transparent,
                topBar = {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Professional Polish profile circle
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(Color(0xFFD0BCFF), CircleShape)
                                    .shadow(elevation = 2.dp, shape = CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "D",
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF381E72),
                                    fontSize = 16.sp
                                )
                            }

                            Column {
                                Text(
                                    text = "Hello,",
                                    color = textSecondary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "Danial",
                                    color = textColor,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Secure Lock indicator button
                            IconButton(
                                onClick = {
                                    if (isBiometricEnabled) {
                                        tryBiometricUnlock()
                                    } else {
                                        Toast.makeText(context, "Datastore is secured and encrypted.", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier
                                    .size(42.dp)
                                    .background(Color(0xFF49454F), CircleShape)
                            ) {
                                Icon(
                                    imageVector = if (isBiometricEnabled) Icons.Default.Fingerprint else Icons.Default.LockOpen,
                                    contentDescription = "Biometrics indicator",
                                    tint = Color(0xFFD0BCFF),
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            // Notification / Settings button
                            IconButton(
                                onClick = { isSettingsOpen = true },
                                modifier = Modifier
                                    .size(42.dp)
                                    .background(Color(0xFF49454F), CircleShape)
                                    .testTag("settings_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = "System Settings",
                                    tint = Color(0xFFD0BCFF),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            ) { paddingValues ->
                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    val availableWidth = maxWidth
                    val isLargeScreen = availableWidth > 600.dp

                    // Split Dashboard layout for tablets, single-column scrollable for compact phones
                    if (isLargeScreen) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(24.dp)
                        ) {
                            // Left side: KPIs & Add Transaction launcher & Chart Settings
                            Column(
                                modifier = Modifier
                                    .weight(0.45f)
                                    .verticalScroll(rememberScrollState())
                            ) {
                                OverviewCard(transactions, isDarkMode)

                                Spacer(modifier = Modifier.height(16.dp))

                                PrimaryActionsGrid(
                                    isDarkMode = isDarkMode,
                                    onInClick = {
                                        transactionPresetType = TransactionType.INCOME
                                        isNewTransactionOpen = true
                                    },
                                    onOutClick = {
                                        transactionPresetType = TransactionType.EXPENSE
                                        isNewTransactionOpen = true
                                    },
                                    onExportClick = {
                                        val csvContent = viewModel.generateCsvContent()
                                        shareCsvText(context, csvContent)
                                    },
                                    onReportsClick = {
                                        selectedTab = 1
                                    }
                                )

                                Spacer(modifier = Modifier.height(20.dp))

                                AnalyticsSection(transactions, isDarkMode, surfaceColor, textColor, textSecondary)
                            }

                            // Right side: Tabs & Scrollable Data
                            Column(
                                modifier = Modifier.weight(0.55f)
                            ) {
                                TabRow(
                                    selectedTabIndex = selectedTab,
                                    containerColor = Color.Transparent,
                                    contentColor = TealGlow,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Tab(
                                        selected = selectedTab == 0,
                                        onClick = { selectedTab = 0 },
                                        text = { Text("Transactions Ledger", fontWeight = FontWeight.Bold) }
                                    )
                                    Tab(
                                        selected = selectedTab == 1,
                                        onClick = { selectedTab = 1 },
                                        text = { Text("Visual Charts", fontWeight = FontWeight.Bold) }
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                if (selectedTab == 0) {
                                    LedgerList(transactions, currentViewModeDark = isDarkMode, onDelete = { tx ->
                                        viewModel.deleteTransaction(tx.id)
                                    })
                                } else {
                                    VisualChartsCollection(transactions, isDarkMode)
                                }
                            }
                        }
                    } else {
                        // Compact mobile layout: Single column scrollable
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                        ) {
                            // Sub-navigation bar representing Dashboard layers
                            TabRow(
                                selectedTabIndex = selectedTab,
                                containerColor = Color.Transparent,
                                contentColor = TealGlow,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp)
                            ) {
                                Tab(
                                    selected = selectedTab == 0,
                                    onClick = { selectedTab = 0 },
                                    text = { Text("Dashboard", fontWeight = FontWeight.Bold) },
                                    modifier = Modifier.testTag("tab_dashboard")
                                )
                                Tab(
                                    selected = selectedTab == 1,
                                    onClick = { selectedTab = 1 },
                                    text = { Text("Visual Charts", fontWeight = FontWeight.Bold) },
                                    modifier = Modifier.testTag("tab_charts")
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            if (selectedTab == 0) {
                                // Scrollable list representing core financial state & transactions
                                LazyColumn(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = 16.dp),
                                    verticalArrangement = Arrangement.spacedBy(16.dp),
                                    contentPadding = PaddingValues(bottom = 80.dp)
                                ) {
                                    item {
                                        OverviewCard(transactions, isDarkMode)
                                    }

                                    item {
                                        PrimaryActionsGrid(
                                            isDarkMode = isDarkMode,
                                            onInClick = {
                                                transactionPresetType = TransactionType.INCOME
                                                isNewTransactionOpen = true
                                            },
                                            onOutClick = {
                                                transactionPresetType = TransactionType.EXPENSE
                                                isNewTransactionOpen = true
                                            },
                                            onExportClick = {
                                                val csvContent = viewModel.generateCsvContent()
                                                shareCsvText(context, csvContent)
                                            },
                                            onReportsClick = {
                                                selectedTab = 1
                                            }
                                        )
                                    }

                                    item {
                                        Text(
                                            text = "Recent Activity",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 18.sp,
                                            color = textColor,
                                            modifier = Modifier.padding(top = 8.dp)
                                        )
                                    }

                                    if (transactions.isEmpty()) {
                                        item {
                                            EmptyStateMessage(isDarkMode, surfaceColor, textSecondary)
                                        }
                                    } else {
                                        items(transactions, key = { it.id }) { tx ->
                                            TransactionItemRow(tx, isDarkMode, onDelete = {
                                                viewModel.deleteTransaction(tx.id)
                                            })
                                        }
                                    }
                                }
                            } else {
                                // Charts Tab
                                VisualChartsCollection(transactions, isDarkMode)
                            }
                        }
                    }
                }
            }
        }

        // Custom Settings Dialog (Security + Config Preferences)
        if (isSettingsOpen) {
            Dialog(onDismissRequest = { isSettingsOpen = false }) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .testTag("settings_dialog"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = surfaceColor)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Vault Settings",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // Dark Mode Preference Option
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (isDarkMode) Icons.Default.DarkMode else Icons.Default.LightMode,
                                    contentDescription = null,
                                    tint = if (isDarkMode) PurpleNeon else OrangeFlame
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text("Dark Mode Experience", color = textColor, fontWeight = FontWeight.Medium)
                            }
                            Switch(
                                checked = isDarkMode,
                                onCheckedChange = { viewModel.setDarkMode(it) },
                                modifier = Modifier.testTag("dark_mode_switch")
                            )
                        }

                        Divider(color = textSecondary.copy(alpha = 0.15f), modifier = Modifier.padding(vertical = 8.dp))

                        // Biometrics Preferences Toggle
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Security,
                                    contentDescription = null,
                                    tint = TealGlow
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text("Biometric Lock screen", color = textColor, fontWeight = FontWeight.Medium)
                            }
                            Switch(
                                checked = isBiometricEnabled,
                                onCheckedChange = { enabled ->
                                    if (enabled) {
                                        // Try standard authenticate check before toggling safely
                                        if (biometricPromptManager.isBiometricAvailable()) {
                                            biometricPromptManager.showBiometricPrompt(
                                                title = "Register Biometric Auth",
                                                subtitle = "Set up unlock preferences",
                                                description = "Secure your local sensitive financial ledger",
                                                onSuccess = {
                                                    viewModel.setBiometricEnabled(true)
                                                    Toast.makeText(context, "Biometric lock activated successfully", Toast.LENGTH_SHORT).show()
                                                },
                                                onError = { error ->
                                                    Toast.makeText(context, "Setup failed: $error", Toast.LENGTH_SHORT).show()
                                                },
                                                onFailed = {
                                                    Toast.makeText(context, "Authentication failed", Toast.LENGTH_SHORT).show()
                                                }
                                            )
                                        } else {
                                            Toast.makeText(context, "Biometric options not supported or registered on this device", Toast.LENGTH_LONG).show()
                                        }
                                    } else {
                                        viewModel.setBiometricEnabled(false)
                                        Toast.makeText(context, "Biometric screen lock disabled", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier.testTag("biometric_lock_switch")
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = { isSettingsOpen = false }) {
                                Text("Done", fontWeight = FontWeight.Bold, color = TealGlow)
                            }
                        }
                    }
                }
            }
        }

        // Custom Creation Sheet Dialog
        if (isNewTransactionOpen) {
            Dialog(onDismissRequest = { isNewTransactionOpen = false }) {
                AddTransactionCard(
                    isDarkMode = isDarkMode,
                    surfaceColor = surfaceColor,
                    textColor = textColor,
                    textSecondary = textSecondary,
                    initialType = transactionPresetType,
                    onDismiss = { isNewTransactionOpen = false },
                    onSave = { amount, description, category, type ->
                        viewModel.addTransaction(amount, description, category, type)
                        isNewTransactionOpen = false
                        Toast.makeText(context, "Transaction securely stored!", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }
    }
}

// Share sheet caller for plain text CSV values
fun shareCsvText(context: Context, csvText: String) {
    try {
        val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(android.content.Intent.EXTRA_TEXT, csvText)
            putExtra(android.content.Intent.EXTRA_SUBJECT, "Financial Statement Summary")
        }
        context.startActivity(android.content.Intent.createChooser(intent, "Share Export Details"))
    } catch (e: Exception) {
        Toast.makeText(context, "Share process failed: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}

@Composable
fun EmptyStateMessage(isDarkMode: Boolean, surfaceColor: Color, textSecondary: Color) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = surfaceColor.copy(alpha = 0.5f)),
        border = BorderStroke(1.dp, textSecondary.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "💰",
                fontSize = 48.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Text(
                text = "No Transactions Found",
                color = if (isDarkMode) Color.White else Color.Black,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Your sensitive transaction metadata is kept secure. Press 'Add Transaction' below to begin reporting logs.",
                textAlign = TextAlign.Center,
                fontSize = 12.sp,
                color = textSecondary
            )
        }
    }
}

// Combined ledger view for Larger screen systems
@Composable
fun LedgerList(
    transactions: List<Transaction>,
    currentViewModeDark: Boolean,
    onDelete: (Transaction) -> Unit
) {
    if (transactions.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            EmptyStateMessage(
                isDarkMode = currentViewModeDark,
                surfaceColor = if (currentViewModeDark) DarkSurface else LightSurface,
                textSecondary = if (currentViewModeDark) Color(0xFF8B949E) else Color(0xFF57606A)
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            items(transactions, key = { it.id }) { tx ->
                TransactionItemRow(tx, currentViewModeDark, onDelete)
            }
        }
    }
}

@Composable
fun PrimaryActionsGrid(
    isDarkMode: Boolean,
    onInClick: () -> Unit,
    onOutClick: () -> Unit,
    onExportClick: () -> Unit,
    onReportsClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val actions = listOf(
            Triple("In", Icons.Default.Add, Color(0xFF381E72)),
            Triple("Out", Icons.Default.Remove, Color(0xFF49454F)),
            Triple("Export", Icons.Default.Share, Color(0xFF49454F)),
            Triple("Reports", Icons.Default.Assessment, Color(0xFF49454F))
        )

        actions.forEach { actionItem ->
            val label = actionItem.first
            val icon = actionItem.second
            val customBg = actionItem.third

            val onClickAction = when (label) {
                "In" -> onInClick
                "Out" -> onOutClick
                "Export" -> onExportClick
                else -> onReportsClick
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onClickAction() },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(
                            color = if (isDarkMode) customBg else customBg.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(16.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = if (isDarkMode) Color(0xFFD0BCFF) else Color(0xFF381E72),
                        modifier = Modifier.size(24.dp)
                    )
                }

                Text(
                    text = label,
                    fontSize = 11.sp,
                    color = if (isDarkMode) Color(0xFFCAC4D0) else Color(0xFF49454F),
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
fun OverviewCard(transactions: List<Transaction>, isDarkMode: Boolean) {
    // Math indicators
    var balance = 0.0
    var totalIncome = 0.0
    var totalExpense = 0.0

    for (tx in transactions) {
        if (tx.type == TransactionType.INCOME) {
            totalIncome += tx.amount
            balance += tx.amount
        } else {
            totalExpense += tx.amount
            balance -= tx.amount
        }
    }

    val currencyFormatter = remember { NumberFormat.getCurrencyInstance(Locale.US) }
    val cardBackground = if (isDarkMode) {
        Brush.linearGradient(listOf(Color(0xFF2B2930), Color(0xFF1C1B1F)))
    } else {
        Brush.linearGradient(listOf(Color(0xFFE9F1FE), Color.White))
    }
    val borderGlow = if (isDarkMode) Color(0xFF49454F).copy(alpha = 0.4f) else Color.Transparent

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(28.dp),
                ambientColor = Color(0xFFD0BCFF).copy(alpha = 0.1f),
                spotColor = Color(0xFFD0BCFF).copy(alpha = 0.3f)
            )
            .testTag("overview_card"),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(cardBackground)
                .border(1.dp, borderGlow, RoundedCornerShape(28.dp))
                .padding(24.dp)
        ) {
            // E2EE Secure Badge
            Row(
                modifier = Modifier
                    .background(Color(0xFFD0BCFF), shape = RoundedCornerShape(50.dp))
                    .padding(horizontal = 8.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "E2EE SECURE",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF381E72),
                    letterSpacing = 0.5.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Total Balance",
                fontSize = 14.sp,
                color = if (isDarkMode) Color(0xFFCAC4D0) else Color(0xFF49454F),
                fontWeight = FontWeight.Normal
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = currencyFormatter.format(balance),
                fontSize = 36.sp,
                fontWeight = FontWeight.Light,
                color = if (isDarkMode) Color(0xFFE6E1E5) else Color(0xFF1C1B1F)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Analytical CSS Bars row from Professional Polish Design
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                val hPercentages = listOf(0.40f, 0.60f, 0.45f, 0.90f, 0.30f, 0.75f, 0.50f, 0.65f)
                val barColors = listOf(
                    Color(0xFF49454F),
                    Color(0xFF49454F),
                    Color(0xFF49454F),
                    Color(0xFFD0BCFF),
                    Color(0xFF49454F),
                    Color(0xFFD0BCFF),
                    Color(0xFF49454F),
                    Color(0xFF49454F)
                )

                hPercentages.forEachIndexed { idx, pct ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(pct)
                            .background(
                                color = if (isDarkMode) barColors[idx] else barColors[idx].copy(alpha = 0.7f),
                                shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Income flow block
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(GreenNeon, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "TOTAL INCOME",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDarkMode) Color(0xFFCAC4D0) else Color(0xFF49454F),
                            letterSpacing = 0.5.sp
                        )
                    }
                    Text(
                        text = currencyFormatter.format(totalIncome),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = GreenNeon,
                        modifier = Modifier.testTag("kpi_total_income")
                    )
                }

                // Expense flow block
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(PinkNeon, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "TOTAL EXPENSES",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDarkMode) Color(0xFFCAC4D0) else Color(0xFF49454F),
                            letterSpacing = 0.5.sp
                        )
                    }
                    Text(
                        text = currencyFormatter.format(totalExpense),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = PinkNeon,
                        modifier = Modifier.testTag("kpi_total_expenses")
                    )
                }
            }
        }
    }
}

// Extra helper for gradient colors elevation mapping
private fun Color.ElevateBrush(factor: Float): Color {
    return Color(0xFF1E2533)
}

@Composable
fun TransactionItemRow(
    transaction: Transaction,
    isDarkMode: Boolean,
    onDelete: (Transaction) -> Unit
) {
    val context = LocalContext.current
    val itemBg = if (isDarkMode) DarkSurface else LightSurface
    val titleColor = if (isDarkMode) Color.White else Color(0xFF212529)
    val textSecondary = if (isDarkMode) Color(0xFF8B949E) else Color(0xFF6C757D)

    val currencyFormatter = remember { NumberFormat.getCurrencyInstance(Locale.US) }
    val formatPattern = remember { SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.getDefault()) }
    val dateText = formatPattern.format(Date(transaction.timestamp))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("tx_item_${transaction.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = itemBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Circle visual layout containing direction indicators
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .background(
                            color = if (transaction.type == TransactionType.INCOME) GreenNeon.copy(alpha = 0.12f) else PinkNeon.copy(alpha = 0.12f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (transaction.type == TransactionType.INCOME) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                        contentDescription = transaction.type.name,
                        tint = if (transaction.type == TransactionType.INCOME) GreenNeon else PinkNeon,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = transaction.description.ifEmpty { "Unspecified Transaction" },
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = titleColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = transaction.category,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (transaction.type == TransactionType.INCOME) GreenNeon.copy(alpha = 0.85f) else TealGlow,
                            modifier = Modifier
                                .background(
                                    color = if (transaction.type == TransactionType.INCOME) GreenNeon.copy(alpha = 0.08f) else TealGlow.copy(alpha = 0.08f),
                                    shape = RoundedCornerShape(4.dp)
                                )
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )

                        Text(
                            text = dateText,
                            fontSize = 11.sp,
                            color = textSecondary
                        )
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = (if (transaction.type == TransactionType.INCOME) "+" else "-") + currencyFormatter.format(transaction.amount),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black,
                    color = if (transaction.type == TransactionType.INCOME) GreenNeon else PinkNeon,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                // Inline quick delete interaction
                IconButton(
                    onClick = { onDelete(transaction) },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete item",
                        tint = textSecondary.copy(alpha = 0.45f)
                    )
                }
            }
        }
    }
}

// Side calculations for screen spaces in larger layouts
@Composable
fun AnalyticsSection(
    transactions: List<Transaction>,
    isDarkMode: Boolean,
    surfaceColor: Color,
    textColor: Color,
    textSecondary: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = surfaceColor),
        border = BorderStroke(1.dp, textSecondary.copy(alpha = 0.1f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Analytical Key Metrics",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = textColor,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Dynamic items
            val totalIncome = transactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
            val totalExpense = transactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
            val saveRatio = if (totalIncome > 0) ((totalIncome - totalExpense) / totalIncome * 100).coerceIn(0.0, 100.0) else 0.0

            ProgressBarItem(
                label = "Savings / Net Income Ratio",
                fraction = (saveRatio / 100f).toFloat(),
                color = TealGlow,
                textColor = textColor,
                textSub = "${saveRatio.toInt()}% Saved"
            )

            Spacer(modifier = Modifier.height(14.dp))

            val expenseCount = transactions.count { it.type == TransactionType.EXPENSE }
            val totalCount = transactions.size
            val transRatio = if (totalCount > 0) (expenseCount.toFloat() / totalCount) else 0f

            ProgressBarItem(
                label = "Expense Transaction Ratio",
                fraction = transRatio,
                color = PinkNeon,
                textColor = textColor,
                textSub = "${expenseCount} of ${totalCount} entries"
            )
        }
    }
}

@Composable
fun ProgressBarItem(
    label: String,
    fraction: Float,
    color: Color,
    textColor: Color,
    textSub: String
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = label, fontSize = 12.sp, color = textColor.copy(alpha = 0.8f))
            Text(text = textSub, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = color)
        }
        Spacer(modifier = Modifier.height(6.dp))
        LinearProgressIndicator(
            progress = { fraction },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(CircleShape),
            color = color,
            trackColor = textColor.copy(alpha = 0.08f)
        )
    }
}

// Collections for holding charts configurations
@Composable
fun VisualChartsCollection(
    transactions: List<Transaction>,
    isDarkMode: Boolean
) {
    val context = LocalContext.current
    val surfaceColor = if (isDarkMode) DarkSurface else LightSurface
    val textColor = if (isDarkMode) Color.White else Color.Black
    val textSecondary = if (isDarkMode) Color(0xFF8B949E) else Color(0xFF57606A)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        if (transactions.isEmpty()) {
            item {
                EmptyStateMessage(isDarkMode, surfaceColor, textSecondary)
            }
        } else {
            // Expenses Donut Arc Composable
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(4.dp, RoundedCornerShape(16.dp))
                        .testTag("expenses_donut_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = surfaceColor)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Expenses Distribution",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = textColor,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Start
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        val expenseItems = transactions.filter { it.type == TransactionType.EXPENSE }
                        val categoriesSum = expenseItems.groupBy { it.category }
                            .mapValues { entry -> entry.value.sumOf { it.amount } }

                        if (categoriesSum.isEmpty()) {
                            Text(
                                text = "Add standard Expense Transactions to see Category distribution logs.",
                                fontSize = 12.sp,
                                color = textSecondary,
                                modifier = Modifier.padding(24.dp)
                            )
                        } else {
                            DonutChartWidget(
                                dataMap = categoriesSum,
                                textModeDark = isDarkMode
                            )
                        }
                    }
                }
            }

            // Dual flow visual trends bar chart
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(4.dp, RoundedCornerShape(16.dp))
                        .testTag("trends_bar_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = surfaceColor)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Financial Flow Bar Chart",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        FlowCompareBarChart(
                            transactions = transactions,
                            isDark = isDarkMode
                        )
                    }
                }
            }
        }
    }
}

// 1. Custom Donut Chart Composable using high-polish Jetpack Canvas
@Composable
fun DonutChartWidget(
    dataMap: Map<String, Double>,
    textModeDark: Boolean
) {
    val totalSum = dataMap.values.sum()
    val listData = dataMap.toList().sortedByDescending { it.second }
    val currencyFormatter = remember { NumberFormat.getCurrencyInstance(Locale.US) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Left - Animated Canvas Arc
        Box(
            modifier = Modifier
                .size(140.dp)
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(130.dp)) {
                var startAngle = -90f
                listData.forEachIndexed { idx, pair ->
                    val angleFraction = (pair.second / totalSum).toFloat()
                    val sweepAngle = angleFraction * 360f
                    val arcColor = ChartCategoryColors[idx % ChartCategoryColors.size]

                    // Draw sleek donut arc strips
                    drawArc(
                        color = arcColor,
                        startAngle = startAngle,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        style = Stroke(width = 24f, cap = StrokeCap.Round),
                        size = Size(size.width, size.height)
                    )
                    startAngle += sweepAngle
                }
            }

            // Inside Center Content Text
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Total Spend",
                    fontSize = 10.sp,
                    color = if (textModeDark) Color(0xFF8B949E) else Color(0xFF6C757D),
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = currencyFormatter.format(totalSum),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black,
                    color = if (textModeDark) Color.White else Color.Black,
                    maxLines = 1
                )
            }
        }

        // Right - Legend layout descriptors
        Column(
            modifier = Modifier
                .weight(1.2f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listData.take(5).forEachIndexed { idx, pair ->
                val arcColor = ChartCategoryColors[idx % ChartCategoryColors.size]
                val percent = (pair.second / totalSum * 100).toInt()

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(arcColor, CircleShape)
                    )
                    Text(
                        text = "${pair.first} ($percent%)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (textModeDark) Color.White else Color(0xFF2D3748),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = currencyFormatter.format(pair.second),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (textModeDark) Color(0xFF8B949E) else Color(0xFF718096)
                    )
                }
            }
            if (listData.size > 5) {
                Text(
                    text = "+ ${listData.size - 5} other categories",
                    fontSize = 10.sp,
                    color = if (textModeDark) Color(0xFF8B949E) else Color(0xFF718096)
                )
            }
        }
    }
}

// 2. Custom Dual-Bar Chart comparing weekly flow metrics
@Composable
fun FlowCompareBarChart(
    transactions: List<Transaction>,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    val axisColor = if (isDark) Color(0xFF30363D) else Color(0xFFD0D7DE)
    val textStyleColor = if (isDark) Color(0xFF8B949E) else Color(0xFF57606A)

    // Compile values based on days of the week (Mon, Tue, Wed, Thu, Fri, Sat, Sun)
    val dayInflow = DoubleArray(7) { 0.0 }
    val dayOutflow = DoubleArray(7) { 0.0 }

    val calendar = Calendar.getInstance()
    transactions.forEach { tx ->
        calendar.timeInMillis = tx.timestamp
        // Calendar.DAY_OF_WEEK returns Sunday = 1, Monday = 2 etc.
        // Convert so Monday = 0..6
        val dayIndex = when (calendar.get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> 0
            Calendar.TUESDAY -> 1
            Calendar.WEDNESDAY -> 2
            Calendar.THURSDAY -> 3
            Calendar.FRIDAY -> 4
            Calendar.SATURDAY -> 5
            Calendar.SUNDAY -> 6
            else -> 0
        }

        if (tx.type == TransactionType.INCOME) {
            dayInflow[dayIndex] += tx.amount
        } else {
            dayOutflow[dayIndex] += tx.amount
        }
    }

    val maxInflow = dayInflow.maxOrNull() ?: 1.0
    val maxOutflow = dayOutflow.maxOrNull() ?: 1.0
    val overallMax = maxOf(maxInflow, maxOutflow).takeIf { it > 0.0 } ?: 100.0

    val weekdays = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

    Column(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .drawBehind {
                    // Draw coordinate plane horizontal grid guidelines
                    val gridLines = 3
                    val stepHeight = size.height / (gridLines + 1)
                    for (i in 1..gridLines) {
                        val yPos = stepHeight * i
                        drawLine(
                            color = axisColor.copy(alpha = 0.4f),
                            start = Offset(0f, yPos),
                            end = Offset(size.width, yPos),
                            strokeWidth = 1f
                        )
                    }

                    // Bottom Baseline
                    drawLine(
                        color = axisColor,
                        start = Offset(0f, size.height),
                        end = Offset(size.width, size.height),
                        strokeWidth = 3f
                    )
                }
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.Bottom
            ) {
                weekdays.forEachIndexed { index, day ->
                    val inflowHeightFraction = (dayInflow[index] / overallMax).toFloat().coerceIn(0.01f, 1f)
                    val outflowHeightFraction = (dayOutflow[index] / overallMax).toFloat().coerceIn(0.01f, 1f)

                    Row(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(1f)
                            .padding(horizontal = 6.dp),
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        // Green Income glow bar
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(inflowHeightFraction)
                                .background(
                                    Brush.verticalGradient(listOf(GreenNeon, GreenNeon.copy(alpha = 0.3f))),
                                    shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                                )
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        // Pink Expense glow bar
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(outflowHeightFraction)
                                .background(
                                    Brush.verticalGradient(listOf(PinkNeon, PinkNeon.copy(alpha = 0.3f))),
                                    shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                                )
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Chart Bottom Legend Weekdays labels
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            weekdays.forEach { day ->
                Text(
                    text = day,
                    fontSize = 11.sp,
                    color = textStyleColor,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Legend details
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .background(GreenNeon, CircleShape)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text("Inflow (Income)", fontSize = 11.sp, color = textStyleColor, fontWeight = FontWeight.Bold)

            Spacer(modifier = Modifier.width(24.dp))

            Box(
                modifier = Modifier
                    .size(10.dp)
                    .background(PinkNeon, CircleShape)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text("Outflow (Expense)", fontSize = 11.sp, color = textStyleColor, fontWeight = FontWeight.Bold)
        }
    }
}



// Custom Composable Card containing Interactive Form Fields
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionCard(
    isDarkMode: Boolean,
    surfaceColor: Color,
    textColor: Color,
    textSecondary: Color,
    initialType: TransactionType = TransactionType.EXPENSE,
    onDismiss: () -> Unit,
    onSave: (amount: Double, description: String, category: String, type: TransactionType) -> Unit
) {
    val context = LocalContext.current
    var amountInput by remember { mutableStateOf("") }
    var descriptionInput by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(initialType) }
    var selectedCategory by remember { mutableStateOf("") }

    val categoriesIncome = listOf("Salary", "Investment", "Freelance", "Gift", "Other")
    val categoriesExpense = listOf("Food", "Shopping", "Utilities", "Entertainment", "Transport", "Rent", "Other")

    val activeCategoriesList = if (selectedType == TransactionType.INCOME) categoriesIncome else categoriesExpense

    // Set initial category when changing type
    LaunchedEffect(selectedType) {
        selectedCategory = activeCategoriesList.first()
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("add_transaction_form"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = surfaceColor)
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Secure Data Record",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = textColor
            )

            Text(
                text = "Secure local encryption handles all entered fields.",
                fontSize = 11.sp,
                color = GreenNeon,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Dual Tab Selector representing Transaction Type
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(textColor.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Income toggle
                val isIncomeSelected = selectedType == TransactionType.INCOME
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isIncomeSelected) GreenNeon else Color.Transparent)
                        .clickable { selectedType = TransactionType.INCOME }
                        .padding(vertical = 10.dp)
                        .testTag("type_toggle_income"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "INCOME",
                        color = if (isIncomeSelected) Color.Black else textColor.copy(alpha = 0.7f),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }

                // Expense toggle
                val isExpenseSelected = selectedType == TransactionType.EXPENSE
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isExpenseSelected) PinkNeon else Color.Transparent)
                        .clickable { selectedType = TransactionType.EXPENSE }
                        .padding(vertical = 10.dp)
                        .testTag("type_toggle_expense"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "EXPENSE",
                        color = if (isExpenseSelected) Color.White else textColor.copy(alpha = 0.7f),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Amount Input Field
            OutlinedTextField(
                value = amountInput,
                onValueChange = { input ->
                    if (input.isEmpty() || input.toDoubleOrNull() != null) {
                        amountInput = input
                    }
                },
                label = { Text("Amount ($)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("amount_textfield"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = TealGlow,
                    unfocusedBorderColor = textSecondary.copy(alpha = 0.3f),
                    focusedLabelColor = TealGlow
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Purpose description field
            OutlinedTextField(
                value = descriptionInput,
                onValueChange = { descriptionInput = it },
                label = { Text("What is this for?") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("description_textfield"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = TealGlow,
                    unfocusedBorderColor = textSecondary.copy(alpha = 0.3f),
                    focusedLabelColor = TealGlow
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Custom Category Chips
            Text(
                text = "Select Category",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = textSecondary,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Start
            )

            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                activeCategoriesList.forEach { category ->
                    val isSelected = selectedCategory == category
                    val chipBg = if (isSelected) {
                        if (selectedType == TransactionType.INCOME) GreenNeon else PinkNeon
                    } else {
                        surfaceColor.copy(alpha = 0.1f)
                    }
                    val chipText = if (isSelected) {
                        if (selectedType == TransactionType.INCOME) Color.Black else Color.White
                    } else {
                        textColor.copy(alpha = 0.7f)
                    }

                    Box(
                        modifier = Modifier
                            .background(
                                color = chipBg,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .border(
                                width = 1.dp,
                                color = if (isSelected) Color.Transparent else textSecondary.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable { selectedCategory = category }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                            .testTag("category_chip_$category"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = category,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = chipText
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = textColor)
                ) {
                    Text("Cancel")
                }

                Button(
                    onClick = {
                        val amountVal = amountInput.toDoubleOrNull() ?: 0.0
                        if (amountVal > 0.0 && descriptionInput.isNotBlank()) {
                            onSave(amountVal, descriptionInput, selectedCategory, selectedType)
                        } else {
                            Toast.makeText(context, "Please enter valid values to save", Toast.LENGTH_SHORT).show()
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1.2f)
                        .testTag("save_transaction_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedType == TransactionType.INCOME) GreenNeon else PinkNeon
                    )
                ) {
                    Icon(Icons.Default.Security, contentDescription = null, tint = if (selectedType == TransactionType.INCOME) Color.Black else Color.White)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "Secure Save",
                        color = if (selectedType == TransactionType.INCOME) Color.Black else Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FlowRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    content: @Composable () -> Unit
) {
    androidx.compose.foundation.layout.FlowRow(
        modifier = modifier,
        horizontalArrangement = horizontalArrangement,
        verticalArrangement = verticalArrangement,
        content = { content() }
    )
}
