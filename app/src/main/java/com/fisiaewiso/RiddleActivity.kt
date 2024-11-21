package com.fisiaewiso

import android.animation.ObjectAnimator
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.text.InputType
import android.util.Log
import android.view.DragEvent
import android.view.Gravity
import android.view.View
import android.view.animation.Animation
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
import android.widget.ScrollView
import android.widget.TextView
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Collections
import kotlin.properties.Delegates
import kotlin.text.replace
import kotlin.text.toDoubleOrNull

class RiddleActivity : AppCompatActivity() {

    // Variablendeklarationen
    private lateinit var currentRiddle: Riddle
    private lateinit var tVRiddle_Initialize: TextView
    private lateinit var tVRiddle_Initialize2: TextView
    private lateinit var riddleTextView: TextView
    private lateinit var scrollViewQuestion: ScrollView
    private lateinit var numberInput: EditText
    private lateinit var numberInput2: EditText
    private lateinit var dateInput: EditText
    private lateinit var nextButton: Button
    private lateinit var answerLater: Button
    private lateinit var startButton: Button
    private lateinit var closeButton: Button
    private lateinit var riddleImageButton: ImageButton
    private lateinit var iBCalculator: ImageButton
    private lateinit var bHelp: Button
    private var currentRiddleIndex = 0
    private lateinit var riddles: List<Riddle>
    private lateinit var riddleDescription: List<RiddleDescription>
    private val selectedAnswersOrder = mutableListOf<String>()
    private lateinit var radioGroupAnswers: RadioGroup
    private val checkBoxes = mutableListOf<CheckBox>()
    private lateinit var linearLayoutAnswers: LinearLayout
    private lateinit var recyclerViewAnswers: RecyclerView
    private lateinit var linearLayoutRecyclerView: ConstraintLayout
    private lateinit var optionsRecyclerView: androidx.recyclerview.widget.RecyclerView
    private lateinit var targetLinearLayout: LinearLayout
    private lateinit var linearTextView: LinearLayout
    private lateinit var frameTimeOut: FrameLayout
    private lateinit var tvRiddleTimeOut: TextView
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
    val unansweredQuestions = mutableListOf<Int>()
    var completedRun = false
    var needHelp = false
    private lateinit var adapter: SortableRecyclerViewAdapter
    var inputCount = 0
    var adminmode = false
    private var timeout = 0
    private var countdownTimer: CountDownTimer? = null
    private var countdown_calc = false

    override fun onCreate(savedInstanceState: Bundle?) {
        val sharedPreferences = getSharedPreferences("com.fisiaewiso_preferences", Context.MODE_PRIVATE)
        adminmode = sharedPreferences.getBoolean(getString(R.string.pref_adminmode), false)
        countdown_calc = sharedPreferences.getBoolean(getString(R.string.pref_countdown_calc), false)
        val pref_available_riddles = sharedPreferences.all[getString(R.string.pref_available_riddle)]?.toString() ?: "0"
        val loadAdminRiddle = pref_available_riddles.toInt()
        val timeoutString = sharedPreferences.all[getString(R.string.pref_timeout)]?.toString() ?: "0"
        timeout = timeoutString.toInt()
        val themePreference = sharedPreferences.getString("theme_preference", "standard")
        when (themePreference) {
            "standard" -> setTheme(R.style.Theme_FiSiAeWISO)
            "light" -> setTheme(R.style.Theme_FiSiAeWISO_Light)
            "dark" -> setTheme(R.style.Theme_FiSiAeWISO_Dark)
        }
        super.onCreate(savedInstanceState)
        // Setze das Layout
        setContentView(R.layout.activity_riddle)
        // Initialisiere die Elemente
        tVRiddle_Initialize = findViewById(R.id.riddle_initialize)
        tVRiddle_Initialize2 = findViewById(R.id.riddle_initialize2)
        riddleTextView = findViewById(R.id.textView3)
        scrollViewQuestion = findViewById(R.id.scrollViewQuestion)
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
        recyclerViewAnswers = findViewById(R.id.recyclerViewAnswers)
        linearLayoutRecyclerView = findViewById(R.id.linearLayoutRecyclerView)
        optionsRecyclerView = findViewById(R.id.optionsRecyclerView)
        targetLinearLayout = findViewById(R.id.targetLinearLayout)
        frameTimeOut = findViewById(R.id.frameTimeout)
        tvRiddleTimeOut = findViewById(R.id.tvRiddleTimeOut)
        linearTextView = findViewById(R.id.linearTextView)
        val repository = ResultRepository(AppDatabase.getDatabase(this).resultDao(), AppDatabase.getDatabase(this).riddleDao())
        viewModel = ViewModelProvider(this, RiddleViewModelFactory(repository)).get(RiddleViewModel::class.java)
        numberInput = findViewById(R.id.riddleResultNumber1)
        numberInput2 = findViewById(R.id.riddleResultNumber2)
        dateInput = findViewById(R.id.riddleResultDate)
        nextButton = findViewById(R.id.bRiddleNext)
        answerLater = findViewById(R.id.bRiddleLater)
        startButton = findViewById(R.id.startButton)
        closeButton = findViewById(R.id.bClose)
        iBCalculator = findViewById(R.id.iBCalculator)
        bHelp = findViewById(R.id.bHelp_Formel)
        riddleImageButton = findViewById(R.id.riddleImageButton)
        currentRiddle = Riddle(0,0, 0, 0, "Datenbank wird beim nächsten Neustart zur Verfügung stehen", listOf(), listOf(), listOf(),false, false,false, false, false, false, false, false,false, listOf(), listOf(), listOf(), mapOf())
        // TextViews 1-30 für die Antworten
        for (i in 1..30) {
            val textViewId = resources.getIdentifier("tVcorrectRiddle$i", "id", packageName)
            val textView = findViewById<TextView>(textViewId)
            answerTextViews.add(textView)
        }
        if (adminmode) {
            loadIntro(loadAdminRiddle)
        } else {
            loadIntro(loadAdminRiddle)
        }
        // Buttons
        startButton.setOnClickListener {
            loadRiddlesByIntro()
            currentIntro = ""
            riddlesLoaded = true
            tVRiddle_Initialize.visibility = View.GONE
            tVRiddle_Initialize2.visibility = View.GONE
            startButton.visibility = View.GONE
            closeButton.visibility = View.GONE
            nextButton.visibility = View.VISIBLE
            answerLater.visibility = View.VISIBLE
            if (adminmode) {
                nextButton.isEnabled = true
                answerLater.isEnabled = true
            } else {
                countdown()
                nextButton.isEnabled = false
                answerLater.isEnabled = false
                Handler(Looper.getMainLooper()).postDelayed({
                    nextButton.isEnabled = true
                    answerLater.isEnabled = true
                },2000) // 2 Sekunden Verzögerung
            }

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
        answerLater.setOnClickListener {
            if (currentRiddleIndex in 0..29) {
                val textView = answerTextViews[currentRiddleIndex]
                textView.setBackgroundColor(Color.GRAY)
            }
            addUnansweredQuestion(currentRiddle.riddleNumber)
            proceedToNextRiddle()
        }
        unansweredQuestions.clear()
    }

    // lade ein zufälliges Rätsel
    private fun loadIntro(pref_available_riddles: Int) {
        lifecycleScope.launch {
            riddleDescription = AppDatabase.getDatabase(this@RiddleActivity).riddleDescriptionDao().getAllRiddleDescriptions().first()
            riddleDescription = riddleDescription.map { riddleDescription ->
                riddleDescription.copy(
                    riddleDescMainNumber = riddleDescription.riddleDescMainNumber,
                    description = riddleDescription.description.replace(";", ","),
                )
            }
            if (pref_available_riddles == 0) {
                val randomRiddle = riddleDescription.random()
                riddleMainNumber = randomRiddle.riddleDescMainNumber
                currentIntro = randomRiddle.description
                tVRiddle_Initialize2.text = currentIntro
            } else {
                riddleMainNumber = pref_available_riddles
                currentIntro = riddleDescription.find { it.riddleDescMainNumber == riddleMainNumber }?.description ?: ""
                tVRiddle_Initialize2.text = currentIntro
            }
        }
    }

    // Lade Rätsel nach Intro
    private fun loadRiddlesByIntro() {
        lifecycleScope.launch {
            loadRiddles(riddleMainNumber) {
                // currentRiddle im ViewModel aktualisieren, nachdem die Rätsel geladen wurden
                viewModel.updateCurrentRiddle(currentRiddle)
            }
        }
        // currentRiddle im ViewModel aktualisieren
        viewModel.updateCurrentRiddle(currentRiddle)
    }

    // Lade Rätsel nach RiddleMainNumber
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

    // Zeige das nächste Rätsel an
    private fun proceedToNextRiddle() {
        if(completedRun){
            if (unansweredQuestions.isNotEmpty()) {
                removeUnansweredQuestion(currentRiddle.riddleNumber)
            } else {
                completedRun = false
            }
        }
        showNextRiddle() // Nächste Frage laden
    }
    // CountDown
    private fun countdown() {
        if (!adminmode && timeout > 0) {
            frameTimeOut.visibility = View.GONE // Timer verstecken, wenn >10 Sekunden

            val timer = object : CountDownTimer(timeout * 1000L, 1000L) {
                override fun onTick(millisUntilFinished: Long) {
                    val secondsRemaining = millisUntilFinished / 1000
                    if (secondsRemaining <= 10) {
                        frameTimeOut.visibility = View.VISIBLE
                        tvRiddleTimeOut.text = secondsRemaining.toString()

                        // Farbe ändern
                        val color = when {
                            secondsRemaining > 5 -> Color.GREEN
                            secondsRemaining > 3 -> Color.YELLOW
                            else -> Color.RED
                        }
                        tvRiddleTimeOut.setTextColor(color)
                        // Animation starten
                        animateCountdownText()
                    }
                }

                override fun onFinish() {
                    frameTimeOut.visibility = View.GONE
                    evaluateAnswer()
                }
            }
            countdownTimer = timer // Referenz speichern
            timer.start() // Lokale Variable verwenden
        }
    }
    // Animation für den Timer Text
    private fun animateCountdownText() {
        val scaleAnimation = android.view.animation.ScaleAnimation(
            1.0f, 2.5f,  // Start- und Endskala in x-Richtung
            1.0f, 2.5f,  // Start- und Endskala in y-Richtung
            Animation.RELATIVE_TO_SELF, 0.5f,  // Pivot-X (Mittelpunkt)
            Animation.RELATIVE_TO_SELF, 0.5f   // Pivot-Y (Mittelpunkt)
        )
        scaleAnimation.duration = 300  // Dauer der Animation (in Millisekunden)
        scaleAnimation.repeatCount = 0 // Kein Wiederholen

        val alphaAnimation = android.view.animation.AlphaAnimation(
            0.25f, 1.0f // Von 50% zu 100% Sichtbarkeit
        )
        alphaAnimation.duration = 300
        alphaAnimation.repeatCount = 0

        // Kombiniere Skalierungs- und Alpha-Animation
        val animationSet = android.view.animation.AnimationSet(true)
        animationSet.addAnimation(scaleAnimation)
        animationSet.addAnimation(alphaAnimation)

        tvRiddleTimeOut.startAnimation(animationSet)
    }

    // Weiter zur nächsten Frage
    private fun showNextRiddle() {
        if (adminmode){
            nextButton.isEnabled = true
            answerLater.isEnabled = true
        } else {
            countdown()
            nextButton.isEnabled = false
            answerLater.isEnabled = false
            Handler(Looper.getMainLooper()).postDelayed({
                nextButton.isEnabled = true
                answerLater.isEnabled = true
            },3000) // 2 Sekunden Verzögerung
            val animator = ObjectAnimator.ofFloat(riddleTextView, "alpha", 0f, 1f)
            animator.duration = 750 // Animationsdauer in Millisekunden
            animator.start()
        }
        if (currentRiddleIndex < riddles.size) { // Überprüfe, ob noch Rätsel vorhanden sind
            currentRiddleIndex++ // Index für das nächste Rätsel aktualisieren
            if (currentRiddleIndex < riddles.size) { // Überprüfe erneut, ob noch Rätsel vorhanden sind
                currentRiddle = riddles[currentRiddleIndex]
                selectedAnswersOrder.clear()
                displayRiddle()
            } else {
                Log.d("RiddleActivity", "No more riddles available")
                if (completedRun) {
                    Log.d("RiddleActivity", "Completed run first 1 $completedRun")
                    Log.d("RiddleActivity", "Unanswered questions size: ${unansweredQuestions.size}")
                    completedRun = false
                    if (unansweredQuestions.isNotEmpty()) {
                        showAlertUnansweredQuestions()
                    } else {
                        showTotalPointsAndSaveResults()
                    }
                } else {
                    Log.d("RiddleActivity", "Completed run first 2 $completedRun")
                    Log.d("RiddleActivity", "Unanswered questions size: ${unansweredQuestions.size}")
                    completedRun = true
                    if (unansweredQuestions.isNotEmpty()) {
                        showAlertUnansweredQuestions()
                    } else {
                        showTotalPointsAndSaveResults()
                    }
                }
            }
        } else {
            if (completedRun) {
                Log.d("RiddleActivity", "Completed run second 1 $completedRun")
                Log.d("RiddleActivity", "Unanswered questions size: ${unansweredQuestions.size}")
                completedRun = false
                if (unansweredQuestions.isNotEmpty()) {
                    showAlertUnansweredQuestions()
                } else {
                    showTotalPointsAndSaveResults()
                }
            } else {
                Log.d("RiddleActivity", "Completed run second 2 $completedRun")
                Log.d("RiddleActivity", "Unanswered questions size: ${unansweredQuestions.size}")
                completedRun = true
                if (unansweredQuestions.isNotEmpty()) {
                    showAlertUnansweredQuestions()
                } else {
                    showTotalPointsAndSaveResults()
                }
            }
        }
    }

    //Abfrage ob nicht beantwortete Rätsel angezeigt werden sollen
    private fun showAlertUnansweredQuestions() {
        val crowd = unansweredQuestions.size
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Unbeantwortete Fragen")
        builder.setMessage("Es gibt noch $crowd unbeantwortete Fragen. Möchtest du diese jetzt beantworten?")
        builder.setPositiveButton("OK") { dialog, which ->
            loadUnAnsweredQuestion {
                viewModel.updateCurrentRiddle(currentRiddle)
            }
        }
        builder.setNegativeButton("Abbrechen") { dialog, which ->
            showTotalPointsAndSaveResults()
        }
        val dialog = builder.create()
        dialog.show()
    }

    // Lade unbeantwortete Rätsel
    fun loadUnAnsweredQuestion(onRiddlesLoaded: () -> Unit) {
        lifecycleScope.launch {
            val riddlesToLoad = mutableListOf<Riddle>()
            withContext(Dispatchers.IO) {
                for (riddleNumber in unansweredQuestions) {
                    val riddle: Riddle = AppDatabase.getDatabase(this@RiddleActivity).riddleDao().getRiddleByNumber(riddleNumber)
                    riddlesToLoad.add(riddle.copy(
                        question = riddle.question.replace(";", ","),
                        answers = riddle.answers.map { it.replace(";", ",") },
                        correctAnswers = riddle.correctAnswers.map { it.replace(";", ",") }
                    ))
                }
            }
            riddles = riddlesToLoad
            if (riddles.isNotEmpty()) {
                currentRiddleIndex = 0 // Setze den aktuellen Rätselindex auf 0
                currentRiddle = riddles[currentRiddleIndex] // Setze das aktuelle Rätsel
                displayRiddle() // Zeige das erste Rätsel an
                onRiddlesLoaded()
            }
        }
    }

    // Zeige das Rätsel an
    private fun displayRiddle() {
        //Entferne die vorherigen Antworten
        radioGroupAnswers.removeAllViews()
        radioButtons.clear()
        linearLayoutAnswers.removeAllViews()
        checkBoxes.clear()
        userMappings.clear()
        // Bild laden, falls vorhanden
        when (currentRiddle.riddleNumber) {
            // Nur für Fragen mit Bildern
            21, 54, 65, 81, 112, 122, 170 -> {
                val imageResource = when (currentRiddle.riddleNumber) {
                    21 -> R.drawable.mehrliniensystem
                    54 -> R.drawable.riddle2unterschriften
                    65 -> R.drawable.riddle65holiday
                    81 -> R.drawable.matrixsystem
                    112 -> R.drawable.gleichgewichtspreis
                    122 -> R.drawable.mehrliniensystem
                    170 -> R.drawable.blauerengel
                    else -> 0 // Sollte nicht erreicht werden, aber zur Sicherheit
                }
                // Bild laden, falls vorhanden
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
        // Layout für einzelne Fragen ändern
        when (currentRiddle.riddleNumber) {
            4, 7, 11, 14, 63, 95 -> {
                val params = scrollViewQuestion.layoutParams as ConstraintLayout.LayoutParams
                params.height = 0
                scrollViewQuestion.layoutParams = params
            } else -> {
            val params = scrollViewQuestion.layoutParams as ConstraintLayout.LayoutParams
            params.height = ConstraintLayout.LayoutParams.WRAP_CONTENT
            scrollViewQuestion.layoutParams = params
            scrollViewQuestion.requestLayout()
            }
        }
        //Welche Frage soll geshuffelt werden?
        val shouldShuffle = when (currentRiddle.riddleNumber) {
            2 -> true // Fragen, die geshuffelt werden sollen
            11 -> false // Frage, die nicht geshuffelt werden soll
            else -> true // Standardmäßig shufflen
        }
        // Countdown für einzelne Fragen deaktivieren
        if (!countdown_calc){
            when (currentRiddle.riddleNumber) {
                4, 16, 20, 46, 47, 79, 80, 108, 142 -> {
                    //kurze mitteilung das der Countdown deaktiviert wurde
                    frameTimeOut.visibility = View.VISIBLE
                    tvRiddleTimeOut.text = "Countdown deaktiviert"
                    tvRiddleTimeOut.setTextColor(Color.GREEN)
                    // tvRiddleTimeOut Textgröße ändern
                    tvRiddleTimeOut.textSize = 30f
                    // Text langsam animiert ausblenden
                    val animator = ObjectAnimator.ofFloat(tvRiddleTimeOut, "alpha", 1f, 0f)
                    animator.duration = 7500 // Animationsdauer in Millisekunden
                    animator.start()
                    // frameTimeOut nach 10 Sekunden verstecken
                    Handler(Looper.getMainLooper()).postDelayed({
                        frameTimeOut.visibility = View.GONE
                    }, 10000)
                    // Timer stoppen
                    countdownTimer?.cancel()
                }
            }
        }

        // Antworten mischen
        val answersshuffle = if (shouldShuffle) {
            currentRiddle.answers.shuffled()
        } else {
            currentRiddle.answers // Originalreihenfolge beibehalten
        }
        if (shouldShuffle) {
            Collections.shuffle(answersshuffle) // answersshuffle direkt mischen
        }
        if (currentRiddle.requiresOrderedAnswers) {
            //nur für Fragen die in die richtige Reihenfolge gebracht werden sollen
            recyclerViewAnswers.visibility = View.VISIBLE
            adapter = SortableRecyclerViewAdapter(this, answersshuffle)
            adapter.items = answersshuffle.toMutableList() // Gemischte Antworten in items speichern
            recyclerViewAnswers.adapter = adapter
            recyclerViewAnswers.layoutManager = LinearLayoutManager(this)
            val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
                ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0
            ) {
                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ): Boolean {
                    val fromPosition = viewHolder.bindingAdapterPosition
                    val toPosition = target.bindingAdapterPosition
                    val adapter = recyclerView.adapter as SortableRecyclerViewAdapter
                    Collections.swap(adapter.items, fromPosition, toPosition)
                    adapter.notifyItemMoved(fromPosition, toPosition)
                    return true
                }

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    // Nicht benötigt, da wir kein Swiping verwenden
                }
            })
            itemTouchHelper.attachToRecyclerView(recyclerViewAnswers)
            //Ende Listview
            checkBoxes.forEach { it.visibility = View.GONE }
            radioGroupAnswers.visibility = View.GONE
            dateInput.visibility = View.GONE
            unit1TextView.visibility = View.GONE
            unit2TextView.visibility = View.GONE
            unitDateTextView.visibility = View.GONE
            iBCalculator.visibility = View.GONE
            bHelp.visibility = View.GONE
        } else if (currentRiddle.requiresDragAndDrop) {
            // Nur für Drag and Drop Fragen
            unit1TextView.visibility = View.GONE
            unit2TextView.visibility = View.GONE
            unitDateTextView.visibility = View.GONE
            iBCalculator.visibility = View.GONE
            bHelp.visibility = View.GONE
            recyclerViewAnswers.visibility = View.GONE
            linearLayoutRecyclerView.visibility = View.VISIBLE
            // Ziele anzeigen
            val target1: FrameLayout = findViewById(R.id.target1)
            val target2: FrameLayout = findViewById(R.id.target2)
            val target3: FrameLayout = findViewById(R.id.target3)
            val target4: FrameLayout = findViewById(R.id.target4)
            val target5: FrameLayout = findViewById(R.id.target5)
            // Alle Ziele leeren
            target1.removeAllViews()
            target2.removeAllViews()
            target3.removeAllViews()
            target4.removeAllViews()
            target5.removeAllViews()
            // RecyclerView für die Optionen hinzufügen
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
            // Ziele hinzufügen
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
                target1.layoutParams.height = 250
                target2.layoutParams.height = 250
                target3.layoutParams.height = 250
                target4.layoutParams.height = 250
                target5.layoutParams.height = 250
            }
            // Alle Target-Layouts zunächst ausblenden
            for (i in 0 until targetLinearLayout.childCount) {
                targetLinearLayout.getChildAt(i).visibility = View.GONE
            }
            // Alle TextView-Layouts zunächst ausblenden
            for (i in 0 until linearTextView.childCount) {
                linearTextView.getChildAt(i).visibility = View.GONE
            }
            // Alle Target-Layouts und TextView-Layouts für die aktuellen Ziele anzeigen
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
                            // Option hinzufügen
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
                            //Option wieder entfernen
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
                            true
                        }
                        else -> true
                    }
                }
            }
        } else {
            if (currentRiddle.requiresNumberInput || currentRiddle.requiresTwoNumberInputs || currentRiddle.requiresDateInput || currentRiddle.requiresTimeInput) {
                if (currentRiddle.requiresCalculate){
                    iBCalculator.visibility = View.VISIBLE
                    bHelp.visibility = View.VISIBLE
                    iBCalculator.setOnClickListener {
                        inputCount = when {
                            currentRiddle.requiresTwoNumberInputs -> 2
                            currentRiddle.requiresNumberInput -> 1
                            else -> 0 // Oder einen anderen Standardwert, falls keine Eingabe benötigt wird
                        }
                        Log.d("RiddleActivity", "inputCount: $inputCount")
                        val intent = Intent(this, CalculatorActivity::class.java)
                        intent.putExtra("question", currentRiddle.question)
                        intent.putExtra("inputCount", inputCount)
                        startForResult.launch(intent)
                    }
                    bHelp.setOnClickListener {
                        AlertDialog.Builder(this)
                            .setTitle("! ! ! Warnung ! ! !")
                            .setMessage("Ok öffnet die Hilfe\n" +
                                    "auch wenn die Frage richtig beantwortet wird\n" +
                                    "werden nur die Hälfte der Punkte berechnet.")
                            .setPositiveButton("OK") { dialog, which ->
                                // Aktion ausführen, z. B. Dialog öffnen
                                needHelp = true
                                val dialog = HelpDialog(currentRiddle.riddleNumber)
                                dialog.show(supportFragmentManager, "helpDialog")
                            }
                            .setNegativeButton("Abbrechen", null)
                            .setCancelable(false)
                            .show()
                    }
                }
                // Eingabefelder anzeigen
                numberInput.text.clear()
                numberInput2.text.clear()
                dateInput.text.clear()
                // Eingabefelder anzeigen
                numberInput.visibility = View.VISIBLE
                unit1TextView.visibility = View.VISIBLE
                unit1TextView.text = currentRiddle.unit[0] // Erste Einheit anzeigen
                dateInput.visibility = View.GONE
                recyclerViewAnswers.visibility = View.GONE
                if (currentRiddle.requiresTwoNumberInputs) {
                    numberInput2.visibility = View.VISIBLE
                    unit2TextView.visibility = View.VISIBLE
                    unit2TextView.text = currentRiddle.unit[1] // Zweite Einheit anzeigen
                }
                if (currentRiddle.requiresDateInput) {
                    // Datum-Eingabefeld anzeigen
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
                    // Zeit-Eingabefeld anzeigen
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
                checkBoxes.forEach { it.visibility = View.GONE } // Verstecke CheckBoxes
                radioGroupAnswers.visibility = View.GONE // Verstecke RadioGroup
                linearLayoutRecyclerView.visibility = View.GONE // Verstecke LinearLayout for RecyclerView
                recyclerViewAnswers.visibility = View.GONE
            } else {
                // Eingabefelder ausblenden
                numberInput.visibility = View.GONE // Verstecke numberInput
                numberInput2.visibility = View.GONE // Verstecke numberInput2
                dateInput.visibility = View.GONE // Verstecke dateInput
                unit1TextView.visibility = View.GONE // Verstecke unit1TextView
                unit2TextView.visibility = View.GONE // Verstecke unit2TextView
                unitDateTextView.visibility = View.GONE // Verstecke unitDateTextView
                iBCalculator.visibility = View.GONE // Verstecke iBCalculator
                bHelp.visibility = View.GONE // Verstecke bHelp
                linearLayoutRecyclerView.visibility = View.GONE // Verstecke LinearLayout for RecyclerView
                recyclerViewAnswers.visibility = View.GONE
                // Antworten zufällig sortieren
                val shuffledAnswers = answersshuffle.toMutableList()
                Collections.shuffle(shuffledAnswers)
                // CheckBoxes und RadioButtons anzeigen
                checkBoxes.forEach { it.visibility = View.VISIBLE } // Zeige CheckBoxes
                //radioGroupAnswers.visibility = View.VISIBLE // Zeige RadioGroup
                if (currentRiddle.hasMultipleCorrectAnswers) {
                    radioGroupAnswers.visibility = View.GONE // Verstecke RadioGroup
                    linearLayoutAnswers.visibility = View.VISIBLE // Zeige LinearLayout for CheckBoxes
                    // Nur CheckBoxes hinzufügen, wenn mehrere Antworten korrekt sein können
                    for (answer in answersshuffle) {
                        val checkBox = CheckBox(this)
                        checkBox.text = answer
                        linearLayoutAnswers.addView(checkBox)
                        checkBoxes.add(checkBox) // Add to the list
                    }
                } else {
                    // Nur RadioButtons hinzufügen, wenn nur eine Antwort korrekt sein kann
                    radioGroupAnswers.visibility = View.VISIBLE // Show RadioGroup
                    linearLayoutAnswers.visibility = View.GONE // Hide LinearLayout for CheckBoxes
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

    fun addUnansweredQuestion(riddleNumber: Int) {
        unansweredQuestions.add(riddleNumber)
        Log.d("RiddleActivity", "Unanswered Questions: $unansweredQuestions")
    }

    fun removeUnansweredQuestion(riddleNumber: Int) {
        unansweredQuestions.remove(riddleNumber)
    }

    private val startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            val intent = result.data
            when (inputCount) {
                1 -> {
                    val resultValue = intent?.getStringExtra("result")
                    numberInput.setText(resultValue) // Ergebnis in numberInput anzeigen
                    Log.d("RiddleActivity", "Result Value: $resultValue")
                }
                2 -> {
                    val resultValue1 = intent?.getStringExtra("result1")
                    val resultValue2 = intent?.getStringExtra("result2")
                    numberInput.setText(resultValue1) // Ergebnis in numberInput anzeigen
                    numberInput2.setText(resultValue2) // Ergebnis in numberInput2 anzeigen
                    Log.d("RiddleActivity", "Result Value 1: $resultValue1")
                    Log.d("RiddleActivity", "Result Value 2: $resultValue2")
                }
                else -> { /* Keine Aktion erforderlich */ }
            }
        }
    }

    fun isOptionMapped(option: String): Boolean {
        return userMappings.containsKey(option)
    }
    // Zeige Bild
    private fun showImageDialog(imageResource : Int){
        val dialog = RiddleImageViewDialog(this, imageResource)
        dialog.show()
    }
    // Überprüfe die Antwort
    private fun evaluateAnswer(): Boolean {
        val riddleNumber = currentRiddle.riddleIndex // Ermittle die Rätselnummer
        val textViewIndex = riddleNumber - 1 // Berechne den Index des TextViews
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
            // Frage mit den Antworten vergleichen
            val userAnswers = adapter.items
            val numCorrectAnswers = userAnswers.withIndex().count { (index, answer) ->
                correctAnswers.getOrNull(index) == answer
            }
            val numPossibleAnswers = correctAnswers.size
            val isCorrect = userAnswers == correctAnswers
            partiallyCorrect = numCorrectAnswers > 0 && numCorrectAnswers < correctAnswers.size
            // Markiere die TextView
            if (textViewIndex in 0..29) {
                countdownTimer?.cancel()
                val textView = answerTextViews[textViewIndex]
                if (isCorrect) {
                    textView.setBackgroundColor(Color.GREEN) // Richtig: Grün
                    totalPoints += 3.33333
                    proceedToNextRiddle() // Weiter zum nächsten Rätsel
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
            // Frage mit mehreren richtigen Antworten
            val numCorrectAnswers = selectedAnswers.count { correctAnswers.contains(it) }
            val numPossibleAnswers = correctAnswers.size
            // isCorrect: Alle korrekten Antworten müssen in selectedAnswers enthalten sein
            // und alle selectedAnswers müssen in correctAnswers enthalten sein
            val isCorrect = correctAnswers.all { selectedAnswers.contains(it) } &&
                    selectedAnswers.all { correctAnswers.contains(it) }
            // partiallyCorrect: Mindestens eine korrekte Antwort muss in selectedAnswers enthalten sein
            // und nicht alle selectedAnswers sind korrekt
            partiallyCorrect = numCorrectAnswers > 0 && !isCorrect
            if (textViewIndex in 0..29) {
                countdownTimer?.cancel()
                val textView = answerTextViews[textViewIndex]
                if (isCorrect) {
                    if (needHelp){
                        needHelp = false
                        totalPoints += 3.33333 / 2
                        textView.setBackgroundColor(Color.MAGENTA) // Richtig aber Hilfe benötigt: Magenta
                    } else {
                        totalPoints += 3.33333
                        textView.setBackgroundColor(Color.GREEN) // Richtig: Grün
                    }
                    proceedToNextRiddle() // Weiter zum nächsten Rätsel
                } else if (partiallyCorrect){
                    if (needHelp){
                        needHelp = false
                        totalPoints += 3.33333 * (numCorrectAnswers.toDouble() / numPossibleAnswers) / 2
                        textView.setBackgroundColor(Color.MAGENTA) // Richtig aber Hilfe benötigt: Magenta
                    } else {
                        totalPoints += 3.33333 * (numCorrectAnswers.toDouble() / numPossibleAnswers)
                        textView.setBackgroundColor(Color.YELLOW) // Teilweise richtig: Gelb
                    }
                    wrongAnswerDialog(correctAnswers, currentRiddle.unit) // Zeige die richtige Antwort
                } else {
                    textView.setBackgroundColor(Color.RED) // Falsch: Rot
                    wrongAnswerDialog(correctAnswers, currentRiddle.unit) // Zeige die richtige Antwort
                }
            }
        }  else if (currentRiddle.hasdifferentanswers) {
            //Fragen mit mehreren richtigen Antworten
            val selectedAnswer = radioGroupAnswers.findViewById<RadioButton>(radioGroupAnswers.checkedRadioButtonId)?.text.toString() ?: ""
            // Überprüfen, ob die ausgewählte Antwort korrekt ist
            val isCorrect = currentRiddle.correctAnswers.contains(selectedAnswer)
            // Punkte vergeben, wenn die Antwort korrekt ist
            if (textViewIndex in 0..29) {
                countdownTimer?.cancel()
                val textView = answerTextViews[textViewIndex]
                if (isCorrect) {
                    if (needHelp){
                        needHelp = false
                        totalPoints += 3.33333 / 2
                        textView.setBackgroundColor(Color.MAGENTA) // Richtig aber Hilfe benötigt: Magenta
                    } else {
                        totalPoints += 3.33333
                        textView.setBackgroundColor(Color.GREEN) // Richtig: Grün
                    }
                    proceedToNextRiddle() // Weiter zum nächsten Rätsel
                } else {
                    textView.setBackgroundColor(Color.RED) // Falsch: Rot
                    wrongAnswerDialog(correctAnswers, currentRiddle.unit) // Zeige die richtige Antwort
                }
            }
        } else if (currentRiddle.requiresDragAndDrop) {
            // Frage mit Drag-and-Drop
            var correctAnswers = 0
            val totalAnswers = currentRiddle.correctMappings.size
            // Überprüfen, ob die ausgewählte Antwort korrekt ist
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
                    R.id.target5 -> R.id.target5TextView
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
            if (textViewIndex in 0..29) {
                countdownTimer?.cancel()
                val textView = answerTextViews[textViewIndex]
                if (correctAnswers == totalAnswers) {
                    textView.setBackgroundColor(Color.GREEN) // Richtig: Grün
                    totalPoints += 3.33333
                    proceedToNextRiddle() // Weiter zum nächsten Rätsel
                } else if (correctAnswers == 0) {
                    textView.setBackgroundColor(Color.RED) // Falsch: Rot
                    wrongAnswerDialog(correctMappings = currentRiddle.correctMappings) // Zeige die richtige Antwort
                } else {
                    textView.setBackgroundColor(Color.YELLOW) // Teilweise richtig: Gelb
                    val pointsPerCorrectAnswer = 3.33333 / totalAnswers
                    totalPoints += pointsPerCorrectAnswer * correctAnswers
                    wrongAnswerDialog(correctMappings = currentRiddle.correctMappings) // Zeige die richtige Antwort
                }
            }
        } else {
            // Fragen mit nur einer richtigen Antwort
            val isCorrect = correctAnswers == selectedAnswers
            // TextView für das aktuelle Rätsel markieren
            if (textViewIndex in 0..29) {
                countdownTimer?.cancel()
                val textView = answerTextViews[textViewIndex]
                if (isCorrect) {
                    if (needHelp){
                        needHelp = false
                        totalPoints += 3.33333 / 2
                        textView.setBackgroundColor(Color.MAGENTA) // Richtig aber Hilfe benötigt: Magenta
                    } else {
                        totalPoints += 3.33333
                        textView.setBackgroundColor(Color.GREEN) // Richtig: Grün
                    }
                    proceedToNextRiddle() // Weiter zum nächsten Rätsel
                } else {
                    wrongAnswerDialog(correctAnswers, currentRiddle.unit) // Zeige die richtige Antwort
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

    // Zeige die Gesamtpunktzahl und Zensur an und speichere die Ergebnisse in der Datenbank
    private fun showTotalPointsAndSaveResults() {
        val grade = viewModel.calculateGrade(totalPoints.toInt())
        val dialog = ResultDialog(this, totalPoints, grade)
        dialog.show()
        viewModel.saveResultsToDatabase(riddleMainNumber, totalPoints.toInt(), grade)
    }
    // Zeige die richtige Antwort
    private fun wrongAnswerDialog(correctAnswers: List<String> = emptyList(), currentRiddleUnit: List<String> = emptyList(), correctMappings: Map<String, String> = emptyMap()) {
        if (adminmode){
            proceedToNextRiddle()
        } else {
            if (currentRiddle.requiresDragAndDrop) {
                // Richtige Antworten für Drag-and-Drop-Fragen anzeigen
                val dialog = WrongAnswerDialog(this, correctAnswers, currentRiddleUnit, correctMappings)
                dialog.listener = object : WrongAnswerDialog.WrongAnswerDialogListener {
                    override fun onOkClicked() {
                        showNextRiddle()
                    }
                }
                dialog.show()
            } else {
                val dialog = WrongAnswerDialog(this, correctAnswers, currentRiddleUnit, correctMappings)
                dialog.listener = object : WrongAnswerDialog.WrongAnswerDialogListener {
                    override fun onOkClicked() {
                        showNextRiddle()
                    }
                }
                dialog.show()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        countdownTimer?.cancel() // Countdown-Timer stoppen
        countdownTimer = null
    }

    override fun onDestroy() {
        super.onDestroy()
        countdownTimer?.cancel() // Countdown-Timer stoppen
        countdownTimer = null
    }
}