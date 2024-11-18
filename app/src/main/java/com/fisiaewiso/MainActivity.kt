package com.fisiaewiso

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.activity.ComponentActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var bRiddle: Button
    private lateinit var bRiddleResult: Button
    private lateinit var bRiddleAdmin: Button
    var adminmode = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Setze das Layout
        setContentView(R.layout.activity_main)
        // Zugriff auf die Datenbank
        AppDatabase.getDatabase(this)
        // Zugriff auf die SharedPreferences
        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val isFirstRun = sharedPreferences.getBoolean("isFirstRun", true)
        // Wenn dies der erste Start ist, initialisiere die Datenbank
        if (isFirstRun) {
            initializeDatabase()
            sharedPreferences.edit().putBoolean("isFirstRun", false).apply()
        }
        // Initialisiere die Buttons
        bRiddle = findViewById(R.id.bRiddle)
        bRiddleResult = findViewById(R.id.bRiddleResult)
        bRiddleAdmin = findViewById(R.id.bRiddleAdmin)
        // Öffne die Activity RiddleActivity
        bRiddle.setOnClickListener {
            val intent = Intent(this, RiddleActivity::class.java)
            startActivity(intent)
        }
        bRiddleResult.setOnClickListener {
            val intent = Intent(this, RiddleResultActivity::class.java)
            startActivity(intent)
        }
        // Zeige den Admin-Button nur bei Bedarf an
        if (adminmode) {
            bRiddleAdmin.visibility = View.VISIBLE
            bRiddleAdmin.setOnClickListener {
                val intent = Intent(this, SettingsActivity::class.java)
                startActivity(intent)
            }
        } else {
            // sobald AdminMode oben auf false gesetzt wird, deaktivere es Global und setze das laden von zufälligen Rätsel
            val sharedRootPreferences = getSharedPreferences("com.fisiaewiso_preferences", Context.MODE_PRIVATE)
            val editor = sharedRootPreferences.edit()
            editor.putBoolean("adminmode", adminmode)
            editor.putString("pref_available_riddles", 0.toString())
            editor.apply()
        }
    }
    // Initialisiere die Datenbank für fülle Sie mit den Daten
    private fun initializeDatabase() {
        CoroutineScope(Dispatchers.IO).launch {
            AppDatabase.getDatabase(applicationContext).riddleDao().insertAll()
        }
    }

}