package com.sw5e.datapad.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sw5e.datapad.ui.theme.*

@Composable
fun HeaderBadge(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelSmall)
        Text(value, style = MaterialTheme.typography.titleMedium, color = HoloBlue, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun StatBox(label: String, score: Int, modifier: Int, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .width(52.dp)
            .border(1.dp, CardBorder, RoundedCornerShape(8.dp))
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(6.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
            Text("$score", style = MaterialTheme.typography.titleMedium)
            Text(if (modifier >= 0) "+$modifier" else "$modifier", color = HoloBlue)
        }
    }
}

@Composable
fun SaveBox(label: String, isProficient: Boolean, modifier: Int, profBonus: Int) {
    val total = modifier + if (isProficient) profBonus else 0
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
        Text(if (total >= 0) "+$total" else "$total", color = if (isProficient) NeonAmber else HoloBlue)
    }
}