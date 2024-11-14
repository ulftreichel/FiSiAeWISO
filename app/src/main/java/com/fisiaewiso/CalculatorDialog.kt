package com.fisiaewiso

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.DialogFragment

class CalculatorDialog : DialogFragment() {
    //Logik für das Dialog zur Verfügung stellen
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            val inflater = requireActivity().layoutInflater
            val view = inflater.inflate(R.layout.calculator_dialog, null)
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
            val buttonCe = view.findViewById<Button>(R.id.button_ce)
            val buttonC = view.findViewById<Button>(R.id.button_c)
            val buttonDel = view.findViewById<Button>(R.id.button_del)
            val buttonEquals = view.findViewById<Button>(R.id.button_equals)
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
            buttonAdd.setOnClickListener { display.append("+") }
            buttonSubtract.setOnClickListener { display.append("-") }
            buttonMultiply.setOnClickListener { display.append("*") }
            buttonDivide.setOnClickListener { display.append("/") }
            buttonDecimal.setOnClickListener { display.append(".") }
            buttonPercent.setOnClickListener { display.append("%") }
            buttonCe.setOnClickListener { display.text.clear() }
            buttonC.setOnClickListener { display.text.clear() }
            buttonEquals.setOnClickListener {
                try {
                    val result = evaluateExpression(display.text.toString())
                    (activity as? CalculatorDialogListener)?.onCalculatorResult(result)
                    dismiss() // Dialog schließen
                } catch (e: Exception) {
                    // Fehlerbehandlung
                }
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

            builder.setView(view)
                .setTitle("Taschenrechner")
                .setNegativeButton("Abbrechen") { _, _ -> dismiss() }
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    private fun evaluateExpression(expression: String): Double {
        return expression.toDoubleOrNull() ?: 0.0
    }

    interface CalculatorDialogListener {
        fun onCalculatorResult(result: Double)
    }
}
