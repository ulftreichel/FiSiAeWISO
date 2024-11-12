package com.fisiaewiso

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.widget.Button
import android.widget.TextView

class ResultDialog(context: Context, private val totalPoints: Double, private val grade: String) : Dialog(context) {

    init {
        setContentView(R.layout.result_dialog)
        val scoreTextView = findViewById<TextView>(R.id.scoreTextView)
        val gradeTextView = findViewById<TextView>(R.id.gradeTextView)
        val okButton = findViewById<Button>(R.id.okButton)

        // Setze die Punktzahl und die Zensur
        scoreTextView.text = "Punktzahl: ${Math.round(totalPoints)}"
        gradeTextView.text = "Zensur: $grade"

        okButton.setOnClickListener {
            dismiss() // Schließe den Dialog
            (context as Activity).finish()
        }
    }
}