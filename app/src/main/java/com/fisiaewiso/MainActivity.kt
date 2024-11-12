package com.fisiaewiso

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.ComponentActivity

class MainActivity : ComponentActivity() {

    private lateinit var bRiddle: Button
    private lateinit var bRiddleResult: Button
    private var isFirstRun = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
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
        // SharedPreferences zurücksetzen
        if (isFirstRun) {
            // SharedPreferences zurücksetzen
            val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.remove("lastIntro")
            editor.apply()
            isFirstRun = false // isFirstRun auf false setzen, damit der Code beim nächsten Start nicht mehr ausgeführt wird
        }
    }
}