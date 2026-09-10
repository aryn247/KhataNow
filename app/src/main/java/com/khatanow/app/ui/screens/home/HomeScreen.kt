package com.khatanow.app.ui.screens.home

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Update
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.khatanow.app.data.local.entities.TransactionWithDetails
import com.khatanow.app.ui.MainViewModel
import com.khatanow.app.ui.screens.customers.AddEditCustomerDialog
import com.khatanow.app.ui.screens.products.AddEditProductDialog
import com.khatanow.app.ui.theme.GreenPrimary
import com.khatanow.app.ui.theme.MicAccent
import com.khatanow.app.util.AppLanguage
import com.khatanow.app.util.DateFormatter

@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    onNavigateToManual: () -> Unit,
    onNavigateToCustomers: () -> Unit = {},
    onNavigateToProducts: () -> Unit = {}
) {
    val context = LocalContext.current
    val customers by viewModel.customers.collectAsState()
    val products by viewModel.products.collectAsState()
    val transactions by viewModel.transactions.collectAsState()
    val speechState by viewModel.speechState.collectAsState()
    val parseResult by viewModel.parseResult.collectAsState()
    val currentLanguage by viewModel.currentLanguage.collectAsState()
    val updateInfo by viewModel.updateInfo.collectAsState()

    var showVoiceModal by remember { mutableStateOf(false) }
    var showAddCustomerModal by remember { mutableStateOf(false) }
    var showAddProductModal by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Bar Header with Language Selector
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "KhataNow",
                    style = MaterialTheme.typography.headlineLarge,
                    color = GreenPrimary
                )
                Text(
                    text = if (currentLanguage == AppLanguage.HINDI) "बोल खाता — लोकल ऐप" else "Local-First Voice Credit Ledger",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.clickable {
                    val nextLang = if (currentLanguage == AppLanguage.ENGLISH) AppLanguage.HINDI else AppLanguage.ENGLISH
                    viewModel.setAppLanguage(nextLang)
                }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Language, contentDescription = null, tint = GreenPrimary, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = currentLanguage.displayName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = GreenPrimary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Lightweight Universal Update Notification Banner
        if (updateInfo?.hasUpdate == true) {
            val update = updateInfo!!
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Update, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
                        Column(modifier = Modifier.padding(start = 10.dp)) {
                            Text(
                                text = "✨ Update Available (v${update.latestVersionName})",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = update.releaseNotes,
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    Button(
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(update.downloadUrl))
                            context.startActivity(intent)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Update", fontSize = 13.sp)
                    }
                }
            }
        }

        // First Boot Setup Guidance (If DB is empty)
        if (customers.isEmpty() || products.isEmpty()) {
            SetupWarningCard(
                customerCount = customers.size,
                productCount = products.size,
                onAddCustomer = { showAddCustomerModal = true },
                onAddProduct = { showAddProductModal = true }
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Main Voice Action Button
        ElevatedCard(
            onClick = {
                showVoiceModal = true
                viewModel.startVoiceRecording()
            },
            colors = CardDefaults.elevatedCardColors(containerColor = MicAccent),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(72.dp)
                        .background(Color.White, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Voice Record",
                        tint = MicAccent,
                        modifier = Modifier.size(44.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = if (currentLanguage == AppLanguage.HINDI) "🎤 बोलकर खाता लिखें" else "🎤 RECORD CREDIT",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                    Text(
                        text = if (currentLanguage == AppLanguage.HINDI) "दबाएं और बोलें (उदा: गुणगुण 5 चिप्स)" else "Tap & speak transaction",
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Manual Entry Fallback Button
        OutlinedButton(
            onClick = onNavigateToManual,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(14.dp)
        ) {
            Icon(Icons.Default.Edit, contentDescription = null, tint = GreenPrimary)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (currentLanguage == AppLanguage.HINDI) "+ हाथ से लिखें (Manual)" else "+ Manual Entry",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = GreenPrimary
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Recent Credit Transactions Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (currentLanguage == AppLanguage.HINDI) "हाल के लेनदेन" else "Recent Transactions",
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = "${transactions.size} entries",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (transactions.isEmpty()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "No credit transactions yet.",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.DarkGray
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Tap the microphone above or use Manual Entry to record credit.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(transactions.take(10)) { item ->
                    TransactionItemRow(item)
                }
            }
        }
    }

    // Voice Dialog Popup
    if (showVoiceModal) {
        VoiceRecordDialog(
            speechState = speechState,
            parseResult = parseResult,
            customers = customers,
            products = products,
            currentLanguage = currentLanguage,
            onLanguageToggle = { lang -> viewModel.setAppLanguage(lang) },
            onDismiss = {
                showVoiceModal = false
                viewModel.resetVoiceState()
            },
            onConfirm = { cust, custName, items ->
                viewModel.confirmVoiceTransaction(cust, custName, items)
                showVoiceModal = false
            },
            onManualFallback = {
                showVoiceModal = false
                viewModel.resetVoiceState()
                onNavigateToManual()
            }
        )
    }

    if (showAddCustomerModal) {
        AddEditCustomerDialog(
            customer = null,
            onDismiss = { showAddCustomerModal = false },
            onSave = { name, phone ->
                viewModel.addCustomer(name, phone)
                showAddCustomerModal = false
            }
        )
    }

    if (showAddProductModal) {
        AddEditProductDialog(
            product = null,
            onDismiss = { showAddProductModal = false },
            onSave = { name, unit ->
                viewModel.addProduct(name, unit)
                showAddProductModal = false
            }
        )
    }
}

@Composable
fun SetupWarningCard(
    customerCount: Int,
    productCount: Int,
    onAddCustomer: () -> Unit,
    onAddProduct: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1)),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Welcome to KhataNow! Setup your shop",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFE65100)
            )
            Text(
                text = "You can add items below or simply speak transactions directly (e.g. 'Gungun 5 chips') to auto-save them on-the-fly!",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF5D4037),
                modifier = Modifier.padding(vertical = 4.dp)
            )
            Row(
                modifier = Modifier.padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (customerCount == 0) {
                    Button(
                        onClick = onAddCustomer,
                        colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("+ Customer", fontSize = 13.sp)
                    }
                }
                if (productCount == 0) {
                    Button(
                        onClick = onAddProduct,
                        colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.ShoppingBag, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("+ Product", fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun TransactionItemRow(item: TransactionWithDetails) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.customerName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${item.productName} × ${item.quantity} ${item.productUnit ?: ""}".trim(),
                    style = MaterialTheme.typography.bodyLarge,
                    color = GreenPrimary,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Column(horizontalAlignment = Alignment.End, modifier = Modifier.padding(start = 8.dp)) {
                Text(
                    text = DateFormatter.formatTime(item.timestamp),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = DateFormatter.formatDate(item.timestamp),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }
    }
}
