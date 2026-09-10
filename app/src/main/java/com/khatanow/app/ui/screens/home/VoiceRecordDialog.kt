package com.khatanow.app.ui.screens.home

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.khatanow.app.data.local.entities.CustomerEntity
import com.khatanow.app.data.local.entities.ProductEntity
import com.khatanow.app.domain.voice.SpeechState
import com.khatanow.app.domain.voice.VoiceParseResult
import com.khatanow.app.ui.theme.GreenPrimary

@Composable
fun VoiceRecordDialog(
    speechState: SpeechState,
    parseResult: VoiceParseResult?,
    customers: List<CustomerEntity>,
    products: List<ProductEntity>,
    onDismiss: () -> Unit,
    onConfirm: (CustomerEntity, ProductEntity, Int) -> Unit,
    onManualFallback: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (parseResult != null) "Confirm Voice Transaction" else "Listening for Voice Credit",
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (parseResult == null) {
                    // Speech Recording Mode
                    when (speechState) {
                        is SpeechState.Listening -> {
                            PulsingMicIcon()
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Speak now...",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = GreenPrimary
                            )
                            Text(
                                text = "Example format:\n'Customer 5 Product'",
                                style = MaterialTheme.typography.bodySmall,
                                textAlign = TextAlign.Center,
                                color = Color.Gray,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                        is SpeechState.PartialResult -> {
                            PulsingMicIcon()
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "\"${speechState.text}\"",
                                style = MaterialTheme.typography.titleMedium,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        is SpeechState.Error -> {
                            Text(
                                text = "⚠️ ${speechState.message}",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.error,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = onManualFallback,
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                            ) {
                                Text("Switch to Manual Entry")
                            }
                        }
                        else -> {
                            Text("Initializing speech recognition...")
                        }
                    }
                } else {
                    // Parsed Confirmation Mode
                    ConfirmationCardContent(
                        parseResult = parseResult,
                        allCustomers = customers,
                        allProducts = products,
                        onConfirm = onConfirm,
                        onEditManual = onManualFallback
                    )
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            if (parseResult == null) {
                TextButton(onClick = onDismiss) {
                    Text("Cancel", fontSize = 18.sp)
                }
            }
        }
    )
}

@Composable
fun PulsingMicIcon() {
    val infiniteTransition = rememberInfiniteTransition(label = "mic_pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "mic_scale"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(90.dp)
            .scale(scale)
            .background(Color(0xFFFFEBEE), CircleShape)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(70.dp)
                .background(Color(0xFFD32F2F), CircleShape)
        ) {
            Icon(
                imageVector = Icons.Default.Mic,
                contentDescription = "Listening",
                tint = Color.White,
                modifier = Modifier.size(40.dp)
            )
        }
    }
}

@Composable
fun ConfirmationCardContent(
    parseResult: VoiceParseResult,
    allCustomers: List<CustomerEntity>,
    allProducts: List<ProductEntity>,
    onConfirm: (CustomerEntity, ProductEntity, Int) -> Unit,
    onEditManual: () -> Unit
) {
    var selectedCustomer by remember { mutableStateOf(parseResult.matchedCustomer ?: allCustomers.firstOrNull()) }
    var selectedProduct by remember { mutableStateOf(parseResult.matchedProduct ?: allProducts.firstOrNull()) }
    var quantity by remember { mutableIntStateOf(parseResult.quantity) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Spoken: \"${parseResult.rawText}\"", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                Spacer(modifier = Modifier.height(12.dp))

                // Customer Field
                Text("CUSTOMER", style = MaterialTheme.typography.labelMedium, color = GreenPrimary)
                Text(
                    text = selectedCustomer?.name ?: "⚠️ Not Found (Select in Edit)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Product Field
                Text("PRODUCT", style = MaterialTheme.typography.labelMedium, color = GreenPrimary)
                Text(
                    text = selectedProduct?.name ?: "⚠️ Not Found (Select in Edit)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Quantity Selector
                Text("QUANTITY", style = MaterialTheme.typography.labelMedium, color = GreenPrimary)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                ) {
                    IconButton(
                        onClick = { if (quantity > 1) quantity-- },
                        modifier = Modifier.background(Color.White, CircleShape)
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = "Decrease")
                    }
                    Text(
                        text = "$quantity",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                    IconButton(
                        onClick = { quantity++ },
                        modifier = Modifier.background(Color.White, CircleShape)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Increase")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Action Buttons
        Button(
            onClick = {
                val cust = selectedCustomer
                val prod = selectedProduct
                if (cust != null && prod != null) {
                    onConfirm(cust, prod, quantity)
                }
            },
            enabled = selectedCustomer != null && selectedProduct != null,
            colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Icon(Icons.Default.Check, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("✓ CONFIRM CREDIT", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedButton(
            onClick = onEditManual,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Icon(Icons.Default.Edit, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("✏ Edit Manually", fontSize = 16.sp)
        }
    }
}
