package com.fisiaewiso

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class OptionWithImageConverter {

    @TypeConverter
    fun fromOptionWithImageList(optionsWithImage: List<OptionWithImage>): String {
        return Gson().toJson(optionsWithImage) // OptionWithImage-Liste in JSON-String umwandeln
    }

    @TypeConverter
    fun toOptionWithImageList(optionsWithImageString: String): List<OptionWithImage> {
        val type = object : TypeToken<List<OptionWithImage>>() {}.type
        return Gson().fromJson(
            optionsWithImageString,
            type
        ) // JSON-String in OptionWithImage-Liste umwandeln
    }
}