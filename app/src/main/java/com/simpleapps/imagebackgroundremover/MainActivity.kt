package com.simpleapps.imagebackgroundremover

import android.Manifest
import android.app.Activity
import android.app.DownloadManager
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.util.Log
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.SeekBar
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.scale
import com.bumptech.glide.Glide
import com.github.drjacky.imagepicker.ImagePicker
import com.github.drjacky.imagepicker.constant.ImageProvider
import com.simpleapps.imagebackgroundremover.databinding.ActivityMainBinding
import com.slowmac.autobackgroundremover.BackgroundRemover
import com.slowmac.autobackgroundremover.DownloadListener
import com.slowmac.autobackgroundremover.OnBackgroundChangeListener
import com.slowmac.autobackgroundremover.SaveBitmapTask
import java.io.File
import kotlin.math.max
import kotlin.math.roundToInt


class MainActivity : AppCompatActivity() {

    var scale: Bitmap? = null
    lateinit var target: File
    lateinit var requestPermissionLauncher: ActivityResultLauncher<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val inflate = ActivityMainBinding.inflate(layoutInflater)
        setContentView(inflate.root)
        requestPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()
            ) { isGranted: Boolean ->
                if (isGranted) {
                    Log.d("texts", "getPermission: GRANTED")
                } else {
                    Log.d("texts", "getPermission: REJECTED")
                }
            }
        val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                val uri = it.data?.data!!
                try {
                    val fileFromUri = getFileFromUri(applicationContext, uri)
                    target = File("${cacheDir.absoluteFile}/images/${fileFromUri?.name}")
                    target.parentFile.mkdirs()
                    target.delete()
                    inflate.addView.visibility = GONE
                    inflate.oldimg.visibility = VISIBLE
                    inflate.procView.visibility = VISIBLE
                    inflate.newimg.visibility = GONE
                    if (target.createNewFile()) {
                        Glide.with(inflate.oldimg)
                            .load(Uri.fromFile(fileFromUri?.copyTo(target, true)))
                            .into(inflate.oldimg)
                    } else {
                        Glide.with(inflate.oldimg)
                            .load(Uri.fromFile(fileFromUri?.copyTo(target, true)))
                            .into(inflate.oldimg)
                    }
                    if (target.exists()) {
                        Glide.with(inflate.oldimg).load(Uri.fromFile(target)).into(inflate.oldimg)
                        inflate.oldimg.setImageURI(Uri.fromFile(target))
                        val decodeFile = BitmapFactory.decodeFile(target.absolutePath)
                        var width = decodeFile.width
                        var height = decodeFile.height
                        val i = 1000
                        while (max(width, height) > i) {
                            width /= 2
                            height /= 2
                        }
                        scale = decodeFile.scale(width, height, true)
                        inflate.addView.visibility = GONE
                        inflate.oldimg.visibility = VISIBLE
                        inflate.procView.visibility = VISIBLE
                        inflate.newimg.setImageBitmap(scale)
                        val tolerance = 255
                        inflate.seekBar.visibility = VISIBLE
                        inflate.seekBar.setOnSeekBarChangeListener(object :
                            SeekBar.OnSeekBarChangeListener {
                            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                                inflate.progressTv.text =
                                    "Threshold ${((p0?.progress ?: 0) * 100F / 255).roundToInt()}%"
                            }

                            override fun onStartTrackingTouch(p0: SeekBar?) {


                            }

                            override fun onStopTrackingTouch(p0: SeekBar?) {
                                inflate.progressTv.visibility = VISIBLE
                                inflate.progressTv.text =
                                    "Threshold ${((p0?.progress ?: 0) * 100F / 255).roundToInt()}%"
                                removeBG(inflate, p0?.progress ?: 50)
                            }
                        })
                        removeBG(inflate, tolerance)
                    }
                } catch (e: Exception) {
                    Log.d("texts", "onCreate: " + e.localizedMessage)
                }
                // Use the uri to load the image
            }
        }
        inflate.switchImage.setOnClickListener {
            ImagePicker.with(this)
                .provider(ImageProvider.BOTH) //Or bothCameraGallery()
                .maxResultSize(100, 100, true)
                .createIntentFromDialog { launcher.launch(it) }
        }
    }

    private fun removeBG(
        inflate: ActivityMainBinding,
        tolerance: Int,
    ) {
        inflate.progressTv.visibility = VISIBLE
        inflate.saveImg.visibility = GONE
        inflate.newimg.visibility = GONE
        val decodeFile = BitmapFactory.decodeFile(target.absolutePath)
        var width = decodeFile.width
        var height = decodeFile.height
        val i = 1000
        while (max(width, height) > i) {
            width /= 2
            height /= 2
        }
        scale = decodeFile.copy(Bitmap.Config.ARGB_8888, true).scale(width, height, true)
        showProgressDialog(inflate)
        val image = scale
        if (image != null) {
            BackgroundRemover.bitmapForProcessing(image,
                object : OnBackgroundChangeListener {
                    override fun onFailed(exception: java.lang.Exception) {
                        Log.d("texts", "onFailed: " + exception.localizedMessage)
                        hideProgressDialog(inflate)
                    }

                    override fun onChange(bitmap: Bitmap) {
                        val l = System.currentTimeMillis() - lastChange
                        if (l > 1000) {
                            runOnUiThread {
                                inflate.procView.visibility = VISIBLE
                                inflate.saveImg.visibility = GONE
                                inflate.newimg.visibility = GONE
                            }
                            lastChange = System.currentTimeMillis()
                        }
                    }

                    override fun onSuccess(bitmap: Bitmap) {
                        inflate.procView.visibility = GONE
                        inflate.newimg.visibility = VISIBLE
                        inflate.newimg.setImageBitmap(bitmap)
                        inflate.saveImg.visibility = VISIBLE
                        inflate.saveImg.setOnClickListener {
                            val saveBitmapTask = SaveBitmapTask(applicationContext,
                                target.name,
                                bitmap,
                                "BGRemover",
                                object : DownloadListener {
                                    override fun onSuccess(path: String) {
                                        Toast.makeText(applicationContext,
                                            "Saved to $path",
                                            Toast.LENGTH_SHORT)
                                            .show()
                                        Log.d("texts", "onSuccess: " + path)
                                    }

                                    override fun onFailure(error: String) {
                                        Log.d("texts", "onFailure: " + error)
                                    }
                                })
                            saveBitmapTask.execute()
                            if (hasPermissions()) {
                            } else {
                                getPermission()
                            }
                        }
                        hideProgressDialog(inflate)
                    }
                }, tolerance)
        }
    }

    var manager: DownloadManager? = null

    private fun getPermission() {
        Log.d("texts", "getPermission: ")
    }


    private fun hasPermissions(): Boolean {
        val b = ContextCompat.checkSelfPermission(applicationContext,
            Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        Log.d("texts", "hasPermissions: " + b)
        return b
    }

    private fun showProgressDialog(inflate: ActivityMainBinding) {
        runOnUiThread {
            inflate.seekBar.isEnabled = false
            inflate.switchImage.isEnabled = false
        }
    }

    private fun hideProgressDialog(inflate: ActivityMainBinding) {
        runOnUiThread {
            inflate.seekBar.isEnabled = true
            inflate.switchImage.isEnabled = true
        }
    }

    var lastChange = System.currentTimeMillis()

    fun getFileFromUri(context: Context, uri: Uri?): File? {
        uri ?: return null
        uri.path ?: return null

        var newUriString = uri.toString()
        newUriString = newUriString.replace(
            "content://com.android.providers.downloads.documents/",
            "content://com.android.providers.media.documents/"
        )
        newUriString = newUriString.replace(
            "/msf%3A", "/image%3A"
        )
        val newUri = Uri.parse(newUriString)

        var realPath = String()
        val databaseUri: Uri
        val selection: String?
        val selectionArgs: Array<String>?
        if (newUri.path?.contains("/document/image:") == true) {
            databaseUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            selection = "_id=?"
            selectionArgs = arrayOf(DocumentsContract.getDocumentId(newUri).split(":")[1])
        } else {
            databaseUri = newUri
            selection = null
            selectionArgs = null
        }
        try {
            val column = "_data"
            val projection = arrayOf(column)
            val cursor = context.contentResolver.query(
                databaseUri,
                projection,
                selection,
                selectionArgs,
                null
            )
            cursor?.let {
                if (it.moveToFirst()) {
                    val columnIndex = cursor.getColumnIndexOrThrow(column)
                    realPath = cursor.getString(columnIndex)
                }
                cursor.close()
            }
        } catch (e: Exception) {
            Log.i("GetFileUri Exception:", e.message ?: "")
        }
        val path = realPath.ifEmpty {
            when {
                newUri.path?.contains("/document/raw:") == true -> newUri.path?.replace(
                    "/document/raw:",
                    ""
                )
                newUri.path?.contains("/document/primary:") == true -> newUri.path?.replace(
                    "/document/primary:",
                    "/storage/emulated/0/"
                )
                else -> return null
            }
        }
        return if (path.isNullOrEmpty()) null else File(path)
    }
}