package com.fisiaewiso

import android.app.TaskStackBuilder
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Resources
import android.os.Bundle
import android.preference.Preference
import android.util.Log
import androidx.activity.result.launch
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.size
import androidx.compose.ui.input.key.key
import androidx.lifecycle.lifecycleScope
import androidx.preference.ListPreference
import androidx.preference.PreferenceFragmentCompat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val sharedPreferences = getSharedPreferences("com.fisiaewiso_preferences", Context.MODE_PRIVATE)
        val themePreference = sharedPreferences.getString("theme_preference", "standard")
        val userType = sharedPreferences.getBoolean("adminmode", false) // Standardmäßig false
        when (themePreference) {
            "standard" -> setTheme(R.style.Theme_FiSiAeWISO)
            "light" -> setTheme(R.style.Theme_FiSiAeWISO_Light)
            "dark" -> setTheme(R.style.Theme_FiSiAeWISO_Dark)
        }
        setContentView(R.layout.settings_activity)

        if (savedInstanceState == null) {
            val fragment = if (userType == true) {
                AdminSettingsFragment() // Fragment für Admin-Einstellungen
            } else {
                SettingsFragment() // Fragment für normale Benutzereinstellungen
            }
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings, fragment)
                .commit()
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    class AdminSettingsFragment : PreferenceFragmentCompat(), SharedPreferences.OnSharedPreferenceChangeListener {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.admin_preferences, rootKey) // Admin-Einstellungen

            val riddlePreference = findPreference<ListPreference>(getString(R.string.pref_available_riddle))
            val sharedPreferences = preferenceManager.sharedPreferences

            if (riddlePreference != null) {
                // Entferne einen vorhandenen SummaryProvider
                riddlePreference.summaryProvider = null

                // Lade aktuellen Wert aus SharedPreferences
                val currentValue = sharedPreferences?.getString(riddlePreference.key, "0") // Standardwert: "0" (Zufall)

                // Setze Summary basierend auf aktuellem Wert
                updateSummary(riddlePreference, currentValue)

                // Listener hinzufügen, um Summary zu aktualisieren, wenn der Wert geändert wird
                riddlePreference.setOnPreferenceChangeListener { preference, newValue ->
                    updateSummary(riddlePreference, newValue as String)
                    true // Änderungen akzeptieren
                }
            }
            val exitPreference = findPreference<androidx.preference.Preference>("exitlink")
            exitPreference?.setOnPreferenceClickListener {
                val sharedPreferences = preferenceManager.sharedPreferences
                val prefAvailableRiddles = sharedPreferences?.getString(getString(R.string.pref_available_riddle), "0") ?: "0"
                val loadAdminRiddle = prefAvailableRiddles.toInt()

                // Setze das Ergebnis und beende die Activity
                val resultIntent = Intent().apply {
                    putExtra("pref_available_riddles", loadAdminRiddle)
                }
                requireActivity().setResult(AppCompatActivity.RESULT_OK, resultIntent)  // RESULT_OK setzen
                requireActivity().finish() // Beendet die App
                true
            }

            val db = AppDatabase.getDatabase(requireContext())
            val riddleDescriptionDao = db.riddleDescriptionDao()

            // Daten aus der Datenbank laden
            lifecycleScope.launch {
                try {
                    // Rätselbeschreibungen abrufen
                    val riddleDescriptions = riddleDescriptionDao.getAllRiddleDescriptions().first()

                    // Listen für die Einträge und Werte
                    val availableRiddleList = mutableListOf<CharSequence>()
                    val availableRiddleValuesList = mutableListOf<CharSequence>()

                    // "Zufall" hinzufügen
                    availableRiddleList.add("Zufall")
                    availableRiddleValuesList.add("0")

                    // Rätsel aus der Datenbank hinzufügen
                    for (riddleDescription in riddleDescriptions) {
                        availableRiddleList.add("Rätsel ${riddleDescription.riddleDescMainNumber}")
                        availableRiddleValuesList.add(riddleDescription.riddleDescMainNumber.toString())
                    }

                    // ListPreference aktualisieren
                    requireActivity().runOnUiThread {
                        if (riddlePreference != null) {
                            riddlePreference.entries = availableRiddleList.toTypedArray()
                            riddlePreference.entryValues = availableRiddleValuesList.toTypedArray()

                            // Optional: Standardwert setzen, falls noch kein Wert gespeichert ist
                            if (riddlePreference.value.isNullOrEmpty()) {
                                riddlePreference.value = "0" // Standardwert: "Zufall"
                            }

                            // Setze den Summary-Wert basierend auf dem aktuellen Wert
                            val currentValue = riddlePreference.value
                            val entryIndex = riddlePreference.findIndexOfValue(currentValue)
                            riddlePreference.summary = if (entryIndex >= 0) {
                                riddlePreference.entries[entryIndex]
                            } else {
                                "Nicht festgelegt"
                            }

                            // Listener hinzufügen, um den Summary-Wert bei Änderungen zu aktualisieren
                            riddlePreference.setOnPreferenceChangeListener { preference, newValue ->
                                val newValueString = newValue as String
                                val newIndex = riddlePreference.findIndexOfValue(newValueString)
                                riddlePreference.summary = if (newIndex >= 0) {
                                    riddlePreference.entries[newIndex]
                                } else {
                                    "Nicht festgelegt"
                                }
                                true // Änderungen akzeptieren
                            }
                        } else {
                            Log.e("AdminSettingsFragment", "ListPreference nicht gefunden")
                        }
                    }
                } catch (e: Exception) {
                    Log.e("AdminSettingsFragment", "Fehler beim Laden der Rätselbeschreibungen", e)
                }
            }
            preferenceManager.sharedPreferences?.registerOnSharedPreferenceChangeListener(this)
        }

        override fun onResume() {
            super.onResume()
            preferenceScreen.sharedPreferences?.registerOnSharedPreferenceChangeListener(this)
        }

        override fun onPause() {
            super.onPause()
            preferenceScreen.sharedPreferences?.unregisterOnSharedPreferenceChangeListener(this)
        }

        override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
            if (key == getString(R.string.pref_available_riddle)) {
                val riddlePreference = findPreference<ListPreference>(key)
                if (riddlePreference != null) {
                    val currentValue = sharedPreferences?.getString(key, "0")
                    updateSummary(riddlePreference, currentValue)
                }
                requireActivity().recreate()
            }
        }

        private fun updateSummary(preference: ListPreference, value: String?) {
            val entryIndex = preference.findIndexOfValue(value)
            preference.summary = if (entryIndex >= 0) {
                preference.entries[entryIndex]
            } else {
                "Nicht festgelegt"
            }
        }
    }


    class SettingsFragment : PreferenceFragmentCompat(), SharedPreferences.OnSharedPreferenceChangeListener {

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.user_preferences, rootKey) // User-Einstellungen
            // Finde die Exit-Preference
            val exitPreference = findPreference<androidx.preference.Preference>("exitlink")
            exitPreference?.setOnPreferenceClickListener {
                requireActivity().finish() // Beendet die App
                true
            }
            preferenceManager.sharedPreferences?.registerOnSharedPreferenceChangeListener(this)
        }

        override fun onResume() {
            super.onResume()
            preferenceScreen.sharedPreferences?.registerOnSharedPreferenceChangeListener(this)
        }

        override fun onPause() {
            super.onPause()
            preferenceScreen.sharedPreferences?.unregisterOnSharedPreferenceChangeListener(this)
        }

        override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
            if (key == "theme_preference") {
                val themePreference = sharedPreferences?.getString(key, "standard")
                when (themePreference) {
                    "standard" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                    "light" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                    "dark" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                }
                // Activity neustarten, um Theme sofort zu übernehmen
                requireActivity().recreate()
            }
        }

    }

    override fun onBackPressed() {
        val sharedPreferences = getSharedPreferences("com.fisiaewiso_preferences", Context.MODE_PRIVATE)
        val currentTheme = sharedPreferences.getString("theme_preference", "standard")
        // Überprüfen, ob Änderungen vorgenommen wurden
        val resultIntent = Intent().apply {
            putExtra("theme_preference", currentTheme)
        }
        setResult(RESULT_OK, resultIntent)  // Result setzen
        super.onBackPressed()
    }

}