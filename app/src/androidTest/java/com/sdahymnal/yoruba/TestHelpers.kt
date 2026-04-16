package com.sdahymnal.yoruba

import com.sdahymnal.yoruba.data.Hymn
import kotlinx.serialization.json.Json

private val json = Json { ignoreUnknownKeys = true }

fun makeTestHymn(
    number: Int,
    title: String = "Title $number",
    englishTitle: String = "English $number",
    lyrics: List<Pair<String, String>> = emptyList(),
    refs: Map<String, Int> = emptyMap(),
): Hymn {
    val lyricsJson = if (lyrics.isNotEmpty()) {
        val lines = lyrics.joinToString(",") { "\"${it.first}\"" }
        """[{"type":"verse","index":1,"lines":[$lines]}]"""
    } else "[]"

    val refsJson = if (refs.isNotEmpty()) {
        refs.entries.joinToString(",") { "\"${it.key}\": ${it.value}" }
    } else ""

    return json.decodeFromString<Hymn>("""
    {
        "index": "${number.toString().padStart(3, '0')}",
        "number": $number,
        "title": "$title",
        "english_title": "$englishTitle",
        "references": {$refsJson},
        "lyrics": $lyricsJson
    }
    """)
}

fun makeTestHymnWithVerses(
    number: Int,
    title: String,
    englishTitle: String = "English $number",
    verses: List<List<String>>,
): Hymn {
    val blocksJson = verses.mapIndexed { i, lines ->
        val linesJson = lines.joinToString(",") { "\"$it\"" }
        """{"type":"verse","index":${i + 1},"lines":[$linesJson]}"""
    }.joinToString(",")

    return json.decodeFromString<Hymn>("""
    {
        "index": "${number.toString().padStart(3, '0')}",
        "number": $number,
        "title": "$title",
        "english_title": "$englishTitle",
        "references": {},
        "lyrics": [$blocksJson]
    }
    """)
}
