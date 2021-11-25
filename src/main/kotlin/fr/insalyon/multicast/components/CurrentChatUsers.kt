package fr.insalyon.multicast.components

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import fr.insalyon.multicast.AppState

@Composable
fun currentChatUsers(appState: AppState) {
    if (appState.multicastClient.value?.isConnected == true) {
        Column(
            Modifier.width(250.dp)
                .fillMaxHeight()
                .verticalScroll(rememberScrollState())
                .background(Color(245, 245, 245))
        ) {
            Spacer(modifier = Modifier.height(10.dp))
            appState.connectedGroupUsers.forEach {
                ConversationUserRow(
                    appState = appState,
                    username = it,
                    connected = true,
                    modifier = Modifier.align(Alignment.Start)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedButton(
                onClick = {
                    appState.username.value = null
                    appState.multicastClient.value?.disconnect()
                    appState.multicastClient.value = null
                    appState.connectedGroupUsers.clear()
                    appState.messages.clear()
                },
                border = BorderStroke(1.dp, Color.Black),
                modifier = Modifier.padding(4.dp).align(Alignment.CenterHorizontally)
            ) {
                Text(text = "Leave chat", color = Color.Blue)
            }
        }
    }
}