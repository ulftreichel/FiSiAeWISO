package com.fisiaewiso

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.compose.ui.semantics.text
import androidx.recyclerview.widget.RecyclerView

class AnswersAdapter(private val answers: List<String>, private val listener: RiddleActivity) : RecyclerView.Adapter<AnswersAdapter.ViewHolder>() {


    private val selectedAnswersOrder = mutableListOf<String>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.answer_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val answer = answers[position]
        holder.answerTextView.text = answer

        holder.itemView.setOnClickListener {
            if (holder.itemView.isSelected) {
                holder.itemView.isSelected = false
                listener.onAnswerDeselected(answer, position)
            } else {
                holder.itemView.isSelected = true
                listener.onAnswerSelected(answer, position)
            }
        }
    }

    override fun getItemCount(): Int {
        return answers.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val answerTextView: TextView = itemView.findViewById(R.id.answerTextView)
    }
}