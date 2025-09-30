package com.nullinnix.clippr.misc

import com.nullinnix.clippr.database.clips.ClipsDao
import com.sun.jna.Library
import com.sun.jna.Native
import com.sun.jna.Pointer
import com.tulskiy.keymaster.common.Provider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.awt.Dimension
import java.awt.Point
import java.awt.Toolkit
import java.awt.Window
import java.time.LocalDateTime
import java.time.ZoneOffset
import javax.swing.KeyStroke

var size = Dimension(300, 200)
var location = Point(0, 0)
var isFullScreen = false

fun toggleFullscreen(window: Window?) {
    try {
        if (window != null) {
            val gd = window.graphicsConfiguration
            val insets = Toolkit.getDefaultToolkit().getScreenInsets(gd)
            val maxSize = Dimension(window.graphicsConfiguration.bounds.size.width, window.graphicsConfiguration.bounds.size.height - (insets.top + insets.bottom))
            val initialFullScreenPosition = Point(0, insets.top)

            if (isFullScreen && window.size == maxSize && window.location == initialFullScreenPosition) {
                window.size = size
                window.location = location

                isFullScreen = false
            } else {
                size = window.size
                location = window.location

                window.location = Point(0, insets.top)
                window.size = Dimension(window.graphicsConfiguration.bounds.size.width, window.graphicsConfiguration.bounds.size.height - (insets.top + insets.bottom))

                isFullScreen = true
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun focusWindow() {
    val nsApp = Cocoa.INSTANCE.objc_getClass("NSApplication")
    val sharedApp = Cocoa.INSTANCE.objc_msgSend(nsApp, Cocoa.INSTANCE.sel_registerName("sharedApplication"))
    Cocoa.INSTANCE.objc_msgSend(sharedApp, Cocoa.INSTANCE.sel_registerName("activateIgnoringOtherApps:"))
}

interface Cocoa : Library {
    fun objc_getClass(name: String): Pointer
    fun sel_registerName(name: String): Pointer
    fun objc_msgSend(receiver: Pointer, selector: Pointer): Pointer

    companion object {
        val INSTANCE: Cocoa = Native.load("objc", Cocoa::class.java)
    }
}

fun registerKeyStroke (
    onTrigger: () -> Unit
) {
    val provider = Provider.getCurrentProvider(true)

    provider.register(
        KeyStroke.getKeyStroke("meta shift V")
    ) {
        onTrigger()
    }
}

fun listenForCopy(
    onCopy: (Clip) -> Unit
) {
    CoroutineScope(Dispatchers.IO).launch {
        while(true) {
            getClipboard(
                onCopy = {
                    onCopy(it)
                }
            )

            delay(300)
        }
    }
}