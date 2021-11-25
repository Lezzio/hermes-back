package fr.insalyon.hermes.components

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import fr.insalyon.hermes.AppState

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun userPanel(appState : AppState){
    Column(
        Modifier.width(250.dp)
            .fillMaxHeight()
            .verticalScroll(rememberScrollState())
            .background(Color(245, 245, 245))
    ) {

        //TODO: mettre un titre avec ip serveur et port serveur

        appState.connections.value.entries.forEach {
            UserRow(
                appState = appState,
                username = it.key,
                modifier = Modifier.align(Alignment.Start)
            )
        }
    }
}