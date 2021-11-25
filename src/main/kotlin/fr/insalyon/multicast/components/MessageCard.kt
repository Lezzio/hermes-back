package fr.insalyon.multicast.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import javax.swing.text.html.parser.DTDConstants.SYSTEM

enum class MessageType {
    SELF,
    OTHER
}

data class Message(val author: String, val body: String, val messageType: MessageType)

@Composable
fun MessageCard(msg: Message, modifier: Modifier) {
    // Add padding around our message
    Row(
        modifier = modifier.padding(all = 8.dp),
    ) {
        if (msg.messageType == MessageType.OTHER) {
            Image(
                painter = painterResource("people.svg"),
                contentDescription = "Contact profile picture",
                modifier = Modifier
                    // Set image size to 40 dp
                    .size(40.dp)
                    // Clip image to be shaped as a circle
                    .clip(CircleShape)
            )

            // Add a horizontal space between the image and the column
            Spacer(modifier = Modifier.width(8.dp))
        }

        Column {
                Text(
                    text = msg.author,
                )
            // Add a vertical space between the author and message texts
            Spacer(modifier = Modifier.height(4.dp))

            Surface(shape = MaterialTheme.shapes.medium, elevation = 2.dp, color = if(msg.messageType == MessageType.SELF) Color(50, 127, 242) else Color.White) {
                if (msg.messageType == MessageType.OTHER) {
                    Text(
                        text = msg.body,
                        modifier = Modifier.padding(all = 10.dp),
                        style = MaterialTheme.typography.body2
                    )
                } else {
                    Text(
                        text = msg.body,
                        modifier = Modifier.padding(all = 10.dp),
                        style = MaterialTheme.typography.body2,
                        color = if(msg.messageType == MessageType.SELF) Color.White else Color.Black
                    )
                }
            }
        }
    }
}