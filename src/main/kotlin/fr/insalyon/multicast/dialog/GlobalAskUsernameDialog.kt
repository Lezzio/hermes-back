package fr.insalyon.multicast.dialog

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import fr.insalyon.multicast.AppState

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun globalAskUsernameDialog(appState: AppState) {
    var usernameInput by rememberSaveable { mutableStateOf("") }
    var serverInput by rememberSaveable { mutableStateOf("") }
    var serverPortInput by rememberSaveable { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = {},
        title = {
            Text(text = "Welcome to Hermes multicast!")
        },
        text = {
            Column {
                TextField(
                    value = usernameInput,
                    onValueChange = {
                        usernameInput = it
                    },
                    label = { Text("Choose your username") },
                )
                Spacer(Modifier.height(10.dp))
                TextField(
                    value = serverInput,
                    onValueChange = {
                        serverInput = it
                    },
                    label = { Text("Choose a server address") },
                )
                Spacer(Modifier.height(10.dp))
                TextField(
                    value = serverPortInput,
                    onValueChange = {
                        serverPortInput = it
                    },
                    label = { Text("Choose a server port") },
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    appState.username.value = usernameInput
                    if (serverInput.isNotBlank() && serverInput.isNotEmpty()) {
                        appState.serverAddress.value = serverInput
                    }
                    if (serverPortInput.isNotBlank() && serverPortInput.isNotEmpty()) {
                        appState.serverPort.value = serverPortInput.toInt()
                    }
                }
            ) {
                Text("Confirm")
            }
        },
        dismissButton = {}
    )
}