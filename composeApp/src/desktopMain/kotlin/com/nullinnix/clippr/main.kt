package com.nullinnix.clippr

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.nullinnix.clippr.database.Database
import com.nullinnix.clippr.misc.clipboardListener
import com.nullinnix.clippr.misc.getClipboard
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
        CoroutineScope(Dispatchers.IO).launch {
            while(true) {
                getClipboard(
                    onPathsFound = {
                        database.createClip(it)
                    },
                    onPlainTextFound = {
                        database.createClip(it)
                    }
                )
                delay(1000)
            }
        }

        App()
    }
}