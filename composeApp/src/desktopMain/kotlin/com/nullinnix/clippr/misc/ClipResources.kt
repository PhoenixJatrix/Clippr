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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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
import javax.swing.JOptionPane

var lastCopiedItemHash = ""
var duplicateHandlerHash = ""

fun getClipboard (
    sourceExceptions: Set<String>,
    clipTypeExceptions: Set<ClipType>,
    onCopy: (Clip) -> Unit,
) {
    try {
        val clipboard = Toolkit.getDefaultToolkit().systemClipboard
        val contents = clipboard.getContents(null)

        if (contents.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
            val paths = contents.getTransferData(DataFlavor.javaFileListFlavor) as List<*>

            if (lastCopiedItemHash != paths.toString().hash() && duplicateHandlerHash != paths.toString().hash()) {
                duplicateHandlerHash = paths.toString().hash()

                for (index in paths.indices) {
                    if ((paths[index] as File).isDirectory) {
                        val path = paths[index] as File
                        val source = getClipSource()

                        log("dir ${path.path} to dir from $source", "")
                        println("dir ${path.path} to dir from $source")

                        if (!sourceExceptions.contains(source ?: ClipType.UNKNOWN) && !clipTypeExceptions.contains(ClipType.FOLDER)) {
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
                                    source = source ?: "unknown"
                                )
                            )

                            lastCopiedItemHash = paths.toString().hash()
                        }
                    } else {
                        val path = paths[index] as File
                        val source = getClipSource()
                        val mimeType = Files.probeContentType(Paths.get(path.path))

                        log("file ${path.path} to $mimeType from $source", "")
                        println("file ${path.path} to $mimeType from $source")

                        val resolvedType = getIconForContent(mimeType, path.path.lowercase())

                        println("sources = $sourceExceptions == $source, type = $clipTypeExceptions == $resolvedType")

                        if (!sourceExceptions.contains(source ?: ClipType.UNKNOWN) && !clipTypeExceptions.contains(resolvedType.toClipType())) {
                            onCopy(
                                Clip(
                                    clipID = UUID.randomUUID().toString(),
                                    content = path.path,
                                    copiedAt = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC),
                                    isPinned = false,
                                    mimeType = mimeType,
                                    isImage = resolvedType.toClipType() == ClipType.IMAGE,
                                    exists = path.exists(),
                                    pinnedAt = 0L,
                                    associatedIcon = resolvedType,
                                    source = source ?: "unknown"
                                )
                            )

                            lastCopiedItemHash = paths.toString().hash()
                        }
                    }
                }
            }
        } else if (contents.isDataFlavorSupported(DataFlavor.stringFlavor)) {
            val content = contents.getTransferData(DataFlavor.stringFlavor) as String
            val hash = content.hash()

            if (lastCopiedItemHash != hash  && duplicateHandlerHash != hash) {
                duplicateHandlerHash = hash
                val source = getClipSource()

                log("str $content from $source", "")
                println("str $content from $source")

                val resolvedType = getIconForContent(MIME_TYPE_PLAIN_TEXT, content)

                println("sources = $sourceExceptions == $source, type = $clipTypeExceptions == $resolvedType")

                if (!sourceExceptions.contains(source ?: ClipType.UNKNOWN) && !clipTypeExceptions.contains(resolvedType.toClipType())) {
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
                            associatedIcon = resolvedType,
                            source = source ?: "unknown"
                        )
                    )

                    lastCopiedItemHash = hash
                }
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

fun onCopyToClipboard(clip: Clip, pasteAsFile: Boolean) {
    val clipboard = Toolkit.getDefaultToolkit().systemClipboard
    val customTransferable = CustomTransferable(clip, pasteAsFile)

    lastCopiedItemHash = customTransferable.hash ?: lastCopiedItemHash

    clipboard.setContents(customTransferable, CustomClipboardOwner())
}

fun onCopyMultipleToClipboard(clips: Set<Clip>) {
    val clipboard = Toolkit.getDefaultToolkit().systemClipboard
    val multiFileTransferable = MultiFileTransferable(clips)

    lastCopiedItemHash = multiFileTransferable.hash ?: lastCopiedItemHash

    clipboard.setContents(multiFileTransferable, CustomClipboardOwner())
}

fun clipTypeToDataFlavor(clipType: ClipType, pasteAsFile: Boolean): DataFlavor {
    return when (clipType) {
        ClipType.PLAIN_TEXT -> {
            DataFlavor.stringFlavor
        }

        ClipType.IMAGE -> {
            DataFlavor.imageFlavor
        }

        else -> {
            if (pasteAsFile) {
                DataFlavor.javaFileListFlavor
            } else {
                DataFlavor.stringFlavor
            }
        }
    }
}

class CustomTransferable(private val clip: Clip, private val pasteAsFile: Boolean): Transferable {
    val dataFlavors = listOf(clipTypeToDataFlavor(clip.associatedIcon.toClipType(), pasteAsFile))
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
        return when (clip.associatedIcon.toClipType()) {
            ClipType.PLAIN_TEXT -> {
                clip.content
            }

            ClipType.IMAGE -> {
                if (pasteAsFile && File(clip.content).exists()) {
                    listOf(File(clip.content))
                } else {
                    clip.content
                }
            }

            else -> {
                if (pasteAsFile && File(clip.content).exists()) {
                    listOf(File(clip.content))
                } else {
                    clip.content
                }
            }
        }
    }
}

class MultiFileTransferable(private val clips: Set<Clip>): Transferable {
    val dataFlavors = clips.map {
        clipTypeToDataFlavor(it.associatedIcon.toClipType(), true)
    }

    var hash: String? = null

    init {
        hash = clips.toString().hash()
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
        val files = mutableListOf<File>()

        clips.forEach {
            files.add(File(it.content))
        }

        return files
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

fun pasteWithRobot(clip: Clip, pasteAsFile: Boolean, wait: Int = 0) {
    CoroutineScope(Dispatchers.Default).launch {
        lastCopiedItemHash = clip.content.hash()

        delay(wait * 1000L)

        onCopyToClipboard(clip = clip, pasteAsFile = pasteAsFile)

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
}

fun pasteMultipleFilesWithRobot(clips: Set<Clip>, wait: Int = 0) {
    CoroutineScope(Dispatchers.Default).launch {
        lastCopiedItemHash = clips.toString().hash()

        delay(wait * 1000L)

        clips.forEach { clip ->
            onCopyToClipboard(clip = clip, pasteAsFile = true)

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

            Thread.sleep(50)
        }
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

suspend fun search(searchParams: String, filters: Filters, clips: List<Clip>, allApps: Map<String, MacApp>): List<Clip> = withContext(Dispatchers.Default) {
    val tempClips = mutableListOf<Clip>()
    val contentMatches = mutableListOf<Clip>()
    val sourceMatches = mutableListOf<Clip>()
    val typeMatches = mutableListOf<Clip>()

    if (searchParams.isNotEmpty()) {
        for (clip in clips) {
            if ((searchParams.lowercase() in clip.content.lowercase())) {
                contentMatches.add(clip)
            } else if (searchParams.lowercase() in (allApps[clip.source]?.name?.lowercase() ?: "")) {
                sourceMatches.add(clip)
            } else if (searchParams.lowercase() in clipTypeToDesc(clip.associatedIcon).lowercase()) {
                typeMatches.add(clip)
            }
        }

        tempClips.addAll(contentMatches)
        tempClips.addAll(sourceMatches)
        tempClips.addAll(typeMatches)
    } else {
        val sorted = clips.sortedBy {
            it.copiedAt
        }

        tempClips.addAll(sorted.reversed())
    }

    return@withContext filterClips(filters = filters, clips = tempClips)
}

suspend fun filterClips(filters: Filters, clips: List<Clip>): List<Clip> = withContext(Dispatchers.Default) {
    var matches = clips.toMutableList()

    //by type
    run {
        val temp = mutableListOf<Clip>()

        for (clip in matches) {
            if (clip.associatedIcon.toClipType() in filters.types) {
                temp.add(clip)
            }
        }

        matches = temp
    }

    // by sources
    run {
        val temp = mutableListOf<Clip>()

        for (clip in matches) {
            if (clip.source in filters.sources || (clip.source == null && filters.sources.contains("unknown"))) {
                temp.add(clip)
            }
        }

        matches = temp
    }

    //by copy time
    if (filters.copyTime != null) {
        val temp = mutableListOf<Clip>()
        val dateTime = LocalDateTime.ofEpochSecond(filters.copyTime, 0, ZoneOffset.UTC).startOfDay()

        for (clip in matches) {
            if (dateTime.toEpochSecond(ZoneOffset.UTC) == LocalDateTime.ofEpochSecond(clip.copiedAt, 0, ZoneOffset.UTC).startOfDay().toEpochSecond(ZoneOffset.UTC)) {
                temp.add(clip)
            }
        }

        matches = temp
    }

    //by line count
    if (filters.lineCount != null) {
        val temp = mutableListOf<Clip>()

        for (clip in matches) {
            if (filters.lineCount == clip.content.lines().size) {
                temp.add(clip)
            }
        }

        matches = temp
    }

    //by pin state
    if (filters.pinState != null) {
        val temp = mutableListOf<Clip>()

        for (clip in matches) {
            if (filters.pinState) {
                if (clip.isPinned)
                    temp.add(clip)
            } else {
                if (!clip.isPinned)
                    temp.add(clip)
            }
        }

        matches = temp
    }

    return@withContext matches.toList()
}

fun ClipMenuAction.desc(secondsBeforePaste: Int): String {
    return when(this) {
        ClipMenuAction.PasteAsText -> "Paste as plain text in ${secondsBeforePaste}s"
        ClipMenuAction.PasteAsFile -> "Paste as file in ${secondsBeforePaste}s"
        ClipMenuAction.CopyAsText -> "Copy as plain text"
        ClipMenuAction.CopyAsFile -> "Copy as file"
        ClipMenuAction.Pin -> "Pin"
        ClipMenuAction.Unpin -> "Unpin"
        ClipMenuAction.Preview -> "Preview clip"
        ClipMenuAction.OpenAsLink -> "Open in browser"
        ClipMenuAction.RevealInFinder -> "Reveal in Finder"
        ClipMenuAction.Delete -> "Delete"
    }
}

fun ClipMenuAction.shortcut(): String {
    return when(this) {
        ClipMenuAction.PasteAsText -> "⌘ + V"
        ClipMenuAction.PasteAsFile -> "⌥ + V"
        ClipMenuAction.CopyAsText -> "⌘ + C"
        ClipMenuAction.CopyAsFile -> "⌥ + C"
        ClipMenuAction.Pin -> "⌘ + P"
        ClipMenuAction.Unpin -> "⌘ + P"
        ClipMenuAction.Preview -> "⏎"
        ClipMenuAction.OpenAsLink -> "⌘ + ⏎"
        ClipMenuAction.RevealInFinder -> "⌘ + ⏎"
        ClipMenuAction.Delete -> "⌘ + ⌦"
    }
}

fun ClipMenuAction.info(secondsBeforePaste: Int): String {
    return when(this) {
        ClipMenuAction.PasteAsText -> "Paste as plain text to focused window in ${secondsBeforePaste}s"
        ClipMenuAction.PasteAsFile -> "Paste as file to focused window in ${secondsBeforePaste}s"
        ClipMenuAction.CopyAsText -> "Copy this clip to global clipboard as plain text"
        ClipMenuAction.CopyAsFile -> "Copy this clip to global clipboard a file"
        ClipMenuAction.Pin -> "Pin this clip"
        ClipMenuAction.Unpin -> "Unpin this clip"
        ClipMenuAction.Preview -> "Open this clip in preview window"
        ClipMenuAction.OpenAsLink -> "Open in a browser"
        ClipMenuAction.RevealInFinder -> "Open the location of this clip if it exists"
        ClipMenuAction.Delete -> "Delete clip"
    }
}

fun MultiSelectClipMenuAction.desc(secondsBeforePaste: Int): String {
    return when(this) {
        MultiSelectClipMenuAction.Paste -> "Paste clips in ${secondsBeforePaste}s"
        MultiSelectClipMenuAction.Copy -> "Copy clips"
        MultiSelectClipMenuAction.Merge -> "Merge all"
        MultiSelectClipMenuAction.PinAll -> "Pin all"
        MultiSelectClipMenuAction.UnpinAll -> "Unpin all"
        MultiSelectClipMenuAction.DeleteAll -> "Delete all"
    }
}

fun MultiSelectClipMenuAction.shortcut(): String {
    return when(this) {
        MultiSelectClipMenuAction.Paste -> "⌘ + V"
        MultiSelectClipMenuAction.Copy -> "⌘ + C"
        MultiSelectClipMenuAction.Merge -> ""
        MultiSelectClipMenuAction.PinAll -> "⌘ + P"
        MultiSelectClipMenuAction.UnpinAll -> "⌥ + P"
        MultiSelectClipMenuAction.DeleteAll -> "⌘ + ⌦"
    }
}

fun MultiSelectClipMenuAction.info(secondsBeforePaste: Int): String {
    return when(this) {
        MultiSelectClipMenuAction.Paste -> "Paste clips to focused window in ${secondsBeforePaste}s"
        MultiSelectClipMenuAction.Copy -> "Copy clips to global keyboard"
        MultiSelectClipMenuAction.Merge -> "Merge text and or files"
        MultiSelectClipMenuAction.PinAll -> "Pin all selected clips"
        MultiSelectClipMenuAction.UnpinAll -> "Unpin all selected clips"
        MultiSelectClipMenuAction.DeleteAll -> "Delete all selected clips"
    }
}

fun MergeAction.desc(): String {
    return when(this) {
        MergeAction.CommaSeparated -> "Separate with comma(,)"
        MergeAction.NewLineSeparated -> "Separate with a new line"
        MergeAction.NumberSeparated -> "Number"
        MergeAction.SpaceSeparated -> "Separate with space"
        MergeAction.NoSeparation -> "No separation"
    }
}

fun MergeAction.info(): String {
    return when(this) {
        MergeAction.CommaSeparated -> "\nClip1,Clip2,Clip3\n"
        MergeAction.NewLineSeparated -> "Clip1\nClip2\nClip3"
        MergeAction.NumberSeparated -> "1. Clip1\n2. Clip2\n3. Clip3"
        MergeAction.SpaceSeparated -> "\nClip1 Clip2 Clip3\n"
        MergeAction.NoSeparation -> "\nClip1Clip2Clip3\n"
    }
}

fun getClipMenuActions(clip: Clip): List<ClipMenuAction> {
    val clipActions = ClipMenuAction.entries.toMutableList()

    if (clip.isPinned) {
        clipActions.remove(ClipMenuAction.Pin)
    } else {
        clipActions.remove(ClipMenuAction.Unpin)
    }

    clipActions.remove(ClipMenuAction.OpenAsLink)

    return when (clip.associatedIcon.toClipType()) {
        ClipType.PLAIN_TEXT -> {
            clipActions.remove(ClipMenuAction.PasteAsFile)
            clipActions.remove(ClipMenuAction.CopyAsFile)
            clipActions.remove(ClipMenuAction.RevealInFinder)
            clipActions
        }
        ClipType.WEB -> {
            clipActions.remove(ClipMenuAction.PasteAsFile)
            clipActions.remove(ClipMenuAction.CopyAsFile)
            clipActions.remove(ClipMenuAction.RevealInFinder)
            clipActions.add(ClipMenuAction.OpenAsLink)
            clipActions
        }
        else -> {
            clipActions
        }
    }
}