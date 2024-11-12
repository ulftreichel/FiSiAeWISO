package com.fisiaewiso

import androidx.room.TypeConverter

class NumberedAnswersTypeConverter {

    @TypeConverter
    fun fromNumberedAnswers(numberedAnswers: List<Pair<Int, String>>): String {
        return numberedAnswers.joinToString(";") { "${it.first},${it.second}" }
    }

    @TypeConverter
    fun toNumberedAnswers(numberedAnswersString: String): List<Pair<Int, String>> {
        return numberedAnswersString.split(";").mapNotNull {
            if (it.isNotEmpty()) {
                val parts = it.split(",")
                if (parts.size == 2 && parts[0].matches(Regex("-?\\d+"))) {
                    Pair(parts[0].toInt(), parts[1])
                } else {
                    null // Ungültiges Format, Paar ignorieren
                }
            } else {
                null // Leerer String, Paar ignorieren
            }
        }
    }
}