package com.fisiaewiso

import android.app.Dialog
import android.content.Context
import android.widget.Button
import com.github.chrisbanes.photoview.PhotoView

class RiddleImageViewDialog(context: Context, imageResource: Int) : Dialog(context) {

    init {
        setContentView(R.layout.riddle_imageview_dialog)
        val okButton = findViewById<Button>(R.id.okIVButton)
        val photoView: PhotoView = findViewById(R.id.image_view)
        photoView.setImageResource(imageResource)

        okButton.setOnClickListener {
            dismiss() // Schließe den Dialog
        }
    }
}