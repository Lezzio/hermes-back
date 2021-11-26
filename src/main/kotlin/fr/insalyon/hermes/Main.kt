package fr.insalyon.hermes// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import androidx.compose.desktop.DesktopMaterialTheme
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import fr.insalyon.hermes.components.logView
import fr.insalyon.hermes.components.userPanel
import fr.insalyon.hermes.dialog.globalAskPortDialog
import fr.insalyon.messenger.net.server.HermesServer


/**
 * WARNING : Not yet usable
 */
@Composable
@Preview
fun App() {


    DesktopMaterialTheme {
      val appState = rememberSaveable { AppState() }

      if(appState.serverPort.value == null){
          Row {
              Row(modifier = Modifier.fillMaxWidth().fillMaxHeight()) {
                  globalAskPortDialog(appState)
              }
          }
      } else {
          appState.hermesServer.value = rememberSaveable { HermesServer(appState) }
          appState.hermesServer.value?.init(appState.serverPort.value!!);

          println("fnezifneui")
            //TODO: test si la connexion est ok
          Row {
              //Chats column
              userPanel(appState = appState)
              //Current chat/conversation
              logView(appState = appState, modifier = Modifier.weight(1F))
          }
      }

    }
}

fun main() = application {
    Window(onCloseRequest = ::exitApplication) {
        App()
    }
}
