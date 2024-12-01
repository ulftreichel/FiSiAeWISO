package com.fisiaewiso

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.widget.Button
import android.widget.TextView

class ResultDialog(context: Context, totalPoints: Double, grade: String) : Dialog(context) {

    init {
        setContentView(R.layout.result_dialog)
        val scoreTextView = findViewById<TextView>(R.id.scoreTextView)
        val gradeTextView = findViewById<TextView>(R.id.gradeTextView)
        val okButton = findViewById<Button>(R.id.okButton)

        // Setze die Punktzahl und die Zensur
        scoreTextView.text = context.getString(R.string.punktzahl, Math.round(totalPoints))
        gradeTextView.text = context.getString(R.string.zensur, grade)

        okButton.setOnClickListener {
            dismiss() // Schließe den Dialog
            (context as Activity).finish()
        }
    }
}