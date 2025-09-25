package com.nullinnix.clippr

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.nullinnix.clippr.database.Database
import com.nullinnix.clippr.misc.getClipboard
import com.nullinnix.clippr.theme.Theme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.awt.Toolkit
import java.awt.datatransfer.FlavorListener

fun main() = application {
    val database = Database()

    Window(
        onCloseRequest = ::exitApplication,
        title = "Clippr",
    ) {
        Theme {
            CoroutineScope(Dispatchers.IO).launch {
                while(true) {
                    getClipboard(
                        onCopy = {
                            database.createClip(it)
                        }
                    )
                    delay(1000)
                }
            }

            App()
        }
    }
}