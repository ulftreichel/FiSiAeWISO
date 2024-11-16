package com.fisiaewiso

import android.app.Dialog
import android.content.Context
import android.widget.Button
import android.widget.TextView

class WrongAnswerDialog(context: Context, correctAnswers: List<String>, currentRiddleUnit: List<String>, correctMappings: Map<String, String> = emptyMap()): Dialog(context) {
    var listener: WrongAnswerDialogListener? = null
    init {
        setContentView(R.layout.wrong_answer_dialog)
        val correctAnswerTextView = findViewById<TextView>(R.id.correctAnswerTextView)
        val okButton = findViewById<Button>(R.id.ok1Button)
        if (correctMappings.isNotEmpty()) {
            // Richtige Antworten für Drag-and-Drop-Fragen anzeigen
            val correctAnswersText = correctMappings.entries.joinToString("\n") { (option, target) ->
                "- $option -> $target"
            }
            correctAnswerTextView.text = "Die richtige${if (correctMappings.size > 1) "n Antworten wären" else " Antwort wäre"}:\n\n$correctAnswersText"
        } else {
            val extendedRiddleUnit = currentRiddleUnit + List(correctAnswers.size - currentRiddleUnit.size) { "" }
            val correctAnswersWithUnits = correctAnswers.zip(extendedRiddleUnit) { answer, unit ->
                if (unit.isNotEmpty()) { // Überprüfen, ob eine Einheit vorhanden ist
                    "$answer $unit" // Antwort und Einheit mit Leerzeichen getrennt
                } else {
                    answer // Nur die Antwort anzeigen, wenn keine Einheit vorhanden ist
                }
            }.joinToString("\n- ", prefix = "- ")
            correctAnswerTextView.text = "Die richtige${if (correctAnswers.size > 1) "n Antworten wären" else " Antwort wäre"}:\n\n$correctAnswersWithUnits"
        }
        okButton.setOnClickListener {
            listener?.onOkClicked()
            dismiss()
        }
    }

    interface WrongAnswerDialogListener {
        fun onOkClicked()
    }

}