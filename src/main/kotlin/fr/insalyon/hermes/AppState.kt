package fr.insalyon.hermes

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import fr.insalyon.messenger.net.model.TextMessage
import fr.insalyon.messenger.net.server.HermesServer
import java.net.Socket

class AppState (
    val serverPort: MutableState<Int?> = mutableStateOf(null),
    val hermesServer: MutableState<HermesServer?> = mutableStateOf(null),
    val connections: MutableState<Map<String, Socket>> = mutableStateOf(mapOf()),
    val messages: SnapshotStateList<TextMessage> = mutableStateListOf(),
)