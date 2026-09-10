package com.khatanow.app.ui.screens.home

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.ShoppingBag
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.khatanow.app.data.local.entities.TransactionWithDetails
import com.khatanow.app.ui.MainViewModel
import com.khatanow.app.ui.screens.customers.AddEditCustomerDialog
import com.khatanow.app.ui.screens.products.AddEditProductDialog
import com.khatanow.app.ui.theme.GreenPrimary
import com.khatanow.app.ui.theme.MicAccent
import com.khatanow.app.util.DateFormatter

@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    onNavigateToManual: () -> Unit,
    onNavigateToCustomers: () -> Unit,
    onNavigateToProducts: () -> Unit
) {
    val customers by viewModel.customers.collectAsState()
    val products by viewModel.products.collectAsState()
    val transactions by viewModel.transactions.collectAsState()
    val speechState by viewModel.speechState.collectAsState()
    val parseResult by viewModel.parseResult.collectAsState()

    var showVoiceModal by remember { mutableStateOf(false) }
    var showAddCustomerModal by remember { mutableStateOf(false) }
    var showAddProductModal by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // App Title Banner
        Text(
            text = "KhataNow",
            style = MaterialTheme.typography.headlineLarge,
            color = GreenPrimary
        )
        Text(
            text = "Local-First Voice Credit Ledger",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(20.dp))

        // First Boot Setup Guidance (If DB is empty)
        if (customers.isEmpty() || products.isEmpty()) {
            SetupWarningCard(
                customerCount = customers.size,
                productCount = products.size,
                onAddCustomer = { showAddCustomerModal = true },
                onAddProduct = { showAddProductModal = true }
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Main Voice Action Button (Rush-Hour Primary CTA)
        ElevatedCard(
            onClick = {
                showVoiceModal = true
                viewModel.startVoiceRecording()
            },
            colors = CardDefaults.elevatedCardColors(containerColor = MicAccent),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
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
                        .size(76.dp)
                        .background(Color.White, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Voice Record",
                        tint = MicAccent,
                        modifier = Modifier.size(48.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = "🎤 RECORD CREDIT",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                    Text(
                        text = "Tap & speak transaction",
                        fontSize = 15.sp,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Manual Entry Fallback Button
        OutlinedButton(
            onClick = onNavigateToManual,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(14.dp)
        ) {
            Icon(Icons.Default.Edit, contentDescription = null, tint = GreenPrimary)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "+ Manual Entry",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = GreenPrimary
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Recent Credit Transactions Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Recent Transactions",
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = "${transactions.size} records",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (transactions.isEmpty()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "No credit transactions yet.",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.DarkGray
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Tap the microphone above or use Manual Entry to add your shop's first credit entry.",
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
            onDismiss = {
                showVoiceModal = false
                viewModel.resetVoiceState()
            },
            onConfirm = { customer, product, qty ->
                viewModel.confirmVoiceTransaction(customer, product, qty)
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
                text = "Voice parsing relies on your local Customers and Products database. Add your shop's items to start.",
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
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.customerName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${item.productName} × ${item.quantity} ${item.productUnit ?: ""}".trim(),
                    style = MaterialTheme.typography.bodyLarge,
                    color = GreenPrimary,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Column(horizontalAlignment = Alignment.End) {
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
