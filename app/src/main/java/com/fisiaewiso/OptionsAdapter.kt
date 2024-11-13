package com.fisiaewiso

import android.content.ClipData
import android.content.Context
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

class OptionsAdapter<T>(
    private val options: MutableList<T>,
    private val userMappings: MutableMap<String, String>,
    private val context: Context
) : RecyclerView.Adapter<OptionsAdapter<T>.ViewHolder>() {
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
        Log.d("OptionsAdapter", "Binding option at position $position: $option")
        if (context is RiddleActivity) {
            val optionText = when (option) {
                is String -> option
                is OptionWithImage -> option.text
                else -> null
            }

            if (optionText != null && context.isOptionMapped(optionText)) {
                // Option ist gemappt, führe die entsprechende Logik aus
            }
        }
        when (option) {
            is String -> {
                holder.textView.text = option
                holder.textView.visibility = View.VISIBLE
                holder.imageView.visibility = View.GONE
            }
            is OptionWithImage -> {
                holder.textView.text = option.text
                holder.textView.visibility = View.GONE // Falls nur Bild angezeigt werden soll
                if (option.imageResId != 0) {
                    holder.imageView.setImageResource(option.imageResId)
                } else {
                    holder.imageView.setImageDrawable(
                        ContextCompat.getDrawable(holder.itemView.context, R.drawable.no_image_available)
                    )
                }
                holder.imageView.visibility = View.VISIBLE
            }
        }
        val optionText = when (option) {
            is String -> option
            is OptionWithImage -> option.text
            else -> null
        }

        holder.itemView.visibility = if (optionText != null && isOptionMapped(optionText)) {
            View.GONE
        } else {
            View.VISIBLE
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
                    if (option is OptionWithImage) {
                        // Erstellen von ClipData mit Text und Bild-ID
                        val clipData = ClipData.newPlainText("option", option.text).apply {
                            addItem(ClipData.Item(option.imageResId.toString())) // Bild-ID hinzufügen
                        }
                        val shadowBuilder = View.DragShadowBuilder(v)
                        v.startDragAndDrop(clipData, shadowBuilder, v, 0)
                    }
                    true
                } else {
                    false
                }
            }
        }
    }

    fun addOption(option: T) {
        options.add(option)
        notifyItemInserted(options.size - 1)
    }

    fun isOptionMapped(option: String): Boolean {
        return userMappings.containsKey(option)
    }

    override fun getItemCount(): Int {
        return options.size
    }

    fun removeOption(optionText: String) {
        val indexToRemove = options.indexOfFirst {
            when (it) {
                is String -> it == optionText
                is OptionWithImage -> it.text == optionText
                else -> false
            }
        }
        if (indexToRemove != -1) {
            options.removeAt(indexToRemove)
            notifyItemRemoved(indexToRemove)
        }
    }
}