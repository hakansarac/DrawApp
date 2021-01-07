package com.hakansarac.drawapplication

import android.Manifest
import android.app.Dialog
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.get
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.dialog_brush_size.*

class MainActivity : AppCompatActivity() {

    private var mImageButtonSelected : ImageButton? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mImageButtonSelected = linearLayoutColors[7] as ImageButton
        mImageButtonSelected!!.setImageDrawable(
            ContextCompat.getDrawable(this,R.drawable.palette_selected)
        )

        imageButtonBrush.setOnClickListener {
            showBrushSizeDialog()
        }

        imageButtonGallery.setOnClickListener {
            if(isReadStorageAllowed()){

            }else{
                requestStoragePermission()
            }
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

    fun paintColor(view: View){
        if(view!=mImageButtonSelected){
            val imageButton = view as ImageButton
            val colorTag = imageButton.tag.toString()
            viewDrawing.setColor(colorTag)
            mImageButtonSelected!!.setImageDrawable(
                ContextCompat.getDrawable(this,R.drawable.palette_normal)
            )
            imageButton.setImageDrawable(
                ContextCompat.getDrawable(this,R.drawable.palette_selected)
            )
            mImageButtonSelected = imageButton
        }
    }



    companion object{
        private const val STORAGE_PERMISSION_CODE = 1
    }

    private fun requestStoragePermission(){
        if(ActivityCompat.shouldShowRequestPermissionRationale(this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE).toString())){
            Toast.makeText(this,"Need permission to add a background.",Toast.LENGTH_LONG).show()
        }

        ActivityCompat.requestPermissions(this,
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE), STORAGE_PERMISSION_CODE)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == STORAGE_PERMISSION_CODE){
            if(grantResults.isNotEmpty() &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this@MainActivity,
                "Permission granted.",
                Toast.LENGTH_LONG).show()
            }else{
                Toast.makeText(this@MainActivity,
                "Permission denied.",
                Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun isReadStorageAllowed(): Boolean{
        return ContextCompat.checkSelfPermission(this,Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
    }

}