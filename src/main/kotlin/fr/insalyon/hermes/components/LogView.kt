package fr.insalyon.hermes.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import fr.insalyon.hermes.AppState

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun logView(appState: AppState, modifier: Modifier) {
    val scrollState = rememberScrollState(Int.MAX_VALUE)
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier.fillMaxHeight()
    ) {
        Column(
            Modifier.fillMaxWidth().verticalScroll(scrollState).weight(1F).background(Color.White)
        ) {
            appState.messages.forEach {
                LogCard(
                    msg = Log(
                        author = it.sender,
                        destination = it.destination,
                        body = it.content,
                        time = it.time
                    ),
                    modifier = Modifier
                )
            }
        }

    }
}