package com.example.elearning.domain.network

import com.example.elearning.domain.model.MaterialType
import org.junit.Assert.assertEquals
import org.junit.Test

class MaterialAvailabilityTest {
    @Test
    fun coversAllTypesAndConnectionAndDownloadStates() {
        for (type in MaterialType.entries) {
            assertEquals(Availability.AVAILABLE, materialAvailability(type, true, true))
            assertEquals(Availability.AVAILABLE, materialAvailability(type, true, false))
            assertEquals(Availability.AVAILABLE, materialAvailability(type, false, true))
        }
        assertEquals(Availability.AVAILABLE, materialAvailability(MaterialType.TEXT, false, false))
        assertEquals(Availability.REQUIRES_INTERNET, materialAvailability(MaterialType.PDF, false, false))
        assertEquals(Availability.REQUIRES_INTERNET, materialAvailability(MaterialType.VIDEO, false, false))
    }
}
