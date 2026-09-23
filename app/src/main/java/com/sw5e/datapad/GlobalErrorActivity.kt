package com.sw5e.datapad

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sw5e.datapad.ui.theme.SW5ETheme

class GlobalErrorActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val crashText = intent.getStringExtra("CRASH_DETAILS") ?: "Unknown crash occurred."

        setContent {
            SW5ETheme {
                var copied by remember { mutableStateOf(false) }
                val context = LocalContext.current

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AlertDialog(
                        onDismissRequest = {}, // Prevent dismissing so the user can read the error
                        title = { Text("App Crash Caught") },
                        text = {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 400.dp)
                                    .verticalScroll(rememberScrollState()),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text("A fatal error occurred anywhere in the app (DB, UI, or JS):")
                                Text(
                                    text = crashText,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        },
                        confirmButton = {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText("Crash StackTrace", crashText)
                                    clipboard.setPrimaryClip(clip)
                                    copied = true
                                }) {
                                    Text(if (copied) "Copied!" else "Copy Error")
                                }
                                Button(onClick = {
                                    // Restart the app
                                    val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
                                    context.startActivity(intent)
                                    finish()
                                }) {
                                    Text("Restart App")
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}