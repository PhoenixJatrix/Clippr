package com.nullinnix.clippr.misc

import androidx.annotation.DrawableRes
import clippr.composeapp.generated.resources.Res
import clippr.composeapp.generated.resources.invalid
import com.nullinnix.clippr.model.ViewModel
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.apache.tika.Tika
import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.Drawable
import org.jetbrains.skiko.toBitmap
import java.awt.Toolkit
import java.awt.datatransfer.DataFlavor
import java.awt.image.BufferedImage
import java.io.File
import java.security.MessageDigest
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.UUID
import javax.imageio.ImageIO
import kotlin.io.encoding.Base64


//fun toBufferedImage(img: Image): BufferedImage {
//    if (img is BufferedImage) return img
//
//    val width = img.getWidth(null)
//    val height = img.getHeight(null)
//
//    val bImage = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
//    val g2d = bImage.createGraphics()
//    g2d.drawImage(img, 0, 0, null)
//    g2d.dispose()
//    return bImage
//}

//fun getImageFromClipboard(): Bitmap? {
//    val toolkit = Toolkit.getDefaultToolkit()
//    val clipboard = toolkit.systemClipboard
//    val contents = clipboard.getContents(null)
//
//    if (contents != null && contents.isDataFlavorSupported(DataFlavor.imageFlavor)) {
//        val image = contents.getTransferData(DataFlavor.imageFlavor) as Image
//        val bitmap = toBufferedImage(image).toBitmap()
//
//        println("image found")
//        return bitmap
//    }
//
//    println("no image")
//    return null
//}

fun getImage(): Bitmap? {
    val clipboard = Toolkit.getDefaultToolkit().systemClipboard
    val contents = clipboard.getContents(null)
    if (contents != null && contents.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
        val files = contents.getTransferData(DataFlavor.javaFileListFlavor) as List<*>
        if (files.isNotEmpty()) {
            val image = ImageIO.read(files.first() as File)
            return image.toBitmap()
        }
    }

    return null
}

fun getText(): String? {
    val clipboard = Toolkit.getDefaultToolkit().systemClipboard
    val contents = clipboard.getContents(null)

    if (contents.isDataFlavorSupported(DataFlavor.stringFlavor)) {
        return contents.getTransferData(DataFlavor.stringFlavor) as String
    }

    return null
}

fun getClipboard(
    onPlainTextFound: (Clip) -> Unit,
    onPathsFound: (Clip) -> Unit
) {
    val clipboard = Toolkit.getDefaultToolkit().systemClipboard
    val contents = clipboard.getContents(null)

    if (contents.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
        val paths = contents.getTransferData(DataFlavor.javaFileListFlavor) as List<*>
        val tika = Tika()

        val mimeType = tika.detect(paths.first().toString())
        val hash = paths.toString().hash()

        if (ViewModel.lastCopiedItemHash != hash) {
            onPathsFound(
                Clip(
                    clipID = UUID.randomUUID().toString(),
                    text = mimeType,
                    uris = paths.map { it.toString() },
                    copiedAt = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC),
                    isPinned = false,
                    mimeType = mimeType,
                    isImage = false
                )
            )

            ViewModel.lastCopiedItemHash = hash
        }
    } else if (contents.isDataFlavorSupported(DataFlavor.stringFlavor)) {
        val content = contents.getTransferData(DataFlavor.stringFlavor) as String
        val hash = content.hash()

        if (ViewModel.lastCopiedItemHash != hash) {
            onPlainTextFound(
                Clip(
                    clipID = UUID.randomUUID().toString(),
                    text = content,
                    uris = listOf(),
                    copiedAt = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC),
                    isPinned = false,
                    mimeType = "text/plain",
                    isImage = false
                )
            )

            ViewModel.lastCopiedItemHash = hash
        }
    }
}

fun clipboardListener(onChange: (String) -> Unit) {
    while (true) {
        Thread.sleep(500)
    }
}

fun setClipboard(data: Any) {
    val process = ProcessBuilder("pbcopy").start()
    process.outputStream.bufferedWriter().use {
        it.write("paste this shit")
    }
}

fun String.hash(): String {
    val digest = MessageDigest.getInstance("SHA-256")

    for (char in this) {
        digest.update((char).code.toByte())
    }

    return digest.digest().joinToString("") { "%02x".format(it) }
}

fun BufferedImage.hash(): String {
    val pixels = IntArray(this.width * this.height)

    this.getRGB(0, 0, this.width, this.height, pixels, 0, this.width)

    val digest = MessageDigest.getInstance("SHA-256")

    for (pixel in pixels) {
        digest.update((pixel shr 24).toByte())
        digest.update((pixel shr 16).toByte())
        digest.update((pixel shr 8).toByte())
        digest.update(pixel.toByte())
    }

    return digest.digest().joinToString("") { "%02x".format(it) }
}

fun MutableMap<String, Clip>.toJsonArray(): JsonArray{
    return buildJsonArray {
        for (clip in this@toJsonArray.values) {
            add(
                buildJsonObject {
                    put(CLIP_ID, JsonPrimitive(clip.clipID))
                    put(TEXT, JsonPrimitive(clip.text))
                    put(URIS, clip.uris.toJsonArray())
                    put(COPIED_AT, JsonPrimitive(clip.copiedAt))
                    put(IS_PINNED, JsonPrimitive(clip.isPinned))
                    put(MIME_TYPE, JsonPrimitive(clip.mimeType))
                }
            )
        }
    }
}

fun Collection<Any>.toJsonArray(): JsonArray {
    return buildJsonArray {
        for (item in this@toJsonArray) {
            add(JsonPrimitive(item.toString()))
        }
    }
}

fun JsonArray.toStringList(): List<String> {
    return this.map { it.jsonPrimitive.content }
}

fun formatText(string: String): String {
    return string.replace("\n", " ")
}