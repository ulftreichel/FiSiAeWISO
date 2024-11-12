package com.fisiaewiso

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "riddles")
data class Riddle(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val riddleMainNumber: Int = 0,
    val riddleNumber: Int = 0,
    val question: String,
    val answers: List<String>,
    val correctAnswers: List<String>,
    val unit: List<String>,
    val hasMultipleCorrectAnswers: Boolean = false,
    val hasdifferentanswers: Boolean = false,
    val requiresNumberInput: Boolean = false,
    val requiresTwoNumberInputs: Boolean = false,
    val requiresOrderedAnswers: Boolean = false,
    val requiresDateInput: Boolean = false,
    val requiresTimeInput: Boolean = false,
    val requiresDragAndDrop: Boolean = false,
    val options: List<String>,
    val optionsWithImage: List<OptionWithImage> = emptyList(),
    val targets: List<String>,
    val correctMappings: Map<String, String>
)

@Entity(tableName = "results")
data class Result(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val riddleMainNumber: Int,
    val points: Int,
    val grade: String
)