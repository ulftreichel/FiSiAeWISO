package com.fisiaewiso

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    // Buttons
    private lateinit var bRiddle: Button
    private lateinit var bRiddleResult: Button
    private lateinit var bRiddleSettings: Button
    var adminmode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        val sharedPreferences = getSharedPreferences("com.fisiaewiso_preferences", Context.MODE_PRIVATE)
        val isFirstRun = sharedPreferences.getBoolean("isFirstRun", true)
        // Wenn dies der erste Start ist, initialisiere die Datenbank
        if (isFirstRun) {
            initializeDatabase()
            sharedPreferences.edit().putBoolean("isFirstRun", false).apply()
        }
        val themePreference = sharedPreferences.getString("theme_preference", "standard")
        Log.d("MainActivity", "Theme preference: $themePreference")
        when (themePreference) {
            "standard" -> setTheme(R.style.Theme_FiSiAeWISO)
            "light" -> setTheme(R.style.Theme_FiSiAeWISO_Light)
            "dark" -> setTheme(R.style.Theme_FiSiAeWISO_Dark)
        }
        super.onCreate(savedInstanceState)
        // Setze das Layout
        setContentView(R.layout.activity_main)
        // Zugriff auf die Datenbank
        AppDatabase.getDatabase(this)
        //Berechtigung setzen
        pref()
        // Initialisiere die Buttons
        bRiddle = findViewById(R.id.bRiddle)
        bRiddleResult = findViewById(R.id.bRiddleResult)
        bRiddleSettings = findViewById(R.id.bRiddleSettings)
        // Öffne die Activity RiddleActivity
        bRiddle.setOnClickListener {
            val intent = Intent(this, RiddleActivity::class.java)
            startActivity(intent)
        }
        bRiddleResult.setOnClickListener {
            val intent = Intent(this, RiddleResultActivity::class.java)
            startActivity(intent)
        }
        if(adminmode){
            bRiddleSettings.visibility = View.VISIBLE
            bRiddleSettings.setOnClickListener {
                val intent = Intent(this, SettingsActivity::class.java)
                startActivity(intent)
            }
        }
        bRiddleSettings.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startSettingsForResult.launch(intent)
        }
    }
    // Initialisiere die Datenbank für fülle Sie mit den Daten
    private fun initializeDatabase() {
        CoroutineScope(Dispatchers.IO).launch {
            AppDatabase.getDatabase(applicationContext).riddleDao().insertAll()
            AppDatabase.getDatabase(applicationContext).riddleDescriptionDao().insertAll()
        }
    }

    private val startSettingsForResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        if (result.resultCode == RESULT_OK) {
            val sharedPreferences = getSharedPreferences("com.fisiaewiso_preferences", Context.MODE_PRIVATE)
            val themePreference = sharedPreferences.getString("theme_preference", "standard")
            when (themePreference) {
                "standard" -> setTheme(R.style.Theme_FiSiAeWISO)
                "light" -> setTheme(R.style.Theme_FiSiAeWISO_Light)
                "dark" -> setTheme(R.style.Theme_FiSiAeWISO_Dark)
            }
            recreate()  // Neue Aktivität mit aktualisiertem Theme
        }
    }

    private fun pref(){
        val sharedRootPreferences = getSharedPreferences("com.fisiaewiso_preferences", Context.MODE_PRIVATE)
        val editor = sharedRootPreferences.edit()
        if (adminmode) {
            // sobald AdminMode im Code auf true gesetzt wird, aktivere es Global und setze das laden von zufälligen Rätsel
            editor.putBoolean("adminmode", adminmode)
            editor.putString("theme_preference", "standard")
            editor.apply()
        } else {
            // sobald AdminMode im Code auf false gesetzt wird, deaktivere es Global und setze das laden von zufälligen Rätsel
            editor.putBoolean("adminmode", adminmode)
            editor.putString("pref_available_riddles", 0.toString())
            editor.apply()
        }
    }
}