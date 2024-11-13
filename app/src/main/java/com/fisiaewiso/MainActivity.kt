package com.fisiaewiso

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.activity.ComponentActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var bRiddle: Button
    private lateinit var bRiddleResult: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        AppDatabase.getDatabase(this)
        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val isFirstRun = sharedPreferences.getBoolean("isFirstRun", true)
        if (isFirstRun) {
            initializeDatabase()
            sharedPreferences.edit().putBoolean("isFirstRun", false).apply()
        }
        bRiddle = findViewById(R.id.bRiddle)
        bRiddleResult = findViewById(R.id.bRiddleResult)
        bRiddle.setOnClickListener {
            val intent = Intent(this, RiddleActivity::class.java)
            startActivity(intent)
        }
        bRiddleResult.setOnClickListener {
            val intent = Intent(this, RiddleResultActivity::class.java)
            startActivity(intent)
        }
    }

    private fun initializeDatabase() {
        CoroutineScope(Dispatchers.IO).launch {
            Log.d("MainActivity", "Insert Data")
            AppDatabase.getDatabase(applicationContext).riddleDao().insertAll()
        }
    }

}