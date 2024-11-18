package com.fisiaewiso

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.TextView
import androidx.fragment.app.DialogFragment

class HelpDialog (private val currentRiddleNumber: Int): DialogFragment() {

    // Hier die Logik für das Dialog zur Verfügung stellen
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            val inflater = requireActivity().layoutInflater
            val view = inflater.inflate(R.layout.help_formel_dialog, null)
            val helpText = view.findViewById<TextView>(R.id.help_formel_textView)
            if (currentRiddleNumber == 4) {
                helpText.text = getString(R.string.BeitragRente)
            } else if (currentRiddleNumber == 16) {
                helpText.text = getString(R.string.Eigenkapitalrentabilität)
            } else if (currentRiddleNumber == 20) {
                helpText.text = getString(R.string.Wirtschaftlichkeit)
            } else if (currentRiddleNumber == 46) {
                helpText.text = getString(R.string.Eigenkapitalrentabilität)
            } else if (currentRiddleNumber == 47) {
                helpText.text = ("noch nachtragen")
            } else if (currentRiddleNumber == 79) {
                helpText.text = getString(R.string.Wirtschaftlichkeit)
            } else if (currentRiddleNumber == 80) {
                helpText.text = getString(R.string.Eigenkapitalrentabilität)
            } else if (currentRiddleNumber == 108) {
                helpText.text = getString(R.string.Eigenkapitalrentabilität)
            }else if (currentRiddleNumber == 142) {
                helpText.text = getString(R.string.Wirtschaftlichkeit)
            } else {
                helpText.text = "es gibt keine Hilfe zur aktuellen Frage"
            }

            builder.setView(view)
                .setTitle("Hilfe zur Berechnung")
                .setNegativeButton("Abbrechen") { _, _ -> dismiss() }
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}
