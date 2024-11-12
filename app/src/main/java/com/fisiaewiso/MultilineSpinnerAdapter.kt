package com.fisiaewiso

import android.content.Context
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import androidx.compose.foundation.background
import androidx.compose.ui.semantics.text
import kotlin.text.split

class MultilineSpinnerAdapter(context: Context, resource: Int, private val answers: List<String>) : ArrayAdapter<String>(context, resource, answers) {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.spinner_item_multiline, parent, false)
        val textViewLine1 = view.findViewById<TextView>(R.id.text_view_line1)
        view.background = context.getDrawable(R.drawable.spinner_background_normal)

        // Text im TextView setzen
        textViewLine1.text = getItem(position)

        return view
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        return getView(position, convertView, parent)
    }

    override fun getItem(position: Int): String? {
        return super.getItem(position) // Element an der Position zurückgeben, kann null sein
    }
}