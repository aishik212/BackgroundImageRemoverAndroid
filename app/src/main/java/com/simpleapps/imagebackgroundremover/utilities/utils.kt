package com.simpleapps.imagebackgroundremover.utilities

import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics

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

        private fun logEvent(context: Context, eventName: String, bundle: Bundle) {
            FirebaseAnalytics.getInstance(context).logEvent(eventName, bundle)
        }
    }
}