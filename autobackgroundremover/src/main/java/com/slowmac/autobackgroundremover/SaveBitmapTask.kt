package com.slowmac.autobackgroundremover

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

private lateinit var outputPath: String
private lateinit var error: String


/**
 * SaveBitmapTask.kt -Save Bitmap to the external storage with support of Android 11
 * @author:  Jignesh N Patel
 * @date: 19-Feb-2021 09:15 AM
 */

/**
 * SaveBitmap  Async Task to save bitmap
 */

interface DownloadListener {
    fun onSuccess(path: String)
    fun onFailure(error: String)
}

class SaveBitmapTask(
    val mContext: Context,
    val fileName: String,
    private val bitmap: Bitmap,
    private val outputDir: String,
    private val downloadListener: DownloadListener,
) : AsyncTask<String?, Int?, Boolean>() {
    private val TAG = javaClass.simpleName

    init {
        // initialization of ProgressDialog

    }


    /**
     * Before saving bitmap show ProgressDialog
     */
    override fun onPreExecute() {
        super.onPreExecute()
        Log.i(TAG, "Downloading started")

    }

    /**
     * Saving bitmap in doInBackground
     */
    override fun doInBackground(vararg f_url: String?): Boolean {
        saveImageToStorage(bitmap)
        try {
            return !isCancelled
        } catch (e: Exception) {
            error = e.toString()
            Log.e(TAG, "Exception: $e")
        }
        return false
    }


    /**
     * After completing background task
     * Dismiss the progress dialog
     */
    override fun onPostExecute(success: Boolean) {
        Log.i(TAG, "onPostExecute")
        // dismiss the dialog after the file was downloaded

        if (success) {
            downloadListener.onSuccess(outputPath)
        } else {
            downloadListener.onFailure(error)
        }

    }

    @Suppress("DEPRECATION")
    private fun saveImageToStorage(
        bitmap: Bitmap,
        filename: String = "${System.currentTimeMillis()}_" + fileName,
        mimeType: String = "image/jpeg",
        directory: String = Environment.DIRECTORY_PICTURES,
        mediaContentUri: Uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
    ) {
        val imageOutStream: OutputStream
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            var outputDirectory = directory

            // If you want to create custom directory inside Download directory only
            outputDirectory = outputDirectory + File.separator + outputDir
            val desFile = File(outputDirectory)
            if (!desFile.exists()) {
                desFile.mkdir()
            }

            // final output path
            outputPath = outputDirectory + File.separator + filename

            val values = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, filename)
                put(MediaStore.Images.Media.MIME_TYPE, mimeType)
                put(MediaStore.Images.Media.RELATIVE_PATH, outputDirectory)
            }
            val cr = mContext.contentResolver
            cr.run {
                val uri = cr.insert(mediaContentUri, values) ?: return
                imageOutStream = openOutputStream(uri) ?: return
            }
        } else {
            // first we create app name folder direct to the root directory
            var imagePath =
                Environment.getExternalStorageDirectory().path + File.separator + outputDir
            var desFile = File(imagePath)
            if (!desFile.exists()) {
                desFile.mkdir()
            }

            // once the app name directory created we create picture directory inside app directory
            imagePath = imagePath + File.separator + Environment.DIRECTORY_PICTURES
            desFile = File(imagePath)
            if (!desFile.exists()) {
                desFile.mkdir()
            }
            val image = File(imagePath, filename)
            // final output path
            outputPath = image.path

            imageOutStream = FileOutputStream(image)
        }
        imageOutStream.use { bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it) }
    }

}