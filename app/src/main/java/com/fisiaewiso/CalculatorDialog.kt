package com.fisiaewiso

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.ScrollView
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import kotlin.text.toDouble
import net.objecthunter.exp4j.ExpressionBuilder

class CalculatorDialog(private val question: String) : DialogFragment() {
    val intermediateResults = mutableListOf<Double>()
    private lateinit var radioGroupResults: RadioGroup
    private lateinit var dialog: AlertDialog
    var selectedRadioButtonId = -1
    private lateinit var buttonApply: Button
    private lateinit var buttonCe: Button

    //Logik für das Dialog zur Verfügung stellen
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            val inflater = requireActivity().layoutInflater
            val view = inflater.inflate(R.layout.calculator_dialog, null)
            val scrollViewQuestion = view.findViewById<ScrollView>(R.id.scrollViewQuestionCalc)
            val riddleQuestion = view.findViewById<TextView>(R.id.tVQuestionCalc)
            val display = view.findViewById<EditText>(R.id.calculator_display)
            val button0 = view.findViewById<Button>(R.id.button_0)
            val button1 = view.findViewById<Button>(R.id.button_1)
            val button2 = view.findViewById<Button>(R.id.button_2)
            val button3 = view.findViewById<Button>(R.id.button_3)
            val button4 = view.findViewById<Button>(R.id.button_4)
            val button5 = view.findViewById<Button>(R.id.button_5)
            val button6 = view.findViewById<Button>(R.id.button_6)
            val button7 = view.findViewById<Button>(R.id.button_7)
            val button8 = view.findViewById<Button>(R.id.button_8)
            val button9 = view.findViewById<Button>(R.id.button_9)
            val buttonAdd = view.findViewById<Button>(R.id.button_add)
            val buttonSubtract = view.findViewById<Button>(R.id.button_subtract)
            val buttonMultiply = view.findViewById<Button>(R.id.button_multiply)
            val buttonDivide = view.findViewById<Button>(R.id.button_divide)
            val buttonDecimal = view.findViewById<Button>(R.id.button_decimal)
            val buttonPercent = view.findViewById<Button>(R.id.button_percent)
            buttonCe = view.findViewById<Button>(R.id.button_ce)
            val buttonC = view.findViewById<Button>(R.id.button_c)
            val buttonDel = view.findViewById<Button>(R.id.button_del)
            val buttonEquals = view.findViewById<Button>(R.id.button_equals)
            buttonApply = view.findViewById<Button>(R.id.button_apply)
            val buttonSave = view.findViewById<Button>(R.id.button_save)
            radioGroupResults = view.findViewById<RadioGroup>(R.id.radio_group_results)
            riddleQuestion.text = question
            // Logik für Button-Klicks
            button0.setOnClickListener { display.append("0") }
            button1.setOnClickListener { display.append("1") }
            button2.setOnClickListener { display.append("2") }
            button3.setOnClickListener { display.append("3") }
            button4.setOnClickListener { display.append("4") }
            button5.setOnClickListener { display.append("5") }
            button6.setOnClickListener { display.append("6") }
            button7.setOnClickListener { display.append("7") }
            button8.setOnClickListener { display.append("8") }
            button9.setOnClickListener { display.append("9") }
            buttonAdd.setOnClickListener {
                val currentInput = display.text.toString()
                display.text = Editable.Factory.getInstance().newEditable(currentInput + "+")
            }
            buttonSubtract.setOnClickListener {
                val currentInput = display.text.toString()
                display.text = Editable.Factory.getInstance().newEditable(currentInput + "-")
            }
            buttonMultiply.setOnClickListener {
                val currentInput = display.text.toString()
                display.text = Editable.Factory.getInstance().newEditable(currentInput + "*")
            }
            buttonDivide.setOnClickListener {
                val currentInput = display.text.toString()
                display.text = Editable.Factory.getInstance().newEditable(currentInput + "/")
            }
            buttonDecimal.setOnClickListener {
                val currentInput = display.text.toString()
                display.text = Editable.Factory.getInstance().newEditable(currentInput + ".")
            }
            buttonPercent.setOnClickListener {
                val currentInput = display.text.toString()
                display.text = Editable.Factory.getInstance().newEditable(currentInput + "%")
            }

            buttonCe.setOnClickListener { radioGroupResults.removeAllViews() }
            buttonC.setOnClickListener { display.text.clear() }
            buttonEquals.setOnClickListener {
                val result = evaluateExpression(display.text.toString())
                display.text = Editable.Factory.getInstance().newEditable(result.toString())
            }
            buttonDel.setOnClickListener {
                val currentText = display.text.toString()
                if (currentText.isNotEmpty()) {
                    display.setText(currentText.substring(0, currentText.length - 1))
                }
            }
            buttonPercent.setOnClickListener {
                try {
                    val currentText = display.text.toString()
                    val currentValue = currentText.toDoubleOrNull()
                    if (currentValue != null) {
                        val result = currentValue / 100.0
                        display.setText(result.toString())
                    } else {
                        // Fehlerbehandlung
                    }
                } catch (e: Exception) {
                    // Fehlerbehandlung
                }
            }
            if (intermediateResults.isEmpty()) {
                buttonApply.isEnabled = false
                buttonCe.isEnabled = false
            }
            // Event-Listener für den "Übernehmen"-Button
            buttonApply.setOnClickListener {
                val selectedRadioButton = dialog.findViewById<RadioButton>(selectedRadioButtonId)
                if (selectedRadioButtonId != -1) {
                    val selectedRadioButton = dialog.findViewById<RadioButton>(selectedRadioButtonId)
                    val selectedResult = selectedRadioButton.text.toString().toDouble()
                    display.text = Editable.Factory.getInstance().newEditable(selectedResult.toString())
                }
            }
            // Event-Listener für den "Speichern"-Button
            buttonSave.setOnClickListener {
                val result = evaluateExpression(display.text.toString())
                // Dialog anzeigen, um zu fragen, ob das Ergebnis gespeichert werden soll
                dialog = AlertDialog.Builder(requireContext()) // this@MainActivity bezieht sich auf den Context der Activity
                    .setTitle("Ergebnis speichern?")
                    .setMessage("Möchten Sie das Ergebnis speichern?")
                    .setPositiveButton("Ja") { _, _ ->
                        intermediateResults.add(result) // Ergebnis speichern
                        addRadioButton(result) // RadioButton erstellen und hinzufügen
                        display.text.clear()
                    }
                    .setNegativeButton("Nein", null)
                    .show()
            }
            builder.setView(view)
                .setTitle("Taschenrechner")
                .setNegativeButton("Abbrechen") { _, _ -> dismiss() }
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }


    fun addRadioButton(result: Double) {
        buttonApply.isEnabled = true
        buttonCe.isEnabled = true
        val radioButton = RadioButton(requireContext())
        radioButton.text = result.toString()
        radioGroupResults.addView(radioButton)
    }

    fun evaluateExpression(expression: String): Double {
        val expressionBuilder = ExpressionBuilder(expression)
        try {
            val result = expressionBuilder.build().evaluate()
            return result
        } catch (e: Exception) {
            // Fehlerbehandlung
            return 0.0
        }
    }

    interface CalculatorDialogListener {
        fun onCalculatorResult(result: Double)
    }
}
