package com.sdahymnal.yoruba.data

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonPrimitive

@Immutable
@Serializable
data class Hymn(
    val index: String,
    val number: Int,
    val title: String,
    @SerialName("english_title") val englishTitle: String,
    val references: Map<String, Int> = emptyMap(),
    val lyrics: List<LyricBlock>,
    val revision: Int = 0,
)

@Stable
@Serializable
data class LyricBlock(
    val type: String,
    val index: Int,
    val lines: List<JsonElement>,
) {
    /** For verse/chorus - returns lines as plain strings. */
    val textLines: List<String>
        get() = lines.mapNotNull { (it as? JsonPrimitive)?.content }

    /** For call_response - returns lines as part/text pairs. */
    val callResponseLines: List<CallResponseLine>
        get() = lines.mapNotNull { el ->
            val obj = el as? JsonObject ?: return@mapNotNull null
            CallResponseLine(
                part = obj["part"]?.jsonPrimitive?.content ?: "",
                text = obj["text"]?.jsonPrimitive?.content ?: "",
            )
        }
}

@Immutable
data class CallResponseLine(
    val part: String,
    val text: String,
)
