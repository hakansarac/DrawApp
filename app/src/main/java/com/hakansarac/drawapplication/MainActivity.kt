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

    private var mImageButtonSelected : ImageButton? = null  // A variable for current color is picked from color pallet.

    companion object{
        /**
         * Permission code that will be checked in the method onRequestPermissionsResult
         * For more Detail visit : https://developer.android.com/training/permissions/requesting#kotlin
         */
        private const val STORAGE_PERMISSION_CODE = 1
        private const val GALLERY = 2       // This is to identify the selection of image from Gallery.
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        /**
         * This is to select the default Image button which is
         * active and color is already defined in the drawing view class.
         * As the default color is black so in our color pallet it is on 8 position.
         * But the array list start position is 0 so the black color is at position 7.
         */
        mImageButtonSelected = linearLayoutColors[7] as ImageButton
        mImageButtonSelected!!.setImageDrawable(
            ContextCompat.getDrawable(this,R.drawable.palette_selected)
        )

        imageButtonBrush.setOnClickListener {
            showBrushSizeDialog()
        }

        imageButtonGallery.setOnClickListener {
            //Very firstly we will check the app required a storage permission.
            // So we will add a permission in the Android.xml for storage.
            //First checking if the app is already having the permission
            if(isReadStorageAllowed()){
                // This is for selecting the image from local store or let say from Gallery/Photos
                val pickPhotoIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(pickPhotoIntent,GALLERY)
            }else{
                //If the app don't have storage access permission we will ask for it.
                requestStoragePermission()
            }
        }

        imageButtonUndo.setOnClickListener{
            // This is for undo recent stroke.
            viewDrawing.undo()
        }

        imageButtonSave.setOnClickListener {
            //First checking if the app is already having the permission
            if(isReadStorageAllowed()){
                BitmapAsyncTask(getBitmapFromView(frameLayoutDrawingView)).execute()
            }else{
                //If the app don't have storage access permission we will ask for it.
                requestStoragePermission()
            }
        }
    }

    /**
     * Method is used to launch the dialog to select different brush sizes.
     */

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

    /**
     * Method is called when color is clicked from pallet_normal.
     *
     * @param view ImageButton on which click took place.
     */

    fun paintColor(view: View){
        if(view!=mImageButtonSelected){
            // Update the color
            val imageButton = view as ImageButton
            // Here the tag is used for swapping the current color with previous color.
            // The tag stores the selected view
            val colorTag = imageButton.tag.toString()
            // The color is set as per the selected tag here.
            viewDrawing.setColor(colorTag)
            // Swap the backgrounds for last active and currently active image button.
            mImageButtonSelected!!.setImageDrawable(
                ContextCompat.getDrawable(this,R.drawable.palette_normal)
            )
            imageButton.setImageDrawable(
                ContextCompat.getDrawable(this,R.drawable.palette_selected)
            )
            //Current view is updated with selected view in the form of ImageButton.
            mImageButtonSelected = imageButton
        }
    }


    /**
     * This is override method here we get the selected image
     * based on the code what we have passed for selecting the image.
     */

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode== Activity.RESULT_OK){
            if(requestCode==GALLERY){
                try{
                    if(data!!.data != null){
                        //if the image uploaded
                        // By Default we will make it VISIBILITY as GONE.
                        imageViewBackground.visibility = View.VISIBLE
                        // Set the selected image to the background view.
                        imageViewBackground.setImageURI(data.data)
                    }else{
                        // If the selected image is not valid. Or not selected.
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


    /**
     * Requesting permission
     */
    private fun requestStoragePermission(){
        if(ActivityCompat.shouldShowRequestPermissionRationale(this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE).toString())){
            Toast.makeText(this,"Need permission to add a background.",Toast.LENGTH_LONG).show()
        }

        //Ask for the permission
        ActivityCompat.requestPermissions(this,
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE), STORAGE_PERMISSION_CODE)
    }

    /**
     * This is override method and the method will be called when the user will tap on allow or deny
     *
     * Determines whether the delegate should handle
     * {@link ActivityCompat#requestPermissions(Activity, String[], int)}, and request
     * permissions if applicable. If this method returns true, it means that permission
     * request is successfully handled by the delegate, and platform should not perform any
     * further requests for permission.
     *
     * @param activity The target activity.
     * @param permissions The requested permissions. Must me non-null and not empty.
     * @param requestCode Application specific request code to match with a result reported to
     */

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        //Checking the request code of our request
        if(requestCode == STORAGE_PERMISSION_CODE){
            //If permission is granted
            if(grantResults.isNotEmpty() &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this@MainActivity,
                "Permission granted.",
                Toast.LENGTH_LONG).show()
            }else{
                //Displaying another toast if permission is not granted
                Toast.makeText(this@MainActivity,
                "Permission denied.",
                Toast.LENGTH_LONG).show()
            }
        }
    }

    /**
     * We are calling this method to check the permission status
     */
    private fun isReadStorageAllowed(): Boolean{
        //Getting the permission status
        // Here the checkSelfPermission is
        /**
         * Determine whether you have been granted a particular permission.
         *
         * @param permission The name of the permission being checked.
         *
         * @return {@link android.content.pm.PackageManager#PERMISSION_GRANTED} if you have the
         * permission, or {@link android.content.pm.PackageManager#PERMISSION_DENIED} if not.
         */
        //If permission is granted returning true and If permission is not granted returning false

        return ContextCompat.checkSelfPermission(this,Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
    }


    /**
     * Create bitmap from view and returns it
     */
    private fun getBitmapFromView(view:View): Bitmap{
        //Define a bitmap with the same size as the view.
        // CreateBitmap : Returns a mutable bitmap with the specified width and height
        val returnedBitmap = Bitmap.createBitmap(view.width,view.height,Bitmap.Config.ARGB_8888)
        //Bind a canvas to it
        val canvas = Canvas(returnedBitmap)
        //Get the view's background
        val backgroundDrawable = view.background //it is our uploaded image from gallery
        if(backgroundDrawable != null){
            //has background drawable, then draw it on the canvas
            backgroundDrawable.draw(canvas)
        }else{
            //does not have background drawable, then draw white background on the canvas
            canvas.drawColor(Color.WHITE)
        }
        // draw the view on the canvas
        view.draw(canvas)
        //return the bitmap
        return returnedBitmap
    }


    //TODO("AsyncTask is deprecated in API level 30.") use Coroutines and check https://developer.android.com/topic/libraries/architecture/coroutines
    private inner class BitmapAsyncTask(val mBitmap: Bitmap?) :
        AsyncTask<Any, Void, String>() {

        /**
         * ProgressDialog is a modal dialog, which prevents the user from interacting with the app.
         *
         * The progress dialog in newer versions is deprecated so we will create a custom progress dialog later on.
         * This is just an idea to use progress dialog.
         */

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

                    /**
                     * Write a compressed version of the bitmap to the specified outputstream.
                     * If this returns true, the bitmap can be reconstructed by passing a
                     * corresponding inputstream to BitmapFactory.decodeStream(). Note: not
                     * all Formats support all bitmap configs directly, so it is possible that
                     * the returned bitmap from BitmapFactory could be in a different bitdepth,
                     * and/or may have lost per-pixel alpha (e.g. JPEG only supports opaque
                     * pixels).
                     *
                     * @param format   The format of the compressed image
                     * @param quality  Hint to the compressor, 0-100. 0 meaning compress for
                     *                 small size, 100 meaning compress for max quality. Some
                     *                 formats, like PNG which is lossless, will ignore the
                     *                 quality setting
                     * @param stream   The outputstream to write the compressed data.
                     * @return true if successfully compressed to the specified stream.
                     */


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

            /*MediaScannerConnection provides a way for applications to pass a
            newly created or downloaded media file to the media scanner service.
            The media scanner service will read metadata from the file and add
            the file to the media content provider.
            The MediaScannerConnectionClient provides an interface for the
            media scanner service to return the Uri for a newly scanned file
            to the client of the MediaScannerConnection class.*/

            //scanFile is used to scan the file when the connection is established with MediaScanner
            MediaScannerConnection.scanFile(this@MainActivity, arrayOf(result),null){ path, uri ->
                // This is used for sharing the image after it has being stored in the storage.
                val shareIntent = Intent()
                shareIntent.action = Intent.ACTION_SEND
                shareIntent.putExtra(Intent.EXTRA_STREAM,uri) // A content: URI holding a stream of data associated with the Intent, used to supply the data being sent.
                shareIntent.type = "image/png"
                startActivity(Intent.createChooser(shareIntent,"Share"))
                // Activity Action: Display an activity chooser,
                // allowing the user to pick what they want to before proceeding.
                // This can be used as an alternative to the standard activity picker
                // that is displayed by the system when you try to start an activity with multiple possible matches,
                // with these differences in behavior
            }
        }


        /**
         * This function is used to show the progress dialog with the title and message to user.
         */
        private fun showProgressDialog() {
            mProgressDialog = Dialog(this@MainActivity)

            /*Set the screen content from a layout resource.
            The resource will be inflated, adding all top-level views to the screen.*/
            mProgressDialog.setContentView(R.layout.dialog_custom_progress)

            //Start the dialog and display it on screen.
            mProgressDialog.show()
        }


        /**
         * This function is used to dismiss the progress dialog if it is visible to user.
         */
        private fun cancelProgressDialog() {
            mProgressDialog.dismiss()
        }

    }
}