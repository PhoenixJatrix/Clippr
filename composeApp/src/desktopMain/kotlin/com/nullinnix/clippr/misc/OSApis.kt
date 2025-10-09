package com.nullinnix.clippr.misc

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.res.loadImageBitmap
import com.sun.jna.Library
import com.sun.jna.Native
import com.sun.jna.Pointer
import com.tulskiy.keymaster.common.Provider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.awt.Dimension
import java.awt.Image
import java.awt.Point
import java.awt.Toolkit
import java.awt.Window
import java.io.File
import javax.imageio.ImageIO
import javax.imageio.stream.ImageInputStream
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

            delay(5000)
        }
    }
}

fun addToLoginItems() {
    val script = """
        tell application "System Events"
            make login item at end with properties {path:"Library/Application Support/Clippr.app", hidden:false}
        end tell
    """.trimIndent()

    ProcessBuilder("osascript", "-e", script).start().waitFor()
}

fun removeFromLoginItems() {
    val script = """
        tell application "System Events"
            delete login item "Clippr"
        end tell
    """.trimIndent()

    ProcessBuilder("osascript", "-e", script).start().waitFor()
}

fun isInLoginItems(): Boolean {
    val script = """
        tell application "System Events"
            get the name of every login item
        end tell
    """.trimIndent()

    val process = ProcessBuilder("osascript", "-e", script)
        .redirectErrorStream(true)
        .start()

    val output = process.inputStream.bufferedReader().readText().trim()
    process.waitFor()

    return output.split(", ").any { it == "Clippr" }
}

fun isInLoginItemsChecker (
    onDone: (Boolean) -> Unit
) {
    CoroutineScope(Dispatchers.IO).launch {
        while(true) {
            delay(5000)
            onDone(isInLoginItems())
        }
    }
}

fun getAllApps(
    onDone: (Map<String, MacApp>) -> Unit
) {
    CoroutineScope(Dispatchers.IO).launch {
        val appDirs = listOf(
            File("/Applications"),
            File(System.getProperty("user.home"), "Applications"),
            File("/System/Applications")
        )

        val apps = mutableMapOf(Pair("com.apple.finder", MacApp("Finder", "com.apple.finder", "composeResources/drawable/finder_icns.webp")))

        appDirs.forEach { dir ->
            dir.listFiles { f -> f.extension == "app" }?.mapNotNull { app ->
                val infoPlist = File(app, "Contents/Info.plist")
                if (!infoPlist.exists()) return@mapNotNull null

                val bundleId = runCommand("defaults", "read", infoPlist.absolutePath, "CFBundleIdentifier") ?: return@mapNotNull null
                val name = runCommand("defaults", "read", infoPlist.absolutePath, "CFBundleName")
                    ?: app.nameWithoutExtension

                val iconFile = runCommand("defaults", "read", infoPlist.absolutePath, "CFBundleIconFile")
                val iconPath = iconFile?.let {
                    val clean = if (it.endsWith(".icns")) it else "$it.icns"
                    File(app, "Contents/Resources/$clean").absolutePath
                }

                apps[bundleId] = MacApp(name, bundleId, iconPath)
            }
        }

        val sorted = mutableMapOf(Pair("unknown", MacApp("Unknown sources", "unknown", null)))

        apps.values.sortedBy {
            it.name
        }.forEach {
            sorted[it.bundleId] = it
        }

        onDone(sorted)
    }
}


fun runCommand(vararg cmd: String): String? {
    return try {
        val proc = ProcessBuilder(*cmd).redirectErrorStream(true).start()
        proc.inputStream.bufferedReader().readText().trim().ifBlank { null }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

fun loadIcns(apps: List<MacApp>, onDone: (Map<String, ImageBitmap>) -> Unit){
    CoroutineScope(Dispatchers.IO).launch {
        val loadedIcns = mutableMapOf<String, ImageBitmap>()

        apps.forEach { app ->
            if (app.iconPath != null) {
                val file = File(app.iconPath)

                try {
                    if (file.exists()) {
                        val img = ImageIO.read(file).toComposeImageBitmap()
                        loadedIcns[app.bundleId] = img
                    } else {
                        println("${app.bundleId} = $file doesn't exist")
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        onDone(loadedIcns)
    }
}