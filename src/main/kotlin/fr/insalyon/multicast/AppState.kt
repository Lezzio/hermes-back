package fr.insalyon.multicast

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import fr.insalyon.messenger.net.model.MulticastMessage
import fr.insalyon.messenger.net.multicastclient.MulticastClient

data class AppState(
    val username: MutableState<String?> = mutableStateOf(null),
    val serverAddress: MutableState<String> = mutableStateOf("224.0.0.1"),
    val serverPort: MutableState<Int> = mutableStateOf(5000),
    val multicastClient: MutableState<MulticastClient?> = mutableStateOf(null),
    val messages: SnapshotStateList<MulticastMessage> = mutableStateListOf(),
)