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
import com.google.gson.Gson
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

// A wrapper payload is required so skills are serialized and deserialized
// seamlessly alongside the main character sheet.
data class ExportWrapper(
    val character: CharacterEntity,
    val skills: List<CharacterSkillEntity>
)

@Composable
fun DataVaultScreen(viewModel: MainViewModel) {
    val context = LocalContext.current
    var statusMessage by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    
    val character by viewModel.character.collectAsState()
    val skills by viewModel.skills.collectAsState()

    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        if (uri != null) { 
            scope.launch(Dispatchers.IO) {
                try {
                    val wrapper = ExportWrapper(character, skills)
                    val jsonString = Gson().toJson(wrapper)
                    context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                        OutputStreamWriter(outputStream).use { writer ->
                            writer.write(jsonString)
                        }
                    }
                    withContext(Dispatchers.Main) { statusMessage = "Exported Successfully!" }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) { statusMessage = "Export Failed: ${e.message}" }
                }
            }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) { 
            scope.launch(Dispatchers.IO) {
                try {
                    context.contentResolver.openInputStream(uri)?.use { inputStream ->
                        val jsonString = InputStreamReader(inputStream).readText()
                        val db = AppDatabase.getInstance(context)
                        val dao = db.characterDao()
                        
                        try {
                            val wrapper = Gson().fromJson(jsonString, ExportWrapper::class.java)
                            if (wrapper.character != null && wrapper.skills != null) {
                                // Save straight to the DB to ensure long-term persistence across restarts
                                dao.insertCharacter(wrapper.character)
                                dao.insertSkills(wrapper.skills)
                                withContext(Dispatchers.Main) {
                                    viewModel.updateCharacter(wrapper.character)
                                    statusMessage = "Imported Successfully!"
                                }
                            } else {
                                throw IllegalArgumentException("Not a valid wrapper")
                            }
                        } catch (e: Exception) {
                            // Fallback in case user loads an older exported JSON format containing only CharacterEntity
                            val char = Gson().fromJson(jsonString, CharacterEntity::class.java)
                            if (char != null) {
                                dao.insertCharacter(char)
                                withContext(Dispatchers.Main) {
                                    viewModel.updateCharacter(char)
                                    statusMessage = "Imported Successfully (Legacy)!"
                                }
                            } else {
                                withContext(Dispatchers.Main) { statusMessage = "Import Failed: Invalid JSON" }
                            }
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) { statusMessage = "Import Failed: ${e.message}" }
                }
            }
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