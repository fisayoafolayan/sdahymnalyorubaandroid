package com.sdahymnalyoruba.data

import org.junit.Assert.assertEquals
import org.junit.Test

class RemoveDiacriticsTest {

    @Test
    fun `strips Yoruba diacritics`() {
        assertEquals("oluwa", HymnRepository.removeDiacritics("Olúwà"))
        assertEquals("eyin", HymnRepository.removeDiacritics("Ẹ̀yìn"))
        assertEquals("olorun", HymnRepository.removeDiacritics("Ọlọ́rùn"))
    }

    @Test
    fun `removes punctuation`() {
        assertEquals("oluwa owo", HymnRepository.removeDiacritics("Olúwà, ọwọ́"))
        assertEquals("jesu ni oba", HymnRepository.removeDiacritics("Jésù ni Ọba!"))
        assertEquals("awa yin in", HymnRepository.removeDiacritics("Àwá' yìn In"))
    }

    @Test
    fun `lowercases everything`() {
        assertEquals("sda hymnal", HymnRepository.removeDiacritics("SDA HYMNAL"))
        assertEquals("gbogbo eyin", HymnRepository.removeDiacritics("Gbogbo Ẹ̀yìn"))
    }

    @Test
    fun `preserves numbers and spaces`() {
        assertEquals("hymn 42", HymnRepository.removeDiacritics("Hymn 42"))
        assertEquals("123", HymnRepository.removeDiacritics("123"))
        assertEquals("sdah 16", HymnRepository.removeDiacritics("SDAH 16"))
    }

    @Test
    fun `handles empty and blank input`() {
        assertEquals("", HymnRepository.removeDiacritics(""))
        assertEquals(" ", HymnRepository.removeDiacritics(" "))
    }

    @Test
    fun `handles plain English text`() {
        assertEquals("all people on earth do dwell", HymnRepository.removeDiacritics("All People On Earth Do Dwell"))
    }
}
