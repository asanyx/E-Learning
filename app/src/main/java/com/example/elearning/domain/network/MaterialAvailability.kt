package com.example.elearning.domain.network

import com.example.elearning.domain.model.MaterialType

enum class Availability { AVAILABLE, REQUIRES_INTERNET }

fun materialAvailability(type: MaterialType, isDownloaded: Boolean, isOnline: Boolean): Availability =
    if (type == MaterialType.TEXT || isDownloaded || isOnline) Availability.AVAILABLE
    else Availability.REQUIRES_INTERNET
