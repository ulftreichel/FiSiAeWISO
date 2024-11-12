package com.fisiaewiso

import androidx.room.TypeConverter

class StringListTypeConverter {

    @TypeConverter
    fun fromStringList(stringList: List<String>): String {
        return stringList.joinToString(",")
    }

    @TypeConverter
    fun toStringList(stringListString: String): List<String> {
        return stringListString.split(",")
    }
}