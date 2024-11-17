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
        if (adminmode) {
            bRiddleAdmin.visibility = View.VISIBLE
            bRiddleAdmin.setOnClickListener {
                val intent = Intent(this, SettingsActivity::class.java)
                startActivity(intent)
            }

        }
    }
    // Initialisiere die Datenbank für fülle Sie mit den Daten
    private fun initializeDatabase() {
        CoroutineScope(Dispatchers.IO).launch {
            AppDatabase.getDatabase(applicationContext).riddleDao().insertAll()
        }
    }

}