package com.fisiaewiso

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
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
    private var adminmode = false
    private var loadAdminRiddle = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        val sharedPreferences = getSharedPreferences("com.fisiaewiso_preferences", Context.MODE_PRIVATE)
        val isFirstRun = sharedPreferences.getBoolean("isFirstRun", true)
        val prefavailableriddles = sharedPreferences.all[getString(R.string.pref_available_riddle)]?.toString() ?: "0"
        loadAdminRiddle = prefavailableriddles.toInt()
        // Zugriff auf die Datenbank
        AppDatabase.getDatabase(this)
        // Wenn dies der erste Start ist, initialisiere die Datenbank
        if (isFirstRun) {
            initializeDatabase()
            sharedPreferences.edit().putBoolean("isFirstRun", false).apply()
        }
        val themePreference = sharedPreferences.getString("theme_preference", "standard")
        when (themePreference) {
            "standard" -> setTheme(R.style.Theme_FiSiAeWISO)
            "light" -> setTheme(R.style.Theme_FiSiAeWISO_Light)
            "dark" -> setTheme(R.style.Theme_FiSiAeWISO_Dark)
        }
        super.onCreate(savedInstanceState)
        // Setze das Layout
        setContentView(R.layout.activity_main)
        //Berechtigung setzen
        pref()
        // Initialisiere die Buttons
        bRiddle = findViewById(R.id.bRiddle)
        bRiddleResult = findViewById(R.id.bRiddleResult)
        bRiddleSettings = findViewById(R.id.bRiddleSettings)
        if (!adminmode) { // wenn Adminmode deaktiviert ist
            bRiddle.text = getString(R.string.zufaelliges_raetsel)
            bRiddleSettings.text = getString(R.string.einstellungen)
        } else { // wenn Adminmode aktiviert ist
            bRiddleSettings.text = getString(R.string.admin_einstellungen)
            if (loadAdminRiddle == 0) {
                bRiddle.text = getString(R.string.zufaelliges_raetsel_laden)
            } else {
                bRiddle.text = getString(R.string.raetsel_laden, loadAdminRiddle)
            }
        }
        // Öffne die Activity RiddleActivity
        bRiddle.setOnClickListener {
            val intent = Intent(this, RiddleActivity::class.java)
            startActivity(intent)
        }
        // Öffne die Activity RiddleResultActivity
        bRiddleResult.setOnClickListener {
            val intent = Intent(this, RiddleResultActivity::class.java)
            startActivity(intent)
        }
        bRiddleSettings.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startSettingsForResult.launch(intent)
        }
    }
    // Initialisiere die Datenbank
    private fun initializeDatabase() {
        CoroutineScope(Dispatchers.IO).launch {
            AppDatabase.getDatabase(applicationContext).riddleDao().insertAll() // fülle die Datenbank mit Rätseln
            AppDatabase.getDatabase(applicationContext).riddleDescriptionDao().insertAll() // fülle die Datenbank mit Erklärungen
        }
    }

    private val startSettingsForResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        val sharedPreferences = getSharedPreferences("com.fisiaewiso_preferences", Context.MODE_PRIVATE)
        val prefavailableriddles = sharedPreferences.all[getString(R.string.pref_available_riddle)]?.toString() ?: "0"
        loadAdminRiddle = prefavailableriddles.toInt()

        if (result.resultCode == RESULT_OK) {
            val themePreference = sharedPreferences.getString("theme_preference", "standard")
            when (themePreference) {
                "standard" -> setTheme(R.style.Theme_FiSiAeWISO)
                "light" -> setTheme(R.style.Theme_FiSiAeWISO_Light)
                "dark" -> setTheme(R.style.Theme_FiSiAeWISO_Dark)
            }
            Log.d("MainActivity", "Settings activity saved changes. Selected Riddle: $loadAdminRiddle")
            recreate()  // Neue Aktivität mit aktualisiertem Theme
        } else {
            Log.d("MainActivity", "Settings activity closed. Selected Riddle: $loadAdminRiddle")
        }
    }
    // Daten in SharedPreferences speichern
    private fun pref(){
        val sharedRootPreferences = getSharedPreferences("com.fisiaewiso_preferences", Context.MODE_PRIVATE) // Zugriff auf die SharedPreferences
        val editor = sharedRootPreferences.edit() // Editor für die SharedPreferences
        if (adminmode) {
            // sobald AdminMode im Code auf true gesetzt wird, setze das standard theme, in der RiddelActivity hat das die größten auswirkungen
            editor.putBoolean("adminmode", adminmode)
            editor.putString("theme_preference", "standard")
            editor.apply()
        } else {
            // sobald AdminMode im Code auf false gesetzt wird, deaktiviere es Global und setze das laden von zufälligen Rätsel
            editor.putBoolean("adminmode", adminmode)
            editor.putString("pref_available_riddles", 0.toString())
            editor.apply()
        }
    }
}