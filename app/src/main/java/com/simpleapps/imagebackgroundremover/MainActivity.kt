package com.simpleapps.imagebackgroundremover

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.scale
import com.github.drjacky.imagepicker.ImagePicker
import com.github.drjacky.imagepicker.constant.ImageProvider
import com.simpleapps.imagebackgroundremover.databinding.ActivityMainBinding
import com.slowmac.autobackgroundremover.BackgroundRemover
import com.slowmac.autobackgroundremover.OnBackgroundChangeListener
import java.io.File
import kotlin.math.max

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val inflate = ActivityMainBinding.inflate(layoutInflater)
        setContentView(inflate.root)


        val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                Log.d("texts", "onCreate: " + it)
                val uri = it.data?.data!!
                val file = File(uri.path)
                try {
                    val fileFromUri = getFileFromUri(applicationContext, uri)
                    val target = File("${cacheDir.absoluteFile}/images/${fileFromUri?.name}")
                    target.parentFile.mkdirs()
                    target.delete()
                    if (target.createNewFile()) {
                        inflate.oldimg.setImageURI(Uri.fromFile(fileFromUri?.copyTo(target, true)))
                    } else {
                        inflate.oldimg.setImageURI(Uri.fromFile(fileFromUri?.copyTo(target, true)))
                    }
                    Log.d("texts", "onCreate: A " + target.absolutePath)
                    Log.d("texts", "onCreate: A " + target.exists())
                    if (target.exists()) {
                        Log.d("texts", "onCreate: B ")
                        inflate.oldimg.setImageURI(Uri.fromFile(target))
                        val decodeFile = BitmapFactory.decodeFile(target.absolutePath)
                        var width = decodeFile.width
                        var height = decodeFile.height
                        val i = 1500
                        while (max(width, height) > i) {
                            width /= 2
                            height /= 2
                        }
                        val scale = decodeFile.scale(width, height, true)
                        inflate.newimg.setImageBitmap(scale)
                        BackgroundRemover.bitmapForProcessing(scale,
                            object : OnBackgroundChangeListener {
                                override fun onFailed(exception: java.lang.Exception) {
                                    Log.d("texts", "onFailed: " + exception.localizedMessage)
                                }

                                override fun onChange(bitmap: Bitmap) {
                                    val l = System.currentTimeMillis() - lastChange
                                    if (l > 1000) {
                                        Log.d("texts", "onChange: $l")
                                        runOnUiThread {
                                            inflate.newimg.setImageBitmap(bitmap)
/*
                                            Glide.with(inflate.newimg).load(bitmap)
                                                .listener(object : RequestListener<Drawable> {
                                                    override fun onResourceReady(
                                                        resource: Drawable?,
                                                        model: Any?,
                                                        target: Target<Drawable>?,
                                                        dataSource: DataSource?,
                                                        isFirstResource: Boolean,
                                                    ): Boolean {
                                                        Log.d("texts", "onResourceReady: ")
                                                        return true
                                                    }

                                                    override fun onLoadFailed(
                                                        e: GlideException?,
                                                        model: Any?,
                                                        target: Target<Drawable>?,
                                                        isFirstResource: Boolean,
                                                    ): Boolean {
                                                        Log.d("texts",
                                                            "onLoadFailed: " + e?.localizedMessage)
                                                        return true
                                                    }

                                                }).into(inflate.newimg)
*/
                                        }
//                                        inflate.newimg.setImageBitmap(bitmap)
                                        lastChange = System.currentTimeMillis()
                                    }
                                }

                                override fun onSuccess(bitmap: Bitmap) {
                                    inflate.newimg.setImageBitmap(bitmap)
                                    Log.d("texts", "onSuccess: " + bitmap)
                                }
                            })
                    }
                } catch (e: Exception) {
                    Log.d("texts", "onCreate: " + e.localizedMessage)
                }
                // Use the uri to load the image
            }
        }

        ImagePicker.with(this)
            .provider(ImageProvider.BOTH) //Or bothCameraGallery()
            .maxResultSize(100, 100, true)
            .createIntentFromDialog { launcher.launch(it) }
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