package com.example.elearning.data.local

import androidx.room.TypeConverter

class Converters {
    private val separator = "\u001F"

    @TypeConverter
    fun encode(options: List<String>): String {
        require(options.none { separator in it })
        return options.joinToString(separator = "") { separator + it }
    }

    @TypeConverter
    fun decode(value: String): List<String> =
        if (value.isEmpty()) emptyList() else value.removePrefix(separator).split(separator)
}
