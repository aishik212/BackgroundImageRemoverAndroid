package com.simpleapps.imagebackgroundremover.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import com.simpleapps.imagebackgroundremover.MainActivity
import com.simpleapps.imagebackgroundremover.R
import com.simpleapps.imagebackgroundremover.databinding.StartActivityLayoutBinding
import com.simpleapps.imagebackgroundremover.utilities.AdUtils.Companion.logAdResult

class StartActivity : AppCompatActivity() {
    private var startActivityLayoutBinding: StartActivityLayoutBinding? = null
    private var loadingTv: TextView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startActivityLayoutBinding = StartActivityLayoutBinding.inflate(layoutInflater)
        val root = startActivityLayoutBinding?.root
        loadingTv = startActivityLayoutBinding?.loadingTv
        setContentView(root)
        showAppOpenAd(this)
    }

    var num = 0
    var showAd = true
    val l: Long = 5000
    val ctd = object : CountDownTimer(l, 500) {
        override fun onTick(millisUntilFinished: Long) {
            loadingTv?.text = "Loading"
            for (i in 0..((l - millisUntilFinished) / 1000)) {
                loadingTv?.append(".")
            }
        }

        override fun onFinish() {
            showAd = false
            goToHomeAct()
        }
    }

    fun showAppOpenAd(activity: Activity) {
        try {
            runOnUiThread {
                ctd.start()
            }
        } catch (e: Exception) {

        }
        try {
            val request = AdRequest.Builder().build()
            runOnUiThread {
                when (num) {
                    0 -> {
                        AppOpenAd.load(
                            activity, activity.getString(R.string.appOpenHighID), request,
                            AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT, loadCallback,
                        )
                    }
                    1 -> {
                        AppOpenAd.load(
                            activity, activity.getString(R.string.appOpenMedID), request,
                            AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT, loadCallback
                        )
                    }
                    2 -> {
                        AppOpenAd.load(
                            activity, activity.getString(R.string.appOpenAllID), request,
                            AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT, loadCallback
                        )
                    }
                    3 -> {
                        goToHomeAct()
                    }
                }
                num++
            }
        } catch (e: Exception) {

        }
    }

    private val loadCallback: AppOpenAd.AppOpenAdLoadCallback =
        object : AppOpenAd.AppOpenAdLoadCallback() {
            override fun onAdLoaded(ad: AppOpenAd) {
                logAppOpen("LOAD", null, null)
                if (showAd) {
                    ad.show(this@StartActivity)
                    ctd.cancel()
                    ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                        override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                            super.onAdFailedToShowFullScreenContent(p0)
                            logAppOpen(null, "SHOW_FAIL", p0.message)
                            showAppOpenAd(this@StartActivity)
//                            goToHomeAct()
                        }

                        override fun onAdShowedFullScreenContent() {
                            super.onAdShowedFullScreenContent()
                            logAppOpen("SHOWN", null, null)
                        }

                        override fun onAdDismissedFullScreenContent() {
                            super.onAdDismissedFullScreenContent()
//                            showAppOpenAd(this@StartActivity)
                            goToHomeAct()
                        }
                    }
                }
            }

            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                logAppOpen(null, "FAIL", loadAdError.message)
                showAppOpenAd(this@StartActivity)
            }
        }

    private fun logAppOpen(success: String?, fail: String?, failReason: String?) {
        when (num - 1) {
            0 -> {
                if (success != null) {
                    logAdResult("APPOPEN_HIGH_$success", null, null, this)
                } else if (fail != null) {
                    logAdResult(null, "APPOPEN_HIGH_$fail", failReason, this)
                }
            }
            1 -> {
                if (success != null) {
                    logAdResult("APPOPEN_MED_$success", null, null, this)
                } else if (fail != null) {
                    logAdResult(null, "APPOPEN_MED_$fail", failReason, this)
                }
            }
            2 -> {
                if (success != null) {
                    logAdResult("APPOPEN_ALL_$success", null, null, this)
                } else if (fail != null) {
                    logAdResult(null, "APPOPEN_ALL_$fail", failReason, this)
                }
            }
        }
    }

    private fun goToHomeAct() {
        try {
            ctd.cancel()
        } catch (e: Exception) {

        }
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }


}