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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
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
import androidx.compose.runtime.mutableStateListOf
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
import com.khatanow.app.domain.voice.ParsedVoiceItem
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
    onConfirm: (CustomerEntity?, String, List<ParsedVoiceItem>) -> Unit,
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
                                text = if (currentLanguage == AppLanguage.HINDI) "बोलिए..." else "Listening... Speak now",
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
                    // Parsed Confirmation Mode supporting Multi-Item entries
                    ConfirmationCardContent(
                        parseResult = parseResult,
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
    onConfirm: (CustomerEntity?, String, List<ParsedVoiceItem>) -> Unit,
    onEditManual: () -> Unit
) {
    var selectedCustomer by remember { mutableStateOf(parseResult.matchedCustomer) }
    var candidateCustomerName by remember { mutableStateOf(parseResult.candidateCustomerName) }
    val itemsList = remember { mutableStateListOf<ParsedVoiceItem>().apply { addAll(parseResult.items) } }

    Column(modifier = Modifier.fillMaxWidth()) {
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text("Spoken: \"${parseResult.rawText}\"", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                Spacer(modifier = Modifier.height(8.dp))

                // Customer Field with On-The-Fly Auto-Create
                Text("CUSTOMER", style = MaterialTheme.typography.labelMedium, color = GreenPrimary)
                if (selectedCustomer != null) {
                    Text(
                        text = selectedCustomer!!.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    OutlinedTextField(
                        value = candidateCustomerName,
                        onValueChange = { candidateCustomerName = it },
                        label = { Text("Customer Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().padding(top = 2.dp)
                    )
                    Text(
                        text = "✨ Auto-saves to customer list",
                        style = MaterialTheme.typography.bodySmall,
                        color = GreenPrimary,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Multi-Item List Header
                Text("PARSED ITEMS (${itemsList.size})", style = MaterialTheme.typography.labelMedium, color = GreenPrimary)
                Spacer(modifier = Modifier.height(4.dp))

                itemsList.forEachIndexed { index, item ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.matchedProduct?.name ?: item.candidateProductName,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                if (item.matchedProduct == null) {
                                    Text("✨ Auto-saves product", style = MaterialTheme.typography.bodySmall, color = GreenPrimary)
                                }
                            }

                            // Quantity Selector per Item
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = {
                                        if (item.quantity > 1) {
                                            itemsList[index] = item.copy(quantity = item.quantity - 1)
                                        }
                                    },
                                    modifier = Modifier.size(32.dp).background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                                ) {
                                    Icon(Icons.Default.Remove, contentDescription = "Decrease", modifier = Modifier.size(16.dp))
                                }
                                Text(
                                    text = "${item.quantity}",
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 10.dp)
                                )
                                IconButton(
                                    onClick = { itemsList[index] = item.copy(quantity = item.quantity + 1) },
                                    modifier = Modifier.size(32.dp).background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = "Increase", modifier = Modifier.size(16.dp))
                                }
                                if (itemsList.size > 1) {
                                    IconButton(
                                        onClick = { itemsList.removeAt(index) },
                                        modifier = Modifier.size(32.dp).padding(start = 4.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Remove Item", tint = Color.Red)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Action Buttons
        Button(
            onClick = {
                onConfirm(
                    selectedCustomer,
                    candidateCustomerName,
                    itemsList
                )
            },
            enabled = itemsList.isNotEmpty(),
            colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Icon(Icons.Default.Check, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (itemsList.size > 1) "✓ CONFIRM ${itemsList.size} ITEMS" else "✓ CONFIRM CREDIT",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )
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
