package com.example.taskplannerapp.data.utils

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun fromListToString(list: List<String>): String {
        return list.joinToString(",")
    }

    @TypeConverter
    fun fromStringToList(value: String): List<String> {
        return if (value.isBlank()) emptyList() else value.split(",")
    }
}
