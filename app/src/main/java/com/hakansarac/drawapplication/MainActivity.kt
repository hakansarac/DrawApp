package com.hakansarac.drawapplication

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.media.MediaScannerConnection
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.ImageButton
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.get
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.dialog_brush_size.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

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
                val pickPhotoIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(pickPhotoIntent,GALLERY)
            }else{
                requestStoragePermission()
            }
        }

        imageButtonUndo.setOnClickListener{
            viewDrawing.undo()
        }

        imageButtonSave.setOnClickListener {
            if(isReadStorageAllowed()){
                BitmapAsyncTask(getBitmapFromView(frameLayoutDrawingView)).execute()
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
        private const val GALLERY = 2
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode== Activity.RESULT_OK){
            if(requestCode==GALLERY){
                try{
                    if(data!!.data != null){    //if the image uploaded
                        imageViewBackground.visibility = View.VISIBLE
                        imageViewBackground.setImageURI(data.data)
                    }else{
                        Toast.makeText(this@MainActivity,
                        "Image cannot be uploaded.",
                        Toast.LENGTH_SHORT).show()
                    }
                }catch(e:Exception){
                    e.printStackTrace()
                }
            }
        }
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


    private fun getBitmapFromView(view:View): Bitmap{
        val returnedBitmap = Bitmap.createBitmap(view.width,view.height,Bitmap.Config.ARGB_8888)
        val canvas = Canvas(returnedBitmap)
        val backgroudDrawable = view.background //it is our uploaded image from gallery
        if(backgroudDrawable != null){
            backgroudDrawable.draw(canvas)
        }else{
            canvas.drawColor(Color.WHITE)
        }
        view.draw(canvas)
        return returnedBitmap
    }


    //TODO("AsyncTask is deprecated in API level 30.") use Coroutines and check https://developer.android.com/topic/libraries/architecture/coroutines
    private inner class BitmapAsyncTask(val mBitmap: Bitmap?) :
        AsyncTask<Any, Void, String>() {

        private lateinit var mProgressDialog: Dialog


        override fun onPreExecute() {
            super.onPreExecute()
            showProgressDialog()
        }

        override fun doInBackground(vararg params: Any): String {

            var result = ""

            if (mBitmap != null) {

                try {
                    val bytes = ByteArrayOutputStream() // Creates a new byte array output stream.
                    // The buffer capacity is initially 32 bytes, though its size increases if necessary.

                    mBitmap.compress(Bitmap.CompressFormat.PNG, 90, bytes)


                    val f = File(
                        externalCacheDir!!.absoluteFile.toString()
                                + File.separator + "DrawApp_" + System.currentTimeMillis() / 1000 + ".jpg"
                    )
                    // Here the Environment : Provides access to environment variables.
                    // getExternalStorageDirectory : returns the primary shared/external storage directory.
                    // absoluteFile : Returns the absolute form of this abstract pathname.
                    // File.separator : The system-dependent default name-separator character. This string contains a single character.

                    val fo = FileOutputStream(f) // Creates a file output stream to write to the file represented by the specified object.
                    fo.write(bytes.toByteArray()) // Writes bytes from the specified byte array to this file output stream.
                    fo.close() // Closes this file output stream and releases any system resources associated with this stream. This file output stream may no longer be used for writing bytes.
                    result = f.absolutePath // The file absolute path is return as a result.
                } catch (e: Exception) {
                    result = ""
                    e.printStackTrace()
                }
            }
            return result
        }

        override fun onPostExecute(result: String) {
            super.onPostExecute(result)


            cancelProgressDialog()


            if (!result.isEmpty()) {
                Toast.makeText(
                    this@MainActivity,
                    "File saved successfully :$result",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(
                    this@MainActivity,
                    "Something went wrong while saving the file.",
                    Toast.LENGTH_SHORT
                ).show()
            }
            MediaScannerConnection.scanFile(this@MainActivity, arrayOf(result),null){ path, uri ->
                val shareIntent = Intent()
                shareIntent.action = Intent.ACTION_SEND
                shareIntent.putExtra(Intent.EXTRA_STREAM,uri)
                shareIntent.type = "image/png"
                startActivity(Intent.createChooser(shareIntent,"Share"))
            }
        }


        private fun showProgressDialog() {
            mProgressDialog = Dialog(this@MainActivity)

            /*Set the screen content from a layout resource.
            The resource will be inflated, adding all top-level views to the screen.*/
            mProgressDialog.setContentView(R.layout.dialog_custom_progress)

            //Start the dialog and display it on screen.
            mProgressDialog.show()
        }


        private fun cancelProgressDialog() {
            mProgressDialog.dismiss()
        }

    }
}