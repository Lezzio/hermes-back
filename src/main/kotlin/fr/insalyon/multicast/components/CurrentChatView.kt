package fr.insalyon.multicast.components

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import fr.insalyon.multicast.AppState
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun currentChatView(appState: AppState, modifier: Modifier = Modifier) {
    val scrollState = rememberScrollState(Int.MAX_VALUE)
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(appState.messages.size) {
        scrollState.animateScrollTo(Int.MAX_VALUE)
    }

    //Current chat viewer
    Column(
        modifier.fillMaxHeight()
    ) {
        Column(
            Modifier.fillMaxWidth().verticalScroll(scrollState).weight(1F).background(Color.White)
        ) {
            appState.messages.forEach {
                val messageType = if (it.sender == appState.username.value) {
                    MessageType.SELF
                } else if (it.sender == "*") {
                    MessageType.SYSTEM
                } else {
                    MessageType.OTHER
                }
                val formatter = SimpleDateFormat("MM/dd 'at' HH:mm")
                val dateInfo = formatter.format(it.time)
                MessageCard(
                    msg = Message(
                        author = it.sender,
                        body = it.content,
                        dateInfo = dateInfo,
                        messageType = messageType
                    ),
                    modifier = Modifier.align(
                        alignment = when (messageType) {
                            MessageType.SELF -> Alignment.End
                            MessageType.OTHER -> Alignment.Start
                            else -> Alignment.CenterHorizontally
                        }
                    )
                )
            }
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.background(Color.White)
        ) {
            var msgInput by rememberSaveable { mutableStateOf("") }
            TextField(
                value = msgInput,
                onValueChange = {
                    msgInput = it
                },
                label = { Text("Type a message...") },
                modifier = Modifier
                    .weight(1F)
                    .onPreviewKeyEvent {
                        if (it.key == Key.Enter && msgInput.isNotEmpty() && msgInput.isNotBlank()) {
                            appState.multicastClient.value?.sendMessage(msgInput)
                            println("Clicked to send $msgInput to ${appState.multicastClient.value}}")
                            msgInput = ""
                            coroutineScope.launch {
                                scrollState.animateScrollTo(Int.MAX_VALUE)
                            }
                            true
                        } else {
                            false
                        }
                    }
            )
            Spacer(modifier = Modifier.size(20.dp))
            Image(
                painter = painterResource("send.svg"),
                contentDescription = "Contact profile picture",
                modifier = Modifier
                    // Set image size to 40 dp
                    .size(40.dp)
                    // Clip image to be shaped as a circle
                    .clip(CircleShape)
                    .clickable {
                        if (msgInput.isNotEmpty() && msgInput.isNotBlank()) {
                            appState.multicastClient.value?.sendMessage(msgInput)
                            println("Clicked to send $msgInput to current group")
                            msgInput = ""
                            coroutineScope.launch {
                                scrollState.animateScrollTo(Int.MAX_VALUE)
                            }
                        }
                    }
            )
            Spacer(modifier = Modifier.size(20.dp))
        }
    }
}