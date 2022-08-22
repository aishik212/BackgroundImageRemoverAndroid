package com.simpleapps.imagebackgroundremover.models

import android.net.Uri
import java.io.File

data class GalleryImages(
    val file: File,
    val uri: Uri = Uri.fromFile(file),
)
