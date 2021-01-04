package com.hakansarac.drawapplication

import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.dialog_brush_size.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        imageButtonBrush.setOnClickListener {
            showBrushSizeDialog()
        }
    }

    private fun showBrushSizeDialog(){
        val brushDialog = Dialog(this)
        brushDialog.setContentView(R.layout.dialog_brush_size)

        val smallButton = brushDialog.imageButtonSmallBrush
        smallButton.setOnClickListener {
            viewDrawing.setBrushSize(6.toFloat())
            brushDialog.dismiss()
        }

        val mediumButton = brushDialog.imageButtonMediumBrush
        mediumButton.setOnClickListener {
            viewDrawing.setBrushSize(12.toFloat())
            brushDialog.dismiss()
        }

        val largeButton = brushDialog.imageButtonLargeBrush
        largeButton.setOnClickListener {
            viewDrawing.setBrushSize(18.toFloat())
            brushDialog.dismiss()
        }

        brushDialog.setTitle("Brush size: ")
        brushDialog.show()
    }



}