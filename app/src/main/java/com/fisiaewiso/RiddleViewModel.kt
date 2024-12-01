package com.fisiaewiso

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class RiddleViewModel(private val repository: ResultRepository) : ViewModel() {

    private var _currentRiddle: Riddle? = null

    fun saveResultsToDatabase(riddleMainNumber: Int, totalPoints: Int, grade: String) {
        viewModelScope.launch {
            //val riddleMainNumber = currentRiddle?.riddleMainNumber ?: 0
            val roundedPoints = totalPoints // Punkte auf- oder abrunden (bereits als Int übergeben)

            val result = Result(
                riddleMainNumber = riddleMainNumber,
                points = roundedPoints,
                grade = grade
            )
            Log.d("RiddleViewModel", "Speichere Ergebnis in Datenbank: $result")
            repository.insert(result)
        }
    }

    // Funktion zum Aktualisieren von currentRiddle
    fun updateCurrentRiddle(riddle: Riddle) {
        _currentRiddle = riddle
    }

    // Funktion zum Berechnen der Zensur
    fun calculateGrade(points: Int): String {
        return when (points) {
            in 92..100 -> "sehr gut"
            in 81..91 -> "gut"
            in 67..80 -> "befriedigend"
            in 50..66 -> "ausreichend"
            in 30..49 -> "mangelhaft"
            else -> "ungenügend"
        }
    }
}