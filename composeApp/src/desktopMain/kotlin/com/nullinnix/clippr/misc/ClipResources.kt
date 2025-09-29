package com.nullinnix.clippr.misc

import clippr.composeapp.generated.resources.Res
import clippr.composeapp.generated.resources.audio
import clippr.composeapp.generated.resources.blank
import clippr.composeapp.generated.resources.broken
import clippr.composeapp.generated.resources.code
import clippr.composeapp.generated.resources.directory
import clippr.composeapp.generated.resources.image
import clippr.composeapp.generated.resources.runnable
import clippr.composeapp.generated.resources.text
import clippr.composeapp.generated.resources.unknown
import clippr.composeapp.generated.resources.video
import clippr.composeapp.generated.resources.web
import clippr.composeapp.generated.resources.zip
import com.sun.jna.Library
import com.sun.jna.Native
import com.sun.jna.Pointer
import org.apache.tika.Tika
import org.jetbrains.compose.resources.DrawableResource
import java.awt.AWTException
import java.awt.Robot
import java.awt.Toolkit
import java.awt.Window
import java.awt.datatransfer.Clipboard
import java.awt.datatransfer.ClipboardOwner
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.Transferable
import java.awt.event.KeyEvent
import java.io.File
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.UUID
import javax.swing.JOptionPane
import javax.swing.JTextField

var lastCopiedItemHash = ""

fun getClipboard (
    onCopy: (Clip) -> Unit,
) {
    val clipboard = Toolkit.getDefaultToolkit().systemClipboard
    val contents = clipboard.getContents(null)

    if (contents.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
        val paths = contents.getTransferData(DataFlavor.javaFileListFlavor) as List<*>
        val hash = paths.toString().hash()

        if (lastCopiedItemHash != hash) {
            lastCopiedItemHash = hash

            for (path in paths) {
                if ((path as File).isDirectory) {
                    println("$path = directory")

                    onCopy(
                        Clip(
                            clipID = UUID.randomUUID().toString(),
                            content = path.path,
                            copiedAt = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC),
                            isPinned = false,
                            mimeType = MIME_TYPE_DIR,
                            isImage = false,
                            exists = path.exists(),
                            pinnedAt = 0L,
                            associatedIcon = getIconForContent(MIME_TYPE_DIR, path.exists(), path.path)
                        )
                    )
                } else {
                    val tika = Tika()
                    val mimeType = tika.detect(path)
                    println("$path = $mimeType")

                    onCopy(
                        Clip(
                            clipID = UUID.randomUUID().toString(),
                            content = path.path,
                            copiedAt = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC),
                            isPinned = false,
                            mimeType = mimeType,
                            isImage = false,
                            exists = path.exists(),
                            pinnedAt = 0L,
                            associatedIcon = getIconForContent(mimeType, path.exists(), path.path)
                        )
                    )
                }
            }
        }
    } else if (contents.isDataFlavorSupported(DataFlavor.stringFlavor)) {
        val content = contents.getTransferData(DataFlavor.stringFlavor) as String
        val hash = content.hash()

        if (lastCopiedItemHash != hash) {
            lastCopiedItemHash = hash

            val tika = Tika()
            val mimeType = tika.detect(content)
            println("$content = $mimeType")

            onCopy(
                Clip(
                    clipID = UUID.randomUUID().toString(),
                    content = content,
                    copiedAt = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC),
                    isPinned = false,
                    mimeType = MIME_TYPE_PLAIN_TEXT,
                    isImage = false,
                    exists = true,
                    pinnedAt = 0L,
                    associatedIcon = getIconForContent(mimeType, true, content)
                )
            )
        }
    }
}

fun getIconForContent (
    mimeType: String,
    exists: Boolean,
    content: String
): String {
    val mediaType = mimeType.split("/")[0]
    val subType = mimeType.split("/")[1]

    println("$mimeType for $content")

    return if (exists) {
        for (url in urlExtensions) {
            if (content.endsWith(url) || "$url/" in content) {
                return WEB
            }
        }

        if (mimeType != MIME_TYPE_PLAIN_TEXT) {
            when (mediaType) {
                "dir" -> {
                    DIRECTORY
                }

                "image" -> {
                    IMAGE
                }

                "audio" -> {
                    AUDIO
                }

                "video" -> {
                    VIDEO
                }

                "application" -> {
                    when (subType) {
                        in listOf("zip", "7z", "gz", "rar", "java-archive", "tar.gz", "app", "zlib") -> {
                            ZIP
                        }

                        in listOf("x-bzip2", "dmg", "x-apple-diskimage", "msi", "x-ms-installer", "x-dosexec", "jar", "x-bat") -> {
                            RUNNABLE
                        }

                        in listOf("json") -> {
                            CODE
                        }

                        in listOf("octet-stream") -> {
                            BLANK
                        }

                        else -> {
                            UNKNOWN
                        }
                    }
                }

                "text" -> {
                    when (subType) {
                        in listOf("plain", "csv") -> {
                            TEXT
                        }

                        in listOf("x-c++src", "x-csrc", "x-java-source", "x-groovy", "x-scala", "x-kotlin", "x-python", "javascript", "x-typescript", "x-go", "x-rustsrc", "x-swift", "x-ruby", "x-php", "x-clojure", "x-haskell", "x-ocaml", "x-perl", "x-sh", "x-r-source", "vnd.dart", "x-elixir", "x-lua", "x-matlab", "x-vbasic", "x-stsrc", "x-coffeescript", "x-less", "x-yaml", "x-rst", "json", "xml", "sql", "x-pascal") -> {
                            CODE
                        }

                        else -> {
                            UNKNOWN
                        }
                    }
                }

                else -> {
                    UNKNOWN
                }
            }
        } else {
            if (content.endsWith(".txt")) {
                TEXT
            } else {
                BLANK
            }
        }
    } else {
        BROKEN
    }
}

fun onCopyToClipboard(clip: Clip, onHashed: (String?) -> Unit) {
    val clipboard = Toolkit.getDefaultToolkit().systemClipboard
    val customTransferable = CustomTransferable(clip)

    if (customTransferable.hash != null) {
        onHashed(customTransferable.hash)
    }

    clipboard.setContents(customTransferable, CustomClipboardOwner())
}

fun mimeTypeToDataFlavor(mimeType: String): List<DataFlavor> {
    val mediaType = mimeType.split("/")[0]
    val subType = mimeType.split("/")[1]

    //data flavor in order of richness
    return when (mediaType) {
        "text" -> {
            when(subType) {
                "plain" -> {
                    listOf(DataFlavor.stringFlavor)
                }

                else -> {
                    listOf(DataFlavor.javaFileListFlavor)
                }
            }
        }

        "image" -> {
            listOf(DataFlavor.imageFlavor)
        }

        else -> {
            listOf(DataFlavor.javaFileListFlavor)
        }
    }
}

class CustomTransferable(private val clip: Clip): Transferable {
    val mediaType = clip.mimeType.split("/")[0]
    val subType = clip.mimeType.split("/")[1]
    val dataFlavors = mimeTypeToDataFlavor(clip.mimeType)
    var hash: String? = null

    init {
        hash = clip.content.hash()
    }

    override fun getTransferDataFlavors(): Array<out DataFlavor?>? {
        val dataFlavorArray = Array(dataFlavors.size) {
            dataFlavors[it]
        }

        return dataFlavorArray
    }

    override fun isDataFlavorSupported(p0: DataFlavor?): Boolean {
        return p0 in dataFlavors
    }

    override fun getTransferData(p0: DataFlavor?): Any {
        return when (mediaType) {
            "text" -> {
                when(subType) {
                    "plain" -> {
                        clip.content
                    }

                    else -> {
                        listOf(File(clip.content))
                    }
                }

            }

            "image" -> {
                println("copying image")

                val file = File(clip.content)
                println(file.exists())
            }

            else -> {
                println("copying other")
                listOf(File(clip.content))
            }
        }
    }
}

class CustomClipboardOwner: ClipboardOwner {
    override fun lostOwnership(
        p0: Clipboard?,
        p1: Transferable?,
    ) {
        println("lost ownership")
    }
}

fun pasteClipboardTextIntoField(field: JTextField) {
    val clipboard = Toolkit.getDefaultToolkit().systemClipboard
    val contents = clipboard.getContents(null)
    if (contents != null && contents.isDataFlavorSupported(DataFlavor.stringFlavor)) {
        val text = contents.getTransferData(DataFlavor.stringFlavor) as String
        field.text = text
    }
}

fun pasteWithRobot(clip: Clip) {
    onCopyToClipboard(clip) {
        lastCopiedItemHash = it ?: lastCopiedItemHash

        val cg = CoreGraphics.INSTANCE
        val source = cg.CGEventSourceCreate(CoreGraphics.kCGEventSourceStateHIDSystemState)

        val vDown = cg.CGEventCreateKeyboardEvent(source, CoreGraphics.kVK_ANSI_V, true)
        cg.CGEventSetFlags(vDown, CoreGraphics.kCGEventFlagMaskCommand)
        cg.CGEventPost(CoreGraphics.kCGSessionEventTap, vDown)
        Thread.sleep(30)

        val vUp = cg.CGEventCreateKeyboardEvent(source, CoreGraphics.kVK_ANSI_V, false)
        cg.CGEventSetFlags(vUp, CoreGraphics.kCGEventFlagMaskCommand)
        cg.CGEventPost(CoreGraphics.kCGSessionEventTap, vUp)
        Thread.sleep(30)

        val cmdUp = cg.CGEventCreateKeyboardEvent(source, 55, false)
        cg.CGEventPost(CoreGraphics.kCGSessionEventTap, cmdUp)

        cg.CFRelease(vDown)
        cg.CFRelease(vUp)
        cg.CFRelease(cmdUp)
        cg.CFRelease(source)
    }
}

fun showMacConfirmDialog(
    title: String,
    description: String,
): Boolean {
    val result = JOptionPane.showConfirmDialog(
        null,
        description,
        title,
        JOptionPane.OK_CANCEL_OPTION,
        JOptionPane.WARNING_MESSAGE
    )
    return result == JOptionPane.OK_OPTION
}

interface CoreGraphics : Library {
    companion object {
        val INSTANCE: CoreGraphics = Native.load("CoreGraphics", CoreGraphics::class.java)
        const val kCGEventSourceStateHIDSystemState = 1
        const val kCGSessionEventTap = 1
        const val kCGEventFlagMaskCommand: Long = 1L shl 20

        // virtual key codes (Carbon)
        const val kVK_ANSI_V = 9
    }

    fun CGEventSourceCreate(stateID: Int): Pointer
    fun CGEventCreateKeyboardEvent(source: Pointer?, virtualKey: Int, keyDown: Boolean): Pointer
    fun CGEventSetFlags(event: Pointer, flags: Long)
    fun CGEventPost(tap: Int, event: Pointer)
    fun CFRelease(ref: Pointer)
}

val urlExtensions = listOf(
    ".com",
    ".org",
    ".net",
    ".xyz",
    ".info",
    ".io",
    ".co",
    ".us",
    ".uk",
    ".de",
    ".jp",
    ".fr",
    ".ru",
    ".br",
    ".in",
    ".cn",
    ".ca",
    ".au",
    ".es",
    ".it"
)

const val AUDIO = "audio"
const val BLANK = "blank"
const val BROKEN = "broken"
const val CODE = "code"
const val DIRECTORY = "directory"
const val IMAGE = "image"
const val RUNNABLE = "runnable"
const val TEXT = "text"
const val UNKNOWN = "unknown"
const val VIDEO = "video"
const val WEB = "web"
const val ZIP = "zip"

val drawableMap: Map<String, DrawableResource> = mapOf(
    AUDIO to Res.drawable.audio,
    BLANK to Res.drawable.blank,
    BROKEN to Res.drawable.broken,
    CODE to Res.drawable.code,
    DIRECTORY to Res.drawable.directory,
    IMAGE to Res.drawable.image,
    RUNNABLE to Res.drawable.runnable,
    TEXT to Res.drawable.text,
    UNKNOWN to Res.drawable.unknown,
    VIDEO to Res.drawable.video,
    WEB to Res.drawable.web,
    ZIP to Res.drawable.zip,
)