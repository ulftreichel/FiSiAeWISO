package com.fisiaewiso

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.lifecycleScope
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class RiddleResultActivity : AppCompatActivity() {

    private lateinit var riddleDao: ResultDao //

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_riddle_result)
        riddleDao = AppDatabase.getDatabase(this).resultDao() // Initialize here
        lifecycleScope.launch {
            val riddleData = riddleDao.getAllRiddles().first()
            if (riddleData.isEmpty()) {
                val dialog = ResultNoDataDialog(this@RiddleResultActivity)
                dialog.show()
            }
            Log.d("RiddleResultActivity", "riddleData: $riddleData") // Log-Ausgabe
            val chart = findViewById<LineChart>(R.id.chart)
            // Daten für das Diagramm (Beispiel)

            val entries = ArrayList<Entry>()
            for (i in riddleData.indices) {
                entries.add(Entry(i.toFloat(), riddleData[i].points.toFloat())) // points verwenden
            }

            val dataSet = LineDataSet(entries, "Rätsel-Auswertung")
            dataSet.color = Color.BLUE
            dataSet.setCircleColor(Color.BLUE)

            val lineData = LineData(dataSet)

            chart.data = lineData
            chart.description.isEnabled = false
            chart.xAxis.granularity = 1f
            chart.invalidate()
        }

    }
}

data class RiddleData(val riddleMainNumber: Int, val points: Int, val grade: String)