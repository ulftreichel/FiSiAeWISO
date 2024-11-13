package com.fisiaewiso

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.InputType
import android.util.Log
import android.view.DragEvent
import android.view.Gravity
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.children
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Collections
import kotlin.properties.Delegates
import kotlin.text.toDoubleOrNull

class RiddleActivity : AppCompatActivity() {

    private lateinit var currentRiddle: Riddle
    private lateinit var tVRiddle_Initialize: TextView
    private lateinit var tVRiddle_Initialize2: TextView
    private lateinit var riddleTextView: TextView
    private lateinit var numberInput: EditText
    private lateinit var numberInput2: EditText
    private lateinit var dateInput: EditText
    private lateinit var nextButton: Button
    private lateinit var startButton: Button
    private lateinit var closeButton: Button
    private lateinit var riddleImageButton: ImageButton
    private var currentRiddleIndex = 0
    private lateinit var riddles: List<Riddle>
    private val selectedAnswersOrder = mutableListOf<String>()
    private lateinit var radioGroupAnswers: RadioGroup
    private val checkBoxes = mutableListOf<CheckBox>()
    private lateinit var linearLayoutAnswers: LinearLayout
    private lateinit var linearLayoutSpinners: LinearLayout
    private lateinit var linearLayoutRecyclerView: ConstraintLayout
    private lateinit var optionsRecyclerView: androidx.recyclerview.widget.RecyclerView
    private lateinit var targetLinearLayout: LinearLayout
    private lateinit var linearTextView: LinearLayout
    private lateinit var target1TextView: TextView
    private lateinit var target2TextView: TextView
    private lateinit var target3TextView: TextView
    private lateinit var target4TextView: TextView
    private lateinit var target5TextView: TextView
    private val radioButtons = mutableListOf<RadioButton>()
    private val answerTextViews = mutableListOf<TextView>()
    private lateinit var unit1TextView: TextView
    private lateinit var unit2TextView: TextView
    private lateinit var unitDateTextView: TextView
    private lateinit var currentIntro: String
    private var riddlesLoaded = false
    private var totalPoints = 0.0
    private lateinit var viewModel: RiddleViewModel
    private var riddleMainNumber: Int by Delegates.notNull()
    private var userMappings = mutableMapOf<String, String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_riddle)
        tVRiddle_Initialize = findViewById(R.id.riddle_initialize)
        tVRiddle_Initialize2 = findViewById(R.id.riddle_initialize2)
        riddleTextView = findViewById(R.id.textView3)
        unit1TextView = findViewById(R.id.unit1TextView)
        unit2TextView = findViewById(R.id.unit2TextView)
        unitDateTextView = findViewById(R.id.unitDateTextView)
        target1TextView = findViewById(R.id.target1TextView)
        target2TextView = findViewById(R.id.target2TextView)
        target3TextView = findViewById(R.id.target3TextView)
        target4TextView = findViewById(R.id.target4TextView)
        target5TextView = findViewById(R.id.target5TextView)
        radioGroupAnswers = findViewById(R.id.rGRiddleAnswer)
        linearLayoutAnswers = findViewById(R.id.linearLayoutAnswers)
        linearLayoutSpinners = findViewById(R.id.linearLayoutSpinners)
        linearLayoutRecyclerView = findViewById(R.id.linearLayoutRecyclerView)
        optionsRecyclerView = findViewById(R.id.optionsRecyclerView)
        targetLinearLayout = findViewById(R.id.targetLinearLayout)
        linearTextView = findViewById(R.id.linearTextView)
        val repository = ResultRepository(AppDatabase.getDatabase(this).resultDao(), AppDatabase.getDatabase(this).riddleDao())
        viewModel = ViewModelProvider(this, RiddleViewModelFactory(repository)).get(RiddleViewModel::class.java)
        numberInput = findViewById(R.id.riddleResultNumber1)
        numberInput2 = findViewById(R.id.riddleResultNumber2)
        dateInput = findViewById(R.id.riddleResultDate)
        nextButton = findViewById(R.id.bRiddleNext)
        startButton = findViewById(R.id.startButton)
        closeButton = findViewById(R.id.bClose)
        riddleImageButton = findViewById(R.id.riddleImageButton)
        currentRiddle = Riddle(0,0, 0,"Datenbank wird beim nächsten Neustart zur Verfügung stehen", listOf(), listOf(), listOf(),false, false, false, false, false, false, false,false, listOf(), listOf(), listOf(), mapOf())
        tVRiddle_Initialize.text = currentRiddle.question
        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val lastIntro = sharedPreferences.getString("lastIntro", null)
        val introTexts = listOf(
            getString(R.string.Riddle1),
            getString(R.string.Riddle2),
            getString(R.string.Riddle3),
            getString(R.string.Riddle4),
            getString(R.string.Riddle5)
        )
        if (lastIntro == null) {
            // Beim ersten Start: Wähle einen zufälligen Intro-Text
            currentIntro = introTexts.random()
        } else {
            // Bei nachfolgenden Starts: Wähle einen anderen Intro-Text als den letzten
            do {
                currentIntro = introTexts.random()
            } while (currentIntro == lastIntro)
        }
        val editor = sharedPreferences.edit()
        editor.putString("lastIntro", currentIntro)
        editor.apply()
        tVRiddle_Initialize2.text = currentIntro
        for (i in 1..30) {
            val textViewId = resources.getIdentifier("tVcorrectRiddle$i", "id", packageName)
            val textView = findViewById<TextView>(textViewId)
            answerTextViews.add(textView)
        }
        startButton.setOnClickListener {
            loadRiddlesByIntro(currentIntro)
            currentIntro = ""
            riddlesLoaded = true
            tVRiddle_Initialize.visibility = View.GONE
            tVRiddle_Initialize2.visibility = View.GONE
            startButton.visibility = View.GONE
            nextButton.visibility = View.VISIBLE
            nextButton.isEnabled = false
            Handler(Looper.getMainLooper()).postDelayed({
                nextButton.isEnabled = true
            },2000) // 2 Sekunden Verzögerung
        }
        nextButton.setOnClickListener {
            if (currentRiddle.requiresNumberInput || currentRiddle.requiresTwoNumberInputs || currentRiddle.requiresDateInput || currentRiddle.requiresTimeInput) {
                // InputMethodManager abrufen
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                // Tastatur ausblenden
                imm.hideSoftInputFromWindow(numberInput.windowToken, 0)
                imm.hideSoftInputFromWindow(numberInput2.windowToken, 0)
                imm.hideSoftInputFromWindow(dateInput.windowToken, 0)
            }
            if (riddlesLoaded) {
                evaluateAnswer()
            }
        }
        closeButton.setOnClickListener {
            finish()
        }
    }

    private suspend fun initializeDatabase() {
        withContext(Dispatchers.IO) {
            AppDatabase.getDatabase(this@RiddleActivity)
        }
    }

    private fun loadRiddlesByIntro(intro: String) {
        riddleMainNumber = when (intro) {
            getString(R.string.Riddle1) -> 1
            getString(R.string.Riddle2) -> 2
            getString(R.string.Riddle3) -> 3
            getString(R.string.Riddle4) -> 4
            getString(R.string.Riddle5) -> 5
            else -> return // Oder eine andere Fehlerbehandlung
        }
        lifecycleScope.launch {
            try {
                initializeDatabase()
                // Zugriff auf die Datenbank
            } catch (e: Exception) {
                Log.e("Database", "Error initializing database", e)
                // Initialisiere Datenbank erneut
                initializeDatabase()
            }
            //initializeDatabase()
            loadRiddles(riddleMainNumber) {
                // currentRiddle im ViewModel aktualisieren, nachdem die Rätsel geladen wurden
                viewModel.updateCurrentRiddle(currentRiddle)
            }
        }
        // currentRiddle im ViewModel aktualisieren
        viewModel.updateCurrentRiddle(currentRiddle)
    }

    private fun loadRiddles(riddleMainNumber: Int, onRiddlesLoaded: () -> Unit) {
        lifecycleScope.launch {
            riddles = AppDatabase.getDatabase(this@RiddleActivity).riddleDao().getRiddlesByNumber(riddleMainNumber).first() // Initialisiere riddles hier
            riddles = riddles.map { riddle ->
                riddle.copy(
                    question = riddle.question.replace(";", ","),
                    answers = riddle.answers.map { it.replace(";", ",") },
                    correctAnswers = riddle.correctAnswers.map { it.replace(";", ",") }
                )
            }
            if (riddles.isNotEmpty()) {
                currentRiddleIndex = 0
                currentRiddle = riddles[currentRiddleIndex]
                withContext(Dispatchers.Main) {
                    displayRiddle()
                    onRiddlesLoaded()
                }
            }
        }
    }

    private fun proceedToNextRiddle() {
        //showNextRiddle() // Nächste Frage laden
        nextButton.isEnabled = false
        Handler(Looper.getMainLooper()).postDelayed({
            nextButton.isEnabled = true
        },2000) // 2 Sekunden Verzögerung
    }

    private fun showNextRiddle() {
        if (currentRiddleIndex < riddles.size) { // Überprüfe, ob noch Rätsel vorhanden sind
            currentRiddleIndex++ // Index für das nächste Rätsel aktualisieren
            if (currentRiddleIndex < riddles.size) { // Überprüfe erneut, ob noch Rätsel vorhanden sind
                currentRiddle = riddles[currentRiddleIndex]
                selectedAnswersOrder.clear()
                displayRiddle()
            } else {
                // Alle Rätsel erfolgreich gelöst
                showTotalPointsAndSaveResults()
            }
        } else {
            // Alle Rätsel erfolgreich gelöst
            showTotalPointsAndSaveResults()
        }
    }

    private fun displayRiddle() {
        radioGroupAnswers.removeAllViews()
        radioButtons.clear()
        linearLayoutAnswers.removeAllViews() // Remove previous CheckBoxes
        checkBoxes.clear() // Clear the list for the new riddle
        userMappings.clear()
        // Bild laden, falls vorhanden
        when (currentRiddle.riddleNumber) {
            21, 54, 65, 81, 112, 122 -> { // Nur für Fragen mit Bildern
                val imageResource = when (currentRiddle.riddleNumber) {
                    21 -> R.drawable.mehrliniensystem
                    54 -> R.drawable.riddle2unterschriften
                    65 -> R.drawable.riddle65holiday
                    81 -> R.drawable.matrixsystem
                    112 -> R.drawable.gleichgewichtspreis
                    122 -> R.drawable.mehrliniensystem
                    else -> 0 // Sollte nicht erreicht werden, aber zur Sicherheit
                }
                if (imageResource != 0) {
                    riddleImageButton.setBackgroundResource(imageResource)
                    riddleImageButton.visibility = View.VISIBLE
                    riddleImageButton.setOnClickListener {
                        showImageDialog(imageResource)
                    }
                }
            } else -> { // Für alle anderen Rätsel
            riddleImageButton.visibility = View.GONE
            }
        }
        val shouldShuffle = when (currentRiddle.riddleNumber) {
            2 -> true // Fragen, die geshuffelt werden sollen
            11 -> false // Frage, die nicht geshuffelt werden soll
            else -> true // Standardmäßig shufflen
        }
        val answersshuffle = if (shouldShuffle) {
            currentRiddle.answers.shuffled()
        } else {
            currentRiddle.answers // Originalreihenfolge beibehalten
        }
        if (shouldShuffle) {
            Collections.shuffle(answersshuffle) // answersshuffle direkt mischen
        }
        if (currentRiddle.requiresOrderedAnswers) {
            linearLayoutSpinners.removeAllViews()
            linearLayoutSpinners.visibility = View.VISIBLE
            linearLayoutRecyclerView.visibility = View.GONE
            val numSpinners = currentRiddle.correctAnswers.size
            for (i in 0 until numSpinners) {
                val spinner = Spinner(this)
                // Separate Liste von Antworten für jeden Spinner erstellen
                val spinnerAnswers = answersshuffle
                val adapter = MultilineSpinnerAdapter(this, R.layout.spinner_item_multiline, spinnerAnswers)
                spinner.adapter = adapter
                linearLayoutSpinners.addView(spinner)
            }
            checkBoxes.forEach { it.visibility = View.GONE }
            radioGroupAnswers.visibility = View.GONE
            dateInput.visibility = View.GONE
            unit1TextView.visibility = View.GONE
            unit2TextView.visibility = View.GONE
            unitDateTextView.visibility = View.GONE
        } else if (currentRiddle.requiresDragAndDrop) {
            unit1TextView.visibility = View.GONE
            unit2TextView.visibility = View.GONE
            unitDateTextView.visibility = View.GONE
            linearLayoutRecyclerView.visibility = View.VISIBLE

            // Ziele anzeigen
            val target1: FrameLayout = findViewById(R.id.target1)
            val target2: FrameLayout = findViewById(R.id.target2)
            val target3: FrameLayout = findViewById(R.id.target3)
            val target4: FrameLayout = findViewById(R.id.target4)
            val target5: FrameLayout = findViewById(R.id.target5)

            target1.removeAllViews()
            target2.removeAllViews()
            target3.removeAllViews()
            target4.removeAllViews()
            target5.removeAllViews()

            target1.addView(TextView(this).apply { text = "" })
            target2.addView(TextView(this).apply { text = "" })
            target3.addView(TextView(this).apply { text = "" })
            target4.addView(TextView(this).apply { text = "" })
            target5.addView(TextView(this).apply { text = "" })

            val optionsAdapter = if (currentRiddle.optionsWithImage.isNotEmpty()) {
                OptionsAdapter(currentRiddle.optionsWithImage.toMutableList(), userMappings, this) // Übergibt userMappings
            } else {
                OptionsAdapter(currentRiddle.options.toMutableList(), userMappings, this)
            }
            optionsRecyclerView.adapter = optionsAdapter
            optionsRecyclerView.adapter = optionsAdapter
            if(currentRiddle.optionsWithImage.isNotEmpty()){
                optionsRecyclerView.layoutManager = GridLayoutManager(this, 3) // 2 Spalten
            } else {
                optionsRecyclerView.layoutManager = LinearLayoutManager(this) // LayoutManager hinzufügen
            }

            target1TextView.text = currentRiddle.targets[0]
            if(currentRiddle.targets.size > 1){
                target2TextView.text = currentRiddle.targets[1]
            }
            if (currentRiddle.targets.size > 2) {
                target3TextView.text = currentRiddle.targets[2]
            }
            if (currentRiddle.targets.size > 3) {
                target4TextView.text = currentRiddle.targets[3]
            }
            if (currentRiddle.targets.size > 4) {
                target5TextView.text = currentRiddle.targets[4]
                target1.layoutParams.height = 200
                target2.layoutParams.height = 200
                target3.layoutParams.height = 200
                target4.layoutParams.height = 200
                target5.layoutParams.height = 200
            }
            // Alle Target-Layouts zunächst ausblenden
            for (i in 0 until targetLinearLayout.childCount) {
                targetLinearLayout.getChildAt(i).visibility = View.GONE
            }
            for (i in 0 until linearTextView.childCount) {
                linearTextView.getChildAt(i).visibility = View.GONE
            }
            for (i in 0 until currentRiddle.targets.size) {
                val targetView = targetLinearLayout.getChildAt(i) as FrameLayout
                val targetTextView = linearTextView.getChildAt(i) as TextView

                targetView.visibility = View.VISIBLE
                targetTextView.visibility = View.VISIBLE

                // Text für das Target setzen
                targetTextView.text = currentRiddle.targets[i]

                // Drag-Listener setzen
                targetView.setOnDragListener { v: View, event: DragEvent ->
                    when (event.action) {
                        DragEvent.ACTION_DROP -> {
                            val clipData = event.clipData
                            val option = clipData.getItemAt(0).text.toString()
                            Log.d("RiddleActivity", "Option dropped: $option")

                            val targetView = v as FrameLayout
                            var optionsLayout = targetView.getTag() as? LinearLayout

                            if (optionsLayout == null) {
                                optionsLayout = LinearLayout(targetView.context).apply { // Hier wird context verwendet
                                    orientation = LinearLayout.VERTICAL
                                    layoutParams = FrameLayout.LayoutParams(
                                        FrameLayout.LayoutParams.WRAP_CONTENT,
                                        FrameLayout.LayoutParams.WRAP_CONTENT
                                    ).apply {
                                        gravity = Gravity.CENTER_HORIZONTAL or Gravity.BOTTOM
                                    }
                                }
                                targetView.setTag(optionsLayout)
                                targetView.addView(optionsLayout)
                            }

                            val optionView = if (clipData.itemCount > 1) {
                                ImageView(targetView.context).apply { // Hier wird context verwendet
                                    val imageResId = clipData.getItemAt(1).text.toString().toInt()
                                    setImageResource(imageResId)
                                    Log.d("RiddleActivity", "Image resource ID: $imageResId")
                                }
                            } else {
                                TextView(targetView.context).apply { // Hier wird context verwendet
                                    text = option
                                }
                            }
                            optionView.setOnLongClickListener {
                                // Entferne die Option aus dem Target
                                optionsLayout.removeView(optionView)
                                if(currentRiddle.optionsWithImage.isNotEmpty()){
                                    val imageId = clipData.getItemAt(1).text.toString()
                                    val optionnew = OptionWithImage(option, imageId.toInt())
                                    Log.d("RiddleActivity", "Optionnew dropped: $optionnew")
                                    // Füge Option dem RecyclerView hinzu
                                    val optionsAdapter = optionsRecyclerView.adapter as OptionsAdapter<Any>
                                    optionsAdapter.addOption(optionnew) // Methode addOption wird hier aufgerufen
                                    userMappings.remove(option) // Entferne das Mapping
                                } else {
                                    // Füge Option dem RecyclerView hinzu
                                    val optionsAdapter = optionsRecyclerView.adapter as OptionsAdapter<Any>
                                    optionsAdapter.addOption(option) // Methode addOption wird hier aufgerufen
                                    userMappings.remove(option) // Entferne das Mapping
                                }
                                true
                            }
                            optionsLayout.addView(optionView)

                            userMappings[option] = targetView.id.toString()

                            val optionsAdapter = optionsRecyclerView.adapter as OptionsAdapter<*>
                            optionsAdapter.removeOption(option)
                            optionsAdapter.notifyDataSetChanged()

                            Log.d("RiddleActivity", "Option added to target: ${targetView.id}")
                            true

                        }

                        else -> true
                    }
                }
            }
        } else {
            if (currentRiddle.requiresNumberInput || currentRiddle.requiresTwoNumberInputs || currentRiddle.requiresDateInput || currentRiddle.requiresTimeInput) {
                numberInput.text.clear()
                numberInput2.text.clear()
                dateInput.text.clear()
                // Eingabefelder anzeigen
                numberInput.visibility = View.VISIBLE
                unit1TextView.visibility = View.VISIBLE
                unit1TextView.text = currentRiddle.unit[0] // Erste Einheit anzeigen
                dateInput.visibility = View.GONE
                if (currentRiddle.requiresTwoNumberInputs) {
                    numberInput2.visibility = View.VISIBLE
                    unit2TextView.visibility = View.VISIBLE
                    unit2TextView.text = currentRiddle.unit[1] // Zweite Einheit anzeigen
                }
                if (currentRiddle.requiresDateInput) {
                    unit1TextView.visibility = View.GONE
                    unit2TextView.visibility = View.GONE
                    unitDateTextView.visibility = View.VISIBLE
                    unitDateTextView.text = currentRiddle.unit[0]
                    numberInput.visibility = View.GONE
                    dateInput.visibility = View.VISIBLE
                    dateInput.inputType = InputType.TYPE_DATETIME_VARIATION_DATE
                    dateInput.setHint("Datum eingeben")
                }
                if (currentRiddle.requiresTimeInput) {
                    unit1TextView.visibility = View.GONE
                    unit2TextView.visibility = View.GONE
                    numberInput.visibility = View.GONE
                    dateInput.visibility = View.VISIBLE
                    dateInput.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
                    dateInput.setHint("Zeit eingeben 07.00")
                    unitDateTextView.visibility = View.VISIBLE
                    unitDateTextView.text = currentRiddle.unit[0]
                }
                // Einheiten-TextViews ausblenden
                if (currentRiddle.unit.isEmpty()) {
                    unit1TextView.visibility = View.GONE
                    unit2TextView.visibility = View.GONE
                    unitDateTextView.visibility = View.GONE
                }
                // CheckBoxes und RadioButtons ausblenden
                checkBoxes.forEach { it.visibility = View.GONE } // Hide CheckBoxes
                radioGroupAnswers.visibility = View.GONE // Hide RadioGroup
                linearLayoutSpinners.visibility = View.GONE // Hide LinearLayout for Spinners
                linearLayoutRecyclerView.visibility = View.GONE // Hide LinearLayout for RecyclerView
            } else {
                // Eingabefelder ausblenden
                numberInput.visibility = View.GONE // Hide numberInput
                numberInput2.visibility = View.GONE // Hide numberInput2
                dateInput.visibility = View.GONE // Hide dateInput
                unit1TextView.visibility = View.GONE
                unit2TextView.visibility = View.GONE
                unitDateTextView.visibility = View.GONE
                linearLayoutRecyclerView.visibility = View.GONE // Hide LinearLayout for RecyclerView
                // Antworten zufällig sortieren
                val shuffledAnswers = answersshuffle.toMutableList()
                Collections.shuffle(shuffledAnswers)
                // CheckBoxes und RadioButtons anzeigen
                checkBoxes.forEach { it.visibility = View.VISIBLE } // Show CheckBoxes
                radioGroupAnswers.visibility = View.VISIBLE // Show RadioGroup
                linearLayoutSpinners.visibility = View.GONE // Hide LinearLayout for Spinners
                if (currentRiddle.hasMultipleCorrectAnswers) {
                    radioGroupAnswers.visibility = View.GONE // Hide RadioGroup
                    linearLayoutAnswers.visibility = View.VISIBLE // Show LinearLayout for CheckBoxes
                    linearLayoutSpinners.visibility = View.GONE // Hide LinearLayout for Spinners
                    dateInput.visibility = View.GONE // Hide dateInput
                    linearLayoutRecyclerView.visibility = View.GONE // Hide LinearLayout for RecyclerView
                    // Nur CheckBoxes hinzufügen, wenn mehrere Antworten korrekt sein können
                    for (answer in answersshuffle) {
                        val checkBox = CheckBox(this)
                        checkBox.text = answer
                        linearLayoutAnswers.addView(checkBox)
                        checkBoxes.add(checkBox) // Add to the list
                    }
                } else {
                    radioGroupAnswers.visibility = View.VISIBLE // Show RadioGroup
                    linearLayoutAnswers.visibility = View.GONE // Hide LinearLayout for CheckBoxes
                    linearLayoutSpinners.visibility = View.GONE // Hide LinearLayout for Spinners
                    dateInput.visibility = View.GONE // Hide dateInput
                    unit1TextView.visibility = View.GONE
                    unit2TextView.visibility = View.GONE
                    unitDateTextView.visibility = View.GONE
                    linearLayoutRecyclerView.visibility = View.GONE // Hide LinearLayout for RecyclerView
                    // Nur RadioButtons hinzufügen, wenn nur eine Antwort korrekt sein kann
                    for (answer in answersshuffle) {
                        val radioButton = RadioButton(this)
                        radioButton.text = answer
                        radioGroupAnswers.addView(radioButton)
                        radioButtons.add(radioButton) // Add to the list
                    }
                }
            }
        }
        riddleTextView.text = currentRiddle.question
    }

    fun isOptionMapped(option: String): Boolean {
        return userMappings.containsKey(option)
    }

    private fun showImageDialog(imageResource : Int){
        val dialog = RiddleImageViewDialog(this, imageResource)
        dialog.show()
    }

    private fun evaluateAnswer(): Boolean {
        val currentRiddle = riddles[currentRiddleIndex]
        val correctAnswers = currentRiddle.correctAnswers
        val selectedAnswers = when {
            currentRiddle.requiresDateInput -> {
                val dateInput = dateInput.text.toString()
                listOf(dateInput)
            }
            currentRiddle.requiresTimeInput -> {
                val timeInput = dateInput.text.toString()
                listOf(timeInput)
            }
            currentRiddle.requiresNumberInput || currentRiddle.requiresTwoNumberInputs -> {
                val number1 = numberInput.text.toString().toDoubleOrNull() ?: ""
                val number2 = if (currentRiddle.requiresTwoNumberInputs) {
                    numberInput2.text.toString().toDoubleOrNull() ?: ""
                } else {
                    0.0
                }
                if (currentRiddle.requiresNumberInput && !currentRiddle.requiresTwoNumberInputs) {
                    listOf(number1.toString())
                } else {
                    listOf(number1.toString(), number2.toString())
                }
            }
            currentRiddle.requiresOrderedAnswers -> {
                val selectedSpinnerAnswers = linearLayoutSpinners.children
                    .filterIsInstance<Spinner>()
                    .map { it.selectedItem as String } // Direkt als String
                    .toList()
                selectedSpinnerAnswers
            }
            currentRiddle.hasMultipleCorrectAnswers -> {
                getSelectedAnswerCheckBoxes()
            }
            else -> {
                listOf(getSelectedAnswerRadioButtons())
            }
        }
        // isCorrect: true, wenn alle richtigen Antworten ausgewählt wurden und keine falschen
        val isCorrect = if (currentRiddle.hasMultipleCorrectAnswers) {
            correctAnswers.all { selectedAnswers.contains(it) } // Prüfe, ob alle richtigen Antworten in den ausgewählten Antworten enthalten sind
        } else if (currentRiddle.requiresOrderedAnswers) {
            // Ausgewählte Antworten mit den richtigen Antworten vergleichen
            selectedAnswers.size == correctAnswers.size && selectedAnswers.zip(correctAnswers).all { (a, b) -> a == b }
        } else {
            selectedAnswers.size == 1 && correctAnswers.contains(selectedAnswers[0])
        }
        var partiallyCorrect = false
        // Markiere die Antwort-TextViews
        if (currentRiddle.requiresOrderedAnswers) {
            val numCorrectAnswers = selectedAnswers.withIndex().count { (index, answer) ->
                correctAnswers.getOrNull(index) == answer
            }
            val numPossibleAnswers = correctAnswers.size
            val isCorrect = selectedAnswers == correctAnswers
            partiallyCorrect = numCorrectAnswers > 0 && numCorrectAnswers < correctAnswers.size
            if (currentRiddleIndex in 0..29) {
                val textView = answerTextViews[currentRiddleIndex]
                if (isCorrect) {
                    textView.setBackgroundColor(Color.GREEN) // Richtig: Grün
                    totalPoints += 3.33333
                    proceedToNextRiddle()
                } else if (partiallyCorrect){
                    textView.setBackgroundColor(Color.YELLOW) // Teilweise richtig: Gelb
                    totalPoints += 3.33333 * (numCorrectAnswers.toDouble() / numPossibleAnswers)
                    wrongAnswerDialog(correctAnswers, currentRiddle.unit)
                } else {
                    textView.setBackgroundColor(Color.RED) // Falsch: Rot
                    wrongAnswerDialog(correctAnswers, currentRiddle.unit)
                }
            }
        }  else if (currentRiddle.hasMultipleCorrectAnswers) {
            val numCorrectAnswers = selectedAnswers.count { correctAnswers.contains(it) }
            val numPossibleAnswers = correctAnswers.size
            // isCorrect: Alle korrekten Antworten müssen in selectedAnswers enthalten sein
            // und alle selectedAnswers müssen in correctAnswers enthalten sein
            val isCorrect = correctAnswers.all { selectedAnswers.contains(it) } &&
                    selectedAnswers.all { correctAnswers.contains(it) }
            // partiallyCorrect: Mindestens eine korrekte Antwort muss in selectedAnswers enthalten sein
            // und nicht alle selectedAnswers sind korrekt
            partiallyCorrect = numCorrectAnswers > 0 && !isCorrect
            // TextView für das aktuelle Rätsel markieren
            if (currentRiddleIndex in 0..29) {
                val textView = answerTextViews[currentRiddleIndex]
                if (isCorrect) {
                    textView.setBackgroundColor(Color.GREEN) // Richtig: Grün
                    totalPoints += 3.33333
                    proceedToNextRiddle()
                } else if (partiallyCorrect){
                    textView.setBackgroundColor(Color.YELLOW) // Teilweise richtig: Gelb
                    totalPoints += 3.33333 * (numCorrectAnswers.toDouble() / numPossibleAnswers)
                    wrongAnswerDialog(correctAnswers, currentRiddle.unit)
                } else {
                    textView.setBackgroundColor(Color.RED) // Falsch: Rot
                    wrongAnswerDialog(correctAnswers, currentRiddle.unit)
                }
            }
        }  else if (currentRiddle.hasdifferentanswers) {
            //Fragen mit mehreren richtigen Antworten
            val selectedAnswer = radioGroupAnswers.findViewById<RadioButton>(radioGroupAnswers.checkedRadioButtonId)?.text.toString() ?: ""
            // Überprüfen, ob die ausgewählte Antwort korrekt ist
            val isCorrect = currentRiddle.correctAnswers.contains(selectedAnswer)
            // Punkte vergeben, wenn die Antwort korrekt ist
            if (currentRiddleIndex in 0..29) {
                val textView = answerTextViews[currentRiddleIndex]
                if (isCorrect) {
                    textView.setBackgroundColor(Color.GREEN) // Richtig: Grün
                    totalPoints += 3.33333
                    proceedToNextRiddle()
                } else {
                    textView.setBackgroundColor(Color.RED) // Falsch: Rot
                    wrongAnswerDialog(correctAnswers, currentRiddle.unit)
                }
            }
        } else if (currentRiddle.requiresDragAndDrop) {
            var correctAnswers = 0
            val totalAnswers = currentRiddle.correctMappings.size

            for ((option, targetIdString) in userMappings) {
                val correctTargetIdString = currentRiddle.correctMappings[option]

                // targetIdString in Int umwandeln
                val targetId = targetIdString.toInt()

                // Ressourcen-ID des TextView aus dem FrameLayout abrufen
                val targetTextViewId = when (targetId) {
                    R.id.target1 -> R.id.target1TextView
                    R.id.target2 -> R.id.target2TextView
                    R.id.target3 -> R.id.target3TextView
                    R.id.target4 -> R.id.target4TextView
                    else -> 0 // Fehlerfall behandeln
                }
                // Text des Target-TextViews abrufen
                val targetText = if (targetTextViewId != 0) {
                    findViewById<TextView>(targetTextViewId).text.toString()
                } else {
                    "" // Fehlerfall behandeln
                }

                if (targetText == correctTargetIdString) {
                    correctAnswers++
                } else {
                    Log.d("RiddleEvaluation", "Incorrect answer for option: $option")
                }
            }

            if (currentRiddleIndex in 0..29) {
                val textView = answerTextViews[currentRiddleIndex]

                if (correctAnswers == totalAnswers) {
                    textView.setBackgroundColor(Color.GREEN) // Richtig: Grün
                    totalPoints += 3.33333
                    proceedToNextRiddle()
                } else if (correctAnswers == 0) {
                    textView.setBackgroundColor(Color.RED) // Falsch: Rot
                    wrongAnswerDialog(correctMappings = currentRiddle.correctMappings)
                } else {
                    textView.setBackgroundColor(Color.YELLOW) // Teilweise richtig: Gelb
                    val pointsPerCorrectAnswer = 3.33333 / totalAnswers
                    totalPoints += pointsPerCorrectAnswer * correctAnswers
                    wrongAnswerDialog(correctMappings = currentRiddle.correctMappings)
                }
            }
        } else {
            // Fragen mit nur einer richtigen Antwort
            val isCorrect = correctAnswers == selectedAnswers
            // TextView für das aktuelle Rätsel markieren
            if (currentRiddleIndex in 0..29) {
                val textView = answerTextViews[currentRiddleIndex]
                if (isCorrect) {
                    textView.setBackgroundColor(Color.GREEN) // Richtig: Grün
                    totalPoints += 3.33333
                    proceedToNextRiddle()
                } else {
                    wrongAnswerDialog(correctAnswers, currentRiddle.unit)
                    textView.setBackgroundColor(Color.RED) // Falsch: Rot
                }
            }
        }
        return isCorrect
    }

    // Hilfsfunktion, um die ausgewählte Antwort zu erhalten
    private fun getSelectedAnswerRadioButtons(): String {
        val selectedRadioButtonId = radioGroupAnswers.checkedRadioButtonId
        if (selectedRadioButtonId != -1) {
            val selectedRadioButton = findViewById<RadioButton>(selectedRadioButtonId)
            return selectedRadioButton?.text?.toString() ?: "" // Sichere Navigation und Elvis-Operator
        } else {
            return ""
        }
    }

    // Hilfsfunktion, um die ausgewählten Antworten zu erhalten
    private fun getSelectedAnswerCheckBoxes(): List<String> {
        val selectedAnswers = mutableListOf<String>()
        for (checkBox in checkBoxes) {
            if (checkBox.isChecked) {
                selectedAnswers.add(checkBox.text.toString())
            }
        }
        return selectedAnswers
    }

    fun onAnswerDeselected(answer: String, position: Int) {
        // Logik zum Abwählen der Antwort
        selectedAnswersOrder.remove(answer) // Antwort aus der Liste entfernen
    }

    fun onAnswerSelected(answer: String, position: Int) {
        // Logik zum Abwählen der Antwort
        selectedAnswersOrder.add(answer) // Antwort zur Liste hinzufügen
    }

    // Zeige die Gesamtpunktzahl und Zensur an und speichere die Ergebnisse in der Datenbank
    private fun showTotalPointsAndSaveResults() {
        val grade = viewModel.calculateGrade(totalPoints.toInt())
        val dialog = ResultDialog(this, totalPoints, grade)
        dialog.show()
        viewModel.saveResultsToDatabase(riddleMainNumber, totalPoints.toInt(), grade)
    }

    private fun wrongAnswerDialog(correctAnswers: List<String> = emptyList(), currentRiddleUnit: List<String> = emptyList(), correctMappings: Map<String, String> = emptyMap()) {
        //proceedToNextRiddle()
        if (currentRiddle.requiresDragAndDrop) {
            // Richtige Antworten für Drag-and-Drop-Fragen anzeigen
            val dialog = WrongAnswerDialog(this, correctAnswers, currentRiddleUnit, correctMappings) // correctMappings übergeben
            dialog.show()
            Handler(Looper.getMainLooper()).postDelayed({
                showNextRiddle() // Nächste Frage laden
            },2000) // 2 Sekunden Verzögerung
        } else {
            val dialog = WrongAnswerDialog(this, correctAnswers, currentRiddleUnit)
            dialog.show()
            Handler(Looper.getMainLooper()).postDelayed({
                showNextRiddle() // Nächste Frage laden
            },2000) // 2 Sekunden Verzögerung
        }
    }
    
}