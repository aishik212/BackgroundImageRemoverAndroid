package com.simpleapps.imagebackgroundremover.utilities

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import androidx.core.content.ContextCompat
import com.google.firebase.analytics.FirebaseAnalytics
import com.simpleapps.imagebackgroundremover.R

class utils {
    companion object {
        fun logClickEvent(context: Context?, where: String) {
            val bundle = Bundle()
            bundle.putString("click", where)
            if (context != null) {
                logEvent(context, "click", bundle)
            }
        }

        fun logProcessingEvent(context: Context?, where: String) {
            val bundle = Bundle()
            bundle.putString("PROCESS", where)
            if (context != null) {
                logEvent(context, "PROCESS", bundle)
            }
        }

        enum class EventKeys {
            LAUNCH_APP,
            NOTIFICATION_EVENT,
            APP_COUNT,
            HUVLE,
            RATING
        }

        var eventInstance: FirebaseAnalytics? = null
        fun logEvent(context: Context?, key: EventKeys?, bundle: Bundle?) {
            if (context != null) {
                if (eventInstance == null) {
                    eventInstance = FirebaseAnalytics.getInstance(context)
                }
                eventInstance?.logEvent(key.toString(), bundle)
            }
        }

        fun mark(src: Bitmap, watermark: String, context: Context?): Bitmap {
            val w = src.width
            val h = src.height
            val result = Bitmap.createBitmap(w, h, src.config)
            val canvas = Canvas(result)
            canvas.drawBitmap(src, 0f, 0f, null)
            val paint = Paint()
            if (context != null) {
                paint.color = ContextCompat.getColor(context, R.color.primaryDarkColor)
            } else {
                paint.color = Color.DKGRAY
            }
            paint.textSize = h * 7F / 100F
            paint.isAntiAlias = true
            paint.isUnderlineText = false
            canvas.drawText(watermark, 0F, h * 9F / 100F, paint)
            return result
        }

        fun logSaveEvent(context: Context?, quality: String) {
            val bundle = Bundle()
            bundle.putString("SAVE", quality)
            if (context != null) {
                logEvent(context, "SAVE", bundle)
            }
        }

        private fun logEvent(context: Context, eventName: String, bundle: Bundle) {
            FirebaseAnalytics.getInstance(context).logEvent(eventName, bundle)
        }
    }
}