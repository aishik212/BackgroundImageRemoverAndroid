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
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAdLoadCallback
import com.google.firebase.analytics.FirebaseAnalytics
import com.simpleapps.imagebackgroundremover.BuildConfig


@SuppressLint("MissingPermission")
public class AdUtils {
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
            adType: String?,
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
            if (adType != null) {
                b.putString("AdType", adType)
            }
            if (error != null && errorAdType != null) {
                b.putString("Error", errorAdType)
                b.putString("ErrorMessage", "$errorAdType $error")
            }
            if (activity != null) {
                FirebaseAnalytics.getInstance(activity).logEvent("AdLog", b)
                if (adType != null && adType == "AdKinowa") {
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


        var bought = 0
        var downloadInterstitialAd: RewardedInterstitialAd? = null

        fun loadTestStartAd(activity: Activity, adType: Int = 0) {
            if (bought == 0) {
                val adRequest = AdRequest.Builder().build()
                var adId =
                    activity.getString(com.simpleapps.imagebackgroundremover.R.string.rins_high_ad_id)
                var adTypeText = "HIGH"

                when (adType) {
                    0 -> {
                        adId =
                            activity.getString(com.simpleapps.imagebackgroundremover.R.string.rins_high_ad_id)
                        adTypeText = "HIGH"
                    }
                    1 -> {
                        adId =
                            activity.getString(com.simpleapps.imagebackgroundremover.R.string.rins_med_ad_id)
                        adTypeText = "MED"
                    }
                    2 -> {
                        adId =
                            activity.getString(com.simpleapps.imagebackgroundremover.R.string.rins_all_ad_id)
                        adTypeText = "ALL"
                    }
                }

                if (adType < 3) {
                    RewardedInterstitialAd.load(
                        activity,
                        adId,
                        adRequest,
                        object : RewardedInterstitialAdLoadCallback() {
                            override fun onAdLoaded(interstitialAd: RewardedInterstitialAd) {
                                logAdResult("BGR_RIAD_$adTypeText", null, null, activity)
                                downloadInterstitialAd = interstitialAd
                            }

                            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                                logAdResult(
                                    null,
                                    "BGR_RIAD_$adTypeText",
                                    loadAdError.message,
                                    activity
                                )
                                loadTestStartAd(activity, (adType + 1))
                            }
                        })
                } else {
                    downloadInterstitialAd = null
                }
            }
        }

    }
}