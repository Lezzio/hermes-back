package fr.insalyon.multicast

import androidx.compose.desktop.DesktopMaterialTheme
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import fr.insalyon.messenger.net.multicastclient.MulticastClient
import fr.insalyon.multicast.components.currentChatUsers
import fr.insalyon.multicast.components.currentChatView
import fr.insalyon.multicast.dialog.globalAskUsernameDialog
import fr.insalyon.multicast.dialog.globalNotification
import kotlin.system.exitProcess

var multicastClient: MulticastClient? = null

@Composable
@Preview
fun App() {
    DesktopMaterialTheme {
        val appState = rememberSaveable { AppState() }

        if (appState.username.value == null) {
            println("Asking username and connection information...")
            Row {
                Row(modifier = Modifier.fillMaxWidth().fillMaxHeight()) {
                    globalAskUsernameDialog(appState)
                }
            }
        } else {
            //Connecting user...
            appState.multicastClient.value = rememberSaveable { MulticastClient(appState.username.value, false, appState) }
            if (appState.multicastClient.value?.isConnected == false) {
                try {
                    appState.multicastClient.value?.connect(appState.serverAddress.value, appState.serverPort.value)
                } catch (e: Exception) {
                    e.printStackTrace()
                    appState.notification.value = "Couldn't establish the connection, try restarting the application" to true
                }
                multicastClient = appState.multicastClient.value
                println("User connected as ${appState.username.value}")
            }

            if (appState.notification.value != null && appState.notification.value?.second == true) {
                globalNotification(appState)
            }

            Row {
                currentChatView(appState = appState, Modifier.weight(1F))
                currentChatUsers(appState = appState)
            }
        }
    }
}

fun main() = application {
    Window(onCloseRequest = {
        multicastClient?.disconnect()
        exitApplication()
        exitProcess(0)
    }) {
        App()
    }
}
