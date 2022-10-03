package com.simpleapps.imagebackgroundremover.activities

import android.app.Activity
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.simpleapps.imagebackgroundremover.databinding.ImageViewerLayoutBinding
import com.simpleapps.imagebackgroundremover.utilities.AdUtils
import java.io.File

class ImageViewerActivity : Activity() {
    lateinit var inflate: ImageViewerLayoutBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        inflate = ImageViewerLayoutBinding.inflate(layoutInflater)
        setContentView(inflate.root)
        val extras = intent.extras
        if (extras != null) {
            val parse = Uri.parse(extras.get("image").toString())
            val file = File(parse.path.toString())
            Log.d("texts", "onCreate: " + file.absolutePath)
            Log.d("texts", "onCreate: " + file.canRead())
            inflate.zoomV.setImageURI(Uri.parse(extras.get("image").toString()))
            AdUtils.showNativeAd(inflate.adView, this)
        } else {
            Toast.makeText(this, "Image Not Found", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}