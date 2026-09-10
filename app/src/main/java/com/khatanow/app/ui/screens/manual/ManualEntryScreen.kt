package com.khatanow.app.ui.screens.manual

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.khatanow.app.data.local.entities.CustomerEntity
import com.khatanow.app.data.local.entities.ProductEntity
import com.khatanow.app.ui.MainViewModel
import com.khatanow.app.ui.screens.customers.AddEditCustomerDialog
import com.khatanow.app.ui.screens.products.AddEditProductDialog
import com.khatanow.app.ui.theme.GreenPrimary

@Composable
fun ManualEntryScreen(
    viewModel: MainViewModel,
    onSuccessBack: () -> Unit
) {
    val customers by viewModel.customers.collectAsState()
    val products by viewModel.products.collectAsState()

    var selectedCustomer by remember { mutableStateOf<CustomerEntity?>(null) }
    var selectedProduct by remember { mutableStateOf<ProductEntity?>(null) }
    var quantity by remember { mutableIntStateOf(1) }

    var customerSearch by remember { mutableStateOf("") }
    var productSearch by remember { mutableStateOf("") }

    var showAddCustomerDialog by remember { mutableStateOf(false) }
    var showAddProductDialog by remember { mutableStateOf(false) }

    val filteredCustomers = remember(customers, customerSearch) {
        if (customerSearch.isBlank()) customers
        else customers.filter { it.name.contains(customerSearch, ignoreCase = true) }
    }

    val filteredProducts = remember(products, productSearch) {
        if (productSearch.isBlank()) products
        else products.filter { it.name.contains(productSearch, ignoreCase = true) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Manual Credit Entry",
            style = MaterialTheme.typography.headlineMedium,
            color = GreenPrimary
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Step 1: Select Customer
        Text(
            text = "1. SELECT CUSTOMER",
            style = MaterialTheme.typography.labelLarge,
            color = GreenPrimary
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        ) {
            OutlinedTextField(
                value = customerSearch,
                onValueChange = { customerSearch = it },
                placeholder = { Text("Search customer...") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = { showAddCustomerDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
            }
        }

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .padding(vertical = 4.dp)
        ) {
            items(filteredCustomers) { cust ->
                val isSelected = selectedCustomer?.id == cust.id
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) GreenPrimary else MaterialTheme.colorScheme.surfaceVariant
                    ),
                    modifier = Modifier.clickable { selectedCustomer = cust }
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Text(
                            text = cust.name,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color.White else Color.Black
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Step 2: Select Product
        Text(
            text = "2. SELECT PRODUCT",
            style = MaterialTheme.typography.labelLarge,
            color = GreenPrimary
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        ) {
            OutlinedTextField(
                value = productSearch,
                onValueChange = { productSearch = it },
                placeholder = { Text("Search product...") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = { showAddProductDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
            }
        }

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .padding(vertical = 4.dp)
        ) {
            items(filteredProducts) { prod ->
                val isSelected = selectedProduct?.id == prod.id
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) GreenPrimary else MaterialTheme.colorScheme.surfaceVariant
                    ),
                    modifier = Modifier.clickable { selectedProduct = prod }
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Text(
                            text = prod.name,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color.White else Color.Black
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Step 3: Select Quantity
        Text(
            text = "3. SELECT QUANTITY",
            style = MaterialTheme.typography.labelLarge,
            color = GreenPrimary
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
        ) {
            IconButton(
                onClick = { if (quantity > 1) quantity-- },
                modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
            ) {
                Icon(Icons.Default.Remove, contentDescription = "Decrease")
            }
            Text(
                text = "$quantity",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Black,
                modifier = Modifier.padding(horizontal = 32.dp)
            )
            IconButton(
                onClick = { quantity++ },
                modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Increase")
            }
        }

        // Quick Quantity Chips
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxWidth()
        ) {
            listOf(1, 2, 5, 10, 12).forEach { q ->
                OutlinedButton(
                    onClick = { quantity = q },
                    modifier = Modifier.weight(1f).padding(4.dp)
                ) {
                    Text("$q", fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Save Button
        val isReady = selectedCustomer != null && selectedProduct != null
        Button(
            onClick = {
                val cust = selectedCustomer
                val prod = selectedProduct
                if (cust != null && prod != null) {
                    viewModel.saveManualTransaction(cust.id, prod.id, quantity, onSuccessBack)
                }
            },
            enabled = isReady,
            colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
        ) {
            Icon(Icons.Default.Check, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("SAVE CREDIT TRANSACTION", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }

    if (showAddCustomerDialog) {
        AddEditCustomerDialog(
            customer = null,
            onDismiss = { showAddCustomerDialog = false },
            onSave = { name, phone ->
                viewModel.addCustomer(name, phone)
                showAddCustomerDialog = false
            }
        )
    }

    if (showAddProductDialog) {
        AddEditProductDialog(
            product = null,
            onDismiss = { showAddProductDialog = false },
            onSave = { name, unit ->
                viewModel.addProduct(name, unit)
                showAddProductDialog = false
            }
        )
    }
}
