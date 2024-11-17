package com.fisiaewiso

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.GridLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import net.objecthunter.exp4j.ExpressionBuilder

class CalculatorActivity : AppCompatActivity() {
    private val intermediateResults = mutableListOf<Double>()
    private lateinit var buttonApply: Button
    private lateinit var buttonCe: Button
    private lateinit var gridLayoutCalc: GridLayout
    private val checkBoxes = mutableListOf<CheckBox>()
    private val resultCheckBoxes = mutableListOf<CheckBox>()
    private var _maxSelections = 1
    private val maxSelections: Int
        get() = _maxSelections
    private var inputCount = 0
    var question: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.calculator_dialog) // Verwende das gleiche Layout
        question = intent.getStringExtra("question") ?: ""
        inputCount = intent.getIntExtra("inputCount", 0)
        val scrollViewQuestion = findViewById<ScrollView>(R.id.scrollViewQuestionCalc)
        val riddleQuestion = findViewById<TextView>(R.id.tVQuestionCalc)
        val display = findViewById<EditText>(R.id.calculator_display)
        val button0 = findViewById<Button>(R.id.button_0)
        val button1 = findViewById<Button>(R.id.button_1)
        val button2 = findViewById<Button>(R.id.button_2)
        val button3 = findViewById<Button>(R.id.button_3)
        val button4 = findViewById<Button>(R.id.button_4)
        val button5 = findViewById<Button>(R.id.button_5)
        val button6 = findViewById<Button>(R.id.button_6)
        val button7 = findViewById<Button>(R.id.button_7)
        val button8 = findViewById<Button>(R.id.button_8)
        val button9 = findViewById<Button>(R.id.button_9)
        val buttonAdd = findViewById<Button>(R.id.button_add)
        val buttonSubtract = findViewById<Button>(R.id.button_subtract)
        val buttonMultiply = findViewById<Button>(R.id.button_multiply)
        val buttonDivide = findViewById<Button>(R.id.button_divide)
        val buttonDecimal = findViewById<Button>(R.id.button_decimal)
        val buttonPercent = findViewById<Button>(R.id.button_percent)
        buttonCe = findViewById(R.id.button_ce)
        val buttonC = findViewById<Button>(R.id.button_c)
        val buttonDel = findViewById<Button>(R.id.button_del)
        val buttonEquals = findViewById<Button>(R.id.button_equals)
        buttonApply = findViewById(R.id.button_apply)
        val buttonSave = findViewById<Button>(R.id.button_save)
        gridLayoutCalc = findViewById(R.id.gridLayoutCal)
        setMaxSelections(2)
        // Frage anzeigen
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

        buttonCe.setOnClickListener { }
        buttonC.setOnClickListener { display.text.clear() }
        buttonEquals.setOnClickListener {
            val result = evaluateExpression(display.text.toString())
            val resultString = if (result.toInt().toDouble() == result) {
                result.toInt().toString() // Ergebnis als Integer anzeigen, wenn es eine ganze Zahl ist
            } else {
                result.toString() // Ergebnis als Double anzeigen, wenn es eine Dezimalzahl ist
            }
            display.text = Editable.Factory.getInstance().newEditable(resultString)
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
        } else {
            buttonApply.isEnabled = true
            buttonCe.isEnabled = true
        }
        // Event-Listener für den "Übernehmen"-Button
        buttonApply.setOnClickListener {
            val selectedCheckboxValues = resultCheckBoxes.filter { it.isChecked }.map { it.text.toString() }

            val resultIntent = Intent()
            when (inputCount) {
                1 -> resultIntent.putExtra("result", selectedCheckboxValues.firstOrNull() ?: "")
                2 -> {
                    val (input1, input2) = selectedCheckboxValues.takeIf { it.size <= 2 }
                        ?: listOf("", "")
                    resultIntent.putExtra("result1", input1)
                    resultIntent.putExtra("result2", input2)
                }
                else -> { /* Keine Aktion erforderlich */ }
            }
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }
        // Event-Listener für den "Speichern"-Button
        buttonSave.setOnClickListener {
            val result = evaluateExpression(display.text.toString())
            // Dialog anzeigen, um zu fragen, ob das Ergebnis gespeichert werden soll
            AlertDialog.Builder(this)
                .setTitle("Ergebnis speichern?")
                .setMessage("Möchten Sie das Ergebnis speichern?")
                .setPositiveButton("Ja") { _, _ ->
                    // Ergebnis formatieren und in Double umwandeln
                    val resultString = if (result.toInt().toDouble() == result) {
                        result.toInt().toString() // Ergebnis als Integer anzeigen, wenn es eine ganze Zahl ist
                    } else {
                        result.toString() // Ergebnis als Double anzeigen, wenn es eine Dezimalzahl ist
                    }
                    val resultDouble = resultString.toDoubleOrNull() // In Double umwandeln

                    if (resultDouble != null) {
                        intermediateResults.add(resultDouble) // Ergebnis speichern
                        display.text.clear()
                    } else {
                        // Fehlerbehandlung, falls die Konvertierung fehlschlägt
                    }
                    val checkBox = CheckBox(this)
                    checkBox.text = resultString
                    resultCheckBoxes.add(checkBox)
                    gridLayoutCalc.addView(checkBox)

                }
                .setNegativeButton("Nein", null)
                .show()
        }
    }

    fun evaluateExpression(expression: String): Double {
        if (expression.isEmpty()) {
            return 0.0 // Oder eine andere geeignete Aktion, z. B. eine Fehlermeldung anzeigen
        }
        val expressionBuilder = ExpressionBuilder(expression)
        try {
            val result = expressionBuilder.build().evaluate()
            return result
        } catch (e: Exception) {
            // Fehlerbehandlung
            return 0.0
        }
    }

    fun addCheckboxes(numCheckboxes: Int) {
        for (i in 0 until numCheckboxes) {
            val checkBox = CheckBox(this)
            checkBoxes.add(checkBox) // Checkbox zur Liste hinzufügen
            gridLayoutCalc.addView(checkBox) // Checkbox zum GridLayout hinzufügen
        }
        updateCheckBoxes() // Checkboxes initialisieren
    }

    // Methode zum Setzen der maximalen Anzahl an Auswahlen
    fun setMaxSelections(max: Int) {
        _maxSelections = max // _maxSelections ändern, nicht maxSelections
        updateCheckBoxes()
    }

    // Methode zum Aktualisieren der Checkboxes
    fun updateCheckBoxes() {
        val selectedCount = resultCheckBoxes.count { it.isChecked }
        resultCheckBoxes.forEach { checkBox ->
            checkBox.isEnabled = !checkBox.isChecked || selectedCount < inputCount // inputCount als Limit verwenden
        }
    }
}
