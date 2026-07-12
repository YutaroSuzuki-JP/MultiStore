package io.github.yutarosuzuki_jp.multistore.sample

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.github.yutarosuzuki_jp.multistore.MultiStoreFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App(factory: MultiStoreFactory) {
    var isSecureSelected by remember { mutableStateOf(false) }
    var inputKey by remember { mutableStateOf("") }
    var inputValue by remember { mutableStateOf("") }
    val logs = remember { mutableStateListOf<String>() }
    
    val store = remember(isSecureSelected) {
        if (isSecureSelected) {
            factory.createSecure("sample_secure_store")
        } else {
            factory.create("sample_standard_store")
        }
    }

    fun addLog(msg: String) {
        logs.add(msg)
    }

    MaterialTheme {
        Scaffold(
            topBar = {
                TopAppBar(title = { Text("MultiStore Demo") })
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Tab Selection
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { isSecureSelected = false },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (!isSecureSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = if (!isSecureSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondaryContainer
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Standard Store")
                    }
                    Button(
                        onClick = { isSecureSelected = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSecureSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = if (isSecureSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondaryContainer
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Secure Store")
                    }
                }

                // Inputs
                OutlinedTextField(
                    value = inputKey,
                    onValueChange = { inputKey = it },
                    label = { Text("Key") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = inputValue,
                    onValueChange = { inputValue = it },
                    label = { Text("Value") },
                    modifier = Modifier.fillMaxWidth()
                )

                // Actions Grid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            if (inputKey.isNotEmpty()) {
                                store.putString(inputKey, inputValue)
                                addLog("Saved: [$inputKey] -> \"$inputValue\"")
                            } else {
                                addLog("Error: Key is empty")
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Save")
                    }

                    Button(
                        onClick = {
                            if (inputKey.isNotEmpty()) {
                                val value = store.getString(inputKey)
                                addLog("Loaded: [$inputKey] -> \"$value\"")
                                if (value != null) {
                                    inputValue = value
                                }
                            } else {
                                addLog("Error: Key is empty")
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Load")
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            if (inputKey.isNotEmpty()) {
                                val exists = store.hasKey(inputKey)
                                addLog("Check key [$inputKey]: exists = $exists")
                            } else {
                                addLog("Error: Key is empty")
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Check Key")
                    }

                    Button(
                        onClick = {
                            if (inputKey.isNotEmpty()) {
                                store.remove(inputKey)
                                addLog("Removed key: [$inputKey]")
                            } else {
                                addLog("Error: Key is empty")
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Delete")
                    }
                }

                Button(
                    onClick = {
                        store.clear()
                        addLog("Cleared all data in store")
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Clear Store")
                }

                // Log Console
                Text("Log Console", style = MaterialTheme.typography.titleMedium)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .background(Color.DarkGray)
                        .padding(8.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    logs.forEach { log ->
                        Text(log, color = Color.Green, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}
