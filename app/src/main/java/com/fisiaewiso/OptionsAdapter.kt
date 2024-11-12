package com.fisiaewiso

import android.content.ClipData
import android.graphics.Color
import android.graphics.Point
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

class OptionsAdapter<T>(private val options: MutableList<T>) :
    RecyclerView.Adapter<OptionsAdapter<T>.ViewHolder>() {
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textView: TextView = itemView.findViewById(R.id.optionTextView) // Passe die ID an dein Layout an
        val imageView: ImageView = itemView.findViewById(R.id.imageView)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.option_item, parent, false) // Layout für Option mit Bild
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val option = options[position]
        if (option is String) {
            holder.textView.text = option
            holder.imageView.visibility = View.GONE // ImageView ausblenden, wenn kein Bild vorhanden
        } else if (option is OptionWithImage) {
            holder.textView.text = option.text
            holder.textView.visibility = View.GONE // TextView ausblenden, wenn kein Text vorhanden
            if (option.imageResId != 0) { // Bedingung für Bild laden
                holder.imageView.setImageResource(option.imageResId) // Bild aus imageResId laden
            } else {
                holder.imageView.setImageDrawable(ContextCompat.getDrawable(holder.itemView.context, R.drawable.no_image_available)) // Standardbild laden
            }
            holder.imageView.visibility = View.VISIBLE
        }

        // Hintergrundfarbe basierend auf der Position ändern
        if (option is String) {
            if (position % 2 == 0) {
                holder.itemView.setBackgroundColor(Color.WHITE)
            } else {
                holder.itemView.setBackgroundColor(Color.LTGRAY)
            }
            holder.itemView.setOnTouchListener { v, event ->
                if (event.actionMasked == MotionEvent.ACTION_DOWN) {
                    val dragData = ClipData.newPlainText("option", option.toString()) // option.toString() verwenden
                    val shadowBuilder = View.DragShadowBuilder(v)
                    v.startDragAndDrop(dragData, shadowBuilder, v, 0)
                    true
                } else {
                    false
                }
            }
        } else {
            // Casten auf OptionWithImage:
            val optionWithImage = option as OptionWithImage
            holder.itemView.setOnTouchListener { v, event ->

                if (event.actionMasked == MotionEvent.ACTION_DOWN) {
                    val optionWithImage = option as OptionWithImage
                    val clipData = ClipData.newPlainText("optionWithImage", optionWithImage.text)
                    clipData.addItem(ClipData.Item(optionWithImage.imageResId.toString())) // Bildressourcen-ID hinzufügen

                    // Verwende einen benutzerdefinierten DragShadowBuilder
                    val shadowBuilder = object : View.DragShadowBuilder(v) {
                        override fun onProvideShadowMetrics(outShadowSize: Point, outShadowTouchPoint: Point) {
                            super.onProvideShadowMetrics(outShadowSize, outShadowTouchPoint)
                            outShadowSize.set(150, 150) // Setzt die Größe des Shadows
                            outShadowTouchPoint.set(event.x.toInt(), event.y.toInt()) // Setzt den Punkt, an dem das Shadow verfolgt wird
                        }
                    }
                    v.startDragAndDrop(clipData, shadowBuilder, v, 0)
                    true
                } else {
                    false
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return options.size
    }

    fun removeOption(optionText: String) {
        val indexToRemove = options.indexOfFirst {
            if (it is String) {
                it == optionText
            } else if (it is OptionWithImage) {
                it.text == optionText
            } else {
                false
            }
        }

        if (indexToRemove != -1) {
            options.removeAt(indexToRemove)
        }
    }
}