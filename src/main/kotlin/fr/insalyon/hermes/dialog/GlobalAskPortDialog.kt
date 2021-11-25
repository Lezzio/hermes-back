package fr.insalyon.hermes.dialog

import fr.insalyon.hermes.AppState
import androidx.compose.foundation.layout.Column
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun globalAskPortDialog(appState: AppState) {
    var serverPortInput by rememberSaveable { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = {},
        title = {
            Text(text = "Welcome to Hermes!")
        },
        text = {
            Column {
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
                    //TODO vérifier que c'est un port valide
                    appState.serverPort.value = serverPortInput.toInt()
                }
            ) {
                Text("Confirm")
            }
        },
        dismissButton = {}
    )
}