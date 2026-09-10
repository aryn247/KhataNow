package com.khatanow.app.ui.screens.home

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Language
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
import androidx.compose.material3.OutlinedTextField
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
import com.khatanow.app.util.AppLanguage

@Composable
fun VoiceRecordDialog(
    speechState: SpeechState,
    parseResult: VoiceParseResult?,
    customers: List<CustomerEntity>,
    products: List<ProductEntity>,
    currentLanguage: AppLanguage,
    onLanguageToggle: (AppLanguage) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: (CustomerEntity?, String, ProductEntity?, String, Int) -> Unit,
    onManualFallback: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (parseResult != null) "Confirm Credit Entry" else "Voice Credit Record",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                // Language Switcher Badge (EN vs HI)
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.clickable {
                        val nextLang = if (currentLanguage == AppLanguage.ENGLISH) AppLanguage.HINDI else AppLanguage.ENGLISH
                        onLanguageToggle(nextLang)
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Language, contentDescription = null, modifier = Modifier.size(16.dp), tint = GreenPrimary)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (currentLanguage == AppLanguage.ENGLISH) "EN" else "हिंदी",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = GreenPrimary
                        )
                    }
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (parseResult == null) {
                    // Speech Recording Mode
                    when (speechState) {
                        is SpeechState.Listening -> {
                            PulsingMicIcon()
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = if (currentLanguage == AppLanguage.HINDI) "बोलिए (उदा: गुणगुण 5 चिप्स)..." else "Speak now (e.g., Gungun 5 chips)...",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = GreenPrimary,
                                textAlign = TextAlign.Center
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
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.error,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = onManualFallback,
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                            ) {
                                Text("Switch to Manual Entry")
                            }
                        }
                        else -> {
                            Text("Initializing microphone...")
                        }
                    }
                } else {
                    // Parsed Confirmation Mode with Auto-Create capability!
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
                    Text("Cancel", fontSize = 16.sp)
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
            .size(80.dp)
            .scale(scale)
            .background(Color(0xFFFFEBEE), CircleShape)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(60.dp)
                .background(Color(0xFFD32F2F), CircleShape)
        ) {
            Icon(
                imageVector = Icons.Default.Mic,
                contentDescription = "Listening",
                tint = Color.White,
                modifier = Modifier.size(36.dp)
            )
        }
    }
}

@Composable
fun ConfirmationCardContent(
    parseResult: VoiceParseResult,
    allCustomers: List<CustomerEntity>,
    allProducts: List<ProductEntity>,
    onConfirm: (CustomerEntity?, String, ProductEntity?, String, Int) -> Unit,
    onEditManual: () -> Unit
) {
    var selectedCustomer by remember { mutableStateOf(parseResult.matchedCustomer) }
    var candidateCustomerName by remember { mutableStateOf(parseResult.candidateCustomerName) }

    var selectedProduct by remember { mutableStateOf(parseResult.matchedProduct) }
    var candidateProductName by remember { mutableStateOf(parseResult.candidateProductName) }

    var quantity by remember { mutableIntStateOf(parseResult.quantity) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Spoken: \"${parseResult.rawText}\"", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                Spacer(modifier = Modifier.height(10.dp))

                // Customer Field with On-The-Fly Auto-Create
                Text("CUSTOMER", style = MaterialTheme.typography.labelMedium, color = GreenPrimary)
                if (selectedCustomer != null) {
                    Text(
                        text = selectedCustomer!!.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth().padding(top = 2.dp)
                    ) {
                        OutlinedTextField(
                            value = candidateCustomerName,
                            onValueChange = { candidateCustomerName = it },
                            label = { Text("New Customer Name") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Text(
                        text = "✨ Will auto-save to customer directory",
                        style = MaterialTheme.typography.bodySmall,
                        color = GreenPrimary,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Product Field with On-The-Fly Auto-Create
                Text("PRODUCT", style = MaterialTheme.typography.labelMedium, color = GreenPrimary)
                if (selectedProduct != null) {
                    Text(
                        text = selectedProduct!!.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth().padding(top = 2.dp)
                    ) {
                        OutlinedTextField(
                            value = candidateProductName,
                            onValueChange = { candidateProductName = it },
                            label = { Text("New Product Name") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Text(
                        text = "✨ Will auto-save to product inventory",
                        style = MaterialTheme.typography.bodySmall,
                        color = GreenPrimary,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Quantity Selector
                Text("QUANTITY", style = MaterialTheme.typography.labelMedium, color = GreenPrimary)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
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

        Spacer(modifier = Modifier.height(16.dp))

        // Action Buttons
        Button(
            onClick = {
                onConfirm(
                    selectedCustomer,
                    candidateCustomerName,
                    selectedProduct,
                    candidateProductName,
                    quantity
                )
            },
            colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
        ) {
            Icon(Icons.Default.Check, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("✓ CONFIRM CREDIT", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedButton(
            onClick = onEditManual,
            modifier = Modifier
                .fillMaxWidth()
                .height(46.dp)
        ) {
            Icon(Icons.Default.Edit, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("✏ Edit Manually", fontSize = 15.sp)
        }
    }
}
