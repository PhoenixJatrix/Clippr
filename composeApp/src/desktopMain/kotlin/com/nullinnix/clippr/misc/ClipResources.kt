package com.nullinnix.clippr.misc

import androidx.compose.ui.graphics.Color
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.DrawableResource
import java.awt.Toolkit
import java.awt.datatransfer.Clipboard
import java.awt.datatransfer.ClipboardOwner
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.Transferable
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.UUID
import java.util.logging.Filter
import javax.swing.JOptionPane

var lastCopiedItemHash = ""

fun getClipboard (
    onCopy: (Clip) -> Unit,
) {
    try {
        val clipboard = Toolkit.getDefaultToolkit().systemClipboard
        val contents = clipboard.getContents(null)

        if (contents.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
            val paths = contents.getTransferData(DataFlavor.javaFileListFlavor) as List<*>
            val hash = paths.toString().hash()

            if (lastCopiedItemHash != hash) {
                lastCopiedItemHash = hash

                for (index in paths.indices) {
                    if ((paths[index] as File).isDirectory) {
                        val path = paths[index] as File
                        val source = getClipSource()

                        log("dir ${path.path} to dir from $source", "")
                        println("dir ${path.path} to dir from $source")
//                        log("dir ${path.path} to red", "")
//                        println("file ${path.path} to ${Files.probeContentType(Paths.get(path.path))}")

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
                                associatedIcon = ClipType.FOLDER.id,
                                source = source
                            )
                        )
                    } else {
                        val path = paths[index] as File
                        val source = getClipSource()
                        val mimeType = Files.probeContentType(Paths.get(path.path))

                        log("file ${path.path} to $mimeType from $source", "")
                        println("file ${path.path} to $mimeType from $source")
//                        log("dir ${path.path} to red", "")
//                        println("file ${path.path} to ${Files.probeContentType(Paths.get(path.path))}")

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
                                associatedIcon = getIconForContent(mimeType, path.path.lowercase()),
                                source = source
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
                val source = getClipSource()

                log("str $content from $source", "")
                println("str $content from $source")

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
                        associatedIcon = getIconForContent(MIME_TYPE_PLAIN_TEXT, content),
                        source = source
                    )
                )
            }
        }
    } catch (e: Exception) {
        log("${e.message} -> ${e.stackTrace.firstOrNull()?.let {it::class.java.name}}", "catch")
    }
}

fun getIconForContent (
    mimeType: String,
    content: String
): String {
    val mediaType = mimeType.split("/")[0]
    val subType = mimeType.split("/")[1]

    for (tld in urlExtensions) {
        if (content.endsWith(tld) || content.contains("$tld/")) {
            return ClipType.WEB.id
        }
    }

    return when (mediaType) {
        "dir" -> {
            ClipType.FOLDER.id
        }

        "image" -> {
            ClipType.IMAGE.id
        }

        "audio" -> {
            ClipType.AUDIO.id
        }

        "video" -> {
            ClipType.VIDEO.id
        }

        "application" -> {
            return when (subType) {
                in setOf("zip", "7z", "gz", "rar", "java-archive", "tar.gz", "app", "zlib", "gzip") -> {
                    ClipType.ZIP.id
                }

                in setOf("x-bzip2", "dmg", "x-apple-diskimage", "msi", "x-ms-installer", "x-dosexec", "jar", "x-bat", "x-msdownload") -> {
                    ClipType.RUNNABLE.id
                }

                in setOf("json", "pdf") -> {
                    ClipType.CODE.id
                }

                in setOf("octet-stream") -> {
                    ClipType.PLAIN_TEXT.id
                }

                else -> {
                    val ext = if (content.contains(".")) content.substring(content.lastIndexOf(".")) else null

                    ext?.let {
                        if (ext in runnableExtensions) {
                            return ClipType.RUNNABLE.id
                        }
                    }

                    ClipType.UNKNOWN.id
                }
            }
        }

        "text" -> {
            if (content.endsWith(".txt")) {
                return ClipType.TEXT.id
            } else {
                val ext = if (content.contains(".")) content.substring(content.lastIndexOf(".")) else null

                ext?.let {
                    if (ext in codeExtensions) {
                        return ClipType.CODE.id
                    }
                }

                return when (subType) {
                    in setOf ("csv") -> {
                        ClipType.TEXT.id
                    }

                    in setOf ("plain") -> {
                        ClipType.PLAIN_TEXT.id
                    }

                    in setOf ("x-c++src", "x-csrc", "x-java-source", "x-groovy", "x-scala", "x-kotlin", "x-python", "javascript", "x-typescript", "x-go", "x-rustsrc", "x-swift", "x-ruby", "x-php", "x-clojure", "x-haskell", "x-ocaml", "x-perl", "x-sh", "x-r-source", "vnd.dart", "x-elixir", "x-lua", "x-matlab", "x-vbasic", "x-stsrc", "x-coffeescript", "x-less", "x-yaml", "x-rst", "json", "xml", "sql", "x-pascal") -> {
                        ClipType.CODE.id
                    }

                    else -> {
                        ClipType.UNKNOWN.id
                    }
                }
            }
        }

        else -> {
            val ext = if (content.contains(".")) content.substring(content.lastIndexOf(".")) else null

            ext?.let {
                if (ext in codeExtensions) {
                    return ClipType.CODE.id
                }
            }

            ext?.let {
                if (ext in runnableExtensions) {
                    return ClipType.RUNNABLE.id
                }
            }

            return ClipType.UNKNOWN.id
        }
    }
}

fun onCopyToClipboard(clip: Clip) {
    val clipboard = Toolkit.getDefaultToolkit().systemClipboard
    val customTransferable = CustomTransferable(clip)

    lastCopiedItemHash = customTransferable.hash ?: lastCopiedItemHash

    clipboard.setContents(customTransferable, CustomClipboardOwner())
}

fun mimeTypeToDataFlavor(mimeType: String): List<DataFlavor> {
    val mediaType = mimeType.split("/")[0]
    val subType = mimeType.split("/")[1]

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

fun pasteWithRobot(clip: Clip) {
    onCopyToClipboard(clip)

    Thread.sleep(50)

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

        const val kVK_ANSI_V = 9
    }

    fun CGEventSourceCreate(stateID: Int): Pointer
    fun CGEventCreateKeyboardEvent(source: Pointer?, virtualKey: Int, keyDown: Boolean): Pointer
    fun CGEventSetFlags(event: Pointer, flags: Long)
    fun CGEventPost(tap: Int, event: Pointer)
    fun CFRelease(ref: Pointer)
}

fun getClipSource(): String? {
    val process = ProcessBuilder(
        "osascript", "-e",
        "tell application \"System Events\" to get bundle identifier of (first process whose frontmost is true)"
    ).start()
    return process.inputStream.bufferedReader().readText().trim().ifBlank { null }
}

val urlExtensions = setOf (
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

val codeExtensions = setOf (
    ".c",
    ".cpp",
    ".h",
    ".cs",
    ".java",
    ".kt",
    ".kts",
    ".py",
    ".rb",
    ".php",
    ".js",
    ".jsx",
    ".ts",
    ".tsx",
    ".go",
    ".rs",
    ".swift",
    ".m",
    ".mm",
    ".sh",
    ".bat",
    ".pl",
    ".r",
    ".scala",
    ".groovy",
    ".hs",
    ".erl",
    ".ex",
    ".exs",
    ".dart",
    ".jl",
    ".html",
    ".htm",
    ".css",
    ".scss",
    ".less",
    ".xml",
    ".json",
    ".yaml",
    ".yml",
    ".toml",
    ".ini",
    ".cfg",
    ".conf",
    ".md",
    ".markdown"
)

val runnableExtensions = listOf(
    ".exe",
    ".msi",
    ".bat",
    ".cmd",
    ".com",
    ".scr",
    ".ps1",
    ".jar",
    ".war",
    ".sh",
    ".run",
    ".bin",
    ".app",
    ".dmg",
    ".pkg",
    ".deb",
    ".rpm",
    ".apk",
    ".aab",
    ".ipa"
)

val drawableMap: Map<String, DrawableResource> = mapOf(
    ClipType.AUDIO.id to Res.drawable.audio,
    ClipType.PLAIN_TEXT.id to Res.drawable.blank,
    ClipType.BROKEN.id to Res.drawable.broken,
    ClipType.CODE.id to Res.drawable.code,
    ClipType.FOLDER.id to Res.drawable.directory,
    ClipType.IMAGE.id to Res.drawable.image,
    ClipType.RUNNABLE.id to Res.drawable.runnable,
    ClipType.TEXT.id to Res.drawable.text,
    ClipType.UNKNOWN.id to Res.drawable.unknown,
    ClipType.VIDEO.id to Res.drawable.video,
    ClipType.WEB.id to Res.drawable.web,
    ClipType.ZIP.id to Res.drawable.zip,
)

fun clipTypeToDesc(type: String): String {
    return when (type.toClipType()) {
        ClipType.AUDIO -> "Audio file"
        ClipType.PLAIN_TEXT -> "Plain text"
        ClipType.BROKEN -> "Missing file"
        ClipType.CODE -> "Source code"
        ClipType.FOLDER -> "Folder"
        ClipType.IMAGE -> "Image file"
        ClipType.RUNNABLE -> "App/Executable"
        ClipType.TEXT -> "Text file"
        ClipType.UNKNOWN -> "Unknown type"
        ClipType.VIDEO -> "Video file"
        ClipType.WEB -> "Web link/URL"
        else -> "Zip/Compressed"
    }
}

fun clipTypeToColor(type: String): Color {
    return when (type.toClipType()) {
        ClipType.AUDIO -> Color(0xFF4CAF50)
        ClipType.PLAIN_TEXT -> Color(0xFF9E9E9E)
        ClipType.BROKEN -> Color(0xFFF44336)
        ClipType.CODE -> Color(0xFF3F51B5)
        ClipType.FOLDER -> Color(0xFFFF9800)
        ClipType.IMAGE -> Color(0xFFE91E63)
        ClipType.RUNNABLE -> Color(0xFF009688)
        ClipType.TEXT -> Color(0xFF2196F3)
        ClipType.UNKNOWN -> Color(0xFF795548)
        ClipType.VIDEO -> Color(0xFF9C27B0)
        ClipType.WEB -> Color(0xFF00BCD4)
        else -> Color(0xFF607D8B)
    }
}

suspend fun search(searchParams: String, filters: Filters, pinnedClips: List<Clip>, otherClips: List<Clip>): Pair<List<Clip>, List<Clip>> = withContext(Dispatchers.Default) {
    val tempPinned = mutableListOf<Clip>()
    val tempOther = mutableListOf<Clip>()

    if (searchParams.isNotEmpty()) {
        for (clip in pinnedClips) {
            if ((searchParams.lowercase() in clip.content.lowercase()) || (searchParams.lowercase() in (clip.source?.lowercase() ?: "")) || (searchParams.lowercase() in clip.associatedIcon.lowercase())) {
                tempPinned.add(clip)
            }
        }

        for (clip in otherClips) {
            if ((searchParams.lowercase() in clip.content.lowercase()) || (searchParams.lowercase() in (clip.source?.lowercase() ?: "")) || (searchParams.lowercase() in clip.associatedIcon.lowercase())) {
                tempOther.add(clip)
            }
        }
    } else {
        tempPinned.addAll(pinnedClips)
        tempOther.addAll(otherClips)
    }


    return@withContext filterClips(filters = filters, pinnedClips = tempPinned, otherClips = tempOther)
}

suspend fun filterClips(filters: Filters, pinnedClips: List<Clip>, otherClips: List<Clip>): Pair<List<Clip>, List<Clip>> = withContext(Dispatchers.Default) {
    var pinnedMatches = pinnedClips
    var otherMatches = otherClips

    //by type
    for (type in filters.types) {
        val tempPinned = mutableListOf<Clip>()
        val tempOther = mutableListOf<Clip>()

        for (clip in pinnedMatches) {
            if (clip.associatedIcon.toClipType() == type) {
                tempPinned.add(clip)
            }
        }

        for (clip in otherMatches) {
            if (clip.associatedIcon.toClipType() == type) {
                tempOther.add(clip)
            }
        }

        pinnedMatches = tempPinned
        otherMatches = tempOther
    }

    // by sources
    for (source in filters.sources) {
        val tempPinned = mutableListOf<Clip>()
        val tempOther = mutableListOf<Clip>()

        for (clip in pinnedMatches) {
            if (clip.source == source) {
                tempPinned.add(clip)
            }
        }

        for (clip in otherMatches) {
            if (clip.source == source) {
                tempOther.add(clip)
            }
        }

        pinnedMatches = tempPinned
        otherMatches = tempOther
    }

    //by copy time
    if (filters.copyTime != null) {
        val tempPinned = mutableListOf<Clip>()
        val tempOther = mutableListOf<Clip>()
        val dateTime = LocalDateTime.ofEpochSecond(filters.copyTime, 0, ZoneOffset.UTC).startOfDay()

        for (clip in pinnedMatches) {
            if (dateTime.toEpochSecond(ZoneOffset.UTC) == LocalDateTime.ofEpochSecond(clip.copiedAt, 0, ZoneOffset.UTC).startOfDay().toEpochSecond(ZoneOffset.UTC)) {
                tempPinned.add(clip)
            }
        }

        for (clip in otherMatches) {
            if (dateTime.toEpochSecond(ZoneOffset.UTC) == LocalDateTime.ofEpochSecond(clip.copiedAt, 0, ZoneOffset.UTC).startOfDay().toEpochSecond(ZoneOffset.UTC)) {
                tempOther.add(clip)
            }
        }

        pinnedMatches = tempPinned
        otherMatches = tempOther
    }

    //by line count
    if (filters.lineCount != null) {
        val tempPinned = mutableListOf<Clip>()
        val tempOther = mutableListOf<Clip>()

        for (clip in pinnedMatches) {
            if (filters.lineCount == clip.content.lines().size) {
                tempPinned.add(clip)
            }
        }

        for (clip in otherMatches) {
            if (filters.lineCount == clip.content.lines().size) {
                tempOther.add(clip)
            }
        }

        pinnedMatches = tempPinned
        otherMatches = tempOther
    }

    //by pin state
    if (filters.pinState != null) {
        if (filters.pinState) {
            otherMatches = emptyList()
        } else {
            pinnedMatches = emptyList()
        }
    }

    return@withContext Pair(pinnedMatches, otherMatches)
}