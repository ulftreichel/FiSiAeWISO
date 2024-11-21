package com.fisiaewiso

import android.app.TaskStackBuilder
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Resources
import android.os.Bundle
import android.preference.Preference
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.ui.input.key.key
import androidx.preference.PreferenceFragmentCompat

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val sharedPreferences = getSharedPreferences("com.fisiaewiso_preferences", Context.MODE_PRIVATE)
        val themePreference = sharedPreferences.getString("theme_preference", "auto")
        val userType = sharedPreferences.getBoolean("adminmode", false) // Standardmäßig false
        when (themePreference) {
            "light" -> setTheme(R.style.Theme_FiSiAeWISO_Light)
            "dark" -> setTheme(R.style.Theme_FiSiAeWISO_Dark)
            "auto" -> setTheme(R.style.Theme_FiSiAeWISO)
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

    class AdminSettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.admin_preferences, rootKey) // Admin Benutzereinstellungen
            // Finde die Exit-Preference
            val exitPreference = findPreference<androidx.preference.Preference>("exitlink")
            exitPreference?.setOnPreferenceClickListener {
                requireActivity().finish() // Beendet die App
                true
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
                val themePreference = sharedPreferences?.getString(key, "auto")
                when (themePreference) {
                    "auto" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
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
        val currentTheme = sharedPreferences.getString("theme_preference", "auto")

        // Überprüfen, ob Änderungen vorgenommen wurden
        val resultIntent = Intent().apply {
            putExtra("theme_preference", currentTheme)
        }
        setResult(RESULT_OK, resultIntent)  // Result setzen
        super.onBackPressed()
    }

}