package com.nullinnix.clippr.database

import com.nullinnix.clippr.misc.CLIP_ID
import com.nullinnix.clippr.misc.COPIED_AT
import com.nullinnix.clippr.misc.Clip
import com.nullinnix.clippr.misc.IS_PINNED
import com.nullinnix.clippr.misc.MIME_TYPE
import com.nullinnix.clippr.misc.CONTENT
import com.nullinnix.clippr.misc.MIME_TYPE_PLAIN_TEXT
import com.nullinnix.clippr.misc.getData
import com.nullinnix.clippr.misc.toJsonArray
import com.nullinnix.clippr.model.ViewModel
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStreamReader

class Database (){
    companion object  {
        var peasantDB: JsonObject? = null
    }

    init {
        getLocalUserDataOnStart()
    }

    private fun getLocalUserDataOnStart() {
        try {
            val fis = FileInputStream(peasantDBFile())
            val bufferedReader = BufferedReader(InputStreamReader(fis))
            var line = bufferedReader.readLine()

            var lines = ""

            while (line != null) {
                lines += line
                line = bufferedReader.readLine()
            }

            fis.close()
            bufferedReader.close()

            if (lines.isNotEmpty()) {
                peasantDB = Json.parseToJsonElement(lines).jsonObject
                loadData()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun loadData() {
        if (peasantDB != null && peasantDB!!.keys.isNotEmpty()) {
            if (peasantDB!!.containsKey(CLIPS_KEY)) {
                val clipsJson = peasantDB!![CLIPS_KEY]!!.jsonArray

                for (clip in clipsJson) {
                    val clipData = clip.jsonObject
                    val clipID = clipData[CLIP_ID]!!.jsonPrimitive.content
                    val content = clipData[CONTENT]?.jsonPrimitive?.content ?: ""
                    val copiedAt = clipData[COPIED_AT]!!.jsonPrimitive.content.toLong()
                    val isPinned = clipData[IS_PINNED]!!.jsonPrimitive.content == "true"
                    val mimeType = clipData[MIME_TYPE]!!.jsonPrimitive.content
                    val isImage = mimeType.split("/")[0] == "image"

                    ViewModel.clips[clipID] = Clip (
                        clipID = clipID,
                        content = content,
                        copiedAt = copiedAt,
                        isPinned = isPinned,
                        mimeType = mimeType,
                        isImage = isImage,
                        exists = if (mimeType != MIME_TYPE_PLAIN_TEXT) {
                            File(content).exists()
                        } else {
                            true
                        }
                    )
                }

                val sortedClips = ViewModel.clips.map {
                    Pair(it.key, it.value.copiedAt)
                }.sortedBy {
                    it.second
                }

                val pinnedClips = mutableListOf<String>()
                var otherClips = mutableListOf<String>()

                for (clip in sortedClips) {
                    if (ViewModel.clips[clip.first]?.isPinned ?: false) {
                        pinnedClips.add(clip.first)
                    } else {
                        otherClips.add(clip.first)
                    }
                }

                otherClips = if (otherClips.size > 100) {
                    otherClips.subList(otherClips.size - 100, otherClips.size)
                } else {
                    otherClips
                }

                ViewModel.pinnedClipKeys.clear()
                ViewModel.otherClipKeys.clear()

                ViewModel.pinnedClipKeys.addAll(pinnedClips)
                ViewModel.otherClipKeys.addAll(otherClips)
            }

            ViewModel.lastCopiedItemHash = peasantDB!!.getData(LAST_COPIED_HASH).ifEmpty { "" }
        }
    }

    fun createClip(
        clip: Clip
    ) {
        ViewModel.clips[clip.clipID] = clip
        ViewModel.otherClipKeys.add(clip.clipID)
        save("create clip")
    }

    private fun save(savingFrom: String) {
        val updatedDB = mutableMapOf<String, Any>()
        updatedDB[CLIPS_KEY] = ViewModel.clips.toJsonArray()
        updatedDB[LAST_COPIED_HASH] = ViewModel.lastCopiedItemHash

        val jsonFromDB = buildJsonObject {
            updatedDB.forEach { (key, value) ->
                put(key, when (value) {
                    is String -> JsonPrimitive(value)
                    is Number -> JsonPrimitive(value)
                    is Boolean -> JsonPrimitive(value)
                    is JsonArray -> value
                    is Map<*, *> -> JsonObject((value as Map<String, Any>).mapValues {
                        JsonPrimitive(it.value.toString())
                    })
                    else -> JsonPrimitive(value.toString())
                })
            }
        }

        val fos = FileOutputStream(peasantDBFile())
        fos.write(jsonFromDB.toString().toByteArray())
        fos.close()
        println("saved db from $savingFrom")
    }
}

fun peasantDBFile(): File {
    val userHome = System.getProperty("user.home")
    val filePath = File(userHome, "Library/Application Support/Clippr/peasantDB.txt")

    if (!filePath.exists()) {
        val fos = FileOutputStream(filePath)
        fos.write("".toByteArray())
        fos.close()
    }

    return filePath
}

const val CLIPS_KEY = "clips"
const val LAST_COPIED_HASH = "lastCopiedHash"