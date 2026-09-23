package com.sw5e.datapad.ui.screens

import android.os.Handler
import android.os.Looper
import android.view.View
import android.webkit.JavascriptInterface
import android.webkit.WebView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.sw5e.datapad.ui.AdvantageMode
import com.sw5e.datapad.ui.MainViewModel
import com.sw5e.datapad.ui.theme.*
import androidx.webkit.WebViewAssetLoader
import androidx.webkit.WebViewClientCompat
import org.json.JSONArray

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TacticalRollerScreen(viewModel: MainViewModel) {
    var dicePool by remember { mutableStateOf<Map<Int, Int>>(emptyMap()) }
    var modifierText by remember { mutableStateOf("0") }
    var advantageMode by remember { mutableStateOf(AdvantageMode.NORMAL) }
    var webTrigger by remember { mutableIntStateOf(0) }
    var showColorPickerModal by remember { mutableStateOf(false) }
    var isWebEngineReady by remember { mutableStateOf(false) }
    
    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    val lastRoll by viewModel.lastRoll.collectAsState()

    var webScriptError by remember { mutableStateOf<String?>(null) }

    val diceColors = mapOf(
        "Red" to "#8B0000", "Blue" to "#00008B", "Green" to "#006400",
        "Yellow" to "#B8860B", "Purple" to "#4B0082", "Silver" to "#C0C0C0"
    )
    var selectedColor by remember { mutableStateOf(diceColors["Yellow"]!!) }

    if (webScriptError != null) {
        AlertDialog(
            onDismissRequest = { webScriptError = null },
            title = { Text("Dice Engine Error", color = SithRed) },
            text = { Text(webScriptError ?: "") },
            confirmButton = {
                Button(onClick = { webScriptError = null }) { Text("Dismiss") }
            }
        )
    }

    if (showColorPickerModal) {
        AlertDialog(
            onDismissRequest = { showColorPickerModal = false },
            title = { Text("Select Dice Color", color = NeonAmber) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    diceColors.forEach { (name, hex) ->
                        Button(
                            onClick = { 
                                selectedColor = hex 
                                showColorPickerModal = false
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selectedColor == hex) NeonAmber else CardBorder
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(name, color = if (selectedColor == hex) SpaceBlack else androidx.compose.ui.graphics.Color.White)
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showColorPickerModal = false }) { Text("Done") }
            }
        )
    }

    // Trigger 3D dice roll in WebView
    LaunchedEffect(webTrigger) {
        if (webTrigger > 0) {
            val adjustedPool = mutableMapOf<Int, Int>()

            if (advantageMode != AdvantageMode.NORMAL) {
                adjustedPool[20] = 2
            } else if (dicePool.isEmpty()) {
                adjustedPool[20] = 1
            } else {
                adjustedPool.putAll(dicePool)
            }

            val diceNotationParts = mutableListOf<String>()
            adjustedPool.entries.filter { it.value > 0 }.forEach { (sides, count) ->
                diceNotationParts.add("${count}d$sides")
            }
            
            val formulaArray = diceNotationParts.joinToString(prefix = "[", postfix = "]", separator = ",") { "'$it'" }
            webViewRef?.evaluateJavascript("if(window.triggerRoll) window.triggerRoll($formulaArray);", null)
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Card(modifier = Modifier.fillMaxWidth().border(1.dp, CardBorder, RoundedCornerShape(8.dp))) {
            Text(lastRoll?.breakdownText ?: "Select dice pool & tap ROLL", modifier = Modifier.padding(12.dp), color = NeonAmber)
        }

        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.fillMaxWidth()) {
            listOf(4, 6, 8, 10, 12, 20).forEach { d ->
                val count = dicePool[d] ?: 0
                FilterChip(
                    selected = count > 0, 
                    onClick = { 
                        // Problem 1 Fix: Selecting individual dice turns off ADV/DIS
                        if (advantageMode != AdvantageMode.NORMAL) {
                            advantageMode = AdvantageMode.NORMAL
                            dicePool = mapOf(d to 1)
                        } else {
                            dicePool = dicePool + (d to count + 1)
                        }
                    }, 
                    label = { Text(if (count > 0) "${count}d$d" else "d$d") }
                )
            }
            if (dicePool.isNotEmpty() || advantageMode != AdvantageMode.NORMAL) {
                FilterChip(
                    selected = false,
                    onClick = { 
                        dicePool = emptyMap() 
                        advantageMode = AdvantageMode.NORMAL
                    },
                    label = { Text("Clear") }
                )
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = modifierText, 
                onValueChange = { modifierText = it }, 
                label = { Text("Modifier") }, 
                modifier = Modifier.width(90.dp), 
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Problem 1 Fix: Selecting ADV clears other dice and locks to 2d20
                FilterChip(
                    selected = advantageMode == AdvantageMode.ADVANTAGE, 
                    onClick = { 
                        val newMode = if (advantageMode == AdvantageMode.ADVANTAGE) AdvantageMode.NORMAL else AdvantageMode.ADVANTAGE
                        advantageMode = newMode
                        if (newMode != AdvantageMode.NORMAL) {
                            dicePool = mapOf(20 to 2)
                        }
                    }, 
                    label = { Text("ADV") }
                )
                Spacer(modifier = Modifier.width(4.dp))
                // Problem 1 Fix: Selecting DIS clears other dice and locks to 2d20
                FilterChip(
                    selected = advantageMode == AdvantageMode.DISADVANTAGE, 
                    onClick = { 
                        val newMode = if (advantageMode == AdvantageMode.DISADVANTAGE) AdvantageMode.NORMAL else AdvantageMode.DISADVANTAGE
                        advantageMode = newMode
                        if (newMode != AdvantageMode.NORMAL) {
                            dicePool = mapOf(20 to 2)
                        }
                    }, 
                    label = { Text("DIS") }
                )
            }

            OutlinedButton(
                onClick = { showColorPickerModal = true },
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text("Color", color = NeonAmber)
            }
        }

        Button(
            onClick = {
                val mod = modifierText.toIntOrNull() ?: 0
                // Problem 2 Fix: If 3D engine is active, trigger 3D roll and wait for onRollComplete callback.
                // Otherwise (offline/error), perform immediate calculation in ViewModel.
                if (isWebEngineReady && webScriptError == null) {
                    webTrigger++
                } else {
                    viewModel.executeRoll(dicePool, mod, advantageMode)
                }
            }, 
            modifier = Modifier.fillMaxWidth()
        ) { 
            Text("EXECUTE TACTICAL ROLL") 
        }

        Box(modifier = Modifier.weight(2.5f).fillMaxWidth().border(1.dp, CardBorder, RoundedCornerShape(8.dp)).background(SpaceBlack)) {
            key(selectedColor) {
                val context = androidx.compose.ui.platform.LocalContext.current
                val assetLoader = remember {
                    WebViewAssetLoader.Builder()
                        .addPathHandler("/assets/", WebViewAssetLoader.AssetsPathHandler(context))
                        .build()
                }

                AndroidView(
                    factory = { ctx ->
                        WebView(ctx).apply {
                            settings.javaScriptEnabled = true
                            settings.domStorageEnabled = true
                            setLayerType(View.LAYER_TYPE_HARDWARE, null)
                            setBackgroundColor(0xFF0B0F19.toInt()) 
                            
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                                WebView.setWebContentsDebuggingEnabled(true)
                            }

                            addJavascriptInterface(object {
                                @JavascriptInterface
                                fun onEngineReady() {
                                    Handler(Looper.getMainLooper()).post {
                                        isWebEngineReady = true
                                    }
                                }

                                @JavascriptInterface
                                fun reportError(errorMsg: String) {
                                    Handler(Looper.getMainLooper()).post {
                                        webScriptError = errorMsg
                                        // Fallback calculation if engine errors
                                        viewModel.executeRoll(dicePool, modifierText.toIntOrNull() ?: 0, advantageMode)
                                    }
                                }

                                // Problem 2 Fix: Receive 3D dice results from JS and calculate text output from them
                                @JavascriptInterface
                                fun onRollComplete(jsonResults: String) {
                                    Handler(Looper.getMainLooper()).post {
                                        try {
                                            val jsonArray = JSONArray(jsonResults)
                                            val rolledValues = List(jsonArray.length()) { jsonArray.getInt(it) }
                                            viewModel.process3DRollResults(rolledValues, modifierText.toIntOrNull() ?: 0, advantageMode)
                                        } catch (e: Exception) {
                                            viewModel.executeRoll(dicePool, modifierText.toIntOrNull() ?: 0, advantageMode)
                                        }
                                    }
                                }
                            }, "AndroidBridge")

                            webViewClient = object : WebViewClientCompat() {
                                override fun shouldInterceptRequest(
                                    view: WebView,
                                    request: android.webkit.WebResourceRequest
                                ): android.webkit.WebResourceResponse? {
                                    return assetLoader.shouldInterceptRequest(request.url)
                                }
                            }
                            
                            val htmlData = """
                                <!DOCTYPE html><html><head>
                                <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
                                <style>
                                    html, body { margin: 0; padding: 0; width: 100%; height: 100%; background: #0B0F19; overflow: hidden; }
                                    #dice-canvas { width: 100%; height: 100%; position: absolute; top: 0; left: 0; }
                                </style>
                                </head><body>
                                <div id="dice-canvas"></div>
                                <script>
                                    window.onerror = function(msg, url, line) {
                                        window.AndroidBridge.reportError("JS Error: " + msg + " (Line: " + line + ")");
                                    };
                                </script>
                                <script type="module">
                                import DiceBox from 'https://unpkg.com/@3d-dice/dice-box@1.1.3/dist/dice-box.es.min.js';
                                
                                let isInitialized = false;
                                let pendingFormula = null;

                                try {
                                    window.Box = new DiceBox("#dice-canvas", { 
                                        assetPath: 'assets/',
                                        origin: 'https://unpkg.com/@3d-dice/dice-box@1.1.3/dist/',
                                        themeColor: '$selectedColor',
                                        scale: 12,          
                                        throwForce: 3,     
                                        startingHeight: 4, 
                                        gravity: 1.5,       
                                    });

                                    window.Box.init().then(() => {
                                        isInitialized = true;
                                        window.AndroidBridge.onEngineReady();

                                        // Problem 2 Fix: Extract 3D dice values when physical roll finishes
                                        window.Box.onRollComplete = function(results) {
                                            try {
                                                let rolledValues = [];
                                                if (Array.isArray(results)) {
                                                    results.forEach(group => {
                                                        if (group.rolls && Array.isArray(group.rolls)) {
                                                            group.rolls.forEach(r => {
                                                                if (typeof r.value === 'number') rolledValues.push(r.value);
                                                            });
                                                        } else if (typeof group.value === 'number') {
                                                            rolledValues.push(group.value);
                                                        }
                                                    });
                                                }
                                                window.AndroidBridge.onRollComplete(JSON.stringify(rolledValues));
                                            } catch(e) {
                                                window.AndroidBridge.reportError("onRollComplete Exception: " + e.message);
                                            }
                                        };

                                        if (pendingFormula) {
                                            window.Box.roll(pendingFormula);
                                            pendingFormula = null;
                                        }
                                    }).catch(err => {
                                        window.AndroidBridge.reportError("Init Catch: " + (err.message || err));
                                    });
                                } catch (e) {
                                    window.AndroidBridge.reportError("Instantiation Exception: " + e.message);
                                }

                                window.triggerRoll = function(formula) {
                                    try {
                                        if (isInitialized) {
                                            window.Box.roll(formula);
                                        } else {
                                            pendingFormula = formula;
                                        }
                                    } catch (e) {
                                        window.AndroidBridge.reportError("Roll Exception: " + e.message);
                                    }
                                };
                                </script></body></html>
                            """.trimIndent()

                            loadDataWithBaseURL("https://appassets.androidplatform.net/assets/dice/index.html", htmlData, "text/html", "UTF-8", null)
                            webViewRef = this
                        }
                    }, modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}