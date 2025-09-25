package com.nullinnix.clippr

import androidx.compose.runtime.collectAsState
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.nullinnix.clippr.database.ClipsDatabaseFactory
import com.nullinnix.clippr.misc.ClipAction
import com.nullinnix.clippr.misc.getClipboard
import com.nullinnix.clippr.model.ClipsViewModel
import com.nullinnix.clippr.theme.Theme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

fun main() = application {
    val database = ClipsDatabaseFactory().create()
    val clipsViewModel = ClipsViewModel(database.clipsDao())

    Window(
        onCloseRequest = ::exitApplication,
        title = "Clippr",
    ) {
        val clipsState = clipsViewModel.clipsState.collectAsState().value

        Theme {
            CoroutineScope(Dispatchers.IO).launch {
                while(true) {
                    getClipboard(
                        onCopy = {
                            clipsViewModel.onAction(ClipAction.OnAddClip(it))
                        }
                    )
                    delay(1000)
                }
            }

            App(
                clipsViewModel = clipsViewModel
            )
        }
    }
}