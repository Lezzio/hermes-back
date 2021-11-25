package fr.insalyon.multicast.components

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
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
        }
    }
}