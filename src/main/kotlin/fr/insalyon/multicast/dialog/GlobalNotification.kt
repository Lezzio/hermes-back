package fr.insalyon.multicast.dialog

import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.width
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import fr.insalyon.multicast.AppState

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun globalNotification(appState: AppState) {
    AlertDialog(
        onDismissRequest = {},
        title = {
            Text(text = "Notification")
        },
        text = {
            Text(text = appState.notification.value?.first ?: "", Modifier.width(300.dp).defaultMinSize())
        },
        confirmButton = {
            Button(
                onClick = {
                    appState.notification.value = (appState.notification.value?.first ?: "") to false
                }
            ) {
                Text("Confirm")
            }
        },
        dismissButton = {}
    )
}