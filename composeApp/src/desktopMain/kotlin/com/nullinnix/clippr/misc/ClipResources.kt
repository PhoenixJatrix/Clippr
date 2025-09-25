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
import clippr.composeapp.generated.resources.zip
import org.apache.tika.Tika
import org.jetbrains.compose.resources.DrawableResource
import java.awt.Image
import java.awt.Toolkit
import java.awt.datatransfer.Clipboard
import java.awt.datatransfer.ClipboardOwner
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.Transferable
import java.io.File
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.UUID
import javax.swing.JTextField

var lastCopiedItemHash = ""

fun getClipboard(
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
                            exists = path.exists()
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
                            exists = path.exists()
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

            onCopy(
                Clip(
                    clipID = UUID.randomUUID().toString(),
                    content = content,
                    copiedAt = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC),
                    isPinned = false,
                    mimeType = MIME_TYPE_PLAIN_TEXT,
                    isImage = false,
                    exists = true
                )
            )
        }
    }
}

fun getIconForContent(
    mimeType: String,
    exists: Boolean
): DrawableResource {
    val mediaType = mimeType.split("/")[0]
    val subType = mimeType.split("/")[1]

    return if (exists) {
        if (mimeType != MIME_TYPE_PLAIN_TEXT) {
            when (mediaType) {
                "dir" -> {
                    Res.drawable.directory
                }

                "image" -> {
                    Res.drawable.image
                }

                "audio" -> {
                    Res.drawable.audio
                }

                "video" -> {
                    Res.drawable.video
                }

                "application" -> {
                    when (subType) {
                        in listOf("zip", "7z", "gz", "rar", "java-archive", "tar.gz", "app", "zlib") -> {
                            Res.drawable.zip
                        }

                        in listOf("x-bzip2", "dmg", "x-apple-diskimage", "msi", "x-ms-installer", "x-dosexec", "jar", "x-bat") -> {
                            Res.drawable.runnable
                        }

                        in listOf("json") -> {
                            Res.drawable.code
                        }

                        else -> {
                            Res.drawable.unknown
                        }
                    }
                }

                "text" -> {
                    when (subType) {
                        in listOf("plain", "csv") -> {
                            Res.drawable.text
                        }

                        in listOf("x-c++src", "x-csrc", "x-java-source", "x-groovy", "x-scala", "x-kotlin", "x-python", "javascript", "x-typescript", "x-go", "x-rustsrc", "x-swift", "x-ruby", "x-php", "x-clojure", "x-haskell", "x-ocaml", "x-perl", "x-sh", "x-r-source", "vnd.dart", "x-elixir", "x-lua", "x-matlab", "x-vbasic", "x-stsrc", "x-coffeescript", "x-less", "x-yaml", "x-rst", "json", "xml", "sql") -> {
                            Res.drawable.code
                        }

                        else -> {
                            Res.drawable.unknown
                        }
                    }
                }

                else -> {
                    Res.drawable.unknown
                }
            }
        } else {
            Res.drawable.blank
        }
    } else {
        Res.drawable.broken
    }
}

fun onCopyToClipboard(content: String, mimeType: String, onHashed: (String?) -> Unit) {
    val clipboard = Toolkit.getDefaultToolkit().systemClipboard
    val customTransferable = CustomTransferable(mimeType, content)

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

class CustomTransferable(mimeType: String, val content: String): Transferable {
    val mediaType = mimeType.split("/")[0]
    val subType = mimeType.split("/")[1]
    val dataFlavors = mimeTypeToDataFlavor(mimeType)
    var hash: String? = null

    init {
        hash = content.hash()
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
                        println("copying text")
                        content
                    }

                    else -> {
                        println("copying other")
                        listOf(File(content))
                    }
                }

            }

            "image" -> {
                println("copying image")

                val toolKit = Toolkit.getDefaultToolkit()
                val file = File(content)
                println(file.exists())
                //it exists
                toolKit.getImage(content) as Image
            }

            else -> {
                println("copying other")
                listOf(File(content))
            }
        }
    }
}

class CustomClipboardOwner: ClipboardOwner {
    override fun lostOwnership(
        p0: Clipboard?,
        p1: Transferable?,
    ) {
        TODO("Not yet implemented")
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