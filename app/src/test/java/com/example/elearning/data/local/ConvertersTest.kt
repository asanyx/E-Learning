package com.example.elearning.data.local

import org.junit.Assert.assertEquals
import org.junit.Test

class ConvertersTest {
    private val converters = Converters()

    @Test
    fun roundTripPreservesOrderAndEmptyOptions() {
        val options = listOf("A", "B", "")
        assertEquals(options, converters.decode(converters.encode(options)))
    }

    @Test
    fun emptyListRoundTrips() {
        assertEquals(emptyList<String>(), converters.decode(converters.encode(emptyList())))
    }

    @Test
    fun preservesSpacesAndCommas() {
        val options = listOf("  pierwsza opcja ", "druga, trzecia")
        assertEquals(options, converters.decode(converters.encode(options)))
    }
}
