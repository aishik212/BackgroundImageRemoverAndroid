package com.simpleapps.imagebackgroundremover.utilities

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View.VISIBLE
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout
import android.widget.LinearLayout
import com.google.android.ads.nativetemplates.NativeTemplateStyle
import com.google.android.ads.nativetemplates.TemplateView
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.nativead.NativeAd
import com.google.firebase.analytics.FirebaseAnalytics
import com.simpleapps.imagebackgroundremover.BuildConfig


@SuppressLint("MissingPermission")
public class adUtils {
    companion object {

        private fun adLoadingMessage(frameLayout: FrameLayout, activity: Activity) {
            activity.runOnUiThread {
                frameLayout.removeAllViews()
                val inflate =
                    activity.layoutInflater.inflate(com.simpleapps.imagebackgroundremover.R.layout.bottom_ad_loading_layout,
                        null)
                inflate.layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
                frameLayout.addView(
                    inflate
                )
                frameLayout.visibility = VISIBLE
            }
        }

        fun logAdResult(
            AdType: String?,
            errorAdType: String?,
            error: String?,
            activity: Activity?,
        ) {
            var error = error
            val b = Bundle()
            try {
                error = error!!.substring(0, 25)
            } catch (e: java.lang.Exception) {
            }
            if (AdType != null) {
                b.putString("AdType", AdType)
            }
            if (error != null && errorAdType != null) {
                b.putString("Error", errorAdType)
                b.putString("ErrorMessage", "$errorAdType $error")
            }
            if (activity != null) {
                FirebaseAnalytics.getInstance(activity).logEvent("AdLog", b)
                if (AdType != null && AdType == "AdKinowa") {
                    val bundle = Bundle()
                    bundle.putString("AdKinowa", "Success")
                    FirebaseAnalytics.getInstance(activity).logEvent("AdKinowa", bundle)
                }
            }
        }


        fun showNativeAd(frameLayout: FrameLayout, activity: Activity) {
            adLoadingMessage(frameLayout, activity)
            val adLoader = AdLoader.Builder(activity,
                activity.getString(com.simpleapps.imagebackgroundremover.R.string.native_ad_id))
                .forNativeAd { nativeAd: NativeAd? ->
                    activity.runOnUiThread {
                        logAdResult("NATIVE_ALL", null, null, activity)
                        val styles = NativeTemplateStyle.Builder().build()
                        val template =
                            activity.layoutInflater.inflate(com.simpleapps.imagebackgroundremover.R.layout.template_small_layout,
                                null)
                                .rootView as TemplateView
                        template.setStyles(styles)
                        template.setNativeAd(nativeAd)
                        frameLayout.removeAllViews()
                        frameLayout.visibility = VISIBLE
                        frameLayout.addView(template)
                        restartAdLoad(frameLayout, activity)
                    }
                }.withAdListener(object : AdListener() {
                    override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                        super.onAdFailedToLoad(loadAdError)
                        restartAdLoad(frameLayout, activity)
                        logAdResult(null, "NATIVE_ALL", loadAdError.message, activity)
                    }

                    override fun onAdClicked() {
                        super.onAdClicked()
                        logAdResult("NATIVE_ALL_Clicked", null, null, activity)
                    }

                })
                .build()
            adLoader.loadAd(AdRequest.Builder().build())
        }

        private fun restartAdLoad(frameLayout: FrameLayout, activity: Activity) {
            if (!activity.isDestroyed) {
                object : CountDownTimer(if (BuildConfig.DEBUG) 25000 else 30000, 1000) {
                    override fun onTick(p0: Long) {
                        //Useless
                    }

                    override fun onFinish() {
                        showNativeAd(frameLayout, activity)
                    }
                }.start()
            }
        }


/*
        private fun checkSpread() {
            val hmap: HashMap<Int, Long> = hashMapOf()
            var i = 0
            while (i < 12000) {
                val key = (floor(Math.random() * 6.0).toInt())
                val l = hmap[key] ?: 0
                hmap[key] = l + 1
                i++
            }
        }
*/

    }
}