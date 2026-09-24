package com.sw5e.datapad.ui.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.sw5e.datapad.ui.theme.*

@Composable
fun FlatBoostDialog(
    currentBoost: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var boostText by remember { mutableStateOf(currentBoost.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Set Global Attack & Damage Boost", color = HoloBlue) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "Apply a flat modifier to all attack and damage rolls.",
                    style = MaterialTheme.typography.bodyMedium
                )
                OutlinedTextField(
                    value = boostText,
                    onValueChange = { boostText = it },
                    label = { Text("Flat Bonus") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val parsed = boostText.toIntOrNull() ?: 0
                    onConfirm(parsed)
                }
            ) {
                Text("Apply", color = NeonAmber, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        containerColor = SpaceBlack,
        shape = RoundedCornerShape(12.dp)
    )
}