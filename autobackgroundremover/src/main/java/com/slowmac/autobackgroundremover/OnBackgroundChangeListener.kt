package com.slowmac.autobackgroundremover

import android.graphics.Bitmap

interface OnBackgroundChangeListener {

    fun onSuccess(bitmap: Bitmap)

    fun onChange(bitmap: Bitmap)

    fun onFailed(exception: Exception)

}