package com.fisiaewiso

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import android.util.Log
import android.widget.ImageView

class SortableRecyclerViewAdapter(context: Context, private val answers: List<String>) : RecyclerView.Adapter<SortableRecyclerViewAdapter.ViewHolder>() {

    var items: MutableList<String> = mutableListOf()

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textViewAnswer: TextView = itemView.findViewById(R.id.textViewAnswer)
        val dragHandle: ImageView = itemView.findViewById(R.id.drag_handle)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_answer, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        Log.d("SortableRecyclerViewAdapter", "items: $items")
        return items.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val answer = answers[position]
        holder.textViewAnswer.text = answer
    }
}
