package com.slowmac.autobackgroundremover

import android.graphics.Bitmap
import android.graphics.Color
import android.util.Log
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.segmentation.Segmentation
import com.google.mlkit.vision.segmentation.Segmenter
import com.google.mlkit.vision.segmentation.selfie.SelfieSegmenterOptions
import kotlinx.coroutines.*
import java.nio.ByteBuffer

object BackgroundRemover {

    private val segment: Segmenter
    private var buffer = ByteBuffer.allocate(0)
    private var width = 0
    private var height = 0


    init {

        val segmentOptions = SelfieSegmenterOptions.Builder()
            .setDetectorMode(SelfieSegmenterOptions.SINGLE_IMAGE_MODE)
            .build()

        segment = Segmentation.getClient(segmentOptions)
    }


    /**
     * Process the image to get buffer and image height and width
     * */
    fun bitmapForProcessing(image: Bitmap, listener: OnBackgroundChangeListener, tolerance: Int) {
        val input = InputImage.fromBitmap(image, 0)
        segment.process(input)
            .addOnSuccessListener { segmentationMask ->
                buffer = segmentationMask.buffer
                width = segmentationMask.width
                height = segmentationMask.height

                CoroutineScope(Dispatchers.IO).launch {
                    val bitmap = removeBackgroundFromImage(image, listener, tolerance)
                    withContext(Dispatchers.Main) {
                        listener.onSuccess(bitmap)
                    }
                }

            }
            .addOnFailureListener { e ->
                println("Image processing failed: $e")
                listener.onFailed(e)
            }
    }

    /**
     * Remove the background pixels from the image
     * */
    private suspend fun removeBackgroundFromImage(
        image: Bitmap,
        listener: OnBackgroundChangeListener,
        tolerance: Int = 50,
    ): Bitmap {
        val bitmap = CoroutineScope(Dispatchers.IO).async {
            for (y in 0 until height) {
                for (x in 0 until width) {
                    try {
                        val bgConfidence = ((1.0 - buffer.float) * 250).toInt()
                        try {
                            if (bgConfidence >= 250) {
                                image.setPixel(x, y, Color.WHITE)
                            } else if (bgConfidence > tolerance) {
                                image.setPixel(x, y, Color.WHITE)
//                                image.setPixel(x, y, Color.BLACK)
                            }
                            listener.onChange(image)
                        } catch (e: Exception) {
                            Log.d("texts", "removeBackgroundFromImage: 1 " + e.localizedMessage)
                        }
                    } catch (e: Exception) {
                        Log.d("texts", "removeBackgroundFromImage: 2 " + e.localizedMessage)
                    }
                }
            }
            buffer.rewind()
            return@async image
        }
        return bitmap.await()
    }

}