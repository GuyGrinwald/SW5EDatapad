package com.sw5e.datapad.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.sw5e.datapad.data.AppDatabase
import com.sw5e.datapad.data.CharacterEntity
import com.sw5e.datapad.data.CharacterSkillEntity
import com.sw5e.datapad.ui.MainViewModel
import com.sw5e.datapad.ui.theme.CardBorder
import com.sw5e.datapad.ui.theme.HoloBlue
import com.sw5e.datapad.ui.theme.NeonAmber
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.InputStreamReader
import java.io.OutputStreamWriter

@Composable
fun DataVaultScreen(viewModel: MainViewModel) {
    val context = LocalContext.current
    var statusMessage by remember { mutableStateOf("") }

    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
            if (uri != null) { 
                viewModel.exportToFile(context, uri)
                statusMessage = "Exported Successfully!"
            }
        }

    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) { 
            viewModel.importFromFile(context, uri)
            statusMessage = "Imported Successfully!"
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Data Vault & Archives", style = MaterialTheme.typography.titleLarge, color = HoloBlue)

        Card(modifier = Modifier.fillMaxWidth().border(1.dp, CardBorder, RoundedCornerShape(12.dp))) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Native Offline Backup (JSON)", style = MaterialTheme.typography.titleMedium, color = NeonAmber)
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(onClick = { exportLauncher.launch("sw5e_character.json") }, modifier = Modifier.fillMaxWidth()) {
                    Text("Export Character")
                }
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = { importLauncher.launch(arrayOf("application/json")) }, modifier = Modifier.fillMaxWidth()) {
                    Text("Import Character")
                }

                if (statusMessage.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(statusMessage, color = HoloBlue)
                }
            }
        }
    }
}