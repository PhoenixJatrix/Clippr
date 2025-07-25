package com.nullinnix.clippr.database

import com.nullinnix.clippr.misc.CLIP_ID
import com.nullinnix.clippr.misc.COPIED_AT
import com.nullinnix.clippr.misc.Clip
import com.nullinnix.clippr.misc.IS_PINNED
import com.nullinnix.clippr.misc.MIME_TYPE
import com.nullinnix.clippr.misc.TEXT
import com.nullinnix.clippr.misc.URIS
import com.nullinnix.clippr.misc.toJsonArray
import com.nullinnix.clippr.misc.toStringList
import com.nullinnix.clippr.model.ViewModel
import kotlinx.serialization.encodeToString
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
import java.util.UUID

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
                    val text = clipData[TEXT]!!.jsonPrimitive.content
                    val uris = Json.parseToJsonElement(clipData[URIS].toString()).jsonArray.toStringList()
                    val copiedAt = clipData[COPIED_AT]!!.jsonPrimitive.content.toLong()
                    val isPinned = clipData[IS_PINNED]!!.jsonPrimitive.content == "true"
                    val mimeType = clipData[MIME_TYPE]!!.jsonPrimitive.content
                    val isImage = mimeType.split("/")[0] == "image"

                    println("key: $clipID")
                    println(UUID.randomUUID().toString())

                    ViewModel.clips[clipID] = Clip (
                        clipID = clipID,
                        text = text,
                        uris = uris,
                        copiedAt = copiedAt,
                        isPinned = isPinned,
                        mimeType = mimeType,
                        isImage = isImage
                    )
                }
            }
        }
    }

    fun createClip(
        clip: Clip
    ) {
        ViewModel.clips[clip.clipID] = clip
        save("create clip")
    }

    private fun save(savingFrom: String) {
        val updatedDB = mutableMapOf<String, Any>()
        updatedDB[CLIPS_KEY] = ViewModel.clips.toJsonArray()

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