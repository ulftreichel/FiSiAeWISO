package com.fisiaewiso

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.widget.Button
import android.widget.TextView

class ResultNoDataDialog(context: Context) : Dialog(context) {

    init {
        setContentView(R.layout.no_data_result)
        val nodataTextView = findViewById<TextView>(R.id.no_data_textview)
        val okButton = findViewById<Button>(R.id.okButton)

        // Setze die Punktzahl und die Zensur
        nodataTextView.text = context.getString(R.string.no_results)

        okButton.setOnClickListener {
            dismiss() // Schließe den Dialog
            (context as Activity).finish()
        }
    }
}