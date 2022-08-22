package com.simpleapps.imagebackgroundremover.fragments

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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.graphics.scale
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.github.drjacky.imagepicker.ImagePicker
import com.github.drjacky.imagepicker.constant.ImageProvider
import com.simpleapps.imagebackgroundremover.MainActivity.Companion.hasPermissions
import com.simpleapps.imagebackgroundremover.MainActivity.Companion.requestPermissionLauncher
import com.simpleapps.imagebackgroundremover.databinding.FragmentConverterBinding
import com.slowmac.autobackgroundremover.BackgroundRemover
import com.slowmac.autobackgroundremover.DownloadListener
import com.slowmac.autobackgroundremover.OnBackgroundChangeListener
import com.slowmac.autobackgroundremover.SaveBitmapTask
import java.io.File
import kotlin.math.max
import kotlin.math.roundToInt


class ConverterFragment : Fragment() {

    lateinit var inflate: FragmentConverterBinding
    var scale: Bitmap? = null
    lateinit var target: File
    lateinit var FragContext: Context
    lateinit var FragActivity: Activity


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        // Inflate the layout for this fragment
        inflate = FragmentConverterBinding.inflate(layoutInflater)
        val context: Context? = context
        if (context != null) {
            FragContext = context
        }
        val activity: Activity? = activity
        if (activity != null) {
            FragActivity = activity
        }
        return inflate.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                val uri = it.data?.data!!
                try {
                    val fileFromUri = getFileFromUri(FragContext, uri)
                    target =
                        File("${FragContext.cacheDir.absoluteFile}/images/${fileFromUri?.name}")
                    target.parentFile.mkdirs()
                    target.delete()
                    inflate.addView.visibility = View.GONE
                    inflate.oldimg.visibility = View.VISIBLE
                    inflate.procView.visibility = View.VISIBLE
                    inflate.newimg.visibility = View.GONE
                    if (target.createNewFile()) {
                        Glide.with(inflate.oldimg)
                            .load(Uri.fromFile(fileFromUri?.copyTo(target, true)))
                            .into(inflate.oldimg)
                    } else {
                        Toast.makeText(context, "Some Error Occured", Toast.LENGTH_SHORT).show()
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
                        inflate.addView.visibility = View.GONE
                        inflate.oldimg.visibility = View.VISIBLE
                        inflate.procView.visibility = View.VISIBLE
                        inflate.newimg.setImageBitmap(scale)
                        val tolerance = 255 / 2
                        val seekBar = inflate.seekBar
                        seekBar.visibility = View.VISIBLE
                        seekBar.setOnSeekBarChangeListener(object :
                            SeekBar.OnSeekBarChangeListener {
                            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                                inflate.progressTv.text =
                                    "Detection Sensitivity ${((p0?.progress ?: 0) * 100F / 255).roundToInt()}%"
                            }

                            override fun onStartTrackingTouch(p0: SeekBar?) {
                                //NOT NEEDED
                            }

                            override fun onStopTrackingTouch(p0: SeekBar?) {
                                inflate.progressTv.visibility = View.VISIBLE
                                inflate.progressTv.text =
                                    "Detection Sensitivity ${((p0?.progress ?: 0) * 100F / 255).roundToInt()}%"
                                removeBG(inflate, p0?.progress ?: 50)
                            }
                        })
                        seekBar.progress = tolerance
                        removeBG(inflate, tolerance)
                    }
                } catch (e: Exception) {
                }
                // Use the uri to load the image
            }
        }
        inflate.addView.setOnClickListener {
            ImagePicker.with(FragActivity)
                .provider(ImageProvider.BOTH) //Or bothCameraGallery()
                .maxResultSize(100, 100, true)
                .createIntentFromDialog { launcher.launch(it) }
        }
        inflate.switchImage.setOnClickListener {
            ImagePicker.with(FragActivity)
                .provider(ImageProvider.BOTH) //Or bothCameraGallery()
                .maxResultSize(100, 100, true)
                .createIntentFromDialog { launcher.launch(it) }
        }
    }

    private fun removeBG(
        inflate: FragmentConverterBinding,
        tolerance: Int,
    ) {
        inflate.progressTv.visibility = View.VISIBLE
        inflate.saveImg.visibility = View.GONE
        inflate.newimg.visibility = View.GONE
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
                            FragActivity.runOnUiThread {
                                inflate.procView.visibility = View.VISIBLE
                                inflate.saveImg.visibility = View.GONE
                                inflate.newimg.visibility = View.GONE
                            }
                            lastChange = System.currentTimeMillis()
                        }
                    }

                    override fun onSuccess(bitmap: Bitmap) {
                        inflate.procView.visibility = View.GONE
                        inflate.newimg.visibility = View.VISIBLE
                        inflate.newimg.setImageBitmap(bitmap)
                        inflate.saveImg.visibility = View.VISIBLE
                        val context = context
                        if (context != null && !hasPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                context)
                        ) {
                            getPermission()
                        }
                        inflate.saveImg.setOnClickListener {
                            val saveBitmapTask = SaveBitmapTask(FragContext,
                                target.name,
                                bitmap,
                                "BGRemover",
                                object : DownloadListener {
                                    override fun onSuccess(path: String) {
                                        Toast.makeText(FragContext,
                                            "Saved to $path",
                                            Toast.LENGTH_SHORT)
                                            .show()
                                    }

                                    override fun onFailure(error: String) {
                                        Log.d("texts", "onFailure: " + error)
                                    }
                                })
                            saveBitmapTask.execute()
                        }
                        hideProgressDialog(inflate)
                    }
                }, tolerance)
        }
    }


    private fun showProgressDialog(inflate: FragmentConverterBinding) {
        FragActivity.runOnUiThread {
            inflate.seekBar.isEnabled = false
            inflate.switchImage.isEnabled = false
        }
    }

    private fun hideProgressDialog(inflate: FragmentConverterBinding) {
        FragActivity.runOnUiThread {
            inflate.seekBar.isEnabled = true
            inflate.switchImage.isEnabled = true
        }
    }

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

    var manager: DownloadManager? = null

    private fun getPermission() {
        if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(
                FragContext,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        ) {
            requestPermissionLauncher.launch(
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
    }


    var lastChange = System.currentTimeMillis()

}